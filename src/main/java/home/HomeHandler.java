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

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.hazelcast.util.StringUtil;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphQLHandler;
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

            System.out.println("\nHeaders:");
            MultiMap headers = request.headers();
            for (Entry<String, String> entry : headers) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
            }

			JsonObject body = new JsonObject(routingContext.getBodyAsString().replaceAll("\\n", "").replaceAll("\\t", ""));
			System.out.println(body.encodePrettily());
			
			JsonObject queryResult = body.getJsonObject("queryResult");
			JsonObject parameters = queryResult.getJsonObject("parameters");
			
			String action = queryResult.getString("action");

			JsonObject json = new JsonObject();
			
			switch (action) {
			
				case "basicProductSearch":
					String searchCriteria = parameters.getString("searchCriteria");
					String price = parameters.getString("price");
					String brand = parameters.getString("brand");
					Double quantity = parameters.getDouble("quantity");
					
					String criteria = "";
					for (String word: searchCriteria.split(" ")) {
						criteria += word + " & ";
					}
					
					System.out.println("searchCriteria: " + criteria);
					System.out.println("price: " + price);
					System.out.println("brand: " + brand);
					System.out.println("quantity: " + quantity);

					HashMap<String, Object> variables = new HashMap<String, Object>();
					variables.put("searchCriteria", criteria.substring(0, criteria.length() - 3));
					if (brand != null && !brand.equals("")) {
						variables.put("brand", brand);
					}
					if (price != null && !price.equals("")) {
						variables.put("price", price);
					}

					String query = "query items(\n" + 
							"  	$searchCriteria: String! \n" + 
							(brand != null && !brand.equals("") ? "  	$brand: String! \n" : "") + 
							(price != null && !price.equals("") ? "  	$price: Float! \n" : "") + 
							"  ) { \n" + 
							"  items: Products_ItemList(\n" + 
							"    where: {\n" + 
							"      AND: [\n" + 
							"        {\n" + 
							"          OR: [\n" + 
							"            { description: { SEARCH: { query: $searchCriteria }}}\n" + 
							"            { name: { SEARCH: { query: $searchCriteria }}}\n" + 
							"          ]\n" + 
							"        }\n" + 
							(brand != null && !brand.equals("") ? "        { brand: { SEARCH: { query: $brand }}}\n" : "") + 
							(price != null && !price.equals("") ? "        { salePrice: { LT: $price }}\n" : "") + 
							"      ]\n" + 
							"    }\n" + 
							"    limit: 1\n" + 
							"  ) {\n" + 
							"    name\n" + 
							"    description\n" + 
							"    brand\n" + 
							"    salePrice\n" + 
							"  }\n" + 
							"}\n";
					
					ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
							.operationName("items")
							.query(query)
							.context(variables)
							.variables(variables);
					
					JsonObject result = new JsonObject();

					System.out.println(variables);
					System.out.println(query);
					
					GraphQL graphql = GraphQLHandler.getGraphQL();
			    	ExecutionResult executionResult = graphql.execute(executionInput.build());
					
			    	result.put("data", (Object) executionResult.getData());
					
			    	JsonObject item = result.getJsonObject("data").getJsonArray("items").getJsonObject(0);
			    	
					json.put("fulfillmentText", "Tengo el producto " + item.getString("name") + " de la marca " + item.getString("brand") + " a " + item.getDouble("salePrice") + " euros")
							.put("source", "niledb.com")
							.put("outputContexts", new JsonArray()
									.add(new JsonObject()
											.put("name", "projects/${PROJECT_ID}/agent/sessions/${SESSION_ID}/contexts/basicProductSearch")
											.put("lifespanCount", 5)
											.put("parameters", new JsonObject()
													.put("param1", "valor"))));
					
					break;
			
				default:
					break;
			}
			
            request.response().putHeader("Content-Type", "application/json").end(json.encode());
		}
		catch (Exception e) {
			logger.debug(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
}
