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

import data.EntityAttribute;
import data.EntityReference;

/**
 * @author NileDB, Inc.
 */
public class EntityMap {
	public Map<String, EntityAttribute> attributes = new HashMap<String, EntityAttribute>();
	public Map<String, EntityReference> directReferences = new HashMap<String, EntityReference>();
	public Map<String, EntityReference> inverseReferences = new HashMap<String, EntityReference>();
}
