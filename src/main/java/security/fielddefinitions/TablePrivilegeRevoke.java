package security.fielddefinitions;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import helpers.ConfigHelper;
import helpers.DatabaseHelper;
import helpers.Helper;
import io.vertx.core.json.JsonObject;

public class TablePrivilegeRevoke {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("__tablePrivilegeRevoke")
			.description("It logins as a user into the system and returns a token that must be used in \"authorization\" variable when invoking GraphQL services.")
			.argument(newArgument()
					.name("username")
					.description("The username.")
					.type(GraphQLNonNull.nonNull(GraphQLString)))
			.argument(newArgument()
					.name("password")
					.description("The password.")
					.type(GraphQLNonNull.nonNull(GraphQLString)))
			.type(GraphQLString)
			.dataFetcher(new DataFetcher<String>() {
				@Override
				public String get(DataFetchingEnvironment environment) {
					List<Field> fields = environment.getFields();
					String jwtToken = null;
					Connection connection = null;
					try {
						String username = null;
						String password = null;
						
						Field field = fields.get(0);
						for (int i = 0; i < field.getArguments().size(); i++) {
							Argument argument = field.getArguments().get(i);
							if (argument.getName().equals("username")) {
								username = (String) Helper.resolveValue(argument.getValue(), environment);
							}
							else if (argument.getName().equals("password")) {
								password = (String) Helper.resolveValue(argument.getValue(), environment);
							}
						}
						
						MessageDigest messageDigest = MessageDigest.getInstance("MD5");
						messageDigest.update((password + username).getBytes());
						password = "md5" + DatatypeConverter.printHexBinary(messageDigest.digest()).toLowerCase();
						System.out.println(password);
						connection = DatabaseHelper.getConnection();
						StringBuffer sql = new StringBuffer()
								.append("SELECT usename ")
								.append("FROM   xxxxx ")
								.append("WHERE  usename = ? ")
								.append("AND    passwd = ?");
						
						PreparedStatement ps = connection.prepareStatement(sql.toString());
						ps.setString(1, username);
						ps.setString(2, password);
						ResultSet rs = ps.executeQuery();
						
						if (rs.next()) {
							Payload payload = new Payload(new JsonObject().put("username", username).encode());
							JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
							JWSObject jwsObject = new JWSObject(header, payload);
							JWSSigner signer = new MACSigner(((String) ConfigHelper.get(ConfigHelper.SECURITY_JWT_SECRET, "password_of_at_least_32_characters")).getBytes());
							
							jwsObject.sign(signer);
							
							jwtToken = jwsObject.serialize();
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e.getMessage());
					}
					finally {
						try {
							if (connection != null) {
								connection.close();
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage());
						}
					}
					return jwtToken;
				}
			});
}
