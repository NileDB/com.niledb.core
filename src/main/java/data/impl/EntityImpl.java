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

import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityKey;
import data.EntityReference;
import data.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EntityImpl implements Entity {

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

	protected List<EntityAttribute> attributes;

	protected static final EntityType TYPE_EDEFAULT = EntityType.TABLE;

	protected EntityType type = TYPE_EDEFAULT;

	protected List<EntityKey> keys;

	protected List<EntityReference> references;

	protected static final String DOCUMENTATION_EDEFAULT = null;

	protected String documentation = DOCUMENTATION_EDEFAULT;

	protected EntityImpl() {
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

	public List<EntityAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<EntityAttribute>();
		}
		return attributes;
	}

	public EntityType getType() {
		return type;
	}

	public void setType(EntityType newType) {
		type = newType == null ? TYPE_EDEFAULT : newType;
	}

	public List<EntityKey> getKeys() {
		if (keys == null) {
			keys = new ArrayList<EntityKey>();
		}
		return keys;
	}

	public List<EntityReference> getReferences() {
		if (references == null) {
			references = new ArrayList<EntityReference>();
		}
		return references;
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
		result.append(", type: ");
		result.append(type);
		result.append(", documentation: ");
		result.append(documentation);
		result.append(')');
		return result.toString();
	}

}
