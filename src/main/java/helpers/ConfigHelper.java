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

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author NileDB, Inc.
 */
public class ConfigHelper {
	final static Logger logger = LoggerFactory.getLogger(ConfigHelper.class);
	
	public final static String SERVICE_HOST = "service.host";
	public final static String SERVICE_PORT = "service.port";
	public final static String SERVICE_SSL = "service.ssl";
	public final static String SERVICE_SSL_AUTO_GENERATE_CERT= "service.ssl.autoGenerateCert";
	public final static String SERVICE_SSL_KEY_PATH = "service.ssl.key.path";
	public final static String SERVICE_SSL_CERT_PATH = "service.ssl.cert.path";
	public final static String SERVICE_AUTHENTICATE = "service.authenticate";
	public final static String SERVICE_USERNAME = "service.username";
	public final static String SERVICE_PASSWORD = "service.password";
	public final static String SERVICE_QUERY_MAX_RESULTS = "service.query.maxResults";
	public final static String SERVICE_FORCE_SSL = "service.forceSsl";
	public final static String SERVICE_REDIRECT_FROM_PORT = "service.redirectFromPort";
	public final static String SERVICE_REDIRECT_URL = "service.redirectUrl";
	public final static String DB_NAME = "db.name";
	public final static String DB_SCHEMA_NAMES = "db.schema.names";
	public final static String DB_HOST = "db.host";
	public final static String DB_PORT = "db.port";
	public final static String DB_REPLICA_HOST = "db.replica.host";
	public final static String DB_REPLICA_PORT = "db.replica.port";
	public final static String DB_USERNAME = "db.username";
	public final static String DB_PASSWORD = "db.password";
	public final static String MODEL_PATH = "model.path";
	public final static String MODEL_CREATE_MODEL_FROM_DB = "model.createModelFromDb";
	public final static String MODEL_CREATE_DB_FROM_MODEL = "model.createDbFromModel";
	
	private static JsonObject config;
	
	public static Object get(String name, Object defaultValue) {
		return config.getValue(name, defaultValue);
	}
	
	public synchronized static void setConfig(JsonObject config) {
		
		// Log configuration values
		logger.info("Configuration: " + config.encodePrettily());
		
		ConfigHelper.config = config;
	}
}
