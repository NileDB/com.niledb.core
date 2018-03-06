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
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

import data.CustomType;
import data.CustomTypeAttribute;
import data.CustomTypeAttributeType;
import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityAttributeType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLTypeReference;
import helpers.maps.EntityMap;
import helpers.maps.SchemaMap;

/**
 * @author NileDB, Inc.
 */
public class GraphQLMutationSchemaHelper {
	
	/**
	 * Define a GraphQL object type for a custom type
	 * 
	 * @param database
	 *            The database model
	 * @param customType
	 *            The custom type
	 * @return The GraphQL object type
	 */
	public static final GraphQLInputObjectType getCustomTypeGraphqlInputObjectType(Database database, CustomType customType, boolean multiSchema) {
		GraphQLInputObjectType.Builder graphqlObjectType = newInputObject()
				.name((multiSchema ? Helper.toFirstUpper(customType.getSchema()) + "_" : "") + Helper.toFirstUpper(customType.getName()) + "InputType")
				.description(customType.getDocumentation());
		
		for (int i = 0; i < customType.getAttributes().size(); i++) {
			CustomTypeAttribute attribute = customType.getAttributes().get(i);
			
			GraphQLInputObjectField.Builder fieldDefinition = newInputObjectField()
					.name(attribute.getName())
					.description(attribute.getDocumentation());
			
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
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType")));
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
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType"));
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
	public static final GraphQLInputObjectType getEntityGraphqlCreateInputObjectType(Database database, Entity entity, boolean multiSchema) {
		
		// Get Fields
		GraphQLInputObjectType.Builder graphqlInputObjectType = newInputObject()
				.name((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "CreateInputType")
				.description(entity.getDocumentation());
		
		EntityMap entityMap = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName());
		
		// Get attributes
		for (int i = 0; i < entity.getAttributes().size(); i++) {
			EntityAttribute attribute = entity.getAttributes().get(i);
			entityMap.attributes.put(attribute.getName(), attribute);
			
			GraphQLInputObjectField.Builder fieldDefinition = newInputObjectField()
					.name(attribute.getName());
			
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
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLString)));
						}
						else {
							fieldDefinition.type(GraphQLList.list(GraphQLString));
						}
						break;
						
					case EntityAttributeType.BOOLEAN_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLBoolean)));
						}
						else {
							fieldDefinition.type(GraphQLList.list(GraphQLBoolean));
						}
						break;
						
					case EntityAttributeType.INTEGER_VALUE:
					case EntityAttributeType.SMALLINT_VALUE:
					case EntityAttributeType.BIGINT_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLInt)));
						}
						else {
							fieldDefinition.type(GraphQLList.list(GraphQLInt));
						}
						break;
						
					case EntityAttributeType.SERIAL_VALUE:
					case EntityAttributeType.SMALLSERIAL_VALUE:
					case EntityAttributeType.BIGSERIAL_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLInt));
						break;
						
					case EntityAttributeType.DECIMAL_VALUE:
					case EntityAttributeType.DOUBLE_PRECISION_VALUE:
					case EntityAttributeType.REAL_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLFloat)));
						}
						else {
							fieldDefinition.type(GraphQLList.list(GraphQLFloat));
						}
						break;
						
					case EntityAttributeType.CUSTOM_TYPE_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLTypeReference
									.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType"))));
						}
						else {
							fieldDefinition.type(GraphQLList.list(GraphQLTypeReference
									.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType")));
						}
						break;
						
					default:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLList.list(GraphQLString)));
						}
						else {
							fieldDefinition.type(GraphQLList.list(GraphQLString));
						}
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
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLString));
						}
						else {
							fieldDefinition.type(GraphQLString);
						}
						break;
						
					case EntityAttributeType.BOOLEAN_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLBoolean));
						}
						else {
							fieldDefinition.type(GraphQLBoolean);
						}
						break;
						
					case EntityAttributeType.INTEGER_VALUE:
					case EntityAttributeType.SMALLINT_VALUE:
					case EntityAttributeType.BIGINT_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLInt));
						}
						else {
							fieldDefinition.type(GraphQLInt);
						}
						break;
						
					case EntityAttributeType.SERIAL_VALUE:
					case EntityAttributeType.SMALLSERIAL_VALUE:
					case EntityAttributeType.BIGSERIAL_VALUE:
						fieldDefinition.type(GraphQLInt);
						break;
						
					case EntityAttributeType.DECIMAL_VALUE:
					case EntityAttributeType.DOUBLE_PRECISION_VALUE:
					case EntityAttributeType.REAL_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLFloat));
						}
						else {
							fieldDefinition.type(GraphQLFloat);
						}
						break;
						
					case EntityAttributeType.CUSTOM_TYPE_VALUE:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLTypeReference
									.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType")));
						}
						else {
							fieldDefinition.type(GraphQLTypeReference
									.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType"));
						}
						break;
						
					default:
						if (attribute.isRequired() && attribute.getDefaultValue() == null) {
							fieldDefinition.type(GraphQLNonNull.nonNull(GraphQLString));
						}
						else {
							fieldDefinition.type(GraphQLString);
						}
				}
			}
			graphqlInputObjectType.field(fieldDefinition);
		}
		return graphqlInputObjectType.build();
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
	public static final GraphQLInputObjectType getEntityGraphqlUpdateInputObjectType(Database database, Entity entity, boolean multiSchema) {
		
		// Get Fields
		GraphQLInputObjectType.Builder graphqlInputObjectType = newInputObject()
				.name((multiSchema ? Helper.toFirstUpper(entity.getSchema()) + "_" : "") + Helper.toFirstUpper(entity.getName()) + "UpdateInputType")
				.description(entity.getDocumentation());
		
		EntityMap entityMap = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName());
		
		// Get attributes
		for (int i = 0; i < entity.getAttributes().size(); i++) {
			EntityAttribute attribute = entity.getAttributes().get(i);
			entityMap.attributes.put(attribute.getName(), attribute);
			
			GraphQLInputObjectField.Builder fieldDefinition = newInputObjectField()
					.name(attribute.getName());
			
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
						fieldDefinition.type(GraphQLList.list(GraphQLString));
						break;
						
					case EntityAttributeType.BOOLEAN_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLBoolean));
						break;
						
					case EntityAttributeType.INTEGER_VALUE:
					case EntityAttributeType.SMALLINT_VALUE:
					case EntityAttributeType.BIGINT_VALUE:
						fieldDefinition.type(GraphQLList.list(GraphQLInt));
						break;
						
					case EntityAttributeType.SERIAL_VALUE:
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
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType")));
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
						fieldDefinition.type(GraphQLString);
						break;
						
					case EntityAttributeType.BOOLEAN_VALUE:
						fieldDefinition.type(GraphQLBoolean);
						break;
						
					case EntityAttributeType.INTEGER_VALUE:
					case EntityAttributeType.SMALLINT_VALUE:
					case EntityAttributeType.BIGINT_VALUE:
						fieldDefinition.type(GraphQLInt);
						break;
						
					case EntityAttributeType.SERIAL_VALUE:
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
								.typeRef((multiSchema ? Helper.toFirstUpper(attribute.getCustomType().getSchema()) + "_" : "") + Helper.toFirstUpper(attribute.getCustomType().getName()) + "InputType"));
						break;
						
					default:
						fieldDefinition.type(GraphQLString);
				}
			}
			graphqlInputObjectType.field(fieldDefinition);
		}
		return graphqlInputObjectType.build();
	}
}
