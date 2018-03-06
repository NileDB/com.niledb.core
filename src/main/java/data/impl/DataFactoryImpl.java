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

import data.*;

public class DataFactoryImpl implements DataFactory {

	public DataFactoryImpl() {
		super();
	}

	public Database createDatabase() {
		DatabaseImpl database = new DatabaseImpl();
		return database;
	}

	public Entity createEntity() {
		EntityImpl entity = new EntityImpl();
		return entity;
	}

	public EntityAttribute createEntityAttribute() {
		EntityAttributeImpl entityAttribute = new EntityAttributeImpl();
		return entityAttribute;
	}

	public EntityKey createEntityKey() {
		EntityKeyImpl entityKey = new EntityKeyImpl();
		return entityKey;
	}

	public EntityReference createEntityReference() {
		EntityReferenceImpl entityReference = new EntityReferenceImpl();
		return entityReference;
	}

	public CustomType createCustomType() {
		CustomTypeImpl customType = new CustomTypeImpl();
		return customType;
	}

	public CustomTypeAttribute createCustomTypeAttribute() {
		CustomTypeAttributeImpl customTypeAttribute = new CustomTypeAttributeImpl();
		return customTypeAttribute;
	}

}
