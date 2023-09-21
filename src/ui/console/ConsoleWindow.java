package ui.console;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ConsoleWindow extends Application {
	String fxmlResource = "ConsoleWindow.fxml";
	String title = "HOIIVUtils Console";
	String styleSheetURL = "resources/javafx_dark.css";
	private Stage stage;

    @FXML private TextArea consoleTextArea;

	public void initializeConsoleWindow(String[] args) {
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
}
