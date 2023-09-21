package ui.console;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import ui.FXWindow;

public class ConsoleWindow extends Application implements FXWindow {
	String fxmlResource = "ConsoleWindow.fxml";
	String title = "HOIIVUtils Console";
	String styleSheetURL = "resources/javafx_dark.css";
	Stage stage;

    @FXML private TextArea consoleTextArea;

	public void initializeConsoleWindow() {
        try {
			FXMLLoader loader = new FXMLLoader(
				getClass().getResource(
					fxmlResource
				)
			);

            // Get the controller
            ConsoleWindow controller = loader.getController();

            // Add any initialization code specific to the console window here
            controller.initialize();


            stage = new Stage();
			Scene scene = new Scene(loader.load());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideConsoleWindow() {
        if (stage != null) {
            stage.hide();
        }
    }

    public void showConsoleWindow() {
        if (stage != null) {
            stage.show();
        }
    }

    // Add other methods and functionality as needed

    public void initialize() {
        // Add any initialization code here if needed
    }

    @Override
    public void start(Stage arg0) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

	public void open() {
        try {
			if (stage != null) {
				stage.show();
			} else {
				start(new Stage());
			}
		} 
		catch (Exception exc) {
			openError(exc);
		}
	}

    @Override
    public void open(String fxmlResource, String title) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'open'");
    }

    @Override
    public String getFxmlResource() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFxmlResource'");
    }

    @Override
    public String getTitle() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTitle'");
    }

    @Override
    public String getStyleSheetURL() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStyleSheetURL'");
    }

    @Override
    public void setFxmlResource(String fxmlResource) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setFxmlResource'");
    }

    @Override
    public void setTitle(String title) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTitle'");
    }

    @Override
    public void setStyleSheetURL(String styleSheetURL) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStyleSheetURL'");
    }
}
