package com.hoi4utils.ui.javafx.application;

import com.hoi4utils.main.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class HOIIVUtilsAbstractController implements JavaFXUIManager {
	protected static final Logger logger = LogManager.getLogger(HOIIVUtilsAbstractController.class);
	protected  String fxmlFile;
	protected  String title;
	private Object[] initargs;
	private final String darkCss = "com/hoi4utils/ui/css/javafx_dark.css";
	private final String lightCss = "com/hoi4utils/ui/css/light.css";
	private final List<Image> icons = List.of(
			new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/settings-icon-gray-gear16.png"))),
			new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/settings-icon-gray-gear32.png"))),
			new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/settings-icon-gray-gear48.png"))),
			new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/settings-icon-gray-gear64.png"))),
			new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/settings-icon-gray-gear128.png")))
	);
	private Stage stage;
	private Scene scene;

	/**
	 * Opens the stage with the specified FXML resource and title.
	 */
	@Override
	public void open() {
		try {
			FXMLLoader fxml = new FXMLLoader(getClass().getResource(fxmlFile));
			if (initargs != null) {
				fxml.setControllerFactory(c -> findMatchingConstructor(initargs));
			}
			Parent root = fxml.load();
			Scene scene = new Scene(root);
			this.scene = scene;
			if (Objects.equals(HOIIVUtils.get("theme"), "dark")) {
				scene.getStylesheets().add(darkCss);
			} else {
				scene.getStylesheets().add(lightCss);
			}
			Stage stage = new Stage();
			this.stage = stage;
			stage.getIcons().addAll(icons);
			stage.setScene(scene);
			stage.setTitle(title);
			decideScreen(stage);
			stage.show();
		} catch (IOException e) {
			handleJavaFXIOException(e);
		}
	}

	/**
	 * Opens the stage with optional initialization arguments.
	 *
	 * @param initargs the initialization arguments for the controller
	 */
	public void open(Object... initargs) {
		this.initargs = initargs;
		open();
	}

	private void handleJavaFXIOException(IOException e) {
		String errorMessage = "\nFailed to open window:\nError loading FXML: " + fxmlFile + "\nTitle: " + title + "\n Error: " + e + "\n" + "Error cause: " + e.getCause().getMessage() + "StackTrace: ";
		logger.error(errorMessage);
		e.printStackTrace();
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
	public void setFxmlFile(String fxmlFile) {
		this.fxmlFile = fxmlFile;
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
