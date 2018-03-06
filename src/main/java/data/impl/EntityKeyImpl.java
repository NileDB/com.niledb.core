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

import data.Entity;
import data.EntityAttribute;
import data.EntityKey;

import java.util.ArrayList;
import java.util.List;

public class EntityKeyImpl implements EntityKey {

	Entity eContainer;

	public void eSetContainer(Entity eContainer) {
		this.eContainer = eContainer;
	}

	public Entity eContainer() {
		return eContainer;
	}

	protected static final String NAME_EDEFAULT = null;

	protected String name = NAME_EDEFAULT;

	protected List<EntityAttribute> attributes;

	protected static final boolean UNIQUE_EDEFAULT = false;

	protected boolean unique = UNIQUE_EDEFAULT;

	protected static final boolean PRIMARY_KEY_EDEFAULT = false;

	protected boolean primaryKey = PRIMARY_KEY_EDEFAULT;

	protected static final String DOCUMENTATION_EDEFAULT = null;

	protected String documentation = DOCUMENTATION_EDEFAULT;

	protected EntityKeyImpl() {
		super();
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

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean newUnique) {
		unique = newUnique;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean newPrimaryKey) {
		primaryKey = newPrimaryKey;
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
		result.append(", unique: ");
		result.append(unique);
		result.append(", primaryKey: ");
		result.append(primaryKey);
		result.append(", documentation: ");
		result.append(documentation);
		result.append(')');
		return result.toString();
	}

}
