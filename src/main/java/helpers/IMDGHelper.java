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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author NileDB, Inc.
 */
public class IMDGHelper {
	final static Logger logger = LoggerFactory.getLogger(IMDGHelper.class);
	
    private static HazelcastInstance imdgInstance;
    
    public static HazelcastInstance get() {
    	if (imdgInstance == null) {
    		synchronized (IMDGHelper.class) {
        		imdgInstance = Hazelcast.newHazelcastInstance();
			}
    	}
    	return imdgInstance;
    }
}
