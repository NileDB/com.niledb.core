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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.postgresql.jdbc.PgDatabaseMetaData;
import org.postgresql.util.PSQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;

import data.CustomType;
import data.CustomTypeAttribute;
import data.CustomTypeAttributeType;
import data.DataFactory;
import data.Database;
import data.Entity;
import data.EntityAttribute;
import data.EntityAttributeType;
import data.EntityKey;
import data.EntityReference;
import data.EntityType;
import data.EnumType;
import data.impl.DataFactoryImpl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author NileDB, Inc.
 */
public class DatabaseHelper {
	
	private static DataSource dataSource = null;
	
	final static Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);
	
	public static final String INVALID_CATALOG_NAME = "3D000";
	
	static {
		dataSource = getDataSource();
	}
	
	public static DataSource getDataSource() {
		if (dataSource != null) {
			return dataSource;
		}
		else {
			try {
				HikariConfig config = new HikariConfig();
				
				config.setJdbcUrl("jdbc:postgresql://"
						+ ConfigHelper.get(ConfigHelper.DB_HOST, "localhost") + ":"
						+ ConfigHelper.get(ConfigHelper.DB_PORT, 5432) + "/"
						+ ConfigHelper.get(ConfigHelper.DB_NAME, "nile"));
				config.setUsername((String) ConfigHelper.get(ConfigHelper.DB_USERNAME, "postgres"));
				config.setPassword((String) ConfigHelper.get(ConfigHelper.DB_PASSWORD, "postgres"));
				if ((Boolean) ConfigHelper.get(ConfigHelper.DB_SSL, false)) {
					config.addDataSourceProperty("ssl", true);
					config.addDataSourceProperty("sslmode", (String) ConfigHelper.get(ConfigHelper.DB_SSL_MODE, "verify-ca"));
					config.addDataSourceProperty("sslrootcert", (String) ConfigHelper.get(ConfigHelper.DB_SSL_ROOT_CERT, "misc/ssl/BaltimoreCyberTrustRoot.crt.pem"));
				}
				config.addDataSourceProperty("cachePrepStmts", "true");
				config.addDataSourceProperty("prepStmtCacheSize", "250");
				config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
				config.setDriverClassName("org.postgresql.Driver");
				config.setConnectionTestQuery("SELECT 1");
				dataSource = new HikariDataSource(config);
				
				// Test connection
				dataSource.getConnection().close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			catch (PoolInitializationException e) {
				if (e.getCause() instanceof PSQLException) {
					PSQLException psqlException = (PSQLException) e.getCause();
					if (psqlException.getSQLState().equals(INVALID_CATALOG_NAME)) {
						Connection connection = null;
						try {
							logger.info("Creating database " + ConfigHelper.get(ConfigHelper.DB_NAME, "nile") + "...");
							connection = DriverManager.getConnection("jdbc:postgresql://"
									+ ConfigHelper.get(ConfigHelper.DB_HOST, "localhost") + ":"
									+ ConfigHelper.get(ConfigHelper.DB_PORT, 5432) + "/", 
									(String) ConfigHelper.get(ConfigHelper.DB_USERNAME, "postgres"), 
									(String) ConfigHelper.get(ConfigHelper.DB_PASSWORD, "postgres"));
							Statement statement = connection.createStatement();
							statement.execute("CREATE DATABASE " + ConfigHelper.get(ConfigHelper.DB_NAME, "nile"));
							
							try {
								if ((Boolean) ConfigHelper.get(ConfigHelper.SECURITY_ENABLED, false)) {
									String anonymousUsername = (String) ConfigHelper.get(ConfigHelper.SECURITY_ANONYMOUS_ROLENAME, null);
									if (anonymousUsername != null
											&& !anonymousUsername.equals("")) {
										statement.execute("CREATE ROLE " + anonymousUsername);
									}
								}
							}
							catch (Exception e2) {
								e2.printStackTrace();
							}
						}
						catch (SQLException e2) {
							e2.printStackTrace();
						}
						finally {
							if (connection != null) {
								try {
									connection.close();
									return getDataSource();
								}
								catch (Exception e3) {
									e3.printStackTrace();
								}
							}
						}
					}
					else {
						e.printStackTrace();
					}
				}
				else {
					e.printStackTrace();
				}
			}
			return dataSource;
		}
	}
	
	public static Connection getConnection() throws Exception {
		Connection connection = dataSource.getConnection();
		connection.createStatement().execute("RESET ROLE");
		return connection;
	}
	
	public static Connection getConnection(String jwtToken) throws Exception {
		Connection connection = dataSource.getConnection();
		String role = Helper.getRole(jwtToken);
		if (role != null) {
			connection.createStatement().execute("SET ROLE " + role);
		}
		else {
			connection.createStatement().execute("RESET ROLE");
		}
		return connection;
	}
	
	public static Database getDatabaseModel(String databaseHost, int databasePort, String databaseName, List<String> schemaNames) {
		Connection connection = null;
		
		String catalogName = null;
		
		Database database = null;
		
		try {
			connection = getConnection();
			
			PgDatabaseMetaData dbmd = (PgDatabaseMetaData) connection.getMetaData();
			
			DataFactory dataFactory = (DataFactory) new DataFactoryImpl();
			
			database = dataFactory.createDatabase();
			database.setName(databaseName);
			database.getSchemaNames().addAll(schemaNames);
			database.setDocumentation("Model generated from Database '" + databaseName + "'");

			// Map domains to its base types
			// We don't care about domains in our services (at least currently)
			Map<String, String> domainToTypeMap = new HashMap<String, String>();
			Statement statement = connection.createStatement();
			StringBuffer sb = new StringBuffer()
					.append("SELECT nd.nspname AS \"domainSchema\",")
					.append("       d.typname AS \"domainName\",")
					.append("       nb.nspname AS \"typeSchema\",")
					.append("       b.typname AS \"typeName\" ")
					.append("FROM pg_type AS d,")
					.append("     pg_type AS b,")
					.append("     pg_namespace AS nd,")
					.append("     pg_namespace AS nb ")
					.append("WHERE d.typtype = 'd' ")
					.append("AND   d.typbasetype = b.oid ")
					.append("AND   nd.oid = d.typnamespace ")
					.append("AND   nb.oid = b.typnamespace");
			ResultSet rs = statement.executeQuery(sb.toString());
			while (rs.next()) {
				String domainSchema = rs.getString("domainSchema");
				String domainName = rs.getString("domainName");
				String typeSchema = rs.getString("typeSchema");
				String typeName = rs.getString("typeName");
				domainToTypeMap.put((domainSchema.equals("pg_catalog") ? domainName : "\"" + domainSchema + "\".\"" + domainName + "\""), 
						(typeSchema.equals("pg_catalog") ? typeName : "\"" + typeSchema + "\".\"" + typeName + "\""));
			}
			rs.close();
			
			// Get catalog name
			rs = dbmd.getCatalogs();
			while (rs.next()) {
				catalogName = (String) rs.getObject("TABLE_CAT");
			}
			rs.close();
			
			List<Entity> entities = database.getEntities();
			List<CustomType> customTypes = database.getCustomTypes();
			List<EnumType> enumTypes = database.getEnumTypes();
			
			// Get custom types
			for (String schemaName : schemaNames) {
				rs = dbmd.getTables(catalogName, schemaName, null, new String[] {"TYPE"});
				
				while (rs.next()) {
					String typeName = (String) rs.getObject("table_name");
					
					// TODO Debe de haber un error en el driver porque los remarks de los tipos no salen
					String remarks = (String) rs.getObject("remarks");
					
					CustomType customType = dataFactory.createCustomType();
					customType.eSetContainer(database);
					customTypes.add(customType);
					customType.setSchema(schemaName);
					customType.setName(typeName);
					customType.setDocumentation(remarks == null ? "Model generated from Type '" + typeName + "'" : remarks);
				}
				rs.close();
			}
			
			// Get custom types attributes
			for (String schemaName : schemaNames) {
				rs = dbmd.getTables(catalogName, schemaName, null, new String[] {"TYPE"});
				
				while (rs.next()) {
					String typeName = (String) rs.getObject("table_name");
					CustomType customType = null;
					for (int i = 0; i < customTypes.size(); i++) {
						if (customTypes.get(i).getSchema().equals(schemaName)
								&& customTypes.get(i).getName().equals(typeName)) {
							customType = customTypes.get(i);
							break;
						}
					}
					
					List<CustomTypeAttribute> attributes = customType.getAttributes();
					
					ResultSet columnsRs = dbmd.getColumns(catalogName, schemaName, typeName, "%");
					while (columnsRs.next()) {
						String columnName = (String) columnsRs.getObject("COLUMN_NAME");
						String columnRemarks = (String) columnsRs.getObject("REMARKS");
						String columnTypeName = (String) columnsRs.getObject("TYPE_NAME");
						if (domainToTypeMap.get(columnTypeName) != null) {
							columnTypeName = domainToTypeMap.get(columnTypeName);
						}
						Integer columnSize = (Integer) columnsRs.getObject("COLUMN_SIZE");
						Integer decimalDigits = (Integer) columnsRs.getObject("DECIMAL_DIGITS");
						
						CustomTypeAttribute attribute = dataFactory.createCustomTypeAttribute();
						attribute.eSetContainer(customType);
						attributes.add(attribute);
						
						attribute.setName(columnName);
						attribute.setArray(Helper.isArray(columnTypeName));
						attribute.setDocumentation(columnRemarks == null ? "Model generated from Column '" + columnName + "'" : columnRemarks);
						attribute.setType(Helper.getCustomTypeAttributeType(columnTypeName));
						if (attribute.getType() == CustomTypeAttributeType.CUSTOM_TYPE) {
							attribute.setCustomType(null);
							String customTypeSchemaName = schemaName;
							String customTypeName = columnTypeName;
							if (columnTypeName.contains(".")) {
								StringTokenizer st = new StringTokenizer(columnTypeName, ".");
								customTypeSchemaName = st.nextToken().replaceAll("\\\"", "");
								customTypeName = st.nextToken().replaceAll("\\\"", "");
							}
							if (customTypeName.startsWith("_")) {
								attribute.setArray(true);
								customTypeName = customTypeName.substring(1);
							}
							for (int i = 0; i < customTypes.size(); i++) {
								if (customTypes.get(i).getSchema().equals(customTypeSchemaName)
										&& customTypes.get(i).getName().equals(customTypeName)) {
									attribute.setCustomType(customTypes.get(i));
									break;
								}
							}
							if (attribute.getCustomType() == null) {
								attribute.setType(CustomTypeAttributeType.TEXT);
								for (int i = 0; i < enumTypes.size(); i++) {
									if (enumTypes.get(i).getSchema().equals(customTypeSchemaName)
											&& enumTypes.get(i).getName().equals(customTypeName)) {
										attribute.setEnumType(enumTypes.get(i));
										break;
									}
								}
								if (attribute.getEnumType() == null) {
									EnumType enumType = dataFactory.createEnumType();
									enumType.eSetContainer(database);
									enumTypes.add(enumType);
									enumType.setSchema(customTypeSchemaName);
									enumType.setName(customTypeName);
									enumType.setDocumentation("Model generated from Type '" + customTypeName + "'");
									PreparedStatement rangePs = connection.prepareStatement("SELECT enum_range(NULL::\"" + customTypeSchemaName + "\".\"" + customTypeName + "\")");
									ResultSet values = rangePs.executeQuery();
									if (values.next()) {
										Object[] stringValues = (Object[]) ((org.postgresql.jdbc.PgArray) values.getArray("enum_range")).getArray();										
										for (int i = 0; i < stringValues.length; i++) {
											enumType.getValues().add(stringValues[i].toString());
										}
									}
									attribute.setEnumType(enumType);
								}
							}
						}
						attribute.setLength(columnSize);
						attribute.setPrecision(columnSize);
						attribute.setScale(decimalDigits);
					}
					columnsRs.close();
				}
				rs.close();
			}
			
			// Get tables
			for (String schemaName : schemaNames) {
				rs = dbmd.getTables(catalogName, schemaName, null, new String[] {
						"TABLE",
						"FOREIGN TABLE",
						"VIEW",
						"MATERIALIZED VIEW"});
				
				while (rs.next()) {
					String tableName = (String) rs.getObject("table_name");
					String remarks = (String) rs.getObject("remarks");
					
					Entity entity = dataFactory.createEntity();
					entity.eSetContainer(database);
					entities.add(entity);
					entity.setSchema(schemaName);
					entity.setName(tableName);
					entity.setDocumentation(remarks == null ? "Model generated from Table '" + tableName + "'" : remarks);
					entity.setType(EntityType.TABLE);
					
					List<EntityAttribute> attributes = entity.getAttributes();
					ResultSet columnsRs = dbmd.getColumns(catalogName, schemaName, tableName, "%");
					while (columnsRs.next()) {
						String columnName = (String) columnsRs.getObject("COLUMN_NAME");
						String columnRemarks = (String) columnsRs.getObject("REMARKS");
						String columnTypeName = (String) columnsRs.getObject("TYPE_NAME");
						if (domainToTypeMap.get(columnTypeName) != null) {
							columnTypeName = domainToTypeMap.get(columnTypeName);
						}
						Integer columnSize = (Integer) columnsRs.getObject("COLUMN_SIZE");
						Integer decimalDigits = (Integer) columnsRs.getObject("DECIMAL_DIGITS");
						Integer nullable = (Integer) columnsRs.getObject("NULLABLE");
						String columnDefault = (String) columnsRs.getObject("COLUMN_DEF");
						String isAutoincrement = (String) columnsRs.getObject("IS_AUTOINCREMENT");
						
						EntityAttribute attribute = dataFactory.createEntityAttribute();
						attribute.eSetContainer(entity);
						attributes.add(attribute);

						attribute.setName(columnName);
						attribute.setArray(Helper.isArray(columnTypeName));
						attribute.setDocumentation(columnRemarks == null ? "Model generated from Column '" + columnName + "'" : columnRemarks);
						attribute.setType(Helper.getEntityAttributeType(columnTypeName, isAutoincrement));
						if (attribute.getType() == EntityAttributeType.CUSTOM_TYPE) {
							attribute.setCustomType(null);
							String customTypeSchemaName = schemaName;
							String customTypeName = columnTypeName;
							if (columnTypeName.contains(".")) {
								StringTokenizer st = new StringTokenizer(columnTypeName, ".");
								customTypeSchemaName = st.nextToken().replaceAll("\\\"", "");
								customTypeName = st.nextToken().replaceAll("\\\"", "");
							}
							if (customTypeName.startsWith("_")) {
								attribute.setArray(true);
								customTypeName = customTypeName.substring(1);
							}
							for (int i = 0; i < customTypes.size(); i++) {
								if (customTypes.get(i).getSchema().equals(customTypeSchemaName)
										&& customTypes.get(i).getName().equals(customTypeName)) {
									attribute.setCustomType(customTypes.get(i));
									break;
								}
							}
							if (attribute.getCustomType() == null) {
								attribute.setType(EntityAttributeType.TEXT);
								for (int i = 0; i < enumTypes.size(); i++) {
									if (enumTypes.get(i).getSchema().equals(customTypeSchemaName)
											&& enumTypes.get(i).getName().equals(customTypeName)) {
										attribute.setEnumType(enumTypes.get(i));
										break;
									}
								}
								if (attribute.getEnumType() == null) {
									EnumType enumType = dataFactory.createEnumType();
									enumType.eSetContainer(database);
									enumTypes.add(enumType);
									enumType.setSchema(customTypeSchemaName);
									enumType.setName(customTypeName);
									enumType.setDocumentation("Model generated from Type '" + customTypeName + "'");
									PreparedStatement rangePs = connection.prepareStatement("SELECT enum_range(NULL::\"" + customTypeSchemaName + "\".\"" + customTypeName + "\")");
									ResultSet values = rangePs.executeQuery();
									if (values.next()) {
										Object[] stringValues = (Object[]) ((org.postgresql.jdbc.PgArray) values.getArray("enum_range")).getArray();										
										for (int i = 0; i < stringValues.length; i++) {
											enumType.getValues().add(stringValues[i].toString());
										}
									}
									attribute.setEnumType(enumType);
								}
							}
						}
						attribute.setDefaultValue(columnDefault);
						attribute.setLength(columnSize);
						attribute.setPrecision(columnSize);
						attribute.setRequired(nullable == 0);
						attribute.setScale(decimalDigits);
					}
					columnsRs.close();
					
					List<EntityKey> keys = entity.getKeys();
					ResultSet pkRs = dbmd.getPrimaryKeys(catalogName, schemaName, tableName);
					while (pkRs.next()) {
						String pkName = (String) pkRs.getObject("pk_name");
						String columnName = (String) pkRs.getObject("column_name");
						EntityAttribute attribute = null;
						for (int i = 0; i < attributes.size(); i++) {
							if (attributes.get(i).getName().equals(columnName)) {
								attribute = attributes.get(i);
								break;
							}
						}
						EntityKey key = null;
						for (int i = 0; i < keys.size(); i++) {
							if (keys.get(i).getName().equals(pkName)) {
								key = keys.get(i);
								break;
							}
						}
						if (key == null) {
							key = dataFactory.createEntityKey();
							key.eSetContainer(entity);
							keys.add(key);
							key.setPrimaryKey(true);
							key.setName(pkName);
							key.setUnique(true);
							key.setDocumentation("Model generated from Key '" + pkName + "'");
						}
						key.getAttributes().add(attribute);
					}
					pkRs.close();
					
					ResultSet indexesRs = dbmd.getIndexInfo(catalogName, schemaName, tableName, false, false);
					while (indexesRs.next()) {
						boolean unique = !(Boolean) indexesRs.getObject("non_unique");
						String indexName = (String) indexesRs.getObject("index_name");
						String columnName = (String) indexesRs.getObject("column_name");
						EntityAttribute attribute = null;
						for (int i = 0; i < attributes.size(); i++) {
							if (attributes.get(i).getName().equals(columnName)) {
								attribute = attributes.get(i);
								break;
							}
						}
						if (attribute != null) {
							EntityKey key = null;
							for (int i = 0; i < keys.size(); i++) {
								if (keys.get(i).getName().equals(indexName)) {
									key = keys.get(i);
									break;
								}
							}
							if (key == null) {
								key = dataFactory.createEntityKey();
								key.eSetContainer(entity);
								keys.add(key);
								key.setPrimaryKey(false);
								key.setName(indexName);
								key.setUnique(unique);
								key.setDocumentation("Model generated from Key '" + indexName + "'");
							}
							key.getAttributes().add(attribute);
						}
					}
					indexesRs.close();
				}
				rs.close();
			}
			
			// Get references
			for (String schemaName : schemaNames) {
				rs = dbmd.getTables(catalogName, schemaName, null, new String[] {
						"TABLE",
						"FOREIGN TABLE",
						"VIEW",
						"MATERIALIZED VIEW"});
				
				while (rs.next()) {
					String tableName = (String) rs.getObject("table_name");
					
					Entity entity = null;
					for (int i = 0; i < entities.size(); i++) {
						if (entities.get(i).getSchema().equals(schemaName)
								&& entities.get(i).getName().equals(tableName)) {
							entity = entities.get(i);
							break;
						}
					}
					
					List<EntityReference> references = entity.getReferences();
					List<EntityAttribute> attributes = entity.getAttributes();
					
					ResultSet importedRs = dbmd.getImportedKeys(catalogName, schemaName, tableName);
					while (importedRs.next()) {
						String pkTableSchema = (String) importedRs.getObject("pktable_schem");
						String pkTableName = (String) importedRs.getObject("pktable_name");
						String fkColumnName = (String) importedRs.getObject("fkcolumn_name");
						String fkName = (String) importedRs.getObject("fk_name");
						String pkName = (String) importedRs.getObject("pk_name");
						
						EntityReference reference = null;
						for (int i = 0; i < references.size(); i++) {
							if (references.get(i).getName().equals(fkName)) {
								reference = references.get(i);
								break;
							}
						}
						if (reference == null) {
							reference = dataFactory.createEntityReference();
							reference.eSetContainer(entity);
							references.add(reference);
							reference.setName(fkName);
							reference.setName(fkName);
							reference.setDocumentation("Model generated from Reference (foreign key) '" + fkName + "'");
							Entity referencedEntity = null;
							for (int i = 0; i < entities.size(); i++) {
								if (entities.get(i).getSchema().equals(pkTableSchema)
										&& entities.get(i).getName().equals(pkTableName)) {
									referencedEntity = entities.get(i);
									break;
								}
							}
							for (int i = 0; i < referencedEntity.getKeys().size(); i++) {
								if (referencedEntity.getKeys().get(i).getName().equals(pkName)) {
									reference.setReferencedKey(referencedEntity.getKeys().get(i));
									break;
								}
							}
						}
						
						for (int i = 0; i < attributes.size(); i++) {
							if (attributes.get(i).getName().equals(fkColumnName)) {
								reference.getAttributes().add(attributes.get(i));
								break;
							}
						}
					}
				}
				rs.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return database;
	}
	
	public static void createDatabase(Database database) {
		if (database == null) {
			return;
		}
		
		Connection connection = null;
		try {
			connection = getConnection();
			
			// Create custom types
			List<CustomType> customTypes = database.getCustomTypes();
			for (int i = 0; i < customTypes.size(); i++) {
				CustomType customType = customTypes.get(i);
				StringBuffer sb = new StringBuffer("CREATE TYPE " + customType.getName() + " AS (");
				List<CustomTypeAttribute> attributes = customType.getAttributes();
				int count = 0;
				for (int j = 0; j < attributes.size(); j++) {
					CustomTypeAttribute attribute = attributes.get(j);
					if (attribute.getType() != CustomTypeAttributeType.CUSTOM_TYPE) {
						sb.append((count != 0 ? ", " : "") 
								+ attribute.getName() 
								+ " " 
								+ attribute.getType().getLiteral() 
								+ (attribute.isArray() ? "[]" : ""));
						count++;
					}
				}
				sb.append(")");
				connection.prepareStatement(sb.toString()).execute();
			}

			// Add attributes that references other types
			for (int i = 0; i < customTypes.size(); i++) {
				CustomType customType = customTypes.get(i);
				List<CustomTypeAttribute> attributes = customType.getAttributes();
				for (int j = 0; j < attributes.size(); j++) {
					CustomTypeAttribute attribute = attributes.get(j);
					if (attribute.getType() == CustomTypeAttributeType.CUSTOM_TYPE) {
						StringBuffer sb = new StringBuffer("ALTER TYPE ")
								.append(customType.getName())
								.append(" ADD ATTRIBUTE ")
								.append(attribute.getName() + " ")
								.append(attribute.getCustomType().getName())
								.append(attribute.isArray() ? "[]" : "");
						logger.debug(sb.toString());
						connection.prepareStatement(sb.toString()).execute();
					}
				}
			}
			
			// Create entities (tables)
			List<Entity> entities = database.getEntities();
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				StringBuffer sb = new StringBuffer("CREATE TABLE " 
						+ entity.getName() 
						+ " (");
				List<EntityAttribute> attributes = entity.getAttributes();
				for (int j = 0; j < attributes.size(); j++) {
					EntityAttribute attribute = attributes.get(j);
					if (attribute.getCustomType() != null) {
						sb.append((j != 0 ? ", " : "") 
								+ attribute.getName() 
								+ " " 
								+ attribute.getCustomType().getName() 
								+ (attribute.isArray() ? "[]" : "") 
								+ (attribute.isRequired() ? " NOT NULL" : "") 
								+ (attribute.getDefaultValue() != null ? " DEFAULT " + attribute.getDefaultValue() : ""));
					}
					else {
						sb.append((j != 0 ? ", " : "") 
								+ attribute.getName() 
								+ " " 
								+ attribute.getType().getLiteral() 
								+ (attribute.isArray() ? "[]" : "") 
								+ (attribute.isRequired() ? " NOT NULL" : "") 
								+ (attribute.getDefaultValue() != null
										&& attribute.getType() != EntityAttributeType.BIGSERIAL
										&& attribute.getType() != EntityAttributeType.SERIAL
										&& attribute.getType() != EntityAttributeType.SMALLSERIAL ? " DEFAULT " + attribute.getDefaultValue() : ""));
					}
				}
				sb.append(")");
				logger.debug(sb.toString());
				connection.prepareStatement(sb.toString()).execute();
			}
			
			// Create keys (primary keys & others)
			entities = database.getEntities();
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				for (int j = 0; j < entity.getKeys().size(); j++) {
					EntityKey key = entity.getKeys().get(j);
					
					StringBuffer sb = new StringBuffer();
					
					if (key.isPrimaryKey()) {
						sb.append("ALTER TABLE " 
								+ entity.getName() 
								+ " ADD PRIMARY KEY (");
						for (int k = 0; k < key.getAttributes().size(); k++) {
							EntityAttribute attribute = key.getAttributes().get(k);
							sb.append((k != 0 ? ", " : "")
									+ attribute.getName()); 
						}
						sb.append(")");
					}
					else {
						sb.append("CREATE "
								+ (key.isUnique() ? "UNIQUE " : "")
								+ "INDEX "
								+ key.getName()
								+ " ON "
								+ entity.getName()
								+ "(");
						for (int k = 0; k < key.getAttributes().size(); k++) {
							EntityAttribute attribute = key.getAttributes().get(k);
							sb.append((k != 0 ? ", " : "")
									+ attribute.getName()); 
						}
						sb.append(")");
					}
					logger.debug(sb.toString());
					connection.prepareStatement(sb.toString()).execute();
				}
			}
			
			// Create references (constraints)
			entities = database.getEntities();
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				for (int j = 0; j < entity.getReferences().size(); j++) {
					EntityReference reference = entity.getReferences().get(j);
					
					/*
					ALTER TABLE customer_ 
					  ADD CONSTRAINT customer_gender_
					  FOREIGN KEY (gender_) REFERENCES gender_(_id_)
					  DEFERRABLE;
					  */
					
					StringBuffer sb = new StringBuffer();
					
					sb.append("ALTER TABLE " 
							+ entity.getName() 
							+ " ADD CONSTRAINT "
							+ reference.getName()
							+ " FOREIGN KEY (");
					
					for (int k = 0; k < reference.getAttributes().size(); k++) {
						EntityAttribute attribute = reference.getAttributes().get(k);
						sb.append((k != 0 ? ", " : "")
								+ attribute.getName()); 
					}
					sb.append(") REFERENCES "
							+ ((Entity) reference.getReferencedKey().eContainer()).getName()
							+ "(");
					for (int k = 0; k < reference.getReferencedKey().getAttributes().size(); k++) {
						EntityAttribute attribute = reference.getReferencedKey().getAttributes().get(k);
						sb.append((k != 0 ? ", " : "")
								+ attribute.getName()); 
					}
					sb.append(") DEFERRABLE");
					logger.debug(sb.toString());
					connection.prepareStatement(sb.toString()).execute();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		/*
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(DataPackage.eNS_URI, DataPackage.eINSTANCE);
		
		Database database = getDatabaseModel(
				Helper.getConfigValue(Helper.NILE_MASTER_DB_HOST, "localhost"), 
				Integer.parseInt(Helper.getConfigValue(Helper.NILE_MASTER_DB_PORT, "5432")), 
				Helper.getConfigValue(Helper.NILE_DB_NAME, "backoffice"));
		
		for (int i = 0; i < database.getSchemas().size(); i++) {
			Schema schema = database.getSchemas().get(i);
			XMIResourceImpl resource = (XMIResourceImpl) resourceSet.createResource(URI.createURI(schema.getName() + ".xmi"));
			resource.setEncoding("UTF-8");
			resource.getContents().add(schema);
		}
		
		// Save schemas (there is only one tentatively)
		EcoreUtil.resolveAll(resourceSet);
		for (int i = 0; i < resourceSet.getResources().size(); i++) {
			XMIResourceImpl resource = (XMIResourceImpl) resourceSet.getResources().get(i);
			FileOutputStream fos = new FileOutputStream(new File("models/db", resource.getURI().toString()));
			resource.save(fos, null);
			fos.close();
		}
		
		// Save database model
		XMIResourceImpl resource = (XMIResourceImpl) resourceSet.createResource(URI.createURI("database.xmi"));
		resource.setEncoding("UTF-8");
		resource.getContents().add(database);
		
		Map<Object, Object> options = resource.getDefaultSaveOptions();
		//options.put(XMIResourceImpl.OPTION_SCHEMA_LOCATION, true); // Esto no funciona ni de coña, ni extendedmetadata, ni leches
		
		FileOutputStream fos = new FileOutputStream("models/db/database.xmi");
		resource.save(fos, options);
		*/

		
		/*
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(DataPackage.eNS_URI, DataPackage.eINSTANCE);

		File modelFolder = new File("models/db");
		String[] files = modelFolder.list();
		for (int i = 0; i < files.length; i++) {
			File file = new File("models/db/" + files[i]);
			Resource resource = resourceSet.createResource(URI.createURI(file.getName()));
			((XMIResourceImpl) resource).setEncoding("UTF-8");
			FileInputStream fis = new FileInputStream(file);
			resource.load(fis, null);
		}
		EcoreUtil.resolveAll(resourceSet);

		Database database = null;
		for (int i = 0; i < resourceSet.getResources().size(); i++) {
			if (resourceSet.getResources().get(i).getContents().get(0) instanceof Database) {
				database = (Database) resourceSet.getResources().get(i).getContents().get(0);
				break;
			}
		}
		logger.debug(database);
		*/
	}
}
