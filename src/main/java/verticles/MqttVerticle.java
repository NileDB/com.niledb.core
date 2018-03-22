/**
 * Copyright (C) 2018 NileDB, Inc.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package verticles;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphQLHandler;
import helpers.ConfigHelper;
import helpers.DatabaseHelper;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

/**
 * @author NileDB, Inc.
 */
public class MqttVerticle extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(MqttVerticle.class);
	
	@Override
	public void start() throws Exception {

		MqttServerOptions options = new MqttServerOptions()
				.setPort((Integer) ConfigHelper.get(ConfigHelper.MQTT_PORT, 1883))
				.setSsl(false);

		if ((Boolean) ConfigHelper.get(ConfigHelper.SERVICE_SSL, false)) {
			PemKeyCertOptions pemOptions = new PemKeyCertOptions();
			
			if ((Boolean) ConfigHelper.get(ConfigHelper.SERVICE_SSL_AUTO_GENERATE_CERT, true)) {
				SelfSignedCertificate certificate = SelfSignedCertificate.create();
				pemOptions
					.setKeyPath(certificate.privateKeyPath())
					.setCertPath(certificate.certificatePath());
			}
			else {
				pemOptions
					.setKeyPath((String) ConfigHelper.get(ConfigHelper.SERVICE_SSL_KEY_PATH, "private.key"))
					.setCertPath((String) ConfigHelper.get(ConfigHelper.SERVICE_SSL_CERT_PATH, "public.crt"));
			}
			
			options
				.setPemKeyCertOptions(pemOptions)
				.setSsl(true);
		}
		
		MqttServer mqttServer = MqttServer.create(vertx, options);
		
		mqttServer.endpointHandler(endpoint -> {
			final StringBuffer token = new StringBuffer();
			if (endpoint.auth() != null) {
				final String username = endpoint.auth().userName();
				String password = endpoint.auth().password();

				Connection connection = null;
				
				try {
					
					MessageDigest messageDigest = MessageDigest.getInstance("MD5");
					messageDigest.update((password + username).getBytes());
					password = "md5" + DatatypeConverter.printHexBinary(messageDigest.digest()).toLowerCase();
					connection = DatabaseHelper.getConnection();
					StringBuffer sql = new StringBuffer()
							.append("SELECT rolname ")
							.append("FROM   pg_authid ")
							.append("WHERE  rolname = ? ")
							.append("AND    rolpassword = ?");
					
					PreparedStatement ps = connection.prepareStatement(sql.toString());
					ps.setString(1, username);
					ps.setString(2, password);
					ResultSet rs = ps.executeQuery();
					
					if (rs.next()) {
						// accept connection from the remote client
						endpoint.accept(false);
						
						Payload payload = new Payload(new JsonObject().put("username", username).encode());
						JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
						JWSObject jwsObject = new JWSObject(header, payload);
						JWSSigner signer = new MACSigner(((String) ConfigHelper.get(ConfigHelper.SECURITY_JWT_SECRET, "password_of_at_least_32_characters")).getBytes());
						
						jwsObject.sign(signer);
						
						token.append(jwsObject.serialize());
					}
					else {
						endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				finally {
					try {
						if (connection != null) {
							connection.close();
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else {
				endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
			}
			
			endpoint.disconnectHandler(v -> {
				logger.debug("Received disconnect from client");
			});
			
			endpoint.publishHandler(message -> {
				String payload = message.payload().toString(Charset.defaultCharset());
				String topicName = message.topicName();

				// If payload has quotes (it comes from NIFI JSON or similar), remove them
				payload = payload.replaceAll("\"(\\w+)\"\\s*:", "$1:");
				
				try {
					GraphQL graphql = GraphQLHandler.getGraphQL();

					StringBuffer query = new StringBuffer()
							.append("mutation {")
							.append(topicName + "Create(")
							.append("entity: " + payload + ") {id}}");							
					
					HashMap<String, Object> variables = new HashMap<String, Object>();
					variables.put("authorization", token.toString());
					
					ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
							.query(query.toString())
							.context(variables)
							.variables(variables);
					
					JsonObject result = new JsonObject();
					
			    	ExecutionResult executionResult = graphql.execute(executionInput.build());
					
					if (executionResult.getErrors().size() > 0) {
						JsonArray jsonErrors = new JsonArray();
						List<GraphQLError> errors = executionResult.getErrors();
						for (int i = 0; i < errors.size(); i++) {
							GraphQLError error = errors.get(i);
							jsonErrors.add(error.toSpecification());
						}
						result.put("errors", jsonErrors);
					}
				    result.put("data", (Object) executionResult.getData());
				    logger.debug(result.encode());
					if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
						endpoint.publishAcknowledge(message.messageId());
					} 
					else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
						endpoint.publishReceived(message.messageId());
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			});
			
			endpoint.publishReleaseHandler(messageId -> {
				endpoint.publishComplete(messageId);
			});
			
		}).listen(ar -> {
			
			if (ar.succeeded()) {
				logger.debug("MQTT server is listening on port " + ar.result().actualPort());
			}
			else {
				logger.debug("Error on starting the MQTT server");
				ar.cause().printStackTrace();
			}
		});
	}
}
