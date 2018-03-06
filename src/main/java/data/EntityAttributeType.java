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
package data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum EntityAttributeType {

	TEXT(0, "TEXT", "text"),

	BOOLEAN(1, "BOOLEAN", "boolean"),

	INTEGER(2, "INTEGER", "integer"),

	DECIMAL(3, "DECIMAL", "decimal"),

	MONEY(4, "MONEY", "money"),

	DATE(5, "DATE", "date"),

	TIMESTAMP(6, "TIMESTAMP", "timestamp"),

	CUSTOM_TYPE(7, "CUSTOM_TYPE", "custom type"),

	SERIAL(8, "SERIAL", "serial"),

	BYTEA(9, "BYTEA", "bytea"),

	SMALLINT(10, "SMALLINT", "smallint"),

	BIGINT(11, "BIGINT", "bitint"),

	DOUBLE_PRECISION(12, "DOUBLE_PRECISION", "double precision"),

	REAL(13, "REAL", "real"),

	SMALLSERIAL(14, "SMALLSERIAL", "smallserial"),

	BIGSERIAL(15, "BIGSERIAL", "bigserial"),

	VARCHAR(16, "VARCHAR", "varchar"),

	CHAR(17, "CHAR", "char"),

	TIME(18, "TIME", "time"),

	INTERVAL(19, "INTERVAL", "interval"),

	TIMESTAMP_WITH_TIME_ZONE(20, "TIMESTAMP_WITH_TIME_ZONE", "timestamp with time zone"),

	TIME_WITH_TIME_ZONE(21, "TIME_WITH_TIME_ZONE", "time with time zone");

	public static final int TEXT_VALUE = 0;

	public static final int BOOLEAN_VALUE = 1;

	public static final int INTEGER_VALUE = 2;

	public static final int DECIMAL_VALUE = 3;

	public static final int MONEY_VALUE = 4;

	public static final int DATE_VALUE = 5;

	public static final int TIMESTAMP_VALUE = 6;

	public static final int CUSTOM_TYPE_VALUE = 7;

	public static final int SERIAL_VALUE = 8;

	public static final int BYTEA_VALUE = 9;

	public static final int SMALLINT_VALUE = 10;

	public static final int BIGINT_VALUE = 11;

	public static final int DOUBLE_PRECISION_VALUE = 12;

	public static final int REAL_VALUE = 13;

	public static final int SMALLSERIAL_VALUE = 14;

	public static final int BIGSERIAL_VALUE = 15;

	public static final int VARCHAR_VALUE = 16;

	public static final int CHAR_VALUE = 17;

	public static final int TIME_VALUE = 18;

	public static final int INTERVAL_VALUE = 19;

	public static final int TIMESTAMP_WITH_TIME_ZONE_VALUE = 20;

	public static final int TIME_WITH_TIME_ZONE_VALUE = 21;

	private static final EntityAttributeType[] VALUES_ARRAY = new EntityAttributeType[] { TEXT, BOOLEAN, INTEGER,
			DECIMAL, MONEY, DATE, TIMESTAMP, CUSTOM_TYPE, SERIAL, BYTEA, SMALLINT, BIGINT, DOUBLE_PRECISION, REAL,
			SMALLSERIAL, BIGSERIAL, VARCHAR, CHAR, TIME, INTERVAL, TIMESTAMP_WITH_TIME_ZONE, TIME_WITH_TIME_ZONE, };

	public static final List<EntityAttributeType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	public static EntityAttributeType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			EntityAttributeType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	public static EntityAttributeType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			EntityAttributeType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	public static EntityAttributeType get(int value) {
		switch (value) {
		case TEXT_VALUE:
			return TEXT;
		case BOOLEAN_VALUE:
			return BOOLEAN;
		case INTEGER_VALUE:
			return INTEGER;
		case DECIMAL_VALUE:
			return DECIMAL;
		case MONEY_VALUE:
			return MONEY;
		case DATE_VALUE:
			return DATE;
		case TIMESTAMP_VALUE:
			return TIMESTAMP;
		case CUSTOM_TYPE_VALUE:
			return CUSTOM_TYPE;
		case SERIAL_VALUE:
			return SERIAL;
		case BYTEA_VALUE:
			return BYTEA;
		case SMALLINT_VALUE:
			return SMALLINT;
		case BIGINT_VALUE:
			return BIGINT;
		case DOUBLE_PRECISION_VALUE:
			return DOUBLE_PRECISION;
		case REAL_VALUE:
			return REAL;
		case SMALLSERIAL_VALUE:
			return SMALLSERIAL;
		case BIGSERIAL_VALUE:
			return BIGSERIAL;
		case VARCHAR_VALUE:
			return VARCHAR;
		case CHAR_VALUE:
			return CHAR;
		case TIME_VALUE:
			return TIME;
		case INTERVAL_VALUE:
			return INTERVAL;
		case TIMESTAMP_WITH_TIME_ZONE_VALUE:
			return TIMESTAMP_WITH_TIME_ZONE;
		case TIME_WITH_TIME_ZONE_VALUE:
			return TIME_WITH_TIME_ZONE;
		}
		return null;
	}

	private final int value;

	private final String name;

	private final String literal;

	private EntityAttributeType(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	public int getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getLiteral() {
		return literal;
	}

	@Override
	public String toString() {
		return literal;
	}

}
