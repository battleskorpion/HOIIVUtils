package com.hoi4utils.ui.parser;


import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.HOIIVUtils;
import com.hoi4utils.hoi4.country.Country;
import com.hoi4utils.hoi4.country.CountryTag$;
import com.hoi4utils.hoi4.focus.FocusTree;
import com.hoi4utils.script.AbstractPDX;
import com.hoi4utils.script.PDXScript;
import map.StrategicRegion;
import map.ResourcesFile;
import map.State;
import com.hoi4utils.parser.Node;
import com.hoi4utils.parser.Parser;
import com.hoi4utils.parser.ParserException;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.pdxscript.PDXTreeViewFactory;
import com.hoi4utils.ui.pdxscript.StratRegionPDXEditorController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;
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
	private TextArea pdxNodeTextArea;
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

				pdxTreeView.setOnMouseClicked(event -> {
					// todo improve visual?
					PDXScript<?> selectedPDX = pdxTreeView.getSelectionModel().getSelectedItem().getValue();
					if (selectedPDX != null) {
						pdxNodeTextArea.setText(selectedPDX.toScript());
					}
				});
			}
		});
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.get("version").toString());
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
//						var firstChild = rootNode.nodeValue().list().apply(0);
						var firstChild = rootNode.toList().apply(0); 
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
				// Create the parser from the selected file.
				Parser pdxParser = new Parser(selected);
				try {
					Node rootNode = pdxParser.parse();
					if (rootNode == null) {
						JOptionPane.showMessageDialog(null, "Error: Selected focus tree not found in loaded focus trees.");
						return;
					}

					// If the root node is not a parent (i.e. its raw value is not a list), set the label to "[empty]" and return.
					if (!rootNode.isParent()) {
						pdxIdentifierLabel.setText("[empty]");
						return;
					}

					// Get the child list from the root node’s raw value.
					scala.Option<scala.collection.mutable.ListBuffer<Node>> maybeList = rootNode.$list();
					if (maybeList.isDefined()) {
						scala.collection.mutable.ListBuffer<Node> childList = maybeList.get();
						Node childPDXNode = childList.apply(0); // Scala's apply(0) returns the first element.
						String pdxIdentifier = childPDXNode.name();
						if (childList.length() == 1) {
							pdxIdentifierLabel.setText(pdxIdentifier);
						} else {
							pdxIdentifierLabel.setText(selected.getName());
						}

						AbstractPDX<?> pdx = null;
						if (pdxIdentifier.equals("focus_tree")) {
							pdx = new FocusTree(selected);
						} else if (pdxIdentifier.equals("state")) {
							pdx = new State(false, selected);
						} else if (selected.getParent().endsWith("countries")
								&& selected.getParentFile().getParent().endsWith("history")) {
							pdx = new Country(selected, CountryTag$.MODULE$.get(selected.getName().substring(0, 3)));
						} else if (pdxIdentifier.equals("resources")) {
							pdx = new ResourcesFile(selected);
						} else if (pdxIdentifier.equals("strategic_region")) {
							pdx = new StrategicRegion(selected);
						}

						if (pdx == null || pdx.isUndefined()) return;
						this.pdxScripts.add(pdx);
					} else {
						pdxIdentifierLabel.setText("[empty]");
					}

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
			// show pdx's node in side view when pdx item is selected in list view
			filesListView.setOnMouseClicked(event -> {
				PDXScript<?> selectedPDX = filesListView.getSelectionModel().getSelectedItem();
				if (selectedPDX != null) {
					// todo improve visual?
					pdxNodeTextArea.setText(selectedPDX.toScript());
				}
			});
			// yes this is after because the last statement can set an empty list in view
			if (pdxScripts.isEmpty()) return;

			/* do stuff with the pdx scripts */


		} else {
			pdxIdentifierLabel.setText("[not found]");
		}
	}

	@FXML
	private void handleSaveAction() {
		savePDX(this.pdxScripts);
	}

	private void savePDX(List<PDXScript<?>> pdxScripts) {
		for (PDXScript<?> pdx : pdxScripts) {
			pdx.savePDX(new File("Parser Viewer PDXScripts"));
		}
	}

//	@FXML
//	private void handleOpenReplAction() {
//		// Launch an Ammonite REPL, passing in the pdxScripts list
//		new ReplUIController().open(pdxScripts); 
//	}

	@FXML
	private void handlePDXListViewEditAll() {
		switch(pdxScripts.getFirst()) {
			case StrategicRegion sr -> {
				// Open the strategic region editor
				StratRegionPDXEditorController stratRegionEditor = new StratRegionPDXEditorController();
				List<StrategicRegion> list = new ArrayList<>();
				// java workaround
				pdxScripts.forEach(pdx -> {
					if (pdx instanceof StrategicRegion) {
						list.add((StrategicRegion) pdx);
					}
				});
				stratRegionEditor.open(list);
			}
			default -> {
				JOptionPane.showMessageDialog(null, "No editor available for this PDX type.");
			}
		}
	}
}
