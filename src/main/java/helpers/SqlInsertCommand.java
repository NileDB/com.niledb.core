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
package helpers;

import java.util.ArrayList;

/**
 * @author NileDB, Inc.
 */
public class SqlInsertCommand {
	
	public String insert = "";
	public String attributes = "";
	public String valuePlaceholders = "";
	public ArrayList<Object> values = new ArrayList<Object>();
	public String returning = "";
	
	@Override
	public String toString() {
		return new StringBuffer()
				.append("WITH \"list\"(")
				.append(returning)
				.append(") AS (")
				.append(insert)
				.append((attributes.equals("") ? "" : "(" + attributes + ") "))
				.append("VALUES (" + (valuePlaceholders.equals("") ? "DEFAULT" : valuePlaceholders) + ") ")
				.append("RETURNING " + returning + ") SELECT array_to_json(array_agg(row_to_json(\"list\"))) AS \"list\" from \"list\"").toString();
	}
}
