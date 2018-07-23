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

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLHandler;
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

			JsonObject body = new JsonObject(routingContext.getBodyAsString().replaceAll("\\n", "").replaceAll("\\t", ""));
			
			JsonObject queryResult = body.getJsonObject("queryResult");
			JsonObject parameters = queryResult.getJsonObject("parameters");
			
			String action = queryResult.getString("action");

			JsonObject json = new JsonObject();
			
			switch (action) {
			
				case "basicProductSearch":
				case "otherBrands":
				case "nextItem":
					
					if (action.equals("otherBrands")
							|| action.equals("nextItem")) {
						parameters = queryResult.getJsonArray("outputContexts").getJsonObject(0).getJsonObject("parameters").getJsonObject("parameters");
						
						if (parameters.getString("searchCriteria") == null || parameters.getString("searchCriteria").equals("")) {
							json.put("fulfillmentText", "Lo siento, pero no te entiendo.")
								.put("source", "niledb.com");
				            response.putHeader("Content-Type", "application/json; charset=utf-8").end(json.encode());
				            return;
						}
					}
					
					String searchCriteria = parameters.getString("searchCriteria");
					Double price = (parameters.getValue("price") == null || parameters.getValue("price").equals("") ? null : parameters.getDouble("price"));
					String brand = parameters.getString("brand");
					Integer quantity = null;
					if (parameters.getValue("quantity") != null && !parameters.getValue("quantity").equals("")) {
						quantity = parameters.getInteger("quantity");
					}
					Integer offset = 0;
					if (parameters.getValue("offset") != null && !parameters.getValue("offset").equals("")) {
						offset = parameters.getInteger("offset");
					}

					if (action.equals("nextItem")) {
						offset = queryResult.getJsonArray("outputContexts").getJsonObject(0).getJsonObject("parameters").getInteger("offset") + 1;
					}
					
					String criteria = "";
					for (String word: searchCriteria.split(" ")) {
						criteria += word + " & ";
					}
					
					String brandCriteria = "";
					if (brand != null & !brand.equals("")) {
						for (String word: brand.split(" ")) {
							brandCriteria += word + " & ";
						}
						if (action.equals("otherBrands")) {
							brandCriteria = "!" + brandCriteria;
						}
					}
					
					/*
					System.out.println("searchCriteria: " + criteria);
					System.out.println("price: " + price);
					System.out.println("brand: " + brandCriteria);
					System.out.println("quantity: " + quantity);
					*/

					HashMap<String, Object> variables = new HashMap<String, Object>();
					variables.put("searchCriteria", criteria.substring(0, criteria.length() - 3));
					if (brand != null && !brand.equals("")) {
						variables.put("brand", brandCriteria.substring(0, brandCriteria.length() - 3));
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
							"    offset: " + offset + "\n" + 
							"    where: {\n" + 
							"      AND: [\n" + 
							"        {\n" + 
							"          OR: [\n" + 
							//"            { description: { SEARCH: { query: $searchCriteria }}}\n" + 
							"            { name: { SEARCH: { query: $searchCriteria }}}\n" + 
							"          ]\n" + 
							"        }\n" + 
							(brand != null && !brand.equals("") ? "        { brand: { SEARCH: { query: $brand }}}\n" : "") + 
							(price != null && !price.equals("") ? "        { salePrice: { LT: $price }}\n" : "") + 
							"      ]\n" + 
							"    }\n" + 
							"    limit: 1\n" + 
							"  ) {\n" + 
							"    id\n" + 
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

					System.out.println("--------");
					System.out.println(variables);
					System.out.println(query);
					
					GraphQL graphql = GraphQLHandler.getGraphQL();
			    	ExecutionResult executionResult = graphql.execute(executionInput.build());
					
			    	result.put("data", (Object) executionResult.getData());
					
			    	JsonArray items = result.getJsonObject("data").getJsonArray("items");
			    	
			    	if (items != null && items.size() > 0) {
				    	JsonObject item = result.getJsonObject("data").getJsonArray("items").getJsonObject(0);
				    	
						json.put("fulfillmentText", "Tengo el producto " + item.getString("name").toLowerCase() + " de la marca " + item.getString("brand").toLowerCase() + " a un precio de " + item.getDouble("salePrice") + " euros")
								.put("source", "niledb.com")
								.put("outputContexts", new JsonArray()
										.add(new JsonObject()
												.put("name", "projects/${PROJECT_ID}/agent/sessions/${SESSION_ID}/contexts/basicProductSearch")
												.put("lifespanCount", 5)
												.put("parameters", new JsonObject()
														.put("parameters", (queryResult.getJsonObject("parameters").isEmpty() ? queryResult.getJsonArray("outputContexts").getJsonObject(0).getJsonObject("parameters").getJsonObject("parameters") : queryResult.getJsonObject("parameters")))
														.put("id", item.getInteger("id"))
														.put("name", item.getString("name"))
														.put("description", item.getString("description"))
														.put("salePrice", item.getDouble("salePrice"))
														.put("quantity", (quantity == null ? 1 : quantity))
														.put("offset", offset)
														.put("brand", item.getString("brand")))));
			    	}
			    	else {
						json.put("fulfillmentText", "Lo siento, pero ahora mismo ya no tengo más.")
								.put("source", "niledb.com");
			    	}
					
					break;
					
				default:
						json.put("fulfillmentText", "Lo siento, pero te entiendo.")
								.put("source", "niledb.com");
					break;
			}
            response.putHeader("Content-Type", "application/json; charset=utf-8").end(json.encode());
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}
}
