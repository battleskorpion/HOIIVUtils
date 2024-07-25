package com.hoi4utils.clausewitz.code.effect;

import com.hoi4utils.clausewitz.code.scope.ScopeType;
import com.hoi4utils.clausewitz.script.PDXScript;
import com.hoi4utils.clausewitz.script.StringPDX;
import com.hoi4utils.clausewitz.script.StructuredPDX;
import org.jetbrains.annotations.NotNull;

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
import java.util.*;
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

	// public static void main(String[] args) {
	// EffectDatabase effectDB = new EffectDatabase("effects.db");
	//
	// // Retrieve and use modifiers
	//// effectDatabase.loadEffects();
	// effectDB.createTable();
	//
	// // Close the database connection
	// effectDB.close();
	// }

	public EffectDatabase() {
		this("databases/effects.db");
		// for(Effect effect : Effect.effects.values()) {
		// System.out.println("effect: " + effect.identifier());
		// }
	}

	private void createTable() {
		String createTableSQL = "CREATE TABLE IF NOT EXISTS effects (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"identifier TEXT," +
				"supported_scopes TEXT," + // Add this column for supported scopes
				"supported_targets TEXT" + // Add this column for supported targets
				")";
		try {
			PreparedStatement createTable = connection.prepareStatement(createTableSQL);
			createTable.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Effect<?>> loadEffects() {
		List<Effect<?>> loadedEffects = new ArrayList<>();
		String retrieveSQL = "SELECT * FROM effects";
		try {
			PreparedStatement retrieveStatement = connection.prepareStatement(retrieveSQL);
			ResultSet resultSet = retrieveStatement.executeQuery();
			while (resultSet.next()) {
				String pdxIdentifier = resultSet.getString("identifier");
				// System.out.println("id " + identifier);
				String supportedScopes_str = resultSet.getString("supported_scopes");
				String supportedTargets_str = resultSet.getString("supported_targets");
				String requiredParametersFull_str = resultSet.getString("required_parameters_full");
				String requiredParametersSimple_str = resultSet.getString("required_parameters_simple");
				String optionalParameters_str = resultSet.getString("optional_parameters");

				EnumSet<ScopeType> supportedScopes = parseEnumSet(supportedScopes_str);
				EnumSet<ScopeType> supportedTargets = parseEnumSet(supportedTargets_str);

				/* required parameters */
				List<Parameter> requiredParameters = new ArrayList<>();
				// Parameter.addValidIdentifier();
				if (requiredParametersFull_str == null || requiredParametersFull_str.isEmpty()
						|| requiredParametersFull_str.equals("none")) {
					Effect<?> effect;
					// todo not effect<string> necessarily
					effect = new EffectSchema<>(pdxIdentifier, supportedScopes, supportedTargets,
							StringPDX::new, null);
					loadedEffects.add(effect);
				} else {
					String[] alternateParameters = requiredParametersFull_str.split("\\s+\\|\\s+");
					for (String alternateParameter : alternateParameters) {
						String[] parametersStrlist = alternateParameter.split("\\s+,\\s+");
						List<? extends PDXScript<?>> childScripts = new ArrayList<>();
						for (int i = 0; i < parametersStrlist.length; i++) {
							String parameterStr = parametersStrlist[i];
							var data = parameterStr.splitWithDelimiters("(<[a-z_-]+>|\\|)", -1);
							data = Arrays.stream(data).filter(s -> !s.isEmpty()).toArray(String[]::new);
							if (data.length >= 2) {
								var paramIdentifierStr = data[0].trim();
								var paramTypeStr = data[1].trim();
								var paramValueType = ParameterValueType.of(paramTypeStr);

							} else {
								throw new InvalidParameterException("Invalid parameter definition: " + parameterStr);
							}
							if (data.length >= 3) {
								// idk
							}
						}
						var structuredEffectBlock = newStructuredEffectBlock(pdxIdentifier, childScripts);
						/* Create a Effect instance and add it to the loaded list */
						Effect<?> effect;
						if (supportedTargets == null) {
							effect = new Effect<>(pdxIdentifier, supportedScopes, null,
									StringPDX::new, structuredEffectBlock);
						} else {
							effect = new Effect<>(pdxIdentifier, supportedScopes, supportedTargets,
									StringPDX::new, structuredEffectBlock);
						}
						loadedEffects.add(effect);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return loadedEffects;
	}

	@NotNull
	private static StructuredPDX newStructuredEffectBlock(String pdxIdentifier, List<? extends PDXScript<?>> childScripts) {
		return new StructuredPDX(pdxIdentifier) {
			@Override
			protected Collection<? extends PDXScript<?>> childScripts() {
				return childScripts;
			}

			@Override
			public boolean objEquals(PDXScript<?> other) {
				// todo
				return false;
			}
		};
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
