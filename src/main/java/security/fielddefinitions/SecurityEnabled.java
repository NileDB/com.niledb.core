package security.fielddefinitions;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import helpers.ConfigHelper;

public class SecurityEnabled {
	public static GraphQLFieldDefinition.Builder builder = newFieldDefinition()
			.name("__securityEnabled")
			.description("It returns if configuration is enabled or not.")
			.type(GraphQLBoolean)
			.dataFetcher(new DataFetcher<Boolean>() {
				@Override
				public Boolean get(DataFetchingEnvironment environment) {
					return (Boolean) ConfigHelper.get(ConfigHelper.SECURITY_ENABLED, false);
				}
			});
}
