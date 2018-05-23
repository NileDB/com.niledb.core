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
import data.Entity;
import data.EntityAttribute;
import data.EntityAttributeType;
import data.EnumType;

public class EntityAttributeImpl implements EntityAttribute {

	Entity eContainer;

	public void eSetContainer(Entity eContainer) {
		this.eContainer = eContainer;
	}

	public Entity eContainer() {
		return eContainer;
	}

	protected static final String NAME_EDEFAULT = null;

	protected String name = NAME_EDEFAULT;

	protected static final EntityAttributeType TYPE_EDEFAULT = EntityAttributeType.TEXT;

	protected EntityAttributeType type = TYPE_EDEFAULT;

	protected static final boolean ARRAY_EDEFAULT = false;

	protected boolean array = ARRAY_EDEFAULT;

	protected static final String DEFAULT_VALUE_EDEFAULT = null;

	protected String defaultValue = DEFAULT_VALUE_EDEFAULT;

	protected static final boolean REQUIRED_EDEFAULT = false;

	protected boolean required = REQUIRED_EDEFAULT;

	protected CustomType customType;

	protected EnumType enumType;
	
	protected static final int LENGTH_EDEFAULT = 0;

	protected int length = LENGTH_EDEFAULT;

	protected static final int PRECISION_EDEFAULT = 0;

	protected int precision = PRECISION_EDEFAULT;

	protected static final int SCALE_EDEFAULT = 0;

	protected int scale = SCALE_EDEFAULT;

	protected static final String DOCUMENTATION_EDEFAULT = null;

	protected String documentation = DOCUMENTATION_EDEFAULT;

	protected EntityAttributeImpl() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public EntityAttributeType getType() {
		return type;
	}

	public void setType(EntityAttributeType newType) {
		type = newType == null ? TYPE_EDEFAULT : newType;
	}

	public boolean isArray() {
		return array;
	}

	public void setArray(boolean newArray) {
		array = newArray;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String newDefaultValue) {
		defaultValue = newDefaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean newRequired) {
		required = newRequired;
	}

	public CustomType getCustomType() {
		return customType;
	}

	public CustomType basicGetCustomType() {
		return customType;
	}

	public void setCustomType(CustomType newCustomType) {
		customType = newCustomType;
	}

	public EnumType getEnumType() {
		return enumType;
	}

	public EnumType basicGetEnumType() {
		return enumType;
	}

	public void setEnumType(EnumType newEnumType) {
		enumType = newEnumType;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int newLength) {
		length = newLength;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int newPrecision) {
		precision = newPrecision;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int newScale) {
		scale = newScale;
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
		result.append(", type: ");
		result.append(type);
		result.append(", array: ");
		result.append(array);
		result.append(", defaultValue: ");
		result.append(defaultValue);
		result.append(", required: ");
		result.append(required);
		result.append(", enumType: ");
		result.append(enumType);
		result.append(", length: ");
		result.append(length);
		result.append(", precision: ");
		result.append(precision);
		result.append(", scale: ");
		result.append(scale);
		result.append(", documentation: ");
		result.append(documentation);
		result.append(')');
		return result.toString();
	}

}
