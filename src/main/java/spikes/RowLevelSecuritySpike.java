package spikes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Random;

/**
 * Architectural spike to see if it is feasible to have a database ROLE per web user
 * in an environment with millions of users, hundreds of millions of orders, ...
 *
 * The test consists of loading millions of users, orders and multiple lines per order
 * then make queries of the orders of each user protected by a PostgreSQL policy.
 *
 * Each user can only see their orders. The inheritance of ROLES is also tested, 
 * creating a "client" group in which filtering policies are created.
 *
 * In this way, the security solution is simplified, moving to the database engine. According
 * to the philosophy centered on data, security should be in the same place as the data.
 *
 * Specifically:
 * 
 * Data Access and data security is the responsibility of the data layer and 
 * it is not managed by the applications.
 *
 * The result of the test is totally satisfactory.
 *
 * Conclusion: Move the security rules to the database to simplify the rest of the layers of the
 * architecture, there is no filter that makes additional security filters in the services.
 *
 * SET ROLE user234234;
 * SELECT * FROM order_;
 *
 */
public class RowLevelSecuritySpike {
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		final int NUMBER_OF_USERS = 1000; //10000000;
		final int NUMBER_OF_ORDERS = 10000; //100000000;
		final int NUMBER_OF_ORDER_LINES = 20000; //200000000;
		
		final int NUMBER_OF_REQUESTS = 10000;
		
		Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/test_rls");
		connection.setAutoCommit(false);
		
		Statement statement = connection.createStatement();
		
		Random random = new Random();
		
		/*
		statement.execute("CREATE ROLE customer");
		connection.commit();
		*/
		
		/*
		for (int i = 0; i < NUMBER_OF_USERS; i++) {
			statement.execute("DROP ROLE user" + i);
			statement.execute("CREATE ROLE user" + i + " PASSWORD 'user" + i + "'");
			statement.execute("GRANT customer TO user" + i);
			
			if (i % 1000 == 0) {
				System.out.println("Users: " + i);
				connection.commit();
			}
		}
		System.out.println("Users: " + NUMBER_OF_USERS);
		connection.commit();
		*/
		
		/*
		statement.execute("DROP TABLE IF EXISTS \"order_line\"");
		statement.execute("DROP TABLE IF EXISTS \"order\"");
		connection.commit();
		
		statement.execute("CREATE TABLE \"order\" (\"id\" SERIAL NOT NULL PRIMARY KEY, \"user\" VARCHAR, \"total\" DOUBLE PRECISION)");
		statement.execute("CREATE TABLE \"order_line\" (\"id\" SERIAL NOT NULL PRIMARY KEY, \"order\" INTEGER REFERENCES \"order\"(\"id\"), \"description\" VARCHAR)");
		
		statement.execute("CREATE INDEX idx1 ON \"order\"(\"user\")");
		statement.execute("CREATE INDEX idx2 ON \"order_line\"(\"order\")");
		
		statement.execute("GRANT ALL ON ALL TABLES IN SCHEMA public TO customer");
		statement.execute("GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO customer");
		
		statement.execute("ALTER TABLE \"order\" ENABLE ROW LEVEL SECURITY");
		statement.execute("ALTER TABLE \"order_line\" ENABLE ROW LEVEL SECURITY");
		
		statement.execute("CREATE POLICY p1 ON \"order\" FOR ALL TO customer USING (\"user\" = current_user::text)");
		statement.execute("CREATE POLICY p1 ON \"order_line\" FOR ALL TO customer USING (\"order\" IN (SELECT \"id\" FROM \"order\" WHERE \"user\" = current_user::text))");
		
		connection.commit();
		
		PreparedStatement ps = connection.prepareStatement("INSERT INTO \"order\"(\"user\", \"total\") VALUES (?, ?)");
		for (int i = 0; i < NUMBER_OF_ORDERS; i++) {
			
			int userNumber = Math.abs(random.nextInt()) % NUMBER_OF_USERS;
			double total = Math.abs(random.nextDouble() * 1000);
			
			ps.setString(1, "user" + userNumber);
			ps.setDouble(2, total);
			ps.execute();
			
			if (i % 1000 == 0) {
				connection.commit();
				System.out.println("Orders: " + i);
			}
		}
		System.out.println("Orders: " + NUMBER_OF_ORDERS);
		ps.close();
		connection.commit();
		
		ps = connection.prepareStatement("INSERT INTO \"order_line\"(\"order\", \"description\") VALUES (?, ?)");
		for (int i = 0; i < NUMBER_OF_ORDER_LINES; i++) {
			
			int orderId = (Math.abs(random.nextInt()) % NUMBER_OF_ORDERS) + 1;
			
			ps.setInt(1, orderId);
			ps.setString(2, "description" + i);
			ps.execute();
			
			if (i % 1000 == 0) {
				connection.commit();
				System.out.println("Lines: " + i);
			}
		}
		System.out.println("Lines: " + NUMBER_OF_ORDER_LINES);
		ps.close();
		connection.commit();
		*/
		
		// Performance test
		
		connection.setAutoCommit(true);
		long time1 = new Date().getTime();
		
		PreparedStatement ps = connection.prepareStatement("SELECT count(DISTINCT \"o\".\"id\") AS \"orders\", count(DISTINCT \"ol\".\"id\") AS \"lines\" FROM \"order\" \"o\" LEFT JOIN \"order_line\" \"ol\" ON \"o\".\"id\" = \"ol\".\"order\"");
		//ps = connection.prepareStatement("SELECT count(DISTINCT o.id) AS \"orders\", 0 AS lines FROM \"order\" o");
		//ps = connection.prepareStatement("SELECT id AS \"orders\", 0 AS lines FROM \"order\" o");
		
		for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
			
			if (i % 1000 == 0) {
				System.out.println("Requests: " + i);
			}
			
			int userNumber = Math.abs(random.nextInt()) % NUMBER_OF_USERS;
			statement.execute("SET ROLE user" + userNumber);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				int orders = rs.getInt("orders");
				int lines = rs.getInt("lines");
				//System.out.println(orders + ", " + lines);
			}
			rs.close();
		}
		long time2 = new Date().getTime();
		ps.close();
		System.out.println("Requests: " + NUMBER_OF_REQUESTS);
		
		System.out.println("Requests/second: " + (NUMBER_OF_REQUESTS * 1000 / (time2 - time1)));
		statement.close();
		connection.close();
	}
}
