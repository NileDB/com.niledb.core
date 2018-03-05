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
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import graphql.schema.GraphQLNonNull;

import java.util.HashSet;

import graphql.schema.GraphQLType;

/**
 * @author NileDB, Inc.
 */
public class GraphQLAdditionalTypesHelper {
	
	public static final HashSet<GraphQLType> getAuxiliaryTypes() {
		HashSet<GraphQLType> additionalTypes = new HashSet<GraphQLType>();
		
		// Full text search type
		additionalTypes.add(newInputObject()
				.name("FullTextQueryType")
				.field(newInputObjectField()
						.name("query")
						.description("It indicates the query to be used. Operators like & (AND), | (OR), ! (NOT), :* (Prefix), and parenthesis can be used (i.e. \"(dog | cat) & !ortho:*\".")
						.type(GraphQLNonNull.nonNull(GraphQLString)))
				.field(newInputObjectField()
						.name("config")
						.description("It indicates the configuration (i.e. \"spanish\") to be used.")
						.type(newEnum()
								.name("FullTextConfigType")
								.value("DANISH")
								.value("DUTCH")
								.value("ENGLISH")
								.value("FINNISH")
								.value("FRENCH")
								.value("GERMAN")
								.value("HUNGARIAN")
								.value("ITALIAN")
								.value("NORWEGIAN")
								.value("PORTUGUESE")
								.value("ROMANIAN")
								.value("RUSSIAN")
								.value("SIMPLE")
								.value("SPANISH")
								.value("SWEDISH")
								.value("TURKISH")
								.build()))
				.build());
		
		// Join types
		additionalTypes.add(newEnum()
				.name("JoinType")
				.description("It indicates the type of join to be used in a query that links several entities. If INNER is used, then entities that have not related entities are not returned. If OUTER is used, all the entities are returned.")
				.value("INNER")
				.value("OUTER")
				.build());
		
		// Order by direction
		additionalTypes.add(newEnum()
				.name("OrderByDirectionType")
				.description("It indicates if the results must be sorted in an ascending or descending way.")
				.value("ASC")
				.value("DESC")
				.build());
		
		// Order by NULLs go first or last
		additionalTypes.add(newEnum()
				.name("OrderByNullsGoType")
				.description("It indicates if null values must be returned in the first or the last position.")
				.value("FIRST")
				.value("LAST")
				.build());
		
		return additionalTypes;
	}
}
