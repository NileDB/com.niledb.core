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

public interface DataPackage {

	String eNAME = "data";

	String eNS_URI = "https://niledb.com/data.ecore";

	String eNS_PREFIX = "data";

	int DATABASE = 0;

	int DATABASE__NAME = 0;

	int DATABASE__SCHEMA_NAME = 1;

	int DATABASE__ENTITIES = 2;

	int DATABASE__CUSTOM_TYPES = 3;

	int DATABASE__DOCUMENTATION = 4;

	int DATABASE_FEATURE_COUNT = 5;

	int DATABASE_OPERATION_COUNT = 0;

	int ENTITY = 1;

	int ENTITY__NAME = 0;

	int ENTITY__ATTRIBUTES = 1;

	int ENTITY__TYPE = 2;

	int ENTITY__KEYS = 3;

	int ENTITY__REFERENCES = 4;

	int ENTITY__DOCUMENTATION = 5;

	int ENTITY_FEATURE_COUNT = 6;

	int ENTITY_OPERATION_COUNT = 0;

	int ENTITY_ATTRIBUTE = 2;

	int ENTITY_ATTRIBUTE__NAME = 0;

	int ENTITY_ATTRIBUTE__TYPE = 1;

	int ENTITY_ATTRIBUTE__ARRAY = 2;

	int ENTITY_ATTRIBUTE__DEFAULT_VALUE = 3;

	int ENTITY_ATTRIBUTE__REQUIRED = 4;

	int ENTITY_ATTRIBUTE__CUSTOM_TYPE = 5;

	int ENTITY_ATTRIBUTE__ENUM_TYPE = 6;

	int ENTITY_ATTRIBUTE__LENGTH = 7;

	int ENTITY_ATTRIBUTE__PRECISION = 8;

	int ENTITY_ATTRIBUTE__SCALE = 9;

	int ENTITY_ATTRIBUTE__DOCUMENTATION = 10;

	int ENTITY_ATTRIBUTE_FEATURE_COUNT = 11;

	int ENTITY_ATTRIBUTE_OPERATION_COUNT = 0;

	int ENTITY_KEY = 3;

	int ENTITY_KEY__NAME = 0;

	int ENTITY_KEY__ATTRIBUTES = 1;

	int ENTITY_KEY__UNIQUE = 2;

	int ENTITY_KEY__PRIMARY_KEY = 3;

	int ENTITY_KEY__DOCUMENTATION = 4;

	int ENTITY_KEY_FEATURE_COUNT = 5;

	int ENTITY_KEY_OPERATION_COUNT = 0;

	int ENTITY_REFERENCE = 4;

	int ENTITY_REFERENCE__NAME = 0;

	int ENTITY_REFERENCE__ATTRIBUTES = 1;

	int ENTITY_REFERENCE__REFERENCED_KEY = 2;

	int ENTITY_REFERENCE__DOCUMENTATION = 3;

	int ENTITY_REFERENCE_FEATURE_COUNT = 4;

	int ENTITY_REFERENCE_OPERATION_COUNT = 0;

	int CUSTOM_TYPE = 5;

	int CUSTOM_TYPE__NAME = 0;

	int CUSTOM_TYPE__ATTRIBUTES = 1;

	int CUSTOM_TYPE__DOCUMENTATION = 2;

	int CUSTOM_TYPE_FEATURE_COUNT = 3;

	int CUSTOM_TYPE_OPERATION_COUNT = 0;

	int CUSTOM_TYPE_ATTRIBUTE = 6;

	int CUSTOM_TYPE_ATTRIBUTE__NAME = 0;

	int CUSTOM_TYPE_ATTRIBUTE__TYPE = 1;

	int CUSTOM_TYPE_ATTRIBUTE__ARRAY = 2;

	int CUSTOM_TYPE_ATTRIBUTE__CUSTOM_TYPE = 3;

	int CUSTOM_TYPE_ATTRIBUTE__ENUM_TYPE = 4;

	int CUSTOM_TYPE_ATTRIBUTE__LENGTH = 5;

	int CUSTOM_TYPE_ATTRIBUTE__PRECISION = 6;

	int CUSTOM_TYPE_ATTRIBUTE__SCALE = 7;

	int CUSTOM_TYPE_ATTRIBUTE__DOCUMENTATION = 8;

	int CUSTOM_TYPE_ATTRIBUTE_FEATURE_COUNT = 9;

	int CUSTOM_TYPE_ATTRIBUTE_OPERATION_COUNT = 0;

	int ENTITY_TYPE = 7;

	int ENTITY_ATTRIBUTE_TYPE = 8;

	int CUSTOM_TYPE_ATTRIBUTE_TYPE = 9;

	DataFactory getDataFactory();

}
