package graphql;

import java.util.ArrayList;
import java.util.List;

import graphql.language.SourceLocation;

public class GraphQLSimpleError implements GraphQLError {

	private String message;
	private	List<GraphQLError> errors;
	
	public List<GraphQLError> getErrors() {
		return errors;
	}
	
	public GraphQLSimpleError(String message) {
		this.message = message;
		errors = new ArrayList<GraphQLError>();
		errors.add(this);
	}
	
	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public List<SourceLocation> getLocations() {
		return null;
	}

	@Override
	public ErrorType getErrorType() {
		return null;
	}
}
