package com.HOIIVUtils.hoi4utils.clausewitz_code.modifier;

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
			connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/" + databaseName);
			createTable();
			loadModifiers();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ModifierDatabase() {
		this("modifiers.db");
//		for(Modifier modifier : Modifier.modifiers.values()) {
//			System.out.println("modifier: " + modifier.identifier());
//		}
	}

//	public static void main(String[] args) {
//		ModifierDatabase modifierDB = new ModifierDatabase("modifiers.db");
//
//		// Insert modifiers
////		modifierDB.insertModifier("modifier1", "good", "percentage", 1, "none", "aggressive");
////		modifierDB.insertModifier("modifier2", "bad", "number", 2, "daily", "defensive");
//
//		// Retrieve and use modifiers
////		modifierDB.loadModifiers();
//
//		// temp
//		for(Modifier modifier : Modifier.modifiers.values()) {
//			modifierDB.insertModifier(modifier);
//		}
//
//		// Close the database connection
//		modifierDB.close();
//	}

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

//	public void retrieveModifiers() {
//		String retrieveSQL = "SELECT * FROM modifiers";
//		try {
//			PreparedStatement retrieveStatement = connection.prepareStatement(retrieveSQL);
//			ResultSet resultSet = retrieveStatement.executeQuery();
//			while (resultSet.next()) {
//				int id = resultSet.getInt("id");
//				String identifier = resultSet.getString("identifier");
//				String colorType = resultSet.getString("color_type");
//				String valueType = resultSet.getString("value_type");
//				int precision = resultSet.getInt("precision");
//				String postfix = resultSet.getString("postfix");
//				String category = resultSet.getString("category");
//
//				// Do something with the retrieved modifier data (e.g., store in a data structure)
//				System.out.println("ID: " + id + ", Identifier: " + identifier + ", Color Type: " + colorType);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

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

