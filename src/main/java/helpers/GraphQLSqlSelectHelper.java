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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityAttributeType;
import data.EntityKey;
import data.EntityReference;
import graphql.language.Argument;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.Selection;
import graphql.language.Value;
import graphql.schema.DataFetchingEnvironment;
import helpers.maps.EntityMap;
import helpers.maps.SchemaMap;

/**
 * @author NileDB, Inc.
 */
public class GraphQLSqlSelectHelper {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static SqlSelectCommand getCommand(Field field, DataFetchingEnvironment environment, Entity entity,
			Database database, int level, boolean limitOffsetEnabled) {
		
		SqlSelectCommand sqlCommand = new SqlSelectCommand();
		
		EntityMap entityMap = SchemaMap.entities.get(entity.getSchema() + "." + entity.getName());
		
		List<Argument> arguments = field.getArguments();
		Map<String, Object> argumentsMap = new HashMap<String, Object>();
		for (int i = 0; i < arguments.size(); i++) {
			Argument argument = arguments.get(i);
			argumentsMap.put(argument.getName(), Helper.resolveValue(argument.getValue(), environment));
		}
		
		sqlCommand.select = "SELECT ";
		sqlCommand.from = " FROM \"" + entity.getSchema() + "\".\"" + entity.getName() + "\" AS \"" + entity.getName() + "_" + level + "\"";
		
		// Pagination
		if (limitOffsetEnabled) {
			int limit = (int) ConfigHelper.get(ConfigHelper.SERVICE_QUERY_MAX_RESULTS, 1000);
			if (argumentsMap.get("limit") != null) {
				limit = Math.min(limit, (int) argumentsMap.get("limit"));
			}
			sqlCommand.pagination += " LIMIT ?";
			sqlCommand.paginationParameters.add(limit);
	
			if (argumentsMap.get("offset") != null) {
				sqlCommand.pagination += " OFFSET ?";
				sqlCommand.paginationParameters.add(argumentsMap.get("offset"));
			}
		}
		
		int attributeCount = 0;
		List<Selection> selections = field.getSelectionSet().getSelections();
		for (int i = 0; i < selections.size(); i++) {
			Selection selection = selections.get(i);
			if (selection instanceof Field) {
				Field childField = (Field) selection;
				String alias = (childField.getAlias() == null ? childField.getName() : childField.getAlias());
				
				List<Argument> childArguments = childField.getArguments();
				Map<String, Object> childArgumentsMap = new HashMap<String, Object>();
				for (int j = 0; j < childArguments.size(); j++) {
					childArgumentsMap.put(childArguments.get(j).getName(), childArguments.get(j).getValue());
				}

				// Attributes
				EntityAttribute attribute = entityMap.attributes.get(childField.getName());
				if (attribute != null) {
					if (attribute.getType() == EntityAttributeType.GEOGRAPHY) {
						sqlCommand.select += (attributeCount > 0 ? ", " : "") + "ST_AsText(\"" + entity.getName() + "_" + level
								+ "\".\"" + childField.getName() + "\") AS \"" + childField.getName() + "\"";
					}
					else {
						sqlCommand.select += (attributeCount > 0 ? ", " : "") + "\"" + entity.getName() + "_" + level
								+ "\".\"" + childField.getName() + "\"";
					}
					sqlCommand.addedAttributes.put(childField.getName(), true);
					attributeCount++;
				} 
				else {
					// Direct references
					EntityReference reference = entityMap.directReferences.get(childField.getName());
					if (reference != null) {
						EntityKey referencedKey = reference.getReferencedKey();
						List<EntityAttribute> referencedAttributes = referencedKey.getAttributes();
						Entity referencedEntity = (Entity) referencedKey.eContainer();
						
						sqlCommand.select += (attributeCount > 0 ? ", " : "") + "\"" + alias + "_" + (level + 1) + "\" AS \"" + alias + "\"";
						sqlCommand.addedDirectReferences.put(alias, true);
						attributeCount++;
						
						SqlSelectCommand sqlSubcommand = getCommand(childField, environment, referencedEntity, database,
								level + 1, false);
						
						List<EntityAttribute> referenceAttributes = reference.getAttributes();
						
						String sqlOn = "";
						for (int j = 0; j < reference.getAttributes().size(); j++) {
							sqlOn += (j > 0 ? " AND " : "") + "\"" + entity.getName() + "_" + level + "\".\""
									+ referenceAttributes.get(j).getName() + "\" = \"" + alias + "_" + (level + 1) + "\".\""
									+ referencedAttributes.get(j).getName() + "\"";
							
							if (sqlSubcommand.addedAttributes.get(referencedAttributes.get(j).getName()) == null) {
								sqlSubcommand.select += ", \"" + referencedEntity.getName() + "_" + (level + 1)
										+ "\".\"" + referencedAttributes.get(j).getName() + "\"";
								sqlSubcommand.addedAttributes.put(referencedAttributes.get(j).getName(), true);
							}
						}
						
						EnumValue joinType = (EnumValue) childArgumentsMap.get("joinType");
						sqlCommand.from += (joinType != null && joinType.getName().equals("INNER") ? " INNER" : " LEFT")
								+ " JOIN (" + sqlSubcommand.toString() + ") AS \"" + alias + "_" + (level + 1) + "\" ON "
								+ sqlOn;
						sqlCommand.fromParameters.addAll(sqlSubcommand.getParameters());
					} 
					else {
						// Inverse references
						reference = entityMap.inverseReferences.get(childField.getName());
						if (reference != null) {
							EntityKey referencedKey = reference.getReferencedKey();
							List<EntityAttribute> referencedAttributes = referencedKey.getAttributes();
							Entity referencingEntity = ((Entity) reference.eContainer());
				
							sqlCommand.inverseReferences = true;
							if (referencedKey.isPrimaryKey()) {
								sqlCommand.primaryKeyIncludedInGroupBy = true;
							}
							
							sqlCommand.select += (attributeCount > 0 ? ", " : "") + "CASE WHEN count(\""
									+ alias + "_" + (level + 1) + "\") > 0 THEN array_agg(distinct \""
									+ alias + "_" + (level + 1) + "\") ELSE NULL END AS \""
									+ alias + "\"";
							attributeCount++;
							
							SqlSelectCommand sqlSubcommand = getCommand(childField, environment, referencingEntity,
									database, level + 1, true);
							
							List<EntityAttribute> referenceAttributes = reference.getAttributes();
							
							String condition = "";
							for (int j = 0; j < reference.getAttributes().size(); j++) {
								condition += (j > 0 ? " AND " : "") + "\"" + referencingEntity.getName() + "_"
										+ (level + 1) + "\".\"" + referenceAttributes.get(j).getName() + "\" = \""
										+ entity.getName() + "_" + level + "\".\""
										+ referencedAttributes.get(j).getName() + "\"";

								if (sqlSubcommand.addedAttributes.get(referenceAttributes.get(j).getName()) == null) {
									sqlSubcommand.select += ", \"" + referencingEntity.getName() + "_" + (level + 1)
											+ "\".\"" + referenceAttributes.get(j).getName() + "\"";
									sqlSubcommand.addedAttributes.put(referenceAttributes.get(j).getName(), true);
								}
								if (sqlCommand.addedGroupByAttributes
										.get(referencedAttributes.get(j).getName()) == null) {
									sqlCommand.groupBy += (sqlCommand.groupBy.equals("") ? " GROUP BY " : ", ") + "\""
											+ entity.getName() + "_" + level + "\".\""
											+ referencedAttributes.get(j).getName() + "\"";
									sqlCommand.addedGroupByAttributes.put(referencedAttributes.get(j).getName(), true);
								}
							}
							
							sqlSubcommand.where += (sqlSubcommand.where.equals("") ? " WHERE " : " AND ") + condition;
							
							EnumValue joinType = (EnumValue) childArgumentsMap.get("joinType");
							sqlCommand.from += (joinType != null && joinType.getName().equals("INNER") ? " INNER"
									: " LEFT") + " JOIN LATERAL (" + sqlSubcommand.toString()
									+ ") AS \"" + alias + "_" + (level + 1) + "\" ON true";
							sqlCommand.fromParameters.addAll(sqlSubcommand.getParameters());
						}
					}
				}
			}
		}
		
		// Where
		List<ObjectField> whereArgument = (List<ObjectField>) argumentsMap.get("where");
		if (whereArgument != null) {
			SqlWhere sqlWhere = GraphQLSqlWhereHelper.getEntityWhereCommand(whereArgument, entity, database, level, environment);
			sqlCommand.where += (sqlCommand.where.equals("") ? " WHERE " : " AND ") + "(" + sqlWhere.where + ")";
			sqlCommand.whereParameters.addAll(sqlWhere.whereParameters);
		}
		
		// If inverse references are selected and the referenced key is not primary key, then
		// add the other attributes to the group by also.
		if (sqlCommand.inverseReferences && !sqlCommand.primaryKeyIncludedInGroupBy) {
			Set<String> addedKeys = sqlCommand.addedAttributes.keySet();
			for (String key : addedKeys) {
				if (sqlCommand.addedAttributes.get(key)) {
					if (sqlCommand.addedGroupByAttributes
							.get(key) == null) {
						sqlCommand.groupBy += (sqlCommand.groupBy.equals("") ? " GROUP BY " : ", ") + "\""
								+ entity.getName() + "_" + level + "\".\""
								+ key + "\"";
						sqlCommand.addedGroupByAttributes.put(key, true);
					}
				}
			}
		}
		
		// If inverse references are selected, then add the direct references to the group by also.
		if (sqlCommand.inverseReferences) {
			Set<String> addedKeys = sqlCommand.addedDirectReferences.keySet();
			for (String key : addedKeys) {
				if (sqlCommand.addedDirectReferences.get(key)) {
					if (sqlCommand.addedGroupByAttributes
							.get(key) == null) {
						sqlCommand.groupBy += (sqlCommand.groupBy.equals("") ? " GROUP BY " : ", ") + "\""
								+ key + "\"";
						sqlCommand.addedGroupByAttributes.put(key, true);
					}
				}
			}
		}
		
		// Order by
		List<Value> values = (List<Value>) argumentsMap.get("orderBy");
		if (values != null) {
			if (values.get(0) instanceof ObjectField) {
				String orderByAttribute = null;
				String orderByDirection = "ASC";
				String orderByNullsGo = "FIRST";
				
				for (int i = 0; i < values.size(); i++) {
					ObjectField orderByField = (ObjectField) values.get(i);
					String orderByFieldName = orderByField.getName();
					switch (orderByFieldName) {
						case "attribute":
							orderByAttribute = ((EnumValue) orderByField.getValue()).getName();
							break;
						case "direction":
							orderByDirection = ((EnumValue) orderByField.getValue()).getName();
							break;
						case "nullsGo":
							orderByNullsGo = ((EnumValue) orderByField.getValue()).getName();
							break;
					}
				}
				
				sqlCommand.orderBy += (sqlCommand.orderBy.equals("") ? " ORDER BY " : ", ") + "\""
						+ entity.getName() + "_" + level + "\".\""
						+ orderByAttribute + "\" " + orderByDirection + " NULLS " + orderByNullsGo;
			}
			else {
				for (int i = 0; i < values.size(); i++) {
					String orderByAttribute = null;
					String orderByDirection = "ASC";
					String orderByNullsGo = "FIRST";
					
					ObjectValue value = (ObjectValue) values.get(i);
					List<ObjectField> orderByFields = value.getObjectFields();
					for (int j = 0; j < orderByFields.size(); j++) {
						ObjectField orderByField = orderByFields.get(j);
						String orderByFieldName = orderByField.getName();
						switch (orderByFieldName) {
							case "attribute":
								orderByAttribute = ((EnumValue) orderByField.getValue()).getName();
								break;
							case "direction":
								orderByDirection = ((EnumValue) orderByField.getValue()).getName();
								break;
							case "nullsGo":
								orderByNullsGo = ((EnumValue) orderByField.getValue()).getName();
								break;
						}
					}
					
					sqlCommand.orderBy += (sqlCommand.orderBy.equals("") ? " ORDER BY " : ", ") + "\""
							+ entity.getName() + "_" + level + "\".\""
							+ orderByAttribute + "\" " + orderByDirection + " NULLS " + orderByNullsGo;
				}
			}
		}
		
		return sqlCommand;
	}
}
