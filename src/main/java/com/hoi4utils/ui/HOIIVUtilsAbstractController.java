package com.hoi4utils.ui;

import com.hoi4utils.clausewitz.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class HOIIVUtilsAbstractController implements JavaFXUIManager {
	public static final Logger LOGGER = LogManager.getLogger(HOIIVUtilsAbstractController.class);
	private String fxmlResource;
	private String title;

	/**
	 * Opens the stage with the specified FXML resource and title.
	 */
	@Override
	public void open() {
		if (fxmlResource == null) {
			handleMissingFXMLResource();
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
			Scene scene = new Scene(loader.load());
			addSceneStylesheets(scene);
			Stage stage = createLaunchStage(scene);
			stage.show();
			LOGGER.debug("Stage created and shown: {}", title);
		} catch (IOException e) {
			handleFXMLLoadError(e);
		}
	}

	/**
	 * Opens the stage with optional initialization arguments.
	 *
	 * @param initargs the initialization arguments for the controller
	 */
	public void open(Object... initargs) {
		if (fxmlResource == null) {
			handleMissingFXMLResource();
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
			loader.setControllerFactory(c -> findMatchingConstructor(initargs));
			Scene scene = new Scene(loader.load());
			addSceneStylesheets(scene);
			Stage stage = createLaunchStage(scene);
			stage.show();
			LOGGER.debug("Stage created and shown: {}", title);
		} catch (IOException e) {
			handleFXMLLoadError(e);
		}
	}

	private void handleMissingFXMLResource() {
		String errorMessage = "Failed to open window\nError: FXML resource is null.";
		LOGGER.error(errorMessage);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void addSceneStylesheets(Scene scene) {
		if ("dark".equals(HOIIVUtils.get("theme"))) {
			scene.getStylesheets().add("com/hoi4utils/ui/javafx_dark.css");
		}
//		scene.getStylesheets().add("com/hoi4utils/ui/highlight-background.css");
	}
	
	@NotNull
	private Stage createLaunchStage(Scene scene) {
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(title);
		decideScreen(stage);

		return stage;
	}

	private void handleFXMLLoadError(IOException e) {
		String errorMessage = "Failed to open window\nError loading FXML: " + fxmlResource;
		LOGGER.fatal("Error loading FXML: {}", fxmlResource, e);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
		throw new RuntimeException(errorMessage, e);
	}

	private Object findMatchingConstructor(Object... initargs) {
		List<List<Class<?>>> ArgsClassHierarchies = new ArrayList<>(initargs.length);

		for (Object arg : initargs) {
			Set<Class<?>> superclassesAndInterfaces = new HashSet<>();
			Class<?> currentClass = arg.getClass();

			while (currentClass != null) {
				superclassesAndInterfaces.add(currentClass);
				superclassesAndInterfaces.addAll(Arrays.asList(currentClass.getInterfaces()));
				currentClass = currentClass.getSuperclass();
			}

			findSuperinterfacesRecursively(superclassesAndInterfaces);
			ArgsClassHierarchies.add(new ArrayList<>(superclassesAndInterfaces));
		}

		for (List<Class<?>> combination : generateCombinations(ArgsClassHierarchies, 0)) {
			try {
				return getClass().getConstructor(combination.toArray(new Class[0])).newInstance(initargs);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
			}
		}

		throw new RuntimeException("No suitable constructor found for arguments: " + Arrays.toString(initargs));
	}

	/**
	 * Recursively find all superinterfaces of the given classes and add them to the set.
	 *
	 * @param hierarchyAndInterfaces the set to add the superinterfaces to
	 */
	private static void findSuperinterfacesRecursively(Set<Class<?>> hierarchyAndInterfaces) {
		int prevSize;
		do {
			prevSize = hierarchyAndInterfaces.size();

			// Collect new interfaces from existing ones
			Set<Class<?>> newInterfaces = hierarchyAndInterfaces.stream()
					.filter(Class::isInterface)
					.flatMap(interf -> Arrays.stream(interf.getInterfaces()))
					.collect(Collectors.toSet());

			// Add only if new interfaces exist
			hierarchyAndInterfaces.addAll(newInterfaces);
		} while (hierarchyAndInterfaces.size() > prevSize); // Stop when no new interfaces are added
	}
	
	private List<List<Class<?>>> generateCombinations(List<List<Class<?>>> classHierarchies, int index) {
		if (index == classHierarchies.size()) {
			return List.of(new ArrayList<>()); // Base case: return a list with an empty list
		}

		List<List<Class<?>>> nextCombinations = generateCombinations(classHierarchies, index + 1);

		return classHierarchies.get(index).stream()
				.flatMap(clazz -> nextCombinations.stream()
						.map(combination -> {
							List<Class<?>> newCombination = new ArrayList<>(combination);
							newCombination.addFirst(clazz);
							return newCombination;
						}))
				.toList();
	}

	@Override
	public void setFxmlResource(String fxmlResource) {
		this.fxmlResource = fxmlResource;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	private List<Class<?>> getClassHierarchy(Class<?> clazz) {
		List<Class<?>> hierarchy = new ArrayList<>();
		while (clazz != null) {
			hierarchy.add(clazz);
			clazz = clazz.getSuperclass();
		}
		return hierarchy;
	}
}
