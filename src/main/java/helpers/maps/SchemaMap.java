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
package helpers.maps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author NileDB, Inc.
 */
public class SchemaMap {
	public static Map<String, EntityMap> entities = new HashMap<String, EntityMap>();
	public static Map<String, CustomTypeMap> customTypes = new HashMap<String, CustomTypeMap>();
}
