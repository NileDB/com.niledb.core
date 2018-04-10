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
import helpers.DatabaseHelper;
import helpers.Helper;

public class RoleRevoke {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("revokeRole")
			.description("It revokes a role (the revoked role) from another existing role.")
			.argument(newArgument()
					.name("rolename")
					.description("The role name to which the revoked role is revoked.")
					.type(GraphQLNonNull.nonNull(GraphQLString)))
			.argument(newArgument()
					.name("revokedRole")
					.description("The revoked role.")
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
						String revokedRole = null;
						
						Field field = fields.get(0);
						for (int i = 0; i < field.getArguments().size(); i++) {
							Argument argument = field.getArguments().get(i);
							if (argument.getName().equals("rolename")) {
								rolename = (String) Helper.resolveValue(argument.getValue(), environment);
							}
							else if (argument.getName().equals("revokedRole")) {
								revokedRole = (String) Helper.resolveValue(argument.getValue(), environment);
							}
						}
						
						// PreparedStatement now allowed, so check to avoid SQL injection
						if (!rolename.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
							throw new Exception("Incorrect role name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
						}
						if (!revokedRole.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
							throw new Exception("Incorrect revoked role name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
						}
						connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
						PreparedStatement ps = connection.prepareStatement("REVOKE " + revokedRole + " FROM " + rolename);
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
