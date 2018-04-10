package security.fielddefinitions;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import helpers.ConfigHelper;
import helpers.DatabaseHelper;
import helpers.Helper;
import io.vertx.core.json.JsonArray;

public class RoleDelete {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("deleteRole")
			.description("It deletes a role. The role can be both, a user (with password) or a group.")
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
						String rolename = null;
						
						Field field = fields.get(0);
						for (int i = 0; i < field.getArguments().size(); i++) {
							Argument argument = field.getArguments().get(i);
							if (argument.getName().equals("rolename")) {
								rolename = (String) Helper.resolveValue(argument.getValue(), environment);
							}
						}
						
						// PreparedStatement now allowed, so check to avoid SQL injection
						if (!rolename.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
							throw new Exception("Incorrect role name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
						}
						connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
						connection.setAutoCommit(false);
						
						PreparedStatement ps = null;
						StringBuffer sb = null;
						
						List<String> schemas = ((JsonArray) ConfigHelper.get(ConfigHelper.DB_SCHEMA_NAMES, null)).getList();
						if (schemas != null) {
							sb = new StringBuffer("REVOKE USAGE ON ALL SEQUENCES IN SCHEMA ");
							for (int i = 0; i < schemas.size(); i++) {
								sb.append((i > 0 ? ", " : "") + schemas.get(i));
							}
							sb.append(" FROM " + rolename);
							ps = connection.prepareStatement(sb.toString());
							ps.execute();
							
							sb = new StringBuffer("REVOKE ALL ON ALL TABLES IN SCHEMA ");
							for (int i = 0; i < schemas.size(); i++) {
								sb.append((i > 0 ? ", " : "") + schemas.get(i));
							}
							sb.append(" FROM " + rolename);
							ps = connection.prepareStatement(sb.toString());
							ps.execute();
							
							sb = new StringBuffer("REVOKE USAGE ON SCHEMA ");
							for (int i = 0; i < schemas.size(); i++) {
								sb.append((i > 0 ? ", " : "") + schemas.get(i));
							}
							sb.append(" FROM " + rolename);
							ps = connection.prepareStatement(sb.toString());
							ps.execute();
						}
						
						ps = connection.prepareStatement("DROP ROLE " + rolename);
						ps.execute();
					}
					catch (Exception e) {
						e.printStackTrace();
						if (connection != null) {
							try {
								connection.rollback();
							}
							catch (Exception e2) {
								e2.printStackTrace();
								throw new RuntimeException(e2.getMessage());
							}
						}
						throw new RuntimeException(e.getMessage());
					}
					finally {
						try {
							if (connection != null) {
								connection.commit();
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
