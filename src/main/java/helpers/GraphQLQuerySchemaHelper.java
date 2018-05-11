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
package helpers;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLEnumType.newEnum;

import data.CustomType;
import data.CustomTypeAttribute;
import data.CustomTypeAttributeType;
import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityAttributeType;
import data.EntityReference;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import io.vertx.core.json.JsonObject;
import helpers.maps.EntityMap;
import helpers.maps.SchemaMap;

/**
 * @author NileDB, Inc.
 */
public class GraphQLQuerySchemaHelper {
	
	/**
	 * Define a GraphQL object type for a custom type
	 * 
	 * @param database
	 *            The database model
	 * @param customType
	 *            The custom type
	 * @return The GraphQL object type
	 */
	public static final GraphQLObjectType getCustomTypeGraphqlObjectType(Database database, CustomType customType, boolean multiSchema) {
		GraphQLObjectType.Builder graphqlObjectType = newObject()
				.name((multiSchema ? Helper.toFirstUpper(customType.getSchema()) + "_" : "") + Helper.toFirstUpper(customType.getName()) + "Type")
				.description("Custom type " + Helper.toFirstUpper(customType.getName()) + ".");
		
		for (int i = 0; i < customType.getAttributes().size(); i++) {
			CustomTypeAttribute attribute = customType.getAttributes().get(i);
			
			GraphQLFieldDefinition.Builder fieldDefinition = newFieldDefinition()
					.name(attribute.getName())
					.description(attribute.getDocumentation())
					.dataFetcher(new DataFetcher<Object>() {
						@Override
						public Object get(DataFetchingEnvironment environment) {
							return ((JsonObject) environment.getSource())
									.getValue(attribute.getName());
						}
					});
			
			if (attribute.isArray()) {
				switch (attribute.getType().getValue()) {
					case CustomTypeAttributeType.TEXT_VALUE:
					case CustomTypeAttributeType.DATE_VALUE:
					case CustomTypeAttributeType.TIMESTAMP_VALUE:
					case CustomTypeAttributeType.BYTEA_VALUE:
					case CustomTypeAttributeType.VARCHAR_VALUE:
					case CustomTypeAttributeType.CHAR_VALUE:
					case CustomTypeAttributeType.TIME_VALUE:
					case CustomTypeAttributeType.INTERVAL_VALUE:
					case CustomTypeAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
					case CustomTypeAttributeType.TIME_WITH_TIME_ZONE_VALUE:
					case CustomTypeAttributeType.MONEY_VALUE:
					case CustomTypeAttributeType.POINT_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLString));
						break;
						
					case CustomTypeAttributeType.BOOLEAN_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLBoolean));
						break;
						
