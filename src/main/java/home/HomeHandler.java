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
package home;

import java.util.Map.Entry;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * @author NileDB, Inc.
 */
public class HomeHandler {
	
	static final Logger logger = LoggerFactory.getLogger(HomeHandler.class);
	
	public static void execute(RoutingContext routingContext) {
		try {
			HttpServerResponse response = routingContext.response();
			HttpServerRequest request = routingContext.request();

            System.out.println(request.method());
            System.out.println(request.absoluteURI());
            System.out.println(request.uri());

            System.out.println("\nParams:");
            MultiMap params = request.params();
            for (Entry<String, String> entry : params) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            System.out.println("\nHeaders:");
            MultiMap headers = request.headers();
            for (Entry<String, String> entry : headers) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
            }

			JsonObject body = new JsonObject(routingContext.getBodyAsString().replaceAll("\\n", "").replaceAll("\\t", ""));
			System.out.println(body.encodePrettily());
			
			JsonObject json = new JsonObject()
					.put("fulfillmentText", "Mola mucho")
					.put("source", "niledb.com")
					.put("payload", new JsonObject().put("data", "hola"));
			
            request.response().putHeader("Content-Type", "application/json").end(json.encode());
		} 
		catch (Exception e) {
			logger.debug(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
}
