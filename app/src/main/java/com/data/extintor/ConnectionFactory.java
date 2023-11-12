package com.data.extintor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

	private final ConnectionConfig template;

	public ConnectionFactory(ConnectionConfig template) {
		this.template = template;
	}

	public Connection getConnection() {
		try {
			Class.forName(template.getDriverClass());
			String jdbcUrl =
					"jdbc:mysql://"
							+ template.getHost()
							+ ":"
							+ template.getPort()
							+ "/"
							+ template.getDatabase();
			return DriverManager.getConnection(jdbcUrl, template.getUsername(), template.getPassword());
		}
		catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException("Error obtaining database connection", e);
		}
	}
}
