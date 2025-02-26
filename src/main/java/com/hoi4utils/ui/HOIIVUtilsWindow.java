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

public abstract class HOIIVUtilsWindow implements FXWindow {
	public static final Logger LOGGER = LogManager.getLogger(HOIIVUtilsWindow.class);
	private String fxmlResource;
	private String title;
	protected Stage stage;
	protected FXMLLoader loader;

	/**
	 * Opens the stage. If the stage is not null, it shows the stage. If the fxmlResource is null, it
	 * prints an error message and calls the openError method. Otherwise, it creates a new FXMLLoader,
	 * loads the fxml file, creates a new Scene, sets the stylesheets, creates a new Stage, sets the
	 * scene and title, calls the decideScreen method, shows the stage, sets the loader and stage
	 * variables, and prints a success message.
	 */
	@Override
	public void open() {
		if (stage != null) {
			showExistingStage();
			return;
		}

		if (fxmlResource == null) {
			handleMissingFXMLResource();
			return;
		}

		try {
			Parent root = loadFXML();
			setupAndShowStage(root);
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
		if (stage != null) {
			showExistingStage();
			return;
		}

		if (fxmlResource == null) {
			handleMissingFXMLResource();
			return;
		}

		try {
			FXMLLoader launchLoader = createFXMLLoaderWithArgs(initargs);
			Parent root = loadFXML(launchLoader);
			setupAndShowStage(root, launchLoader);
		} catch (IOException e) {
			handleFXMLLoadError(e);
		}
	}

	private void showExistingStage() {
		stage.show();
		LOGGER.info("Stage already exists, showing: {}", title);
	}

	private void handleMissingFXMLResource() {
		String errorMessage = "Failed to open window\nError: FXML resource is null.";
		LOGGER.error(errorMessage);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private Parent loadFXML() throws IOException {
		FXMLLoader launchLoader = new FXMLLoader(getClass().getResource(fxmlResource));
		try {
			return launchLoader.load();
		} catch (IOException e) {
			throw new IOException("Failed to load FXML: " + fxmlResource, e);
		}
	}
	
	private Parent loadFXML(FXMLLoader loader) throws IOException {
		return loader.load();
	}

	private void setupAndShowStage(Parent root) {
		Scene scene = new Scene(root);
		addSceneStylesheets(scene);
		this.stage = createLaunchStage(scene);
		LOGGER.debug("Stage created and shown: {}", title);
	}
	
	private void setupAndShowStage(Parent root, FXMLLoader loader) {
		Scene scene = new Scene(root);
		addSceneStylesheets(scene);
		this.loader = loader;
		this.stage = createLaunchStage(scene);
		LOGGER.debug("Stage created and shown: {}", title);
	}

	private void addSceneStylesheets(Scene scene) {
		scene.getStylesheets().add("com/hoi4utils/ui/javafx_dark.css");

		try {
			scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/hoi4utils/ui/highlight-background.css")).toExternalForm());
		} catch (NullPointerException e) {
			System.err.println("Warning: Stylesheet 'highlight-background.css' not found!");
		}
	}
	
	@NotNull
	private Stage createLaunchStage(Scene scene) {
		Optional.ofNullable(stage).ifPresent(Stage::close);

		Stage launchStage = new Stage();
		launchStage.setScene(scene);
		launchStage.setTitle(title);
		decideScreen(launchStage);
		launchStage.show();

		return launchStage;
	}

	private void handleFXMLLoadError(IOException e) {
		String errorMessage = "Failed to open window\nError loading FXML: " + fxmlResource;
		LOGGER.fatal("Error loading FXML: {}", fxmlResource, e);
		JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
		throw new RuntimeException(errorMessage, e);
	}

	private FXMLLoader createFXMLLoaderWithArgs(Object... initargs) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
		loader.setControllerFactory(c -> findMatchingConstructor(initargs));
		return loader;
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

	// TODO move to a more appropriate class or delete
//	private List<Class<?>> getClassHierarchy(Class<?> clazz) {
//		List<Class<?>> hierarchy = new ArrayList<>();
//		while (clazz != null) {
//			hierarchy.add(clazz);
//			clazz = clazz.getSuperclass();
//		}
//		return hierarchy;
//	}
}
