package security.fielddefinitions;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import helpers.DatabaseHelper;
import helpers.Helper;
import io.vertx.core.json.JsonObject;

public class CurrentUser {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("__currentUser")
			.description("It returns info about current logged user.")
			.type(GraphQLObjectType.newObject()
					.name("CurrentUserType")
					.field(newFieldDefinition()
							.name("currentUser")
							.type(GraphQLString)
							.dataFetcher(new DataFetcher<Object>() {
								@Override
								public Object get(DataFetchingEnvironment environment) {
									return ((JsonObject) environment.getSource()).getValue("currentUser");
								}
							}))
					.field(newFieldDefinition()
							.name("isSuperUser")
							.type(GraphQLBoolean)
							.dataFetcher(new DataFetcher<Object>() {
								@Override
								public Object get(DataFetchingEnvironment environment) {
									return ((JsonObject) environment.getSource()).getValue("isSuperUser");
								}
							})))
			.dataFetcher(new DataFetcher<Object>() {
				@SuppressWarnings("unchecked")
				@Override
				public Object get(DataFetchingEnvironment environment) {
					JsonObject result = new JsonObject();
					Connection connection = null;
					try {
						String currentRole = Helper.getRole((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
						
						connection = DatabaseHelper.getConnection();
						StringBuffer sql = new StringBuffer()
								.append("SELECT rolname AS \"currentUser\", ")
								.append("       rolsuper AS \"isSuperUser\" ")
								.append("FROM pg_roles ")
								.append("WHERE rolname = ?");
						
						PreparedStatement ps = connection.prepareStatement(sql.toString());
						ps.setString(1, currentRole);
						ResultSet rs = ps.executeQuery();
						
						if (rs.next()) {
							result.put("currentUser", rs.getString("currentUser"));
							result.put("isSuperUser", rs.getBoolean("isSuperUser"));
						}
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
					return result;
				}
			});
}
