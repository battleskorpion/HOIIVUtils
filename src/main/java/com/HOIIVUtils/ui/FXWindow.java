package com.HOIIVUtils.ui;

import com.HOIIVUtils.hoi4utils.FileUtils;
import com.HOIIVUtils.hoi4utils.Settings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import com.HOIIVUtils.ui.javafx.table.IntegerOrPercentTableCell;
import com.HOIIVUtils.ui.javafx.table.TableViewWindow;
import com.HOIIVUtils.ui.message.MessageController;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.function.Function;

public interface FXWindow {
	static void openGlobalErrorWindow(Exception exception) {
		openGlobalErrorWindow(exception.getLocalizedMessage());
	}

	/**
	 * Opens windows file and directory chooser
	 *
	 * @param fxcomponent      The node (javafx component) that was pressed to open
	 *                         the chooser, must belong to a scene
	 * @param initialDirectory The initial directory of the file chooser, instead of
	 *                         the default user directory.
	 * @param ford             A quirky boolean that specifies whether you want to
	 *                         return a directory or file: true = return directory,
	 *                         false = return file
	 * @return theChosenOne, It is up to the the page to handle what you do if the
	 *         user returns a null
	 * @see File
	 * @see Node
	 */
	default File openChooser(Node fxcomponent, boolean ford, File initialDirectory) {
		File theChosenOne;
		Stage stage = (Stage) (fxcomponent.getScene().getWindow());
		if (ford) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			if (initialDirectory.exists() && initialDirectory.isDirectory()) {
				directoryChooser.setInitialDirectory(initialDirectory);
			} else {
				directoryChooser.setInitialDirectory(FileUtils.usersDocuments);
			}
			theChosenOne = directoryChooser.showDialog(stage);
		} else {
			FileChooser fileChooser = new FileChooser();
			if (initialDirectory.exists() && initialDirectory.isDirectory()) {
				fileChooser.setInitialDirectory(initialDirectory);
			} else {
				fileChooser.setInitialDirectory(FileUtils.usersDocuments);
			}
			theChosenOne = fileChooser.showOpenDialog(stage);
		}
		return theChosenOne;
	}

	/**
	 * Opens windows file and directory chooser
	 * <p>
	 * For if you don't want to set a initial directory
	 *
	 * @param fxcomponent The node (javafx component) that was pressed to open the
	 *                    chooser, must belong to a scene
	 * @param ford        A quirky boolean that specifies whether you want to return
	 *                    a directory or file: true = return directory, false =
	 *                    return file
	 * @return theChosenOne, It is up to the the page to handle what you do if the
	 *         user returns a null
	 * @see Node
	 */
	default File openChooser(Node fxcomponent, Boolean ford) {
		return openChooser(fxcomponent, ford, FileUtils.usersDocuments);
	}

	static void openGlobalErrorWindow(String s) {
		MessageController errWindow = new MessageController();
		errWindow.open(s);
	}

	/**
	 * @param stage
	 */
	default void decideScreen(Stage stage) {
		Integer preferredScreen = (Integer) Settings.PREFERRED_SCREEN.getSetting();
		ObservableList<Screen> screens = Screen.getScreens();
		if (preferredScreen > screens.size() - 1) {
			System.err.println("Preferred screen does not exist, resorting to defaults.");
			return;
		}
		Screen screen = screens.get(preferredScreen);
		if (screen == null) {
			System.err.println("Preferred screen is null error, resorting to defaults.");
			return;
		}

		Rectangle2D bounds = screen.getVisualBounds();
		stage.setX(bounds.getMinX() + 200);
		stage.setY(bounds.getMinY() + 200);
	}

	default void closeWindow(Button button) {
		try {
			((Stage) (button.getScene().getWindow())).close();
		} catch (Exception exception) {
			openError(exception);
		}
	}

	/**
	 * @param fxcomponent The node (javafx component), must belong to a scene. The
	 *                    stage the scene belongs to will be hidden.
	 * @see Node
	 * @see javafx.stage.Window
	 */
	default void hideWindow(Node fxcomponent) {
		try {
			((Stage) (fxcomponent.getScene().getWindow())).hide();
		} catch (Exception exception) {
			openError(exception);
		}
	}

	default void openError(Exception exception) {
		if (Settings.DEV_MODE.enabled()) {
			exception.printStackTrace();
		}
		JOptionPane.showMessageDialog(null, exception, "ln: " + exception.getStackTrace()[0].getLineNumber(),
				JOptionPane.WARNING_MESSAGE);
	}

	default void openError(String s) {
		if (Settings.DEV_MODE.enabled()) {
			System.err.println("open error window for error message: " + s);
		}
		JOptionPane.showMessageDialog(null, s, "Error Message", JOptionPane.WARNING_MESSAGE);
	}

	void open();

	void open(String fxmlResource, String title);

	String getFxmlResource();

	String getTitle();

	void setFxmlResource(String fxmlResource);

	void setTitle(String title);

	default <S> void loadTableView(TableViewWindow window, TableView<S> dataTable, ObservableList<S> data,
			List<Function<S, ?>> dataFunctions) {
		window.setDataTableCellFactories();

		setTableCellValueFactories(dataFunctions, dataTable);

		dataTable.setItems(data); // country objects, cool! and necessary for the cell value factory,
									// this is giving the factories the list of objects to collect
									// their data from.

		System.out.println("Loaded data into table: " + dataTable.getId());
	}

	/**
	 * Set the cell value factories for the table columns
	 * 
	 * @param <S>           the type of the table row
	 * @param dataFunctions a list of functions that take a table row and return the
	 *                      value
	 *                      for the column
	 * @param tableView     the table view to set the cell value factories for
	 */
	default <S> void setTableCellValueFactories(List<Function<S, ?>> dataFunctions, TableView<S> tableView) {
		// set the cell value factories for the table columns
		ObservableList<TableColumn<S, ?>> columns = tableView.getColumns();

		// get the minimum size of the dataFunctions and columns lists
		// so we don't index out of bounds
		int minSize = Math.min(dataFunctions.size(), columns.size());

		// loop through the minimum size of the lists
		for (int i = 0; i < minSize; i++) {
			// get the column and data function for the current index
			TableColumn<S, ?> column = columns.get(i);
			Function<S, ?> dataFunction = dataFunctions.get(i);

			// set the cell value factory for the column
			column.setCellValueFactory(cellDataCallback(dataFunction));
		}
	}

	/**
	 * Create a cell value factory for a table column
	 * 
	 * @param <S>            the type of the table row
	 * @param <T>            the type of the table column
	 * @param propertyGetter a function that takes a table row and returns the value
	 *                       for the column
	 * @return a cell value factory that applies the given function to the table row
	 */
	static <S, T> Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> cellDataCallback(
			Function<S, ?> propertyGetter) {
		return cellData -> {
			if (Settings.DEV_MODE.enabled()) {
				System.out.println("Table callback created, data: " +
						propertyGetter.apply(cellData.getValue()));
			}
			// unchecked cast is necessary because the compiler doesn't know that the given
			// function will return a value of type T
			@SuppressWarnings("unchecked")
			T result = (T) propertyGetter.apply(cellData.getValue()); // yap
			return new SimpleObjectProperty<>(result);
		};
	}

	/**
	 * Update cell behavior within a column
	 * 
	 * This method updates the cell behavior of a given column within a table
	 * view to either display the values as integers or as a percentage of the
	 * total value of the column.
	 * 
	 * @param columnIndex        the table column to update
	 * @param displayPercentages whether to display the values as percentages or not
	 * 
	 * @see IntegerOrPercentTableCell
	 */
	static <S> void updateColumnPercentBehavior(TableColumn<S, Double> columnIndex,
			boolean displayPercentages) {
		// Set the cell factory for the column to a cell that can display
		// either integers or percentages
		columnIndex.setCellFactory(col -> {
			// Create a new cell instance that can display either integers or percentages
			IntegerOrPercentTableCell<S> cell = new IntegerOrPercentTableCell<>();
			cell.setInteger(!displayPercentages);
			cell.setPercent(displayPercentages);
			return cell;
		});
	}
}
