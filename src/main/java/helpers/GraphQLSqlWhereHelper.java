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

import java.util.List;

import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityAttributeType;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.DataFetchingEnvironment;
import helpers.maps.SchemaMap;

/**
 * @author NileDB, Inc.
 */
public class GraphQLSqlWhereHelper {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static SqlWhere getAttributeWhereCommand(ObjectField field, EntityAttribute attribute, Database database, int level, DataFetchingEnvironment environment) {
		SqlWhere where = new SqlWhere();
		Entity entity = (Entity) attribute.eContainer();
		List<ObjectField> conditions = (List<ObjectField>) Helper.resolveValue(field.getValue(), environment);
		
		for (int i = 0; i < conditions.size(); i++) {
			if (i != 0) {
				where.where.append(" AND ");
			}
			
			ObjectField condition = conditions.get(i);
			String conditionName = condition.getName();
			Object conditionValue = Helper.resolveValue(condition.getValue(), environment);
			
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
					switch (conditionName) {
						case "EQ": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar = ?");
							where.whereParameters.add(conditionValue);
							break;
						case "NE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar != ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LIKE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar LIKE ?");
							where.whereParameters.add(conditionValue);
							break;
						case "ILIKE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar ILIKE ?");
							where.whereParameters.add(conditionValue);
							break;
						case "SEARCH": 
							List<ObjectField> fields = (List<ObjectField>) conditionValue;
							
