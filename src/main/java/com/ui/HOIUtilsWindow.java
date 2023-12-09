package ui;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class HOIUtilsWindow implements FXWindow {
	private String fxmlResource;
	private String title;
	protected Stage stage;
	protected FXMLLoader loader;
	
	/**
	 * Opens the stage
	 */
	@Override
	public void open() {
		if (stage != null) {
			stage.show();
			System.out.println("HOIUtilsWindow showed stage with open cuz stage was NOT null. fxml: " + fxmlResource + " title: " + title);
		} else if (fxmlResource == null) {
			System.out.println("HOIUtilsWindow couldn't create a new scene cause the fxml was null. fxmlResource: " + fxmlResource + " title: " + title);
			openError("FXML Resource does not exist, Window Title: " + title);
		} else {
			try {
				FXMLLoader launchLoader = new FXMLLoader(getClass().getResource(fxmlResource));
				System.out.println("HOIUtils Window creating stage with fxml" + fxmlResource);
				Parent root = launchLoader.load();
				Scene scene = new Scene(root);
				scene.getStylesheets().add(HOIIVUtils.DARK_MODE_STYLESHEETURL);
				
				// TODO 
				scene.getStylesheets().add("resources/utils-highlight-background.css");
				
				Stage launchStage = new Stage();
				launchStage.setScene(scene);
	
				launchStage.setTitle(title);
				decideScreen(launchStage);
				launchStage.show();
				this.loader = launchLoader;
				this.stage = launchStage;
				System.out.println("HOIUtilsWindow created and showed stage with open cuz stage was null and fxml resource is: " + fxmlResource + " title: " + title);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Opens stage and updates fxmlResource and title
	 * @param fxmlResource stage .fxml resource
	 * @param title stage title
	 */
	@Override
	public void open(String fxmlResource, String title) {
		this.fxmlResource = fxmlResource;
		this.title = title;
		System.out.println("open(String fxmlResource, String title)" + "fxmlResource: " + fxmlResource + " title: " + title);
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
}
