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
package verticles;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import helpers.ConfigHelper;
import helpers.DatabaseHelper;

/**
 * @author NileDB, Inc.
 */
public class MainVerticle extends AbstractVerticle {
	
	final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	
	@Override
	public void start() {
		// Set configuration values from external configuration (config.json)
		ConfigHelper.setConfig(config());
		
		vertx.deployVerticle(HttpVerticle.class,
				new DeploymentOptions()
						.setWorker(true)
						.setInstances(4)
						.setConfig(config()),
				result -> {
					logger.info("Deployed HttpVerticle.");

					try {
						DatabaseHelper.getConnection().close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(((Boolean) ConfigHelper.get(ConfigHelper.SERVICE_SSL, false) ? "https" : "http") + "://" + ConfigHelper.get(ConfigHelper.SERVICE_HOST, "localhost") + ":" + ConfigHelper.get(ConfigHelper.SERVICE_PORT, 8080)));
						}
						catch (Exception e) {
							logger.error(e.getMessage());
						}
					}
				});
	}
	
	public static void main(String[] args) throws Exception {
		
		final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
		final Vertx vertx = Vertx.vertx();
		
		vertx.deployVerticle(new MainVerticle(), 
				new DeploymentOptions()
						.setConfig(new JsonObject(IOUtils.toString(new FileReader("config.json")))),
				
				result -> {
					if (result.succeeded()) {
						logger.info("OK");
					}
					else if (result.failed()) {
						logger.error("ERROR. " + result.cause().getMessage());
					}
					else {
						logger.error("ERROR. Unknown");
					}
				}
		);
		
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		vertx.close();
	}	
}