							String query = null;
							String config = "ENGLISH";
							for (int j = 0; j < fields.size(); j++) {
								ObjectField objectField = fields.get(j);
								Object objectFieldValue = Helper.resolveValue(objectField.getValue(), environment);
								if (objectField.getName().equals("query")) {
									query = (String) objectFieldValue;
								}
								else if (objectField.getName().equals("config")) {
									config = (String) objectFieldValue;
								}
							}
							if (attribute.getEnumType() == null) {
								where.where.append("to_tsvector('" + config + "', \"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\") @@ to_tsquery('" + config + "', ?)");
							}
							else if (attribute.getEnumType().equals("tsvector")) {
								where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" @@ to_tsquery('" + config + "', ?)");
							}
							where.whereParameters.add(query);
							break;
						case "GT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar > ?");
							where.whereParameters.add(conditionValue);
							break;
						case "GE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar >= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar < ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar <= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "IN": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar IN (");
							List<Value> values = (List<Value>) conditionValue;
							for (int j = 0; j < values.size(); j++) {
								where.where.append(j != 0 ? ", " : "");
								where.where.append("?");
								where.whereParameters.add(((StringValue) values.get(j)).getValue());
							}
							where.where.append(")");
							break;
						case "IS_NULL": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" IS" + ((Boolean) conditionValue ? "" : " NOT") + " NULL");
							break;
					}
					break;
					
				case EntityAttributeType.MONEY_VALUE:
					switch (conditionName) {
						case "EQ": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar = ?");
							where.whereParameters.add(conditionValue);
							break;
						case "NE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar != ?");
							where.whereParameters.add(conditionValue);
							break;
						case "GT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar > ?");
							where.whereParameters.add(conditionValue);
							break;
						case "GE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar >= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar < ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar <= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "IN": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\"::varchar IN (");
							List<Value> values = (List<Value>) conditionValue;
							for (int j = 0; j < values.size(); j++) {
								where.where.append(j != 0 ? ", " : "");
								where.where.append("?");
								where.whereParameters.add(((StringValue) values.get(j)).getValue());
							}
							where.where.append(")");
							break;
						case "IS_NULL": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" IS" + ((Boolean) conditionValue ? "" : " NOT") + " NULL");
							break;
					}
					break;
					
				case EntityAttributeType.BOOLEAN_VALUE:
					switch (conditionName) {
						case "EQ": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" = ?");
							where.whereParameters.add((Boolean) conditionValue);
							break;
						case "NE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" != ?");
							where.whereParameters.add((Boolean) conditionValue);
							break;
						case "IS_NULL": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" IS" + ((Boolean) conditionValue ? "" : " NOT") + " NULL");
							break;
					}
					break;
				
				case EntityAttributeType.INTEGER_VALUE:
				case EntityAttributeType.SERIAL_VALUE:
				case EntityAttributeType.SMALLINT_VALUE:
				case EntityAttributeType.BIGINT_VALUE:
				case EntityAttributeType.SMALLSERIAL_VALUE:
				case EntityAttributeType.BIGSERIAL_VALUE:
					switch (conditionName) {
						case "EQ": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" = ?");
							where.whereParameters.add(conditionValue);
							break;
						case "NE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" != ?");
							where.whereParameters.add(conditionValue);
							break;
						case "GT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" > ?");
							where.whereParameters.add(conditionValue);
							break;
						case "GE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" >= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" < ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" <= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "IN": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" IN (");
							List<Value> values = (List<Value>) conditionValue;
							for (int j = 0; j < values.size(); j++) {
								where.where.append(j != 0 ? ", " : "");
								where.where.append("?");
								where.whereParameters.add(((IntValue) values.get(j)).getValue());
							}
							where.where.append(")");
							break;
						case "IS_NULL": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" IS" + ((Boolean) conditionValue ? "" : " NOT") + " NULL");
							break;
					}
					break;
					
				case EntityAttributeType.DECIMAL_VALUE:
				case EntityAttributeType.DOUBLE_PRECISION_VALUE:
				case EntityAttributeType.REAL_VALUE:
					switch (conditionName) {
						case "EQ": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" = ?");
							where.whereParameters.add(conditionValue);
							break;
						case "NE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" != ?");
							where.whereParameters.add(conditionValue);
							break;
						case "GT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" > ?");
							where.whereParameters.add(conditionValue);
							break;
						case "GE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" >= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LT": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" < ?");
							where.whereParameters.add(conditionValue);
							break;
						case "LE": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" <= ?");
							where.whereParameters.add(conditionValue);
							break;
						case "IN": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" IN (");
							where.whereParameters.add(conditionValue);
							List<Value> values = (List<Value>) conditionValue;
							for (int j = 0; j < values.size(); j++) {
								where.where.append(j != 0 ? ", " : "");
								where.where.append("?");
								where.whereParameters.add(((FloatValue) values.get(j)).getValue());
							}
							where.where.append(")");
							break;
						case "IS_NULL": 
							where.where.append("\"" + entity.getName() + "_" + level + "\".\"" + attribute.getName() + "\" IS" + ((Boolean) conditionValue ? "" : " NOT") + " NULL");
							break;
					}
					break;
			}
		}
		return where;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static SqlWhere getEntityWhereCommand(List<ObjectField> fields, Entity entity, Database database, int level, DataFetchingEnvironment environment) {
		SqlWhere where = new SqlWhere();
		
		for (int i = 0; i < fields.size(); i++) {
			if (i != 0) {
				where.where.append(" AND ");
			}
			
			ObjectField field = fields.get(i);
			System.out.println(field.getName());
			
			String fieldName = field.getName();
			
			switch (fieldName) {
				case "AND":
					List<Value> values = (List<Value>) Helper.resolveValue(field.getValue(), environment);
					for (int j = 0; j < values.size(); j++) {
						Value value = values.get(j);
						if (j != 0) {
							where.where.append(" AND ");
						}
						SqlWhere subWhere = getEntityWhereCommand((List<ObjectField>) Helper.resolveValue(value, environment), entity, database, level, environment);
						where.where.append(subWhere.where);
						where.whereParameters.addAll(subWhere.whereParameters); 
					}
					break;
					
				case "OR":
					where.where.append("(");
					values = (List<Value>) Helper.resolveValue(field.getValue(), environment);
					for (int j = 0; j < values.size(); j++) {
						Value value = values.get(j);
						if (j != 0) {
							where.where.append(" OR ");
						}
						SqlWhere subWhere = getEntityWhereCommand((List<ObjectField>) Helper.resolveValue(value, environment), entity, database, level, environment);
						where.where.append(subWhere.where);
						where.whereParameters.addAll(subWhere.whereParameters); 
					}
					where.where.append(")");
					break;
					
				case "NOT":
					where.where.append("NOT (");
					SqlWhere subWhere = getEntityWhereCommand((List<ObjectField>) Helper.resolveValue(field.getValue(), environment), entity, database, level, environment);
					where.where.append(subWhere.where);
					where.whereParameters.addAll(subWhere.whereParameters); 
					where.where.append(")");
					break;
					
				default:
					subWhere = getAttributeWhereCommand(
							field, 
							SchemaMap.entities.get(entity.getName()).attributes.get(field.getName()), 
							database,
							level,
							environment);
					where.where.append(subWhere.where);
					where.whereParameters.addAll(subWhere.whereParameters); 
					break;
			}
		}
		
		return where;
	}
}
