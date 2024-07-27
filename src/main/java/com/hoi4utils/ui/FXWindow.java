package com.hoi4utils.ui;

import com.hoi4utils.FileUtils;
import com.hoi4utils.Settings;
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
import com.hoi4utils.ui.javafx.table.IntegerOrPercentTableCell;
import com.hoi4utils.ui.javafx.table.TableViewWindow;
import com.hoi4utils.ui.message.MessageController;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.function.Function;

public interface FXWindow {
	static void openGlobalErrorWindow(Exception exception) {
		openGlobalErrorWindow(exception.getLocalizedMessage());
	}

	static File openChooser(Stage stage, File initialDirectory, boolean ford) {
		File theChosenOne;
		if (ford) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			if (initialDirectory != null && initialDirectory.exists() && initialDirectory.isDirectory()) {
				directoryChooser.setInitialDirectory(initialDirectory);
			} else {
				directoryChooser.setInitialDirectory(FileUtils.usersDocuments);
			}
			theChosenOne = directoryChooser.showDialog(stage);
		} else {
			FileChooser fileChooser = new FileChooser();
			if (initialDirectory != null && initialDirectory.exists() && initialDirectory.isDirectory()) {
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
	 *
	 * @param fxcomponent      The node (javafx component) that was pressed to open
	 *                         the chooser, must belong to a scene
	 * @param initialDirectory The initial directory of the file chooser, instead of
	 *                         the default user directory.
	 *                         If null, the initial directory will not be specified.
	 * @param ford             A quirky boolean that specifies whether you want to
	 *                         return a directory or file: true = return directory,
	 *                         false = return file
	 * @return theChosenOne, It is up to the the page to handle what you do if the
	 * user returns a null
	 * @see File
	 * @see Node
	 */
	static File openChooser(Node fxcomponent, File initialDirectory, boolean ford) {
		return openChooser((Stage) (fxcomponent.getScene().getWindow()), initialDirectory, ford);
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
	static File openChooser(Node fxcomponent, Boolean ford) {
		return openChooser(fxcomponent, FileUtils.usersDocuments, ford);
	}

	static File openChooser(File initialDirectory, boolean ford) {
		return openChooser(new Stage(), initialDirectory, ford);
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

	default <S> void loadTableView(TableViewWindow window, TableView<S> table, ObservableList<S> data,
	                               List<Function<S, ?>> dataFunctions) {
		window.setDataTableCellFactories();

		setTableCellValueFactories(dataFunctions, table);

		table.setItems(data);       // this is giving the factories the list of objects to collect
									// their data from.

		if (Settings.DEV_MODE.enabled()) System.out.println("Loaded data into table: " + table.getId());
	}

//	default <S> void loadTreeTableView(TableViewWindow window, TreeTableView<S> table, ObservableList<S> data,
//	                                   List<Function<S, ?>> dataFunctions, Function<S, ?> parentingFunction) {
//		window.setDataTableCellFactories();
//
//
//	}


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

		// get the minimum size of the dataFunctions and columns lists, prevents indexing error
		int minSize = Math.min(dataFunctions.size(), columns.size());

		for (int i = 0; i < minSize; i++) {
			// get the column and data function for the current index
			TableColumn<S, ?> column = columns.get(i);
			Function<S, ?> dataFunction = dataFunctions.get(i);

			// set the cell value factory for the column
			column.setCellValueFactory(tableCellDataCallback(dataFunction));
		}
	}

	default <S> void setTreeTableCellValueFactories(List<Function<S, ?>> dataFunctions, TreeTableView<S> treeTableView) {
		// set the cell value factories for the table columns
		ObservableList<TreeTableColumn<S, ?>> columns = treeTableView.getColumns();

		// get the minimum size of the dataFunctions and columns lists, prevents indexing error
		int minSize = Math.min(dataFunctions.size(), columns.size());

		for (int i = 0; i < minSize; i++) {
			// get the column and data function for the current index
			TreeTableColumn<S, ?> column = columns.get(i);
			Function<S, ?> dataFunction = dataFunctions.get(i);

			// set the cell value factory for the column
			column.setCellValueFactory(treeTableCellDataCallback(dataFunction));
		}
	}

	/**
	 * Creates a cell value factory for a table column that applies a given function
	 * to a table row to retrieve the value for that column.
	 *
	 * @param <S>            the type of the table row
	 * @param <T>            the type of the table column
	 * @param propertyGetter a function that takes a table row of type {@code S} and
	 *                       returns a value of type {@code T} for the column
	 * @return a {@code Callback} that produces an {@code ObservableValue<T>} by
	 *         applying the given function to the table row
	 */
	static <S, T> Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> tableCellDataCallback(
			Function<S, ?> propertyGetter) {
		return cellData -> {
			if (Settings.DEV_MODE.enabled()) {
				System.out.println("Table callback created, data: " + propertyGetter.apply(cellData.getValue()));
			}
			// unchecked cast is necessary because the compiler doesn't know that the given
			// function will return a value of type T
			@SuppressWarnings("unchecked")
			T result = (T) propertyGetter.apply(cellData.getValue()); // yap
			return new SimpleObjectProperty<>(result);
		};
	}

	/**
	 * Creates a cell value factory for a tree table column that applies a given
	 * function to a tree table row to retrieve the value for that column.
	 *
	 * @param <S>            the type of the tree table row
	 * @param <T>            the type of the tree table column
	 * @param propertyGetter a function that takes a tree table row of type {@code S}
	 *                       and returns a value of type {@code T} for the column
	 * @return a {@code Callback} that produces an {@code ObservableValue<T>} by
	 *         applying the given function to the tree table row
	 */
	static <S, T> Callback<TreeTableColumn.CellDataFeatures<S, T>, ObservableValue<T>> treeTableCellDataCallback(
			Function<S, ?> propertyGetter) {
		return cellData -> {
			if (Settings.DEV_MODE.enabled()) {
				System.out.println("Table callback created, data: " + propertyGetter.apply(cellData.getValue().getValue()));
			}
			// unchecked cast is necessary because the compiler doesn't know that the given
			// function will return a value of type T
			@SuppressWarnings("unchecked")
			T result = (T) propertyGetter.apply(cellData.getValue().getValue());
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
	 * @param column        the table column to update
	 * @param displayPercentages whether to display the values as percentages or not
	 * 
	 * @see IntegerOrPercentTableCell
	 */
	static <S> void updateColumnPercentBehavior(TableColumn<S, Double> column,
	                                            boolean displayPercentages) {
		// Set the cell factory for the column to a cell that can display
		// either integers or percentages
		column.setCellFactory(col -> {
			// Create a new cell instance that can display either integers or percentages
			IntegerOrPercentTableCell<S> cell = new IntegerOrPercentTableCell<>();
			cell.setInteger(!displayPercentages);
			cell.setPercent(displayPercentages);
			return cell;
		});
	}
}