package com.hoi4utils.ui.parser;

import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.data.country.Country;
import com.hoi4utils.clausewitz.data.country.CountryTag$;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.map.StrategicRegion;
import com.hoi4utils.clausewitz.map.state.ResourcesFile;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.clausewitz.script.AbstractPDX;
import com.hoi4utils.clausewitz.script.PDXFile;
import com.hoi4utils.clausewitz.script.PDXScript;
import com.hoi4utils.clausewitz_parser.Parser;
import com.hoi4utils.clausewitz_parser.ParserException;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.pdxscript.PDXTreeViewFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParserViewerController extends HOIIVUtilsAbstractController {

	@FXML
	public Label idVersion;
	@FXML
	public TextField parsePDXFileTextField;
	@FXML
	public Button browseButton;
	@FXML
	public Label pdxIdentifierLabel;
	@FXML
	public AnchorPane pdxTreeViewPane;
	@FXML
	private TextField searchTextField;  // user enters search text here
	// Suppose we have a ListView
	@FXML
	private ListView<PDXScript<?>> filesListView;
	@FXML
	private MenuItem saveMenuItem;
	
	private final List<PDXScript<?>> pdxScripts = new ArrayList<>();
	
	public ParserViewerController() {
		setFxmlResource("ParserViewer.fxml");
		setTitle("HOIIVUtils Parser Viewer");
	}

	@FXML
	void initialize() {
		includeVersion();

		// Then on list selection:
		filesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				// Create a tree view for the newly selected script
				TreeView<PDXScript<?>> pdxTreeView = PDXTreeViewFactory.createPDXTreeView(newVal);
				pdxTreeViewPane.getChildren().removeIf(node -> node instanceof TreeView);
				pdxTreeViewPane.getChildren().add(pdxTreeView);

				AnchorPane.setTopAnchor(pdxTreeView, 25.0);
				AnchorPane.setBottomAnchor(pdxTreeView, 0.0);
				AnchorPane.setLeftAnchor(pdxTreeView, 0.0);
				AnchorPane.setRightAnchor(pdxTreeView, 0.0);

				searchTextField.setOnAction(event -> {
					String searchTerm = searchTextField.getText();
					PDXTreeViewFactory.searchAndSelect(pdxTreeView, searchTerm);
				});
			}
		});
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	}

	@FXML
	private void handlePDXFileBrowseAction() {
		File initialDirectory = HOIIVFiles.Mod.folder;
		File selected = JavaFXUIManager.openChooser(browseButton, initialDirectory, true);

		System.out.println(selected);

		if (selected != null) {
			pdxScripts.clear();

			if (selected.isDirectory()) {
				List<File> allPDXFiles = CollectionConverters.asJava(PDXScript.allPDXFilesInDirectory(selected));

				for (File file : allPDXFiles) {
					try {
						Parser parser = new Parser(file);
						var rootNode = parser.parse();
						if (rootNode == null) continue;

						// Identify if it’s a strategic_region, state, focus_tree, etc.
						var firstChild = rootNode.nodeValue().list().apply(0);
						String pdxIdentifier = firstChild.name();

						AbstractPDX<?> pdx = switch (pdxIdentifier) {
							case "focus_tree"       -> new FocusTree(file);
							case "state"            -> new State(false, file);
							case "strategic_region" -> new StrategicRegion(file);

							// ...
							default -> null;
						};

						if (pdx != null && !pdx.isUndefined()) {
							pdxScripts.add(pdx);
						}
					} catch (ParserException e) {
						// handle errors
					}
				}
			}

			else {
				parsePDXFileTextField.setText(selected.getAbsolutePath());
				//focusTree = FocusTree$.MODULE$.get(selectedFile).getOrElse(() -> null);
				Parser pdxParser = new Parser(selected);
				try {
					var rootNode = pdxParser.parse();
					if (rootNode == null) {
						JOptionPane.showMessageDialog(null, "Error: Selected focus tree not found in loaded focus trees.");
						return;
					}

					var rootNodeValue = rootNode.nodeValue();
					if (!rootNodeValue.isList()) {
						pdxIdentifierLabel.setText("[empty]");
						return;
					}

					var childPDXNode = rootNodeValue.list().apply(0);
					var pdxIdentifier = childPDXNode.name();
					if (rootNodeValue.list().length() == 1) {
						pdxIdentifierLabel.setText(pdxIdentifier);
					} else {
						pdxIdentifierLabel.setText(selected.getName());
					}

					AbstractPDX<?> pdx = null;
					if (pdxIdentifier.equals("focus_tree")) {
						pdx = new FocusTree(selected);
					} else if (pdxIdentifier.equals("state")) {
						pdx = new State(false, selected);
					} else if (selected.getParent().endsWith("countries") && selected.getParentFile().getParent().endsWith("history")) {
						pdx = new Country(selected, CountryTag$.MODULE$.get(selected.getName().substring(0, 3)));
					} else if (pdxIdentifier.equals("resources")) {
						pdx = new ResourcesFile(selected);
					} else if (pdxIdentifier.equals("strategic_region")) {
						pdx = new StrategicRegion(selected);
					}

					if (pdx == null || pdx.isUndefined()) return;
					this.pdxScripts.add(pdx);

//					// Build a TreeView out of the rootScript
//					TreeView<PDXScript<?>> pdxTreeView = PDXTreeViewFactory.createPDXTreeView(pdx);
//
//					pdxTreeViewPane.getChildren().removeIf(node -> node instanceof TreeView);
//					pdxTreeViewPane.getChildren().add(pdxTreeView);
//					AnchorPane.setTopAnchor(pdxTreeView, 25.0);
//					AnchorPane.setBottomAnchor(pdxTreeView, 0.0);
//					AnchorPane.setLeftAnchor(pdxTreeView, 0.0);
//					AnchorPane.setRightAnchor(pdxTreeView, 0.0);
//
//					searchTextField.setOnAction(event -> {
//						String searchTerm = searchTextField.getText();
//						PDXTreeViewFactory.searchAndSelect(pdxTreeView, searchTerm);
//					});
				} catch (ParserException e) {
					throw new RuntimeException(e);
				}
			}

			// After loading all PDX scripts:
			filesListView.setItems(FXCollections.observableList(pdxScripts));
		} else {
			pdxIdentifierLabel.setText("[not found]");
		}
	}

	@FXML
	private void handleSaveAction() {
		savePDX(this.pdxScripts);
	}

	private void savePDX(PDXScript<?> pdx) {
		if (pdx == null) return; 
		var dir = new File("ParserViewer"); 
		if (!dir.mkdir()) {
			LOGGER.error("Error creating directory for saving PDXScript: \n{}", pdx);
		}
		File path = switch (pdx) {
			case PDXFile pdxf -> new File(pdxf.fileName());
			default -> new File(pdx.getClass().getSimpleName() + ".txt");
		};
		
		try (PrintWriter writer = new PrintWriter(path)) {
			// Write the focus tree to the file
			writer.println("### Generated by HOI4Utils, time: " + LocalDateTime.now() + "###");
			writer.println(pdx.toScript());
			LOGGER.info("Exported PDXScript {} to {}", pdx.getClass().getSimpleName(), path);
		} catch (FileNotFoundException e) {
			LOGGER.error("Error exporting PDXScript: {}", e.getMessage());
			JOptionPane.showMessageDialog(null, "Error exporting PDXScript: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void savePDX(List<PDXScript<?>> pdxScripts) {
		for (PDXScript<?> pdx : pdxScripts) {
			savePDX(pdx);
		}
	}
}
