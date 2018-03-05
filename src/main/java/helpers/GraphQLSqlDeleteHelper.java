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

import data.Database;
import data.Entity;
import data.EntityAttribute;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.ObjectField;
import graphql.language.Selection;
import graphql.schema.DataFetchingEnvironment;
import helpers.maps.EntityMap;
import helpers.maps.SchemaMap;

/**
 * @author NileDB, Inc.
 */
public class GraphQLSqlDeleteHelper {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static SqlDeleteCommand getCommand(Field field, DataFetchingEnvironment environment, Entity entity, Database database) {
		
		SqlDeleteCommand sqlCommand = new SqlDeleteCommand();
		
		EntityMap entityMap = SchemaMap.entities.get(entity.getName());
		
		List<Argument> arguments = field.getArguments();
		for (int i = 0; i < arguments.size(); i++) {
			Argument argument = arguments.get(i);
			if (argument.getName().equals("where")) {
				SqlWhere sqlWhere = GraphQLSqlWhereHelper.getEntityWhereCommand((List<ObjectField>) Helper.resolveValue(argument.getValue(), environment), entity, database, 1, environment);
				sqlCommand.where = sqlWhere.where.toString();
				sqlCommand.whereParameters = sqlWhere.whereParameters;
			}
		}
		
		sqlCommand.delete = "DELETE FROM \"" + entity.getName() + "\" AS \"" + entity.getName() + "_1\" ";
		
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
