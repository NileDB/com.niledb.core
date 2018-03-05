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
import java.util.HashMap;
import java.util.List;

/**
 * @author NileDB, Inc.
 */
public class SqlSelectCommand {
	public String select = "";
	public String from = "";
	public List<Object> fromParameters = new ArrayList<Object>();
	
	public String where = "";
	public List<Object> whereParameters = new ArrayList<Object>();
	
	public String orderBy = "";
	public String groupBy = "";
	
	public String pagination = "";
	public List<Object> paginationParameters = new ArrayList<Object>();
	
	public HashMap<String, Boolean> addedAttributes = new HashMap<String, Boolean>();
	public HashMap<String, Boolean> addedDirectReferences = new HashMap<String, Boolean>();
	public HashMap<String, Boolean> addedGroupByAttributes = new HashMap<String, Boolean>();
	
	public boolean inverseReferences = false;
	public boolean primaryKeyIncludedInGroupBy = false;
	
	@Override
	public String toString() {
		return select
				+ from
				+ where
				+ groupBy
				+ orderBy
				+ pagination;
	}
	
	public List<Object> getParameters() {
		List<Object> parameters = new ArrayList<Object>();
		parameters.addAll(fromParameters);
		parameters.addAll(whereParameters);
		parameters.addAll(paginationParameters);
		return parameters;
	}
}
