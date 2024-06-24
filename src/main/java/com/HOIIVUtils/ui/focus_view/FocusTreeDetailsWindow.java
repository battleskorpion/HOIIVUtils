package com.HOIIVUtils.ui.focus_view;

import com.HOIIVUtils.clauzewitz.data.focus.Focus;
import com.HOIIVUtils.clauzewitz.localization.Localizable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Set;

public class FocusTreeDetailsWindow extends Stage {
	private Label focusNameLabel;
	private Label focusIDLabel;
	private Label focusDetailsLabel;

	public FocusTreeDetailsWindow(Focus focus, Point2D p) {
		setTitle("Details");

		if (focus == null) {
			System.err.println("Focus is null");
			return;
		}

		// Create JavaFX UI components
		VBox root = new VBox(10);
		focusNameLabel = new Label(focus.localization(Localizable.Property.NAME).text());
		focusIDLabel = new Label(focus.id());
		focusDetailsLabel = new Label();

		StringBuilder details = new StringBuilder();
		details.append("\n");
		details.append("Completion time: ");
		details.append(focus.preciseCompletionTime());
		details.append("\n");

		for (Set<Focus> prereqSet : focus.getPrerequisites()) {
			if (prereqSet.size() > 1) {
				details.append("Requires one of the following: \n");
				for (Focus f : prereqSet) {
					details.append("- ");
					details.append(f.localization(Localizable.Property.NAME).text());
					details.append("\n");
				}
			} else {
				details.append("Requires: ");
				details.append(prereqSet.iterator().next().localization(Localizable.Property.NAME).text());
				details.append("\n");
			}
		}

		details.append("\n\nEffect: \n");
		focusDetailsLabel.setText(details.toString());

		// Add components to the root layout
		root.getChildren().addAll(focusNameLabel, focusIDLabel, focusDetailsLabel);

		// Create and set the scene
		Scene scene = new Scene(root, 300, 200);
		setScene(scene);
		// Calculate the position relative to the mouse cursor
		double xOffset = 10; // Adjust as needed
		double yOffset = 10; // Adjust as needed
		setX(p.getX() + xOffset);
		setY(p.getY() + yOffset);
	}
}
