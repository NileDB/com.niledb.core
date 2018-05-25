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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGmoney;

import data.CustomType;
import data.CustomTypeAttribute;
import data.CustomTypeAttributeType;
import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityAttributeType;
import graphql.language.Argument;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.Selection;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.DataFetchingEnvironment;
import helpers.maps.EntityMap;
import helpers.maps.SchemaMap;

/**
 * @author NileDB, Inc.
 */
public class GraphQLSqlUpdateHelper {
	
	@SuppressWarnings("unchecked")
	public static SqlUpdateCommand getCommand(List<ObjectField> fields, CustomType type, String relativePath, DataFetchingEnvironment environment) throws Exception {
		SqlUpdateCommand sqlCommand = new SqlUpdateCommand();
		int attributeCount = 0;
		if (fields.size() == 0) {
			throw new RuntimeException("At least one field must be set.");
		}
		for (int i = 0; i < fields.size(); i++) {
			ObjectField objectField = fields.get(i);
			String fieldName = objectField.getName();
			Object fieldValue = (Object) Helper.resolveValue(objectField.getValue(), environment);
			
			if (fieldValue == null
					|| fieldValue instanceof Integer
					|| fieldValue instanceof Double
					|| fieldValue instanceof Boolean) {
				sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
				sqlCommand.values.add(fieldValue);
				sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
				attributeCount++;
			}
			else if (fieldValue instanceof String) {
				CustomTypeAttribute attribute = SchemaMap.customTypes.get(type.getSchema() + "." + type.getName()).attributes.get(fieldName);
				CustomTypeAttributeType attributeType = attribute.getType();
				switch (attributeType.getValue()) {
					case CustomTypeAttributeType.BYTEA_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(((String) fieldValue).getBytes());
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.DATE_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"yyyy-MM-dd"}).getTime()));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.TIME_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"HH:mm", "HH:mm:ss", "HH:mm:ss.SSS"}).getTime()));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.TIME_WITH_TIME_ZONE_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"HH:mm", "HH:mm:ss", "HH:mm:ss.SSS", "HH:mmX", "HH:mm:ssX", "HH:mm:ss.SSSX"}).getTime()));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.TIMESTAMP_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS"}).getTime()));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mmX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm:ss.SSSX"}).getTime()));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.INTERVAL_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new PGInterval(((String) fieldValue)));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.MONEY_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new PGmoney(((String) fieldValue)));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					case CustomTypeAttributeType.POINT_VALUE:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(new PGpoint(((String) fieldValue)));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						break;
					default:
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"";
						sqlCommand.values.add(((String) fieldValue));
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?" + (attribute.getEnumType() != null ? "::\"" + attribute.getEnumType().getSchema() + "\".\"" + attribute.getEnumType().getName() + "\"" : "");
				}
				attributeCount++;
			}
			else if (fieldValue instanceof List) {
				if (((List) fieldValue).size() > 0) {
					if (((List) fieldValue).get(0) instanceof Value) {
						CustomTypeAttribute attribute = SchemaMap.customTypes.get(type.getSchema() + "." + type.getName()).attributes.get(fieldName);
						for (int k = 0; k < ((List) fieldValue).size(); k++) {
							Object value = ((List) fieldValue).get(k);
							if (value instanceof EnumValue) {
								sqlCommand.values.add(((EnumValue) value).getName());
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"[" + (k + 1) + "]";
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								attributeCount++;
							}
							else if (value instanceof ObjectValue) {
								SqlUpdateCommand customTypeSqlCommand = getCommand(((ObjectValue) value).getObjectFields(), attribute.getCustomType(), "\"" + fieldName + "\"[" + (k + 1) + "]", environment);
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.attributes;
								sqlCommand.values.addAll(customTypeSqlCommand.values);
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.valuePlaceholders;
								attributeCount++;
							}
							else {  // TODO StringValue or others (implement these cases when necessary)
								sqlCommand.values.add(((StringValue) value).getValue());
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + relativePath + ".\"" + fieldName + "\"[" + (k + 1) + "]";
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								attributeCount++;
							}
						}
					}
					else { // instanceof ObjectField
						SqlUpdateCommand customTypeSqlCommand = getCommand((List<ObjectField>) fieldValue, SchemaMap.customTypes.get(type.getSchema() + "." + type.getName()).attributes.get(fieldName).getCustomType(), relativePath + ".\"" + fieldName + "\"", environment);
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.attributes;
						sqlCommand.values.addAll(customTypeSqlCommand.values);
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.valuePlaceholders;
						attributeCount++;
					}
				}
			}
		}
		return sqlCommand;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static SqlUpdateCommand getCommand(Field field, DataFetchingEnvironment environment, Entity entity, Database database) throws Exception {
		
		SqlUpdateCommand sqlCommand = new SqlUpdateCommand();
		
		EntityMap entityMap = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName());
		
		List<Argument> arguments = field.getArguments();
		for (int i = 0; i < arguments.size(); i++) {
			Argument argument = arguments.get(i);
			if (argument.getName().equals("entity")) {
				List<ObjectField> fields = (List<ObjectField>) Helper.resolveValue(argument.getValue(), environment);
				if (fields.size() == 0) {
					throw new RuntimeException("At least one field must be set.");
				}
				int attributeCount = 0;
				for (int j = 0; j < fields.size(); j++) {
					ObjectField objectField = fields.get(j);
					String fieldName = objectField.getName();
					Object fieldValue = Helper.resolveValue(objectField.getValue(), environment);
					
					if (fieldValue == null
							|| fieldValue instanceof Integer
							|| fieldValue instanceof Double
							|| fieldValue instanceof Boolean) {
						sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
						sqlCommand.values.add(fieldValue);
						sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
						attributeCount++;
					}
					else if (fieldValue instanceof String) {
						EntityAttribute attribute = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName()).attributes.get(fieldName);
						EntityAttributeType attributeType = attribute.getType();
						switch (attributeType.getValue()) {
							case EntityAttributeType.BYTEA_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(((String) fieldValue).getBytes());
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.DATE_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"yyyy-MM-dd"}).getTime()));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.TIME_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"HH:mm", "HH:mm:ss", "HH:mm:ss.SSS"}).getTime()));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.TIME_WITH_TIME_ZONE_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"HH:mm", "HH:mm:ss", "HH:mm:ss.SSS", "HH:mmX", "HH:mm:ssX", "HH:mm:ss.SSSX"}).getTime()));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.TIMESTAMP_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS"}).getTime()));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.TIMESTAMP_WITH_TIME_ZONE_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new Timestamp(DateUtils.parseDate(((String) fieldValue), new String[] {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mmX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm:ss.SSSX"}).getTime()));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.INTERVAL_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new PGInterval(((String) fieldValue)));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.MONEY_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new PGmoney(((String) fieldValue)));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							case EntityAttributeType.POINT_VALUE:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(new PGpoint(((String) fieldValue)));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
								break;
							default:
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"";
								sqlCommand.values.add(((String) fieldValue));
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?" + (attribute.getEnumType() != null ? "::\"" + attribute.getEnumType().getSchema() + "\".\"" + attribute.getEnumType().getName() + "\"" : "");
						}
						attributeCount++;
					}
					else if (fieldValue instanceof List) {
						if (((List) fieldValue).size() > 0) {
							if (((List) fieldValue).get(0) instanceof Value) {
								EntityAttribute attribute = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName()).attributes.get(fieldName);
								for (int k = 0; k < ((List) fieldValue).size(); k++) {
									Object value = ((List) fieldValue).get(k);
									if (value instanceof EnumValue) {
										sqlCommand.values.add(((EnumValue) value).getName());
										sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"[" + (k + 1) + "]";
										sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
										attributeCount++;
									}
									else if (value instanceof ObjectValue) {
										SqlUpdateCommand customTypeSqlCommand = getCommand(((ObjectValue) value).getObjectFields(), attribute.getCustomType(), "\"" + fieldName + "\"[" + (k + 1) + "]", environment);
										sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.attributes;
										sqlCommand.values.addAll(customTypeSqlCommand.values);
										sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.valuePlaceholders;
										attributeCount++;
									}
									else {  // TODO StringValue or others (implement these cases when necessary)
										sqlCommand.values.add(((StringValue) value).getValue());
										sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + "\"" + fieldName + "\"[" + (k + 1) + "]";
										sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + "?";
										attributeCount++;
									}
								}
							}
							else { // instanceof ObjectField
								EntityAttribute attribute = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName()).attributes.get(fieldName);
								SqlUpdateCommand customTypeSqlCommand = getCommand((List<ObjectField>) fieldValue, attribute.getCustomType(), "\"" + fieldName + "\"", environment);
								sqlCommand.attributes += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.attributes;
								sqlCommand.values.addAll(customTypeSqlCommand.values);
								sqlCommand.valuePlaceholders += (attributeCount > 0 ? ", " : "") + customTypeSqlCommand.valuePlaceholders;
								attributeCount++;
							}
						}
					}
				}
			}
			else if (argument.getName().equals("where")) {
				SqlWhere sqlWhere = GraphQLSqlWhereHelper.getEntityWhereCommand((List<ObjectField>) Helper.resolveValue(argument.getValue(), environment), entity, database, 1, environment);
				sqlCommand.where = sqlWhere.where.toString();
				sqlCommand.whereParameters = sqlWhere.whereParameters;
			}
		}
		
		sqlCommand.update = "UPDATE \"" + entity.getSchema() + "\".\"" + entity.getName() + "\" AS \"" + entity.getName() + "_1\" ";
		
		int attributeCount = 0;
		List<Selection> selections = field.getSelectionSet().getSelections();
		for (int i = 0; i < selections.size(); i++) {
			Selection selection = selections.get(i);
			if (selection instanceof Field) {
				Field childField = (Field) selection;
				
				List<Argument> childArguments = childField.getArguments();
				Map<String, Object> childArgumentsMap = new HashMap<String, Object>();
				for (int j = 0; j < childArguments.size(); j++) {
					childArgumentsMap.put(childArguments.get(j).getName(), childArguments.get(j).getValue());
				}
				
				// Attributes
				EntityAttribute attribute = entityMap.attributes.get(childField.getName());
				if (attribute != null) {
					sqlCommand.returning += 
							(attributeCount > 0 ? ", " : "")
							+ "\"" + childField.getName() + "\"";
					attributeCount++;
				}
			}
		}
		return sqlCommand;
	}
}
