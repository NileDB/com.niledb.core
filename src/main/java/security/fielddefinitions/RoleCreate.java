package security.fielddefinitions;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.Value;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import helpers.DatabaseHelper;
import helpers.Helper;

public class RoleCreate {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("__roleCreate")
			.description("It creates a new role. The role can be both, a user (with password) or a group.")
			.argument(newArgument()
					.name("rolename")
					.description("The role name.")
					.type(GraphQLNonNull.nonNull(GraphQLString)))
			.argument(newArgument()
					.name("password")
					.description("The password.")
					.type(GraphQLString))
			.argument(newArgument()
					.name("roles")
					.description("The list of granted roles.")
					.type(GraphQLList.list(GraphQLString)))
			.type(GraphQLString)
			.dataFetcher(new DataFetcher<String>() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public String get(DataFetchingEnvironment environment) {
					List<Field> fields = environment.getFields();
					Connection connection = null;
					try {
						String rolename = null;
						String password = null;
						List<String> roles = null;
						
						Field field = fields.get(0);
						for (int i = 0; i < field.getArguments().size(); i++) {
							Argument argument = field.getArguments().get(i);
							if (argument.getName().equals("rolename")) {
								rolename = (String) Helper.resolveValue(argument.getValue(), environment);
							}
							else if (argument.getName().equals("password")) {
								password = (String) Helper.resolveValue(argument.getValue(), environment);
							}
							else if (argument.getName().equals("roles")) {
								List<Value> values = (List<Value>) Helper.resolveValue(argument.getValue(), environment);
								if (values.size() > 0) {
									roles = new ArrayList<String>();
									for (int j = 0; j < values.size(); j++) {
										String grantedRole = (String) Helper.resolveValue(values.get(j), environment);
										if (!grantedRole.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
											throw new Exception("Incorrect role name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
										}
										roles.add(grantedRole);
									}
								}
							}
						}
						
						// PreparedStatement now allowed, so check to avoid SQL injection
						if (!rolename.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
							throw new Exception("Incorrect role name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
						}
						if (!password.matches("[^']+")) {
							throw new Exception("Incorrect password. Please, don't use single quote.");
						}
						connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
						connection.setAutoCommit(false);
						PreparedStatement ps = connection.prepareStatement("CREATE ROLE " + rolename + (password != null ? " PASSWORD '" + password + "'" : ""));
						ps.execute();
						
						if (roles != null) {
							for (int i = 0; i < roles.size(); i++) {
								ps = connection.prepareStatement("GRANT " + roles.get(i) + " TO " + rolename);
								ps.execute();
							}
						}
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
