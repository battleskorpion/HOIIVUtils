package com.HOIIVUtils.hoi4utils.clausewitz_code.modifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModifierDatabase {

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Connection connection;

	public ModifierDatabase(String databaseName) {
		// todo
		try {
			byte[] dbBytes = readDatabaseAsByteArray(databaseName);
			connection = DriverManager.getConnection("jdbc:sqlite::memory:");
			loadDatabase(dbBytes);
			createTable();
			loadModifiers();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	public ModifierDatabase() {
		this("databases/modifiers.db");
//		for(Modifier modifier : Modifier.modifiers.values()) {
//			System.out.println("modifier: " + modifier.identifier());
//		}
	}

	private byte[] readDatabaseAsByteArray(String resourcePath) throws IOException {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
		     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			if (inputStream == null) {
				throw new IOException("Resource not found: " + resourcePath);
			}

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			return outputStream.toByteArray();
		}
	}

	private void loadDatabase(byte[] dbBytes) throws SQLException {
		String sql = "RESTORE FROM MEMORY";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setBytes(1, dbBytes);
			statement.executeUpdate();
		}
	}

	private void createTable() {
		String createTableSQL = "CREATE TABLE IF NOT EXISTS modifiers (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"identifier TEXT," +
				"color_type TEXT," +
				"value_type TEXT," +
				"precision INTEGER," +
				"postfix TEXT," +
				"category TEXT" +
				")";
		try {
			PreparedStatement createTable = connection.prepareStatement(createTableSQL);
			createTable.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertModifier(String identifier, String colorType, String valueType, int precision, String postfix, String category) {
		String insertSQL = "INSERT INTO modifiers " +
				"(identifier, color_type, value_type, precision, postfix, category) VALUES (?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
			insertStatement.setString(1, identifier);
			insertStatement.setString(2, colorType);
			insertStatement.setString(3, valueType);
			insertStatement.setInt(4, precision);
			insertStatement.setString(5, postfix);
			insertStatement.setString(6, category);
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertModifier(Modifier modifier) {
		insertModifier(modifier.identifier(), modifier.colorType().name(), modifier.valueType().name(), modifier.precision,
				modifier.postfix().name(), modifier.category().stream().toList().get(0).toString());
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Modifier> loadModifiers() {
		List<Modifier> loadedModifiers = new ArrayList<>();
		String retrieveSQL = "SELECT * FROM modifiers";
		try {
			PreparedStatement retrieveStatement = connection.prepareStatement(retrieveSQL);
			ResultSet resultSet = retrieveStatement.executeQuery();
			while (resultSet.next()) {
				String identifier = resultSet.getString("identifier");
				String colorType = resultSet.getString("color_type");
				String valueType = resultSet.getString("value_type");
				int precision = resultSet.getInt("precision");
				String postfix = resultSet.getString("postfix");
				String category = resultSet.getString("category");

				// Create a Modifier instance and add it to the loaded list
				Modifier modifier = new Modifier(identifier, Modifier.ColorType.valueOf(colorType), Modifier.ValueType.valueOf(valueType), precision, Modifier.ValuePostfix.valueOf(postfix), ModifierCategory.valueOf(category));
				loadedModifiers.add(modifier);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return loadedModifiers;
	}
}
