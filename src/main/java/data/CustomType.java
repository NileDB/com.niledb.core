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

import java.util.List;

public interface CustomType {

	void eSetContainer(Database eContainer);

	Database eContainer();
	
	String getSchema();
	
	void setSchema(String schema);

	String getName();

	void setName(String value);

	List<CustomTypeAttribute> getAttributes();

	String getDocumentation();

	void setDocumentation(String value);
}
