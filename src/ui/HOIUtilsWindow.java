package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class HOIUtilsWindow implements FXWindow {
	private Stage stage;
	private Scene scene;
	private Parent root;
	protected FXMLLoader loader;
	private String fxmlResource;
	private String title = "HOIIVUtils (default title)";
	private String styleSheetURL = "resources/javafx_dark.css";
	/**
	 * Allows windows to get the controller
	 */
	

	/**
	 * Opens the window
	 */
	@Override
	public void open() {
		if (stage != null) {
			stage.show();
		} else if (fxmlResource == null) {
			openError("FXML Resource does not exist, Window Title: " + title);
		} else {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
				Parent root = loader.load();
				Scene scene = new Scene(root);
				scene.getStylesheets().add(styleSheetURL);
				scene.getStylesheets().add("resources/utils-highlight-background.css"); // todo temp? idk
	
				Stage launchStage = new Stage();
				launchStage.setScene(scene);
	
				launchStage.setTitle(title);
				decideScreen(launchStage);
				launchStage.show();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Opens window and updates fxmlResource and title
	 * @param fxmlResource window .fxml resource
	 * @param title window title
	 */
	@Override
	public void open(String fxmlResource, String title) {
		this.fxmlResource = fxmlResource;
		this.title = title;
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
	public String getStyleSheetURL() {
		return styleSheetURL;
	}

	@Override
	public void setFxmlResource(String fxmlResource) {
		this.fxmlResource = fxmlResource;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setStyleSheetURL(String styleSheetURL) {
		this.styleSheetURL = styleSheetURL;
	}

}
