package com.HOIIVUtils.ui.settings;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.application.Platform;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testfx.assertions.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class SettingsControllerTest {

	private Stage stage;

	@BeforeEach
	public void setup() {
		stage = new Stage();
	}

	@Start
	public void start(Stage stage) {
		this.stage = stage;
		Platform.runLater(() -> {
			SettingsController controller = new SettingsController();
			controller.fxmlResource = "settings.fxml";
			controller.title = "Settings";
			controller.stage = stage;
			controller.start(stage);
		});
	}

	/**
	 * Test that the start method throws an IOException when the specified
	 * FXML resource does not exist.
	 * 
	 * @param robot the test robot, not used in this test
	 */
	@Test
	void testFxmlResourceNotFound(FxRobot robot) {
		// Given
		SettingsController controller = new SettingsController();
		controller.fxmlResource = "non_existent_file.fxml";
		controller.title = "Settings";
		controller.stage = stage;

		controller.start(stage);
		fail("Expected IOException to be thrown");
	}

	/**
	 * Test that the start method throws an Exception when the specified
	 * FXML resource cannot be loaded.
	 * 
	 * @param robot the test robot, not used in this test
	 */
	@Test
	void testFxmlResourceLoadFailure(FxRobot robot) {
		// Given
		SettingsController controller = new SettingsController();
		// Load an invalid FXML resource
		controller.fxmlResource = "invalid_file.fxml";
		controller.title = "Settings";
		controller.stage = stage;

		// When
		assertThrows(Exception.class, () -> controller.start(stage));

		// Then
		// The stage should be null
		assertThat(controller.stage).isNull();
	}

	/**
	 * Test that the start method throws an Exception when the specified
	 * FXML resource cannot be loaded.
	 * 
	 * @param robot the test robot, not used in this test
	 */
	@Test
	void testFxmlResourceLoadSuccess(FxRobot robot) {
		// Given
		SettingsController controller = new SettingsController();
		// Load a valid FXML resource
		controller.fxmlResource = "settings.fxml";
		controller.title = "Settings";
		controller.stage = stage;

		// When
		controller.start(stage);

		// Then
		// The stage should not be null
		assertThat(controller.stage).isNotNull();
	}

}