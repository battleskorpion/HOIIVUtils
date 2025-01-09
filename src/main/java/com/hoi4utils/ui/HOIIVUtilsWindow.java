package com.hoi4utils.ui;

import com.hoi4utils.clausewitz.HOIIVUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class HOIIVUtilsWindow implements FXWindow {
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
			stage.show();
			System.out.println("HOIIVUtilsStageLoader showed stage with open cuz stage was NOT null. fxml: " + fxmlResource + " title: " + title);
		} else if (fxmlResource == null) {
			System.out.println(	"HOIIVUtilsStageLoader couldn't create a new scene cause the fxml was null. title: " + title);
			openError("FXML Resource does not exist, Window Title: " + title);
		} else {
			FXMLLoader launchLoader = new FXMLLoader(getClass().getResource(fxmlResource));
//			System.out.println("HOIIVUtilsStageLoader creating stage with fxml: " + fxmlResource);
            Parent root;
            try {
                root = launchLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Scene scene = new Scene(root);

			addSceneStylesheets(scene);

			this.loader = launchLoader;
			this.stage = createLaunchStage(scene);
//			System.out.println("HOIIVUtilsStageLoader created and showed stage with open cuz stage was null and fxml resource is: " + fxmlResource + " title: " + title);
		}
	}

	/**
	 * Opens the stage with optional initialization arguments.
	 *
	 * @param initargs the initialization arguments for the controller
	 */
	public void open(Object... initargs) {
		Class<?>[] initargs_classes = new Class[initargs.length];
		for (int i = 0; i < initargs.length; i++) {
			initargs_classes[i] = initargs[i].getClass();
		}

		if (stage != null) {
			stage.show();
			System.out.println("HOIIVUtilsStageLoader showed stage with open cuz stage was NOT null. fxml: " + fxmlResource + " title: " + title);
		} else if (fxmlResource == null) {
			System.out.println("HOIIVUtilsStageLoader couldn't create a new scene cause the fxml was null. fxmlResource: " + fxmlResource + " title: " + title);
			openError("FXML Resource does not exist, Window Title: " + title);
		} else {
			try {
				FXMLLoader launchLoader = new FXMLLoader(getClass().getResource(fxmlResource));
				launchLoader.setControllerFactory(c -> {
					List<List<Class<?>>> ArgsClassHierarchies = new ArrayList<>(initargs.length);
					for (int i = 0; i < initargs.length; i++) {
						Set<Class<?>> superclassesAndInterfaces = new HashSet<>();
						Class<?> currentClass = initargs[i].getClass();
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
					throw new RuntimeException("No suitable constructor found");
				});
				Parent root = launchLoader.load();
				Scene scene = new Scene(root);
				addSceneStylesheets(scene);
				Stage launchStage = createLaunchStage(scene);
				this.loader = launchLoader;
				this.stage = launchStage;
//				System.out.println("HOIIVUtilsStageLoader created and showed stage with open cuz stage was null and fxml resource is: " + fxmlResource + " title: " + title);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void findSuperinterfacesRecursively(Set<Class<?>> hierarchyAndInterfaces) {
		/* necessary due to how scala trait/class structure works */
		// get interfaces of any interfaces, until there is no new interfaces to add
		// 'interface' is also to be equivalent to a Scala trait
		// todo improve this. can be optimized via recursion on new interfaces
		int prevSize = hierarchyAndInterfaces.size();
		while (true) {
			Set<Class<?>> newInterfaces = new HashSet<>();
			for (Class<?> interf : hierarchyAndInterfaces) {
				if (interf.isInterface()) {
					newInterfaces.addAll(Arrays.asList(interf.getInterfaces()));
				}
			}
			if (newInterfaces.isEmpty()) {
				break;
			}
			hierarchyAndInterfaces.addAll(newInterfaces);
			if (prevSize == hierarchyAndInterfaces.size()) {
				break;
			}
			prevSize = hierarchyAndInterfaces.size();
		}
	}

	/**
	 * Opens stage and updates fxmlResource and title
	 * 
	 * @param fxmlResource stage .fxml resource
	 * @param title stage title
	 */
	@Override
	public void open(String fxmlResource, String title) {
		this.fxmlResource = fxmlResource;
		this.title = title;
		System.out.println("open(String fxmlResource, String title)" + "fxmlResource: " + fxmlResource + " title: " + title);
		open();
	}

	/**
	 * Opens stage in thread and runLater javafx thingy
	 */
	@FXML
    public void openRunLater() {
		new Thread(() -> {
			Platform.runLater(this::open);
		}).start();
	}

	@NotNull
	private Stage createLaunchStage(Scene scene) {
		Stage launchStage = new Stage();
		launchStage.setScene(scene);

		launchStage.setTitle(title);
		decideScreen(launchStage);
		launchStage.show();
		return launchStage;
	}

	private void addSceneStylesheets(Scene scene) {
		scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);
		//scene.getStylesheets().add(getClass().getResource("../highlight-background.css").toExternalForm());
		scene.getStylesheets().add(getClass().getResource("/com/hoi4utils/ui/highlight-background.css").toExternalForm());
	}

	@Override
	public String getFxmlResource() {
		return fxmlResource;
	}

	@Override
	public String getTitle() {
		return title;
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

	private List<List<Class<?>>> generateCombinations(List<List<Class<?>>> classHierarchies, int index) {
		List<List<Class<?>>> combinations = new ArrayList<>();
		if (index == classHierarchies.size()) {
			combinations.add(new ArrayList<>());
		} else {
			for (Class<?> clazz : classHierarchies.get(index)) {
				for (List<Class<?>> combination : generateCombinations(classHierarchies, index + 1)) {
					combination.add(0, clazz);
					combinations.add(new ArrayList<>(combination));
				}
			}
		}
		return combinations;
	}
}
