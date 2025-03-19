package com.hoi4utils.ui.parser;

import com.hoi4utils.clausewitz.HOIIVFiles;
import com.hoi4utils.clausewitz.HOIIVUtils;
import com.hoi4utils.clausewitz.data.country.Country;
import com.hoi4utils.clausewitz.data.country.CountryTag$;
import com.hoi4utils.clausewitz.data.focus.FocusTree;
import com.hoi4utils.clausewitz.data.focus.FocusTree$;
import com.hoi4utils.clausewitz.map.StrategicRegion;
import com.hoi4utils.clausewitz.map.state.ResourcesFile;
import com.hoi4utils.clausewitz.map.state.State;
import com.hoi4utils.clausewitz.script.AbstractPDX;
import com.hoi4utils.clausewitz.script.PDXScript;
import com.hoi4utils.clausewitz.script.StructuredPDX;
import com.hoi4utils.clausewitz_parser.Parser;
import com.hoi4utils.clausewitz_parser.ParserException;
import com.hoi4utils.ui.HOIIVUtilsAbstractController;
import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.pdxscript.PDXTreeViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import scala.jdk.javaapi.CollectionConverters;

import javax.swing.*;
import java.io.File;

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
	
	public ParserViewerController() {
		setFxmlResource("ParserViewer.fxml");
		setTitle("HOIIVUtils Parser Viewer");
	}

	@FXML
	void initialize() {
		includeVersion();
	}

	private void includeVersion() {
		idVersion.setText(HOIIVUtils.HOIIVUTILS_VERSION);
	}

	public void handlePDXFileBrowseButtonAction() {
		File initialDirectory = HOIIVFiles.Mod.folder;
		File selectedFile = JavaFXUIManager.openChooser(browseButton, initialDirectory, false);

		System.out.println(selectedFile);

		if (selectedFile != null) {
			parsePDXFileTextField.setText(selectedFile.getAbsolutePath());
			//focusTree = FocusTree$.MODULE$.get(selectedFile).getOrElse(() -> null);
			Parser pdxParser = new Parser(selectedFile);
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
				var pdxIdentifier = childPDXNode.identifier();
	            if (rootNodeValue.list().length() == 1) {
					pdxIdentifierLabel.setText(pdxIdentifier);
	            } else {
					pdxIdentifierLabel.setText(selectedFile.getName()); 
	            }

	            AbstractPDX<?> pdx = null;
				if (pdxIdentifier.equals("focus_tree")) {
					pdx = new FocusTree(selectedFile);
				} else if (pdxIdentifier.equals("state")) {
					pdx = new State(false, selectedFile);
				} else if (selectedFile.getParent().endsWith("countries") && selectedFile.getParentFile().getParent().endsWith("history")) {
					pdx = new Country(selectedFile, CountryTag$.MODULE$.get(selectedFile.getName().substring(0, 3)));
				} else if (pdxIdentifier.equals("resources")) {
					pdx = new ResourcesFile(selectedFile);
				} else if (pdxIdentifier.equals("strategic_region")) {
					pdx = new StrategicRegion(selectedFile);
				}

				if (pdx == null || pdx.isUndefined()) return;

	            // Build a TreeView out of the rootScript
	            TreeView<PDXScript<?>> pdxTreeView = PDXTreeViewFactory.createPDXTreeView(pdx);

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
            } catch (ParserException e) {
                throw new RuntimeException(e);
            }
		} else {
			pdxIdentifierLabel.setText("[not found]");
		}
	}
}
