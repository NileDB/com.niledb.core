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
import data.CustomTypeAttribute;
import data.Database;

import java.util.ArrayList;
import java.util.List;

public class CustomTypeImpl implements CustomType {

	Database eContainer;

	public void eSetContainer(Database eContainer) {
		this.eContainer = eContainer;
	}

	public Database eContainer() {
		return eContainer;
	}
	
	protected static final String SCHEMA_EDEFAULT = null;
	
	protected static final String NAME_EDEFAULT = null;
	
	protected String schema = SCHEMA_EDEFAULT;
	
	protected String name = NAME_EDEFAULT;

	protected List<CustomTypeAttribute> attributes;

	protected static final String DOCUMENTATION_EDEFAULT = null;

	protected String documentation = DOCUMENTATION_EDEFAULT;

	protected CustomTypeImpl() {
		super();
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setSchema(String newSchema) {
		schema = newSchema;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public List<CustomTypeAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<CustomTypeAttribute>();
		}
		return attributes;
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
		result.append(" (schema: ");
		result.append(schema);
		result.append(", name: ");
		result.append(name);
		result.append(", documentation: ");
		result.append(documentation);
		result.append(')');
		return result.toString();
	}

}
