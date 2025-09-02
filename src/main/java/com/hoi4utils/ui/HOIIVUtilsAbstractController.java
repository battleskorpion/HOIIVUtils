package com.hoi4utils.ui;

import com.hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class HOIIVUtilsAbstractController implements JavaFXUIManager {
	public static final Logger logger = LogManager.getLogger(HOIIVUtilsAbstractController.class);
	private String fxmlResource;
	private String title;

	/**
	 * Opens the stage with the specified FXML resource and title.
	 */
	@Override
	public void open() {
		try {
			FXMLLoader fxml = new FXMLLoader(getClass().getResource(fxmlResource));
			open(fxml);
			logger.debug("{} Started", title);
		} catch (IOException e) {
			String errorMessage = "Failed to open window\nError loading FXML: " + fxmlResource + " Title: " + title;
			logger.error("Error loading FXML: {}\n Title: {}", fxmlResource, title, e);
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(errorMessage, e);
		}
	}

	/**
	 * Opens the stage with optional initialization arguments.
	 *
	 * @param initargs the initialization arguments for the controller
	 */
	public void open(Object... initargs) {
		try {
			FXMLLoader fxml = new FXMLLoader(getClass().getResource(fxmlResource));
			fxml.setControllerFactory(c -> findMatchingConstructor(initargs));
			open(fxml);
			logger.debug("{} Started with arguments {}", title, initargs);
		} catch (IOException e) {
			String errorMessage = "Failed to open window\nError loading FXML: " + fxmlResource + " Title: " + title;
			logger.error("Error loading FXML: {}\n Title: {}", fxmlResource, title, e);
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(errorMessage, e);
		}
	}

	public void open(FXMLLoader fxml) throws IOException {
		Parent root = fxml.load();
		Scene scene = new Scene(root);
		if (Objects.equals(HOIIVUtils.get("theme"), "dark")) {
			scene.getStylesheets().add("com/hoi4utils/ui/javafx_dark.css");
		} else {
			scene.getStylesheets().add("/com/hoi4utils/ui/highlight-background.css");
		}
		Stage stage = new Stage();
		stage.getIcons().addAll(
				new Image(getClass().getResourceAsStream("/icons/settings-icon-gray-gear16.png")),
				new Image(getClass().getResourceAsStream("/icons/settings-icon-gray-gear32.png")),
				new Image(getClass().getResourceAsStream("/icons/settings-icon-gray-gear48.png")),
				new Image(getClass().getResourceAsStream("/icons/settings-icon-gray-gear64.png")),
				new Image(getClass().getResourceAsStream("/icons/settings-icon-gray-gear128.png"))
		);
		stage.setScene(scene);
		stage.setTitle(title);
		decideScreen(stage);
		stage.show();
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
							newCombination.add(0, clazz);
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
