package com.HOIIVUtils.hoi4utils.clausewitz_code.effect;

import com.HOIIVUtils.hoi4utils.clausewitz_code.scope.ScopeType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Connection connection;

	public EffectDatabase(String databaseName) {
		try {
			URL url = getClass().getClassLoader().getResource(databaseName);
			if (url == null) {
				throw new SQLException("Unable to find '" + databaseName + "'");
			}

			File tempFile = File.createTempFile("effects", ".db");
			tempFile.deleteOnExit();
			try (InputStream inputStream = url.openStream()) {
				Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			connection = DriverManager.getConnection("jdbc:sqlite:" + tempFile.getAbsolutePath());
			loadEffects();
		} catch (IOException | SQLException e) {
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
				String requiredParametersFull_str = resultSet.getString("required_parameters_full");
				String requiredParametersSimple_str = resultSet.getString("required_parameters_simple");
				String optionalParameters_str = resultSet.getString("optional_parameters");

				EnumSet<ScopeType> supportedScopes = parseEnumSet(supportedScopes_str);
				EnumSet<ScopeType> supportedTargets = parseEnumSet(supportedTargets_str);

				/* required parameters */
				List<Parameter> requiredParameters = new ArrayList<>();
				//Parameter.addValidIdentifier();
				if (requiredParametersFull_str == null || requiredParametersFull_str.isEmpty() || requiredParametersFull_str.equals("none")) {
					//
				} else {
					String[] parameters_str = requiredParametersFull_str.split(", ");

					/* single parameter */
					if (parameters_str.length == 1) {
						//String[] args = parameters_str[0].split(" ");
						String[] args = parameters_str[0].split("\\s(?![^<>]*>)");
						if (ParameterValueType.isParameterValueType(args[0])) {
							ParameterValueType pValueType = ParameterValueType.scope;
							try {
								requiredParameters.add(new Parameter(args[0], pValueType));
							} catch (NullParameterTypeException e) {
								System.out.println("null param exception: " + Arrays.toString(args));
								throw new RuntimeException(e);
							}
						} else if (args[0].contains("<") || args[0].contains(">")) {
							// err
							throw new RuntimeException("Invalid effects database element: not recognized parameter value type: " + args[0]);
						} else {
							// else is identifier (and parameter value type)
							// FIXME
							//Parameter.addValidParameter(parameters_str[0]);
							if (args.length < 2) {
								System.out.println(Arrays.toString(args));
							}
							ParameterValueType pValueType = ParameterValueType.of(args[1]);
							try {
								requiredParameters.add(new Parameter(parameters_str[0], pValueType));
							} catch (NullParameterTypeException e) {
								System.out.println("null param exception: " + Arrays.toString(parameters_str));
								throw new RuntimeException(e);
							}
						}
					}
					/* multi parameter */
					else {
						String parameter_str;
						for (int i = 0; i < parameters_str.length; i++) {
							parameter_str = parameters_str[i];
							String[] args = parameter_str.split("\\s(?![^<>]*>)");
							if (args[0].contains("<") || args[0].contains(">")) {
								// err
								throw new RuntimeException("Invalid effects database element: invalid placement of parameter value type: " + args[0]);
							}
//							Parameter.addValidParameter(args[0]);
							if (args.length < 2) {
								System.out.println(Arrays.toString(args));
							}
							ParameterValueType pValueType = ParameterValueType.of(args[1]);
							try {
								requiredParameters.add(new Parameter(args[0], pValueType));
							} catch (NullParameterTypeException e) {
								System.out.println("null param exception: " + Arrays.toString(args));
								throw new RuntimeException(e);
							}

//							for (int j = 1; j < args.length; j++) {
//								//if (args[j])
//							}
						}
					}
				}

				/* optional parameters */

				// Create a Effect instance and add it to the loaded list
				Effect effect;
				if (supportedTargets == null) {
					effect = new Effect(identifier, supportedScopes, requiredParameters);
				} else {
					effect = new Effect(identifier, supportedScopes, supportedTargets, requiredParameters);
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
