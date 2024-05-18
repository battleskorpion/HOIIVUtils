package com.HOIIVUtils.ui.settings;

import org.junit.jupiter.api.Test;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

class SettingsControllerTest {
	@Test
	void testGetFxmlResource() {
		assertEquals("Settings.fxml", new SettingsController().getFxmlResource());
	}

	@Test
	void testGetTitle() {
		assertEquals("Settings", new SettingsController().getTitle());
	}

	@Test
	void testStart() {
		SettingsController settingsController = new SettingsController();
		settingsController.start(new Stage());
		FXMLLoader loader = new FXMLLoader(getClass().getResource(settingsController.getFxmlResource()));
		assertNotNull(loader.getController(), "Controller should not be null");
	}
}
