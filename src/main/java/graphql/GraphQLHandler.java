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
package graphql;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import data.CustomType;
import data.CustomTypeAttribute;
import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityReference;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import security.fielddefinitions.ColumnPrivilegeGrant;
import security.fielddefinitions.ColumnPrivilegeRevoke;
import security.fielddefinitions.CurrentUser;
import security.fielddefinitions.DisableRowLevelSecurity;
import security.fielddefinitions.EnableRowLevelSecurity;
import security.fielddefinitions.FilterByCurrentUserCreate;
import security.fielddefinitions.FilterByCurrentUserDelete;
import security.fielddefinitions.Login;
import security.fielddefinitions.RoleChangePassword;
import security.fielddefinitions.RoleCreate;
import security.fielddefinitions.RoleDelete;
import security.fielddefinitions.RoleGrant;
import security.fielddefinitions.RoleRevoke;
import security.fielddefinitions.SecurityEnabled;
import security.fielddefinitions.TablePrivilegeGrant;
import security.fielddefinitions.TablePrivilegeRevoke;
import helpers.ConfigHelper;
import helpers.DatabaseHelper;
import helpers.GraphQLAdditionalTypesHelper;
import helpers.GraphQLMutationSchemaHelper;
import helpers.GraphQLQuerySchemaHelper;
import helpers.GraphQLSqlDeleteHelper;
import helpers.GraphQLSqlInsertHelper;
import helpers.GraphQLSqlSelectHelper;
import helpers.GraphQLSqlUpdateHelper;
import helpers.Helper;
import helpers.IMDGHelper;
import helpers.SqlDeleteCommand;
import helpers.SqlInsertCommand;
import helpers.SqlSelectCommand;
import helpers.SqlUpdateCommand;
import helpers.maps.CustomTypeMap;
import helpers.maps.EntityMap;
import helpers.maps.SchemaMap;

/**
 * @author NileDB, Inc.
 */
public class GraphQLHandler {
	
	static final Logger logger = LoggerFactory.getLogger(GraphQLHandler.class);
	
	private static GraphQL graphql = null;
	
	static final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.setSerializationInclusion(Include.NON_NULL);
	
