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

public enum EntityType {

	TABLE(0, "TABLE", "table"),

	EXTERNAL(1, "EXTERNAL", "external");

	public static final int TABLE_VALUE = 0;

	public static final int EXTERNAL_VALUE = 1;

	private static final EntityType[] VALUES_ARRAY = new EntityType[] { TABLE, EXTERNAL, };

	public static final List<EntityType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	public static EntityType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			EntityType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	public static EntityType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			EntityType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	public static EntityType get(int value) {
		switch (value) {
		case TABLE_VALUE:
			return TABLE;
		case EXTERNAL_VALUE:
			return EXTERNAL;
		}
		return null;
	}

	private final int value;

	private final String name;

	private final String literal;

	private EntityType(int value, String name, String literal) {
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
