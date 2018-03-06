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
package data.impl;

import data.CustomType;
import data.Database;
import data.Entity;

import java.util.ArrayList;
import java.util.List;

public class DatabaseImpl implements Database {

	protected static final String NAME_EDEFAULT = null;

	protected String name = NAME_EDEFAULT;

	protected List<String> schemaNames;

	protected List<Entity> entities;

	protected List<CustomType> customTypes;

	protected static final String DOCUMENTATION_EDEFAULT = null;

	protected String documentation = DOCUMENTATION_EDEFAULT;

	protected DatabaseImpl() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public List<String> getSchemaNames() {
		if (schemaNames == null) {
			schemaNames = new ArrayList<String>();
		}
		return schemaNames;
	}

	public List<Entity> getEntities() {
		if (entities == null) {
			entities = new ArrayList<Entity>();
		}
		return entities;
	}

	public List<CustomType> getCustomTypes() {
		if (customTypes == null) {
			customTypes = new ArrayList<CustomType>();
		}
		return customTypes;
	}

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String newDocumentation) {
		documentation = newDocumentation;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", documentation: ");
		result.append(documentation);
		result.append(')');
		return result.toString();
	}
}