	public static synchronized void refreshSchema() {
		
		logger.info("Reloading GraphQL schema from database...");
		
		@SuppressWarnings("unchecked")
		List<String> schemaNames = (List<String>) ((JsonArray) ConfigHelper.get(ConfigHelper.DB_SCHEMA_NAMES, new JsonArray().add("public"))).getList();
		boolean multiSchema = schemaNames.size() > 1;
		
		final Database database = DatabaseHelper.getDatabaseModel(
				(String) ConfigHelper.get(ConfigHelper.DB_HOST, "localhost"), 
				(Integer) ConfigHelper.get(ConfigHelper.DB_PORT, 5432), 
				(String) ConfigHelper.get(ConfigHelper.DB_NAME, "nile"),
				schemaNames);
		
		GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
		SchemaMap.database = database;
		SchemaMap.entities = new HashMap<String, EntityMap>();
		SchemaMap.customTypes = new HashMap<String, CustomTypeMap>();
		SchemaMap.entityNameByUnderscoredName = new HashMap<String, String>();
		
		// Define the GraphQL object type for the mutation
		GraphQLObjectType.Builder mutationBuilder = newObject()
				.name("Mutation")
				.description("Operations that make changes to the system.")
				
				.field(newFieldDefinition()
						.name("__setData")
						.description("It sets data into In-Memory Data Grid.")
						.argument(newArgument()
								.name("map")
								.description("The map name.")
								.type(GraphQLNonNull.nonNull(GraphQLString)))
						.argument(newArgument()
								.name("key")
								.description("The key name.")
								.type(GraphQLNonNull.nonNull(GraphQLString)))
						.argument(newArgument()
								.name("value")
								.description("The value.")
								.type(GraphQLNonNull.nonNull(GraphQLString)))
						.type(GraphQLBoolean)
						.dataFetcher(new DataFetcher<Boolean>() {
							@Override
							public Boolean get(DataFetchingEnvironment environment) {
								IMDGHelper.get().getMap(environment.getArgument("map")).set(environment.getArgument("key"), environment.getArgument("value"));
								return true;
							}
						}))
				
				.field(newFieldDefinition()
						.name("__deleteData")
						.description("It deletes data from In-Memory Data Grid.")
						.argument(newArgument()
								.name("map")
								.description("The map name.")
								.type(GraphQLNonNull.nonNull(GraphQLString)))
						.argument(newArgument()
								.name("key")
								.description("The key name.")
								.type(GraphQLNonNull.nonNull(GraphQLString)))
						.type(GraphQLBoolean)
						.dataFetcher(new DataFetcher<Boolean>() {
							@Override
							public Boolean get(DataFetchingEnvironment environment) {
								IMDGHelper.get().getMap(environment.getArgument("map")).delete(environment.getArgument("key"));
								return true;
							}
						}))
				
				.field(newFieldDefinition()
						.name("__reloadSchema")
						.description("It reloades the GraphQL schema from database.")
						.type(GraphQLString)
						.dataFetcher(new DataFetcher<String>() {
							@Override
							public String get(DataFetchingEnvironment environment) {
								refreshSchema();
								return "ok";
							}
						}));
		
		// Define the GraphQL object type for the query
		GraphQLObjectType.Builder queryBuilder = newObject()
				.name("Query")
				.description("Query operations. They do not make changes to the system.");
		
		// Get Schemas
		HashSet<GraphQLType> additionalTypes = new HashSet<GraphQLType>();
		
		// Get CustomTypes
		logger.info("Adding custom types...");
		for (int i = 0; i < database.getCustomTypes().size(); i++) {
			CustomType customType = database.getCustomTypes().get(i);
			additionalTypes.add(GraphQLQuerySchemaHelper.getCustomTypeGraphqlObjectType(database, customType, multiSchema));
			additionalTypes.add(GraphQLMutationSchemaHelper.getCustomTypeGraphqlInputObjectType(database, customType, multiSchema));
			
			CustomTypeMap customTypeMap = new CustomTypeMap();
			customTypeMap.attributes = new HashMap<String, CustomTypeAttribute>();
			for (int j = 0; j < customType.getAttributes().size(); j++) {
				CustomTypeAttribute customTypeAttribute = customType.getAttributes().get(j);
				customTypeMap.attributes.put(customTypeAttribute.getName(), customTypeAttribute);
			}
			SchemaMap.customTypes.put(customType.getSchema() + "." + customType.getName(), customTypeMap);
		}
		
		// Several additional types
		logger.info("Adding additional types...");
		additionalTypes.addAll(GraphQLAdditionalTypesHelper.getAuxiliaryTypes());
		
		// Get Entities
		logger.info("Adding entities...");
		for (int i = 0; i < database.getEntities().size(); i++) {
			Entity entity = database.getEntities().get(i);
			logger.info("Adding " + entity.getName() + " entity...");
			
			for (int j = 0; j < entity.getAttributes().size(); j++) {
				EntityAttribute attribute = entity.getAttributes().get(j);
				GraphQLInputObjectType objectType = GraphQLQuerySchemaHelper.getEntityAttributeWhereTypeGraphqlObjectType(database, attribute, multiSchema);
				if (objectType != null) {
						additionalTypes.add(objectType);
				}
			}
			
			// EntityOrderByType
			additionalTypes.add(GraphQLQuerySchemaHelper.getEntityOrderByAttributesGraphqlEnumType(database, entity, multiSchema));
			additionalTypes.add(GraphQLQuerySchemaHelper.getEntityOrderByGraphqlObjectType(database, entity, multiSchema));
			
			// EntityWhereType
			additionalTypes.add(GraphQLQuerySchemaHelper.getEntityWhereGraphqlObjectType(database, entity, multiSchema));
			
			EntityMap entityMap = new EntityMap();
			entityMap.entity = entity;
			entityMap.attributes = new HashMap<String, EntityAttribute>();
			entityMap.directReferences = new HashMap<String, EntityReference>();
			entityMap.inverseReferences = new HashMap<String, EntityReference>();
			SchemaMap.entities.put(entity.getSchema() + "." + entity.getName(), entityMap);
			
			// Entity query definition
			queryBuilder.field(newFieldDefinition()
					.name((schemaNames.size() == 1 ? "" : entity.getSchema() + "_") + entity.getName() + "List")
					.description("It queries " + Helper.toFirstUpper(entity.getName()) + " entities.")
					.type(new GraphQLList(GraphQLQuerySchemaHelper.getEntityResultGraphqlObjectType(database, entity, multiSchema)))
					.argument(newArgument()
							.name("where")
							.description("Search criteria.")
							.type(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "WhereType")))
					.argument(newArgument()
							.name("orderBy")
							.description("Sorting criteria.")
							.type(new GraphQLList(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "OrderByType"))))
					.argument(newArgument()
							.name("limit")
							.description("Maximum number of items to return.")
							.type(GraphQLInt))
					.argument(newArgument()
							.name("offset")
							.description("Number of items to skip.")
							.type(GraphQLInt))
					
					.dataFetcher(new DataFetcher<Object>() {
						@SuppressWarnings("unchecked")
						@Override
						public Object get(DataFetchingEnvironment environment) {
							List<Field> fields = environment.getFields();
							
							Connection connection = null;
							try {
								connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
								
								Field field = fields.get(0);
								
								SqlSelectCommand sqlSelectCommand = GraphQLSqlSelectHelper.getCommand(field, environment, entity, database, 1, true);
								
								StringBuffer sb = new StringBuffer("SELECT array_to_json(array_agg(row_to_json(\"list_0\", true)), true) AS \"list\" FROM (\n")
												.append(sqlSelectCommand)
												.append(") AS \"list_0\"");
								
								PreparedStatement ps = connection.prepareStatement(sb.toString());
								logger.debug(sb.toString());
								List<Object> parameters = sqlSelectCommand.getParameters();
								for (int k = 0; k < parameters.size(); k++) {
									ps.setObject(k + 1, parameters.get(k));
								}
								ResultSet rs = ps.executeQuery();
								rs.next();

								JsonArray result = null;
								String resultString = rs.getString("list");
								if (resultString != null) {
									result = new JsonArray(resultString);
								}
								else {
									result = new JsonArray();
								}
								rs.close();
								
								logger.debug(result.encodePrettily());
								
								return result;
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
						}
					}));
			
			// Entity mutation insert definition
			mutationBuilder.field(newFieldDefinition()
					.name((schemaNames.size() == 1 ? "" : entity.getSchema() + "_") + entity.getName() + "Create")
					.description("It creates a new " + Helper.toFirstUpper(entity.getName()) + " entity.")
					.type(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "ResultType"))
					.argument(newArgument()
							.name("entity")
							.description("The " + entity.getName() + " entity to create.")
							.type(GraphQLNonNull.nonNull(GraphQLMutationSchemaHelper.getEntityGraphqlCreateInputObjectType(database, entity, multiSchema))))
					.dataFetcher(new DataFetcher<Object>() {
						@SuppressWarnings("unchecked")
						@Override
						public Object get(DataFetchingEnvironment environment) {
							List<Field> fields = environment.getFields();
							
							Connection connection = null;
							try {
								connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
								
								Field field = fields.get(0);
								
								SqlInsertCommand sqlInsertCommand = GraphQLSqlInsertHelper.getCommand(field, environment, entity, database);
								
								StringBuffer sb = new StringBuffer()
												.append(sqlInsertCommand);
								
								logger.debug(sb.toString());
								
								PreparedStatement ps = connection.prepareStatement(sb.toString());
								for (int j = 0; j < sqlInsertCommand.values.size(); j++) {
									ps.setObject(j + 1, sqlInsertCommand.values.get(j));
								}
								ResultSet rs = ps.executeQuery();
								rs.next();
								
								JsonObject result = new JsonArray(rs.getString("list")).getJsonObject(0);
								logger.debug(result.encodePrettily());
								rs.close();
								
								// Chaining return values to input values
								Map<String, Object> context = (Map<String, Object>) environment.getContext();
								for (String fieldName: result.fieldNames()) {
									context.put(fieldName, result.getValue(fieldName));
									
								}
								
								return result;
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
						}
					}));
			
			// Entity mutation update definition
			mutationBuilder.field(newFieldDefinition()
					.name((schemaNames.size() == 1 ? "" : entity.getSchema() + "_") + entity.getName() + "Update")
					.description("It updates one or more " + Helper.toFirstUpper(entity.getName()) + " entities.")
					.type(GraphQLList.list(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "ResultType")))
					.argument(newArgument()
							.name("entity")
							.description("The changes to be applied.")
							.type(GraphQLNonNull.nonNull(GraphQLMutationSchemaHelper.getEntityGraphqlUpdateInputObjectType(database, entity, multiSchema))))
					.argument(newArgument()
							.name("where")
							.description("Search criteria for selecting the entities that must be updated.")
							.type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "WhereType"))))
					.dataFetcher(new DataFetcher<Object>() {
						@SuppressWarnings("unchecked")
						@Override
						public Object get(DataFetchingEnvironment environment) {
							List<Field> fields = environment.getFields();
							
							Connection connection = null;
							try {
								connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
								
								Field field = fields.get(0);
								
								SqlUpdateCommand sqlUpdateCommand = GraphQLSqlUpdateHelper.getCommand(field, environment, entity, database);
								
								StringBuffer sb = new StringBuffer()
												.append(sqlUpdateCommand);
								
								logger.debug(sb.toString());
								
								PreparedStatement ps = connection.prepareStatement(sb.toString());
								sqlUpdateCommand.values.addAll(sqlUpdateCommand.whereParameters);
								for (int j = 0; j < sqlUpdateCommand.values.size(); j++) {
									ps.setObject(j + 1, sqlUpdateCommand.values.get(j));
								}
								ResultSet rs = ps.executeQuery();
								rs.next();
								
								JsonArray result = new JsonArray();
								String resultString = rs.getString("list");
								if (resultString != null) {
									result = new JsonArray(resultString);
								}
								logger.debug(result.encodePrettily());
								rs.close();
								
								return result;
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
						}
					}));
			
			// Entity mutation delete definition
			mutationBuilder.field(newFieldDefinition()
					.name((schemaNames.size() == 1 ? "" : entity.getSchema() + "_") + entity.getName() + "Delete")
					.description("It deletes one or more " + Helper.toFirstUpper(entity.getName()) + " entities.")
					.type(GraphQLList.list(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "ResultType")))
					.argument(newArgument()
							.name("where")
							.description("Search criteria for selecting the entities that must be deleted.")
							.type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "WhereType"))))
					.dataFetcher(new DataFetcher<Object>() {
						@SuppressWarnings("unchecked")
						@Override
						public Object get(DataFetchingEnvironment environment) {
							List<Field> fields = environment.getFields();
							
							Connection connection = null;
							try {
								connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
								
								Field field = fields.get(0);
								
								SqlDeleteCommand sqlDeleteCommand = GraphQLSqlDeleteHelper.getCommand(field, environment, entity, database);
								
								StringBuffer sb = new StringBuffer()
												.append(sqlDeleteCommand);
								
								logger.debug(sb.toString());
								
								PreparedStatement ps = connection.prepareStatement(sb.toString());
								for (int j = 0; j < sqlDeleteCommand.whereParameters.size(); j++) {
									ps.setObject(j + 1, sqlDeleteCommand.whereParameters.get(j));
								}
								ResultSet rs = ps.executeQuery();
								rs.next();
								
								JsonArray result = new JsonArray();
								String resultString = rs.getString("list");
								if (resultString != null) {
									result = new JsonArray(resultString);
								}
								logger.debug(result.encodePrettily());
								rs.close();
								
								return result;
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
						}
					}));
		}
		
		queryBuilder.field(newFieldDefinition()
				.name("__getData")
				.description("It gets data from In-Memory Data Grid.")
				.argument(newArgument()
						.name("map")
						.description("The map name.")
						.type(GraphQLNonNull.nonNull(GraphQLString)))
				.argument(newArgument()
						.name("key")
						.description("The key name.")
						.type(GraphQLNonNull.nonNull(GraphQLString)))
				.type(GraphQLString)
				.dataFetcher(new DataFetcher<String>() {
					@Override
					public String get(DataFetchingEnvironment environment) {
						return (String) IMDGHelper.get().getMap(environment.getArgument("map")).get(environment.getArgument("key"));
					}
				}));
		
		queryBuilder.field(newFieldDefinition()
				.name("__systemAverageLoad")
				.description("It returns the system average load in order to decide to scale or not in a elastic context.")
				.type(GraphQLFloat)
				.dataFetcher(new DataFetcher<Double>() {
					@Override
					public Double get(DataFetchingEnvironment environment) {
						OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
						return osBean.getSystemLoadAverage();
					}
				}));
		
		// Security operations
		queryBuilder.field(SecurityEnabled.builder);
		if ((Boolean) ConfigHelper.get(ConfigHelper.SECURITY_ENABLED, false)) {
			queryBuilder.field(CurrentUser.builder);

			/*
			queryBuilder.field(RoleList.builder);
			queryBuilder.field(TablePrivilegeList.builder);
			queryBuilder.field(ColumnPrivilegeList.builder);
			queryBuilder.field(PolicyList.builder);
			*/
			
			mutationBuilder.field(Login.builder);
			
			mutationBuilder.field(RoleCreate.builder);
			mutationBuilder.field(RoleDelete.builder);
			mutationBuilder.field(RoleChangePassword.builder);
			mutationBuilder.field(RoleGrant.builder);
			mutationBuilder.field(RoleRevoke.builder);
			
			mutationBuilder.field(TablePrivilegeGrant.builder);
			mutationBuilder.field(TablePrivilegeRevoke.builder);
			
			mutationBuilder.field(ColumnPrivilegeGrant.builder);
			mutationBuilder.field(ColumnPrivilegeRevoke.builder);
			
			mutationBuilder.field(DisableRowLevelSecurity.builder);
			mutationBuilder.field(EnableRowLevelSecurity.builder);
			
			mutationBuilder.field(FilterByCurrentUserCreate.builder);
			mutationBuilder.field(FilterByCurrentUserDelete.builder);
		}
		
		// EntityEnumType
		GraphQLEnumType.Builder entityEnumType = newEnum()
				.name("EntityEnumType")
				.description("List of entity names");
		for (String entityName : SchemaMap.entities.keySet()) {
			String underscoredName = entityName.replaceAll("\\.", "_");
			entityEnumType.value(underscoredName);
			String fullName = "";
			StringTokenizer st = new StringTokenizer(entityName, ".");
			while (st.hasMoreTokens()) {
				fullName += "\"" + st.nextToken() + "\".";
			}
			SchemaMap.entityNameByUnderscoredName.put(underscoredName, fullName.substring(0, fullName.length() - 1));
		}
		additionalTypes.add(entityEnumType.build());
		
		// Build GraphQL schema
		schemaBuilder.query(queryBuilder);
		schemaBuilder.mutation(mutationBuilder);
		schemaBuilder.additionalTypes(additionalTypes);
		
		GraphQLSchema schema = schemaBuilder.build();
		
		graphql = GraphQL.newGraphQL(schema).build();
		logger.info("New GraphQL schema loaded successfully.");
	}
	
	public static synchronized GraphQL getGraphQL() {
		if (graphql == null) {
			refreshSchema();
		}
		return graphql;
	}
	
	public static void execute(RoutingContext routingContext) {
		try {
			HttpServerResponse response = routingContext.response();
			HttpServerRequest request = routingContext.request();
			
			GraphQL graphql = getGraphQL();
			String query = request.getParam("query");
			
			HashMap<String, Object> variables = new HashMap<String, Object>();
			
			String operationName = null;
			if (request.method() == HttpMethod.POST) {
				JsonObject body = new JsonObject(
						routingContext.getBodyAsString().replaceAll("\\n", "").replaceAll("\\t", ""));
				if (query == null) {
					query = body.getString("query");
				}
				JsonObject variablesJson = body.getJsonObject("variables");
				if (variablesJson != null) {
					Iterator<String> fieldNames = variablesJson.fieldNames().iterator();
					while (fieldNames.hasNext()) {
						String fieldName = fieldNames.next();
						variables.put(fieldName, variablesJson.getValue(fieldName));
					}
				}
				operationName = body.getString("operationName");
			}
			
			ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput()
					.operationName(operationName)
					.query(query)
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
			headers.add("Content-Type", "application/json");
			response.end(result.encode());
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
}
