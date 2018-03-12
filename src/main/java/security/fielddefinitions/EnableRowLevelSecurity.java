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
import graphql.schema.GraphQLTypeReference;
import helpers.DatabaseHelper;
import helpers.Helper;
import helpers.maps.SchemaMap;

public class EnableRowLevelSecurity {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("__enableRowLevelSecurity")
			.description("It enables row-level security on the specified table.")
			.argument(newArgument()
					.name("table")
					.description("The table name.")
					.type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef("EntityEnumType"))))
			.type(GraphQLString)
			.dataFetcher(new DataFetcher<String>() {
				@SuppressWarnings("unchecked")
				@Override
				public String get(DataFetchingEnvironment environment) {
					List<Field> fields = environment.getFields();
					Connection connection = null;
					try {
						String table = null;
						
						Field field = fields.get(0);
						for (int i = 0; i < field.getArguments().size(); i++) {
							Argument argument = field.getArguments().get(i);
							if (argument.getName().equals("table")) {
								table = (String) Helper.resolveValue(argument.getValue(), environment);
							}
						}
						
						// PreparedStatement now allowed, so check to avoid SQL injection
						if (!table.matches("[_a-zA-Z][_0-9a-zA-Z]*")) {
							throw new Exception("Incorrect table name. Please, use this format: [_a-zA-Z][_0-9a-zA-Z]*");
						}
						table = SchemaMap.entityNameByUnderscoredName.get(table);
						connection = DatabaseHelper.getConnection((String) ((Map<String, Object>) environment.getContext()).get("authorization"));
						PreparedStatement ps = connection.prepareStatement("ALTER TABLE " + table + " ENABLE ROW LEVEL SECURITY");
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
