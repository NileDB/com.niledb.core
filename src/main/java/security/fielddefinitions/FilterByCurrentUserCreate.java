package security.fielddefinitions;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import data.EntityReference;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLTypeReference;
import helpers.DatabaseHelper;
import helpers.Helper;
import helpers.maps.EntityMap;
import helpers.maps.SchemaMap;

public class FilterByCurrentUserCreate {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("__filterByCurrentUserCreate")
			.description("It filters rows by current authenticated user on the specified table.")
			.argument(newArgument()
					.name("table")
					.description("The table name.")
					.type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef("EntityEnumType"))))
			.argument(newArgument()
					.name("path")
					.description("The path to current user through foreign key constraints (i.e. \"order.user\").")
					.type(GraphQLNonNull.nonNull(GraphQLString)))
			.argument(newArgument()
					.name("rolename")
					.description("The role name.")
					.type(GraphQLNonNull.nonNull(GraphQLString)))
			.type(GraphQLString)
			.dataFetcher(new DataFetcher<String>() {
				@SuppressWarnings("unchecked")
				@Override
				public String get(DataFetchingEnvironment environment) {
					List<Field> fields = environment.getFields();
					Connection connection = null;
					try {
						String table = null;
						String path = null;
						String rolename = null;
						
						Field field = fields.get(0);
						for (int i = 0; i < field.getArguments().size(); i++) {
							Argument argument = field.getArguments().get(i);
							if (argument.getName().equals("table")) {
								table = (String) Helper.resolveValue(argument.getValue(), environment);
							}
							else if (argument.getName().equals("path")) {
								path = (String) Helper.resolveValue(argument.getValue(), environment);
							}
							else if (argument.getName().equals("rolename")) {
								rolename = (String) Helper.resolveValue(argument.getValue(), environment);
							}
						}
						
						// PreparedStatement now allowed, so check to avoid SQL injection
						if (!table.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
							throw new Exception("Incorrect table name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
						}
						if (!path.matches("[_a-zA-Z][_0-9a-zA-Z\\.]*")) {
							throw new Exception("Incorrect path. Please, use this format: [_a-zA-Z][_0-9a-zA-Z\\.]*");
						}
						if (!rolename.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
							throw new Exception("Incorrect role name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
						}
						table = SchemaMap.entityNameByUnderscoredName.get(table);
						connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));

						StringTokenizer st = new StringTokenizer(path, ".");
						List<String> pathItems = new ArrayList<String>();
						while (st.hasMoreTokens()) {
							pathItems.add(st.nextToken());
						}
						
						String prefix = "";
						String suffix = "";
						
						EntityMap currentEntity = SchemaMap.entities.get(table.replaceAll("\"", ""));
						
						for (int i = 0; i < pathItems.size() - 1; i++) {
							EntityReference reference = currentEntity.directReferences.get(pathItems.get(i));
							prefix += "(\"" + reference.getAttributes().get(0).getName() + "\" IN (SELECT \"" + reference.getReferencedKey().getAttributes().get(0).getName() + "\" FROM \"" + reference.getReferencedKey().eContainer().getName() + "\" WHERE ";
							suffix += "))";
							currentEntity = SchemaMap.entities.get(reference.getReferencedKey().eContainer().getName());
						}
						prefix += "\"" + pathItems.get(pathItems.size() - 1) + "\" = current_user::text";
						
						String expression = prefix + suffix;
						String sql = "CREATE POLICY filter_by_user_" + rolename + " ON " + table + " FOR ALL TO " + rolename + " USING (" + expression + ") WITH CHECK (" + expression + ")";
						PreparedStatement ps = connection.prepareStatement(sql);
						ps.execute();
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
					return "ok";
				}
			});
}
