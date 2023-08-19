package ui.buildings;

import java.awt.MenuItem;

import hoi4utils.HOIIVUtils;
import javafx.fxml.FXML;

public class BuildingsByCountryWindowController {
	@FXML
	public MenuItem exportAsMenuItem;
	public MenuItem percentageTogglMenuItem;
	public MenuItem versionMenuItem;

	@FXML
	void initialize() {
		versionMenuItem.setName(HOIIVUtils.hoi4utilsVersion);
	}

	public void handleExport() {

	}
}
