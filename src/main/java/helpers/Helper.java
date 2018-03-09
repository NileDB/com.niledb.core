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

import java.util.Map;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;

import data.CustomTypeAttributeType;
import data.EntityAttributeType;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.json.JsonObject;

/**
 * @author NileDB, Inc.
 */
public class Helper {
	
	public static String getRole(String jwtToken) {
		if ((Boolean) ConfigHelper.get(ConfigHelper.SECURITY_ENABLED, false)) {
			if (jwtToken != null) {
				try {
					JWSObject jwsObject = JWSObject.parse(jwtToken);
					
					JWSVerifier verifier = new MACVerifier(((String) ConfigHelper.get(ConfigHelper.SECURITY_JWT_SECRET, "password_of_at_least_32_characters")).getBytes());
					boolean verified = jwsObject.verify(verifier);
					
					if (verified) {
						return new JsonObject(jwsObject.getPayload().toString()).getString("username");
					}
					else {
						return null;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return (String) ConfigHelper.get(ConfigHelper.SECURITY_ANONYMOUS_ROLENAME, null);
				}
			}
			else {
				return (String) ConfigHelper.get(ConfigHelper.SECURITY_ANONYMOUS_ROLENAME, null);
			}
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Object resolveValue(@SuppressWarnings("rawtypes") Value value, DataFetchingEnvironment environment) {
		if (value instanceof StringValue) {
			return ((StringValue) value).getValue();
		}
		else if (value instanceof IntValue) {
			return ((IntValue) value).getValue().intValue();
		}
		else if (value instanceof FloatValue) {
			return ((FloatValue) value).getValue().doubleValue();
		}
		else if (value instanceof VariableReference) {
			return ((Map<String, Object>) environment.getContext()).get(((VariableReference) value).getName());
		}
		else if (value instanceof BooleanValue) {
			return ((BooleanValue) value).isValue();
		}
		else if (value instanceof EnumValue) {
			return ((EnumValue) value).getName();
		}
		else if (value instanceof ArrayValue) {
			return ((ArrayValue) value).getValues();
		}
		else if (value instanceof ObjectValue) {
			return ((ObjectValue) value).getObjectFields();
		}
		else if (value instanceof NullValue) {
			return null;
		}
		else {
			return null;
		}
	}
	
	public static EntityAttributeType getEntityAttributeType(String typeName) {
		return getEntityAttributeType(typeName, null);
	}
	
	public static EntityAttributeType getEntityAttributeType(String typeName, String isAutoincrement) {
		switch (typeName) {
			case "text":
			case "_text":
				return EntityAttributeType.TEXT;
			case "bool":
			case "_bool":
				return EntityAttributeType.BOOLEAN;
			case "int4":
			case "_int4":
				return EntityAttributeType.INTEGER;
			case "numeric":
			case "_numeric":
				return EntityAttributeType.DECIMAL;
			case "money":
			case "_money":
				return EntityAttributeType.MONEY;
			case "date":
			case "_date":
				return EntityAttributeType.DATE;
			case "timestamp":
			case "_timestamp":
				return EntityAttributeType.TIMESTAMP;
			case "serial":
				return EntityAttributeType.SERIAL;
			case "bytea":
			case "_bytea":
				return EntityAttributeType.BYTEA;
			case "int2":
			case "_int2":
				if (isAutoincrement != null && isAutoincrement.toLowerCase().equals("yes")) {
					return EntityAttributeType.SMALLSERIAL;
				}
				else {
					return EntityAttributeType.SMALLINT;
				}
			case "int8":
			case "_int8":
				return EntityAttributeType.BIGINT;
			case "float8":
			case "_float8":
				return EntityAttributeType.DOUBLE_PRECISION;
			case "float4":
			case "_float4":
				return EntityAttributeType.REAL;
			case "bigserial":
				return EntityAttributeType.BIGSERIAL;
			case "varchar":
			case "_varchar":
				return EntityAttributeType.VARCHAR;
			case "bpchar":
			case "_bpchar":
				return EntityAttributeType.CHAR;
			case "time":
			case "_time":
				return EntityAttributeType.TIME;
			case "interval":
			case "_interval":
				return EntityAttributeType.INTERVAL;
			case "timestamptz":
			case "_timestamptz":
				return EntityAttributeType.TIMESTAMP_WITH_TIME_ZONE;
			case "timetz":
			case "_timetz":
				return EntityAttributeType.TIME_WITH_TIME_ZONE;
			default:
				return EntityAttributeType.CUSTOM_TYPE;
		}
	}
	
	public static CustomTypeAttributeType getCustomTypeAttributeType(String typeName) {
		switch (typeName) {
			case "text":
			case "_text":
				return CustomTypeAttributeType.TEXT;
			case "bool":
			case "_bool":
				return CustomTypeAttributeType.BOOLEAN;
			case "int4":
			case "_int4":
				return CustomTypeAttributeType.INTEGER;
			case "numeric":
			case "_numeric":
				return CustomTypeAttributeType.DECIMAL;
			case "money":
			case "_money":
				return CustomTypeAttributeType.MONEY;
			case "date":
			case "_date":
				return CustomTypeAttributeType.DATE;
			case "timestamp":
			case "_timestamp":
				return CustomTypeAttributeType.TIMESTAMP;
			case "bytea":
			case "_bytea":
				return CustomTypeAttributeType.BYTEA;
			case "int2":
			case "_int2":
				return CustomTypeAttributeType.SMALLINT;
			case "int8":
			case "_int8":
				return CustomTypeAttributeType.BIGINT;
			case "float8":
			case "_float8":
				return CustomTypeAttributeType.DOUBLE_PRECISION;
			case "float4":
			case "_float4":
				return CustomTypeAttributeType.REAL;
			case "varchar":
			case "_varchar":
				return CustomTypeAttributeType.VARCHAR;
			case "bpchar":
			case "_bpchar":
				return CustomTypeAttributeType.CHAR;
			case "time":
			case "_time":
				return CustomTypeAttributeType.TIME;
			case "interval":
			case "_interval":
				return CustomTypeAttributeType.INTERVAL;
			case "timestamptz":
			case "_timestamptz":
				return CustomTypeAttributeType.TIMESTAMP_WITH_TIME_ZONE;
			case "timetz":
			case "_timetz":
				return CustomTypeAttributeType.TIME_WITH_TIME_ZONE;
			default:
				return CustomTypeAttributeType.CUSTOM_TYPE;
		}
	}
	
	public static boolean isArray(String typeName) {
		switch (typeName) {
			case "_text":
			case "_bool":
			case "_int4":
			case "_numeric":
			case "_money":
			case "_date":
			case "_timestamp":
			case "_bytea":
			case "_int2":
			case "_int8":
			case "_float8":
			case "_float4":
			case "_varchar":
			case "_bpchar":
			case "_time":
			case "_interval":
			case "_timestamptz":
			case "_timetz":
				return true;
			default:
				return false;
		}
	}
	
	public static String toFirstUpper(String word) {
		return (word == null ? 
					null : 
					(word.length() <= 1 ? 
						word.toUpperCase() : 
						word.substring(0, 1).toUpperCase() + word.substring(1)));
	}
	
	public static String toFirstLower(String word) {
		return (word == null ? 
					null : 
					(word.length() <= 1 ? 
						word.toLowerCase() : 
						word.substring(0, 1).toLowerCase() + word.substring(1)));
	}
}
