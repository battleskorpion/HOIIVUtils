package com.HOIIVUtils.ui;

import com.HOIIVUtils.hoi4utils.FileUtils;
import com.HOIIVUtils.hoi4utils.Settings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
	 * @param fxcomponent      The node (javafx component) that was pressed to open the chooser, must belong to a scene
	 * @param initialDirectory The initial directory of the file chooser, instead of the default user directory.
	 * @param ford             A quirky boolean that specifies whether you want to return a directory or file: true = return directory, false = return file
	 * @return theChosenOne, It is up to the the page to handle what you do if the user returns a null
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
	 * @param fxcomponent The node (javafx component) that was pressed to open the chooser, must belong to a scene
	 * @param ford        A quirky boolean that specifies whether you want to return a directory or file: true = return directory, false = return file
	 * @return theChosenOne, It is up to the the page to handle what you do if the user returns a null
	 * @see Node
	 */
	default File openChooser(Node fxcomponent, Boolean ford) {
		return openChooser(fxcomponent, ford, FileUtils.usersDocuments);
	}

	static void openGlobalErrorWindow(String s) {
		MessageController errWindow = new MessageController();
		errWindow.open("s");
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
	 * @param fxcomponent The node (javafx component), must belong to a scene. The stage the scene belongs to will be hidden.
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
		JOptionPane.showMessageDialog(null, exception, "ln: " + exception.getStackTrace()[0].getLineNumber(), JOptionPane.WARNING_MESSAGE);
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

	default <S> void loadTableView(TableViewWindow window, TableView<S> dataTable, ObservableList<S> data, List<Function<S, ?>> dataFunctions) {
		window.setDataTableCellFactories();

		setTableCellValueFactories(dataFunctions, dataTable);

		dataTable.setItems(data);       // country objects, cool! and necessary for the cell value factory,
													// this is giving the factories the list of objects to collect
													// their data from.

		System.out.println("Loaded data into table: " + dataTable.getId());
	}

	// todo put this in hoi4window parent class or whatever
	default <S> void setTableCellValueFactories(List<Function<S, ?>> dataFunctions, TableView<S> dataTable) {
		/* table columns */
		ObservableList<TableColumn<S, ?>> tableColumns = dataTable.getColumns();
		for (int i = 0; i < Math.min(dataFunctions.size(), tableColumns.size()); i++) {
			TableColumn<S, ?> tableColumn = tableColumns.get(i);
			Function<S, ?> dataFunction = dataFunctions.get(i);

			tableColumn.setCellValueFactory(FXWindow.cellDataCallback(dataFunction));
		}

		/* table rows */
	}

	/**
	 * @param propertyGetter
	 * @param <S>
	 * @param <T>
	 * @return
	 */
	static <S, T> Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> cellDataCallback(Function<S, ?> propertyGetter) {
		return cellData -> {
//			if (Settings.DEV_MODE.enabled()) {
//				System.out.println("Table callback created, data: " + propertyGetter.apply(cellData.getValue()));
//			}
			// return new SimpleObjectProperty<T>((T) propertyGetter.apply(cellData.getValue())); // ? Type safety: Unchecked cast from capture#6-of ? to TJava(16777761)
			@SuppressWarnings("unchecked")
			T result = (T) propertyGetter.apply(cellData.getValue());   // yap
			return new SimpleObjectProperty<>(result);
			// mehhhh
		};
	}

	/**
	 * 	Update cell behavior within a column
	 */
	static <S, T extends TableCell<S, Double>> void updateColumnPercentBehavior(TableColumn<S, Double> column, boolean resourcesPercent) {
		column.setCellFactory(col -> {
			IntegerOrPercentTableCell<S> cell = new IntegerOrPercentTableCell<>();
			if (resourcesPercent) {
				cell.setPercent(true);
			} else {
				cell.setInteger(true);
			}
			return cell;
		});
	}
}