					case CustomTypeAttributeType.INTEGER_VALUE:
					case CustomTypeAttributeType.SMALLINT_VALUE:
					case CustomTypeAttributeType.BIGINT_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLInt));
						break;
						
					case CustomTypeAttributeType.DECIMAL_VALUE:
					case CustomTypeAttributeType.DOUBLE_PRECISION_VALUE:
					case CustomTypeAttributeType.REAL_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLFloat));
						break;
						
					case CustomTypeAttributeType.CUSTOM_TYPE_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLTypeReference
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "Type")));
						break;
						
					default:
						fieldDefinition.type(GraphQLList.list(GraphQLString));
				}
			} 
			else {
				switch (attribute.getType().getValue()) {
					case CustomTypeAttributeType.TEXT_VALUE:
					case CustomTypeAttributeType.DATE_VALUE:
					case CustomTypeAttributeType.TIMESTAMP_VALUE:
					case CustomTypeAttributeType.BYTEA_VALUE:
					case CustomTypeAttributeType.VARCHAR_VALUE:
					case CustomTypeAttributeType.CHAR_VALUE:
					case CustomTypeAttributeType.TIME_VALUE:
					case CustomTypeAttributeType.INTERVAL_VALUE:
					case CustomTypeAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
					case CustomTypeAttributeType.TIME_WITH_TIME_ZONE_VALUE:
					case CustomTypeAttributeType.MONEY_VALUE:
					case CustomTypeAttributeType.POINT_VALUE:
						fieldDefinition.type(GraphQLString);
						break;
						
					case CustomTypeAttributeType.BOOLEAN_VALUE:
						fieldDefinition.type(GraphQLBoolean);
						break;
						
					case CustomTypeAttributeType.INTEGER_VALUE:
					case CustomTypeAttributeType.SMALLINT_VALUE:
					case CustomTypeAttributeType.BIGINT_VALUE:
						fieldDefinition.type(GraphQLInt);
						break;
						
					case CustomTypeAttributeType.DECIMAL_VALUE:
					case CustomTypeAttributeType.DOUBLE_PRECISION_VALUE:
					case CustomTypeAttributeType.REAL_VALUE:
						fieldDefinition.type(GraphQLFloat);
						break;
						
					case CustomTypeAttributeType.CUSTOM_TYPE_VALUE:
						fieldDefinition.type(GraphQLTypeReference
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "Type"));
						break;
						
					default:
						fieldDefinition.type(GraphQLString);
				}
			}
			graphqlObjectType.field(fieldDefinition);
		}
		return graphqlObjectType.build();
	}
	
	/**
	 * Define a GraphQL object type for an entity
	 * 
	 * @param database
	 *            The database model
	 * @param customType
	 *            The entity
	 * @return The GraphQL object type
	 */
	public static final GraphQLObjectType getEntityResultGraphqlObjectType(Database database, Entity entity, boolean multiSchema) {
		
		// Get Fields
		GraphQLObjectType.Builder graphqlObjectType = newObject()
				.name((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "ResultType")
				.description("Entity " + Helper.toFirstUpper(entity.getName()) + " result type.");
		
		EntityMap entityMap = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName());
		
		// Get attributes
		for (int i = 0; i < entity.getAttributes().size(); i++) {
			EntityAttribute attribute = entity.getAttributes().get(i);
			entityMap.attributes.put(attribute.getName(), attribute);
			
			GraphQLFieldDefinition.Builder fieldDefinition = newFieldDefinition()
					.name(attribute.getName())
					.description(attribute.getDocumentation())
					.dataFetcher(new DataFetcher<Object>() {
						@Override
						public Object get(DataFetchingEnvironment environment) {
							return ((JsonObject) environment.getSource())
									.getValue(attribute.getName());
						}
					});
			
			if (attribute.isArray()) {
				switch (attribute.getType().getValue()) {
					case EntityAttributeType.TEXT_VALUE:
					case EntityAttributeType.DATE_VALUE:
					case EntityAttributeType.TIMESTAMP_VALUE:
					case EntityAttributeType.BYTEA_VALUE:
					case EntityAttributeType.VARCHAR_VALUE:
					case EntityAttributeType.CHAR_VALUE:
					case EntityAttributeType.TIME_VALUE:
					case EntityAttributeType.INTERVAL_VALUE:
					case EntityAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
					case EntityAttributeType.TIME_WITH_TIME_ZONE_VALUE:
					case EntityAttributeType.MONEY_VALUE:
					case EntityAttributeType.POINT_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLString));
						break;
						
					case EntityAttributeType.BOOLEAN_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLBoolean));
						break;
						
					case EntityAttributeType.INTEGER_VALUE:
					case EntityAttributeType.SERIAL_VALUE:
					case EntityAttributeType.SMALLINT_VALUE:
					case EntityAttributeType.BIGINT_VALUE:
					case EntityAttributeType.SMALLSERIAL_VALUE:
					case EntityAttributeType.BIGSERIAL_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLInt));
						break;
						
					case EntityAttributeType.DECIMAL_VALUE:
					case EntityAttributeType.DOUBLE_PRECISION_VALUE:
					case EntityAttributeType.REAL_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLFloat));
						break;
						
					case EntityAttributeType.CUSTOM_TYPE_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLTypeReference
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "Type")));
						break;
						
					default:
						fieldDefinition.type(GraphQLList.list(GraphQLString));
				}
			} 
			else {
				switch (attribute.getType().getValue()) {
					case EntityAttributeType.TEXT_VALUE:
					case EntityAttributeType.DATE_VALUE:
					case EntityAttributeType.TIMESTAMP_VALUE:
					case EntityAttributeType.BYTEA_VALUE:
					case EntityAttributeType.VARCHAR_VALUE:
					case EntityAttributeType.CHAR_VALUE:
					case EntityAttributeType.TIME_VALUE:
					case EntityAttributeType.INTERVAL_VALUE:
					case EntityAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
					case EntityAttributeType.TIME_WITH_TIME_ZONE_VALUE:
					case EntityAttributeType.MONEY_VALUE:
					case EntityAttributeType.POINT_VALUE:
						fieldDefinition.type(GraphQLString);
						break;
						
					case EntityAttributeType.BOOLEAN_VALUE:
						fieldDefinition.type(GraphQLBoolean);
						break;
						
					case EntityAttributeType.INTEGER_VALUE:
					case EntityAttributeType.SERIAL_VALUE:
					case EntityAttributeType.SMALLINT_VALUE:
					case EntityAttributeType.BIGINT_VALUE:
					case EntityAttributeType.SMALLSERIAL_VALUE:
					case EntityAttributeType.BIGSERIAL_VALUE:
						fieldDefinition.type(GraphQLInt);
						break;
						
					case EntityAttributeType.DECIMAL_VALUE:
					case EntityAttributeType.DOUBLE_PRECISION_VALUE:
					case EntityAttributeType.REAL_VALUE:
						fieldDefinition.type(GraphQLFloat);
						break;
						
					case EntityAttributeType.CUSTOM_TYPE_VALUE:
						fieldDefinition.type(GraphQLTypeReference
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "Type"));
						break;
						
					default:
						fieldDefinition.type(GraphQLString);
				}
			}
			graphqlObjectType.field(fieldDefinition);
		}
		
		// Get direct references - References are required to point to unique
		// indexes, so only one instance will be returned.
		for (int i = 0; i < entity.getReferences().size(); i++) {
			EntityReference reference = entity.getReferences().get(i);
			
			String referenceName = ((Entity) reference.getReferencedKey().eContainer()).getName() 
					+ "Via"
					+ Helper.toFirstUpper(reference.getName());
			entityMap.directReferences.put(referenceName, reference);
			
			GraphQLFieldDefinition.Builder referenceFieldDefinition = newFieldDefinition()
					.type(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(((Entity) reference.getReferencedKey().eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) reference.getReferencedKey().eContainer()).getName()) + "ResultType"))
					.argument(newArgument()
							.name("joinType")
							.description("Join type (INNER doesn't returns entities with null references, while OUTER does).")
							.type(GraphQLTypeReference.typeRef("JoinType")))
					.argument(newArgument()
							.name("where")
							.description("Search criteria.")
							.type(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(((Entity) reference.getReferencedKey().eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) reference.getReferencedKey().eContainer()).getName()) + "EntityWhereType")))
					.name(referenceName)
					.description("Referenced entity of type " + (multiSchema ? Helper.toFirstUpper(((Entity) reference.getReferencedKey().eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) reference.getReferencedKey().eContainer()).getName()))
					.dataFetcher(new DataFetcher<Object>() {
						@Override
						public Object get(DataFetchingEnvironment environment) {
							Field field = environment.getFields().get(0);
							String alias = field.getAlias();
							if (alias == null) {
								return ((JsonObject) environment.getSource()).getValue(field.getName());
							}
							else {
								return ((JsonObject) environment.getSource()).getValue(alias);
							}
						}
					});
			graphqlObjectType.field(referenceFieldDefinition);
		}
		
		// Get inverse references - Inverse references may return n items (list)
		for (int j = 0; j < database.getEntities().size(); j++) {
			Entity referencingEntity = database.getEntities().get(j);
			for (int k = 0; k < referencingEntity.getReferences().size(); k++) {
				EntityReference reference = referencingEntity.getReferences().get(k);
				if ((Entity) reference.getReferencedKey().eContainer() == entity) {
					
					String referenceName = referencingEntity.getName() + "ListVia" + Helper.toFirstUpper(reference.getName());
					entityMap.inverseReferences.put(referenceName, reference);
					
					GraphQLFieldDefinition.Builder referencingFieldDefinition = newFieldDefinition()
							.type(GraphQLList.list(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(referencingEntity.getSchema()) + "_" : "") + Helper.toFirstUpper(referencingEntity.getName()) + "ResultType")))
							.argument(newArgument()
									.name("joinType")
									.description("Join type (INNER doesn't returns entities with null references, while OUTER does).")
									.type(GraphQLTypeReference.typeRef("JoinType")))
							.argument(newArgument()
									.name("where")
									.description("Search criteria.")
									.type(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(referencingEntity.getSchema()) + "_" : "") + Helper.toFirstUpper(referencingEntity.getName()) + "EntityWhereType")))
							.argument(newArgument()
									.name("orderBy")
									.description("Sorting criteria.")
									.type(GraphQLList.list(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(referencingEntity.getSchema()) + "_" : "") + Helper.toFirstUpper(referencingEntity.getName()) + "OrderByType"))))
							.argument(newArgument()
									.name("limit")
									.description("Maximum number of items to return.")
									.type(GraphQLInt))
							.argument(newArgument()
									.name("offset")
									.description("Number of items to skip.")
									.type(GraphQLInt))
							.name(referenceName)
							.description("Referencing entities of type " + Helper.toFirstUpper(referencingEntity.getName()) + ".")
							.dataFetcher(new DataFetcher<Object>() {
								@Override
								public Object get(DataFetchingEnvironment environment) {
									Field field = environment.getFields().get(0);
									String alias = field.getAlias();
									if (alias == null) {
										return ((JsonObject) environment.getSource()).getValue(field.getName());
									}
									else {
										return ((JsonObject) environment.getSource()).getValue(alias);
									}
								}
							});
					
					graphqlObjectType.field(referencingFieldDefinition);
				}
			}
		}
		return graphqlObjectType.build();
	}
	
	public static final GraphQLInputObjectType getEntityAttributeWhereTypeGraphqlObjectType(Database database, EntityAttribute attribute, boolean multiSchema) {
		GraphQLInputObjectType.Builder graphqlObjectType = null;
		
		if (!attribute.isArray()) {
			switch (attribute.getType().getValue()) {
				case EntityAttributeType.TEXT_VALUE:
				case EntityAttributeType.VARCHAR_VALUE:
					graphqlObjectType = newInputObject()
							.name((multiSchema ? Helper.toFirstUpper(((Entity) attribute.eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) attribute.eContainer()).getName()) + Helper.toFirstUpper(attribute.getName()) + "FieldWhereType")
							.description("Condition that the attribute must meet.");
					
					if (attribute.getEnumType() == null
							|| attribute.getEnumType().equals("tsvector")) {
						graphqlObjectType.field(newInputObjectField()
								.name("SEARCH")
								.description("Full text query.")
								.type(GraphQLTypeReference.typeRef("FullTextQueryType")).build());
					}
					
					graphqlObjectType.field(newInputObjectField()
									.name("EQ")
									.description("It is equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("NE")
									.description("It is not equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("LIKE")
									.description("It is equals to (using LIKE expressions).")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("ILIKE")
									.description("It is equals to (using ILIKE expressions [case-insensitive]).")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("GT")
									.description("It is greater than.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("GE")
									.description("It is greater than or equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("LT")
									.description("It is less than.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("LE")
									.description("It is less than or equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("IN")
									.description("It is in the list of values.")
									.type(GraphQLList.list(GraphQLString)))
							.field(newInputObjectField()
									.name("IS_NULL")
									.description("It is null (true) or not (false).")
									.type(GraphQLBoolean));
					break;
				
				case EntityAttributeType.DATE_VALUE:
				case EntityAttributeType.TIMESTAMP_VALUE:
				case EntityAttributeType.CHAR_VALUE:
				case EntityAttributeType.TIME_VALUE:
				case EntityAttributeType.INTERVAL_VALUE:
				case EntityAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
				case EntityAttributeType.TIME_WITH_TIME_ZONE_VALUE:
					graphqlObjectType = newInputObject()
							.name((multiSchema ? Helper.toFirstUpper(((Entity) attribute.eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) attribute.eContainer()).getName()) + Helper.toFirstUpper(attribute.getName()) + "FieldWhereType")
							.description("Condition that the attribute must meet.")
							.field(newInputObjectField()
									.name("EQ")
									.description("It is equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("NE")
									.description("It is not equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("GT")
									.description("It is greater than.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("GE")
									.description("It is greater than or equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("LT")
									.description("It is less than.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("LE")
									.description("It is less than or equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("IN")
									.description("It is in the list of values.")
									.type(GraphQLList.list(GraphQLString)))
							.field(newInputObjectField()
									.name("IS_NULL")
									.description("It is null (true) or not (false).")
									.type(GraphQLBoolean));
					break;
					
				case EntityAttributeType.MONEY_VALUE:
					graphqlObjectType = newInputObject()
							.name((multiSchema ? Helper.toFirstUpper(((Entity) attribute.eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) attribute.eContainer()).getName()) + Helper.toFirstUpper(attribute.getName()) + "FieldWhereType")
							.description("Condition that the attribute must meet.")
							.field(newInputObjectField()
									.name("EQ")
									.description("It is equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("NE")
									.description("It is not equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("GT")
									.description("It is greater than.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("GE")
									.description("It is greater than or equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("LT")
									.description("It is less than.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("LE")
									.description("It is less than or equals to.")
									.type(GraphQLString))
							.field(newInputObjectField()
									.name("IN")
									.description("It is in the list of values.")
									.type(GraphQLList.list(GraphQLString)))
							.field(newInputObjectField()
									.name("IS_NULL")
									.description("It is null (true) or not (false).")
									.type(GraphQLBoolean));
					break;
					
				case EntityAttributeType.BOOLEAN_VALUE:
					graphqlObjectType = newInputObject()
							.name((multiSchema ? Helper.toFirstUpper(((Entity) attribute.eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) attribute.eContainer()).getName()) + Helper.toFirstUpper(attribute.getName()) + "FieldWhereType")
							.description("Condition that the attribute must meet.")
							.field(newInputObjectField()
									.name("EQ")
									.description("It is equals to.")
									.type(GraphQLBoolean))
							.field(newInputObjectField()
									.name("NE")
									.description("It is not equals to.")
									.type(GraphQLBoolean))
							.field(newInputObjectField()
									.name("IS_NULL")
									.description("It is null (true) or not (false).")
									.type(GraphQLBoolean));
					break;
					
				case EntityAttributeType.INTEGER_VALUE:
				case EntityAttributeType.SERIAL_VALUE:
				case EntityAttributeType.SMALLINT_VALUE:
				case EntityAttributeType.BIGINT_VALUE:
				case EntityAttributeType.SMALLSERIAL_VALUE:
				case EntityAttributeType.BIGSERIAL_VALUE:
					graphqlObjectType = newInputObject()
							.name((multiSchema ? Helper.toFirstUpper(((Entity) attribute.eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) attribute.eContainer()).getName()) + Helper.toFirstUpper(attribute.getName()) + "FieldWhereType")
							.description("Condition that the attribute must meet.")
							.field(newInputObjectField()
									.name("EQ")
									.description("It is equals to.")
									.type(GraphQLInt))
							.field(newInputObjectField()
									.name("NE")
									.description("It is not equals to.")
									.type(GraphQLInt))
							.field(newInputObjectField()
									.name("GT")
									.description("It is greater than.")
									.type(GraphQLInt))
							.field(newInputObjectField()
									.name("GE")
									.description("It is greater than or equals to.")
									.type(GraphQLInt))
							.field(newInputObjectField()
									.name("LT")
									.description("It is less than.")
									.type(GraphQLInt))
							.field(newInputObjectField()
									.name("LE")
									.description("It is less than or equals to.")
									.type(GraphQLInt))
							.field(newInputObjectField()
									.name("IN")
									.description("It is in the list of values.")
									.type(GraphQLList.list(GraphQLInt)))
							.field(newInputObjectField()
									.name("IS_NULL")
									.description("It is null (true) or not (false).")
									.type(GraphQLBoolean));
					break;
					
				case EntityAttributeType.DECIMAL_VALUE:
				case EntityAttributeType.DOUBLE_PRECISION_VALUE:
				case EntityAttributeType.REAL_VALUE:
					graphqlObjectType = newInputObject()
							.name((multiSchema ? Helper.toFirstUpper(((Entity) attribute.eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) attribute.eContainer()).getName()) + Helper.toFirstUpper(attribute.getName()) + "FieldWhereType")
							.description("Condition that the attribute must meet.")
							.field(newInputObjectField()
									.name("EQ")
									.description("It is equals to.")
									.type(GraphQLFloat))
							.field(newInputObjectField()
									.name("NE")
									.description("It is not equals to.")
									.type(GraphQLFloat))
							.field(newInputObjectField()
									.name("GT")
									.description("It is greater than.")
									.type(GraphQLFloat))
							.field(newInputObjectField()
									.name("GE")
									.description("It is greater than or equals to.")
									.type(GraphQLFloat))
							.field(newInputObjectField()
									.name("LT")
									.description("It is less than.")
									.type(GraphQLFloat))
							.field(newInputObjectField()
									.name("LE")
									.description("It is less than or equals to.")
									.type(GraphQLFloat))
							.field(newInputObjectField()
									.name("IN")
									.description("It is in the list of values.")
									.type(GraphQLList.list(GraphQLFloat)))
							.field(newInputObjectField()
									.name("IS_NULL")
									.description("It is null (true) or not (false).")
									.type(GraphQLBoolean));
					break;
			}
		}
		if (graphqlObjectType != null) {
			return graphqlObjectType.build();
		}
		else {
			return null;
		}
	}
	
	public static final GraphQLInputObjectType getEntityWhereGraphqlObjectType(Database database, Entity entity, boolean multiSchema) {
		GraphQLInputObjectType.Builder graphqlObjectType = newInputObject()
				.name((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "EntityWhereType")
				.description("Conditions that the entities must meet to be returned.")
				.field(newInputObjectField()
						.name("AND")
						.description("All the conditions must be met.")
						.type(GraphQLList.list(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "EntityWhereType"))))
				.field(newInputObjectField()
						.name("OR")
						.description("At least one of the conditions must be met.")
						.type(GraphQLList.list(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "EntityWhereType"))))
				.field(newInputObjectField()
						.name("NOT")
						.description("The condition must not be met.")
						.type(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "EntityWhereType")));

				for (int i = 0; i < entity.getAttributes().size(); i++) {
					EntityAttribute attribute = entity.getAttributes().get(i);
					GraphQLInputObjectType objectType = GraphQLQuerySchemaHelper.getEntityAttributeWhereTypeGraphqlObjectType(database, attribute, multiSchema);
					if (objectType != null) {
						graphqlObjectType.field(newInputObjectField()
								.name(attribute.getName())
								.description(attribute.getDocumentation())
								.type(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(((Entity) attribute.eContainer()).getSchema()) + "_" : "") + Helper.toFirstUpper(((Entity) attribute.eContainer()).getName()) + Helper.toFirstUpper(attribute.getName()) + "FieldWhereType")));
					}
				}
		
		return graphqlObjectType.build();
	}
	
	public static final GraphQLInputObjectType getEntityOrderByGraphqlObjectType(Database database, Entity entity, boolean multiSchema) {
		GraphQLInputObjectType.Builder graphqlObjectType = newInputObject()
				.name((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "OrderByType")
				.description("Sorting criteria.")
				.field(newInputObjectField()
						.name("attribute")
						.description("The attribute to be ordered by.")
						.type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "OrderByAttributesType"))))
				.field(newInputObjectField()
						.name("direction")
						.description("It indicates if the results must be sorted in an ascending or descending way.")
						.type(GraphQLTypeReference.typeRef("OrderByDirectionType")))
				.field(newInputObjectField()
						.name("nullsGo")
						.description("It indicates if null values must be returned in the first or the last position.")
						.type(GraphQLTypeReference.typeRef("OrderByNullsGoType")));
		
		return graphqlObjectType.build();
	}
	
	public static final GraphQLEnumType getEntityOrderByAttributesGraphqlEnumType(Database database, Entity entity, boolean multiSchema) {
		GraphQLEnumType.Builder enumType = newEnum()
				.name((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "OrderByAttributesType")
				.description("The attribute to be ordered by.");
		
		// Get attributes
		boolean hasValues = false;
		for (int i = 0; i < entity.getAttributes().size(); i++) {
			EntityAttribute attribute = entity.getAttributes().get(i);
			
			if (!attribute.isArray()) {
				switch (attribute.getType().getValue()) {
					case EntityAttributeType.TEXT_VALUE:
					case EntityAttributeType.DATE_VALUE:
					case EntityAttributeType.TIMESTAMP_VALUE:
					case EntityAttributeType.VARCHAR_VALUE:
					case EntityAttributeType.CHAR_VALUE:
					case EntityAttributeType.TIME_VALUE:
					case EntityAttributeType.INTERVAL_VALUE:
					case EntityAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
					case EntityAttributeType.TIME_WITH_TIME_ZONE_VALUE:
					case EntityAttributeType.INTEGER_VALUE:
					case EntityAttributeType.SERIAL_VALUE:
					case EntityAttributeType.SMALLINT_VALUE:
					case EntityAttributeType.BIGINT_VALUE:
					case EntityAttributeType.SMALLSERIAL_VALUE:
					case EntityAttributeType.BIGSERIAL_VALUE:
					case EntityAttributeType.DECIMAL_VALUE:
					case EntityAttributeType.MONEY_VALUE:
					case EntityAttributeType.DOUBLE_PRECISION_VALUE:
					case EntityAttributeType.REAL_VALUE:
						enumType.value(attribute.getName());
						hasValues = true;
						break;
				}
			}
		}
		return (hasValues ? enumType.build() : null);
	}
}
