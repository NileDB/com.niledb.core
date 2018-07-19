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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import helpers.ConfigHelper;
import home.HomeHandler;
import graphql.GraphQLHandler;

/**
 * @author NileDB, Inc.
 */
public class HttpVerticle extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(HttpVerticle.class);
	
	@Override
	public void start() throws Exception {

		HttpServerOptions options = new HttpServerOptions()
				.setCompressionSupported(true);
		
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
		HttpServer server = vertx.createHttpServer(options);
		
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		if ((boolean) ConfigHelper.get(ConfigHelper.SERVICE_AUTHENTICATE, false)) {
			AuthHandler basicAuthHandler = BasicAuthHandler.create(new AuthProvider() {
				@Override
				public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
					
					resultHandler.handle(new AsyncResult<User>() {

						@Override
						public User result() {
							return new User() {
								
								@Override
								public void setAuthProvider(AuthProvider authProvider) {
								}
								
								@Override
								public JsonObject principal() {
									return authInfo;
								}
								
								@Override
								public User isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
									return null;
								}
								
								@Override
								public User clearCache() {
									return null;
								}
							};
						}

						@Override
						public Throwable cause() {
							return null;
						}

						@Override
						public boolean succeeded() {
							return (ConfigHelper.get(ConfigHelper.SERVICE_USERNAME, "niledb").equals(authInfo.getString("username"))
									&& ConfigHelper.get(ConfigHelper.SERVICE_PASSWORD, "1234").equals(authInfo.getString("password")));
						}

						@Override
						public boolean failed() {
							return false;
						}
					});
				}
			});
			router.route().handler(basicAuthHandler);
		}

		// Deep learning tests
		//router.get("/deep").blockingHandler(DeepLearningHandler::execute);
		
		// We replace GET method with GraphQL-Playground
		//router.get("/graphql").blockingHandler(GraphQLHandler::execute);

		router.post("/home").blockingHandler(HomeHandler::execute);
		
		router.post("/graphql").blockingHandler(GraphQLHandler::execute);
		router.options("/graphql").handler(routingContext -> {
			final HttpServerRequest request = routingContext.request();
			final HttpServerResponse response = routingContext.response();
			response.setChunked(true);
			
			MultiMap headers = response.headers();
			
			// CORS Headers
			String origin = request.headers().get("Origin");
			if (origin != null && !origin.equals("")) {
				headers.add("Access-Control-Allow-Origin", origin);
				headers.add("Access-Control-Allow-Credentials", "true");
				headers.add("Vary", "Accept-Encoding, Origin");
			}
			else {
				headers.add("Access-Control-Allow-Origin", "*");
			}
			headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS, HEAD");
			headers.add("Access-Control-Allow-Headers", "X-Apollo-Tracing, Authorization, Cache-Control, X-XSRF, Origin, X-Requested-With, Content-Type, Accept, Content-Length");
			headers.add("Allow", "HEAD, GET, DELETE, OPTIONS");
			
			response.setStatusCode(200);
			response.end();
		});
		
		router.route("/*").handler(StaticHandler.create()
				.setAllowRootFileSystemAccess(false)
				.setCachingEnabled(true)
				.setMaxAgeSeconds(60 * 60 * 24 * 30)
				.setSendVaryHeader(true)
				.setDefaultContentEncoding("UTF-8")
				.setDirectoryListing(false)
				.setFilesReadOnly(true)
				.setIndexPage("index.html"));

		//server.requestHandler(router::accept).listen((Integer) ConfigHelper.get(ConfigHelper.SERVICE_PORT, 8080), (String) ConfigHelper.get(ConfigHelper.SERVICE_HOST, "localhost"));
		server.requestHandler(router::accept).listen((Integer) ConfigHelper.get(ConfigHelper.SERVICE_PORT, 8080));
		
		if ((Boolean) ConfigHelper.get(ConfigHelper.SERVICE_FORCE_SSL, true)) {
			HttpServer redirectServer = vertx.createHttpServer();
			Router redirectRouter = Router.router(vertx);
			
			redirectRouter.get().handler(routingContext -> {
                routingContext.response().headers().add("Location", (String) ConfigHelper.get(ConfigHelper.SERVICE_REDIRECT_URL, "https://localhost:8443"));
                routingContext.response().setStatusCode(301);
                routingContext.response().end();
			});
			redirectServer.requestHandler(redirectRouter::accept).listen((Integer) ConfigHelper.get(ConfigHelper.SERVICE_REDIRECT_FROM_PORT, 8080));
		}
	}
}
