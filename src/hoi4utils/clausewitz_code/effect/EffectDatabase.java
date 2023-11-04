package hoi4utils.clausewitz_code.effect;

import hoi4utils.clausewitz_code.scope.ScopeType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class EffectDatabase {
	private Connection connection;

	public EffectDatabase(String databaseName) {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:resources/" + databaseName);
			loadEffects();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	public static void main(String[] args) {
//		EffectDatabase effectDB = new EffectDatabase("effects.db");
//
//		// Retrieve and use modifiers
////		effectDatabase.loadEffects();
//		effectDB.createTable();
//
//		// Close the database connection
//		effectDB.close();
//	}

	public EffectDatabase() {
		this("effects.db");
//		for(Effect effect : Effect.effects.values()) {
//			System.out.println("effect: " + effect.identifier());
//		}
	}

	private void createTable() {
		String createTableSQL = "CREATE TABLE IF NOT EXISTS effects (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"identifier TEXT," +
				"supported_scopes TEXT," + // Add this column for supported scopes
				"supported_targets TEXT" +  // Add this column for supported targets
				")";
		try {
			PreparedStatement createTable = connection.prepareStatement(createTableSQL);
			createTable.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Effect> loadEffects() {
		List<Effect> loadedEffects = new ArrayList<>();
		String retrieveSQL = "SELECT * FROM effects";
		try {
			PreparedStatement retrieveStatement = connection.prepareStatement(retrieveSQL);
			ResultSet resultSet = retrieveStatement.executeQuery();
			while (resultSet.next()) {
				String identifier = resultSet.getString("identifier");
//				System.out.println("id " + identifier);
				String supportedScopes_str = resultSet.getString("supported_scopes");
				String supportedTargets_str = resultSet.getString("supported_targets");

				EnumSet<ScopeType> supportedScopes = parseEnumSet(supportedScopes_str);
				EnumSet<ScopeType> supportedTargets = parseEnumSet(supportedTargets_str);

				// Create a Modifier instance and add it to the loaded list
				Effect effect;
				if (supportedTargets == null) {
					effect = new Effect(identifier, supportedScopes);
				} else {
					effect = new Effect(identifier, supportedScopes, supportedTargets);
				}
				loadedEffects.add(effect);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return loadedEffects;
	}

	private EnumSet<ScopeType> parseEnumSet(String enumSetString) {
		if (enumSetString == null || enumSetString.isEmpty() || enumSetString.equals("none")) {
			return null;
		} else {
			String[] enumValues = enumSetString.split(", ");
			return Arrays.stream(enumValues)
					.map(ScopeType::valueFromString)
					.collect(Collectors.toCollection(() -> EnumSet.noneOf(ScopeType.class)));
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
