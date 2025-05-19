package com.hoi4utils.ui;

import com.hoi4utils.HOIIVUtils;
import com.hoi4utils.ui.javafx_ui.table.DoubleOrPercentTableCell;
import com.hoi4utils.ui.javafx_ui.table.TableViewWindow;
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
import scala.Function1;
import scala.collection.Iterable;
import scala.jdk.javaapi.CollectionConverters;
import scala.jdk.javaapi.FunctionConverters;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface JavaFXUIManager {

	static File openChooser(Stage stage, File initialDirectory, boolean ford) {
		File usersDocuments = new File(System.getProperty("user.home") + File.separator + "Documents");
		File theChosenOne;
		if (ford) {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			if (initialDirectory != null && initialDirectory.exists() && initialDirectory.isDirectory()) {
				directoryChooser.setInitialDirectory(initialDirectory);
			} else {
				directoryChooser.setInitialDirectory(usersDocuments);
			}
			theChosenOne = directoryChooser.showDialog(stage);
		} else {
			FileChooser fileChooser = new FileChooser();
			if (initialDirectory != null && initialDirectory.exists() && initialDirectory.isDirectory()) {
				fileChooser.setInitialDirectory(initialDirectory);
			} else {
				fileChooser.setInitialDirectory(usersDocuments);
			}
			theChosenOne = fileChooser.showOpenDialog(stage);
		}
		return theChosenOne;
	}

	static File openChooser(Node fxcomponent, File initialDirectory, boolean ford) {
		return openChooser((Stage) (fxcomponent.getScene().getWindow()), initialDirectory, ford);
	}

	static File openChooser(Node fxcomponent, Boolean ford) {
		File usersDocuments = new File(System.getProperty("user.home") + File.separator + "Documents");
		return openChooser(fxcomponent, usersDocuments, ford);
	}

	static File openChooser(File initialDirectory, boolean ford) {
		return openChooser(new Stage(), initialDirectory, ford);
	}

	default void decideScreen(Stage stage) {
		Screen screen = validateAndGetPreferredScreen();
		if (screen == null) {
			return;
		}
		
		Rectangle2D bounds = screen.getVisualBounds();
		stage.setX(bounds.getMinX() + 200);
		stage.setY(bounds.getMinY() + 200);
	}
	
	default Screen validateAndGetPreferredScreen() {
		int preferredScreen = -1;
		try {
			preferredScreen = Integer.parseInt(HOIIVUtils.get("preferred.screen"));
		} catch (NumberFormatException e) {
			return null;
		}

		ObservableList<Screen> screens = Screen.getScreens();
		
		if (preferredScreen > screens.size() - 1) {
			return null;
		}
		
		Screen screen = screens.get(preferredScreen);
		if (screen == null) {
			return null;
		}
		
		return screen;
	}

	default void closeWindow(Button button) {
		try {
			((Stage) (button.getScene().getWindow())).close();
		} catch (Exception exception) {
			openError(exception);
		}
	}

	default void hideWindow(Node fxcomponent) {
		try {
			fxcomponent.getScene().getWindow().hide();
		} catch (Exception exception) {
			openError(exception);
		}
	}

	default void openError(Exception exception) {
		exception.printStackTrace();
		JOptionPane.showMessageDialog(null, exception, "ln: " + exception.getStackTrace()[0].getLineNumber(),
				JOptionPane.WARNING_MESSAGE);
	}

	void open();

	void setFxmlResource(String fxmlResource);

	void setTitle(String title);

	default <S> void loadTableView(TableViewWindow window, TableView<S> table, ObservableList<S> data,
	                               List<Function<S, ?>> dataFunctions) {
		window.setDataTableCellFactories();

		setTableCellValueFactories(dataFunctions, table);

		table.setItems(data);       // this is giving the factories the list of objects to collect
									// their data from.

		System.out.println("Loaded data into table: " + table.getId());
	}

	default <S> void loadTableView(TableViewWindow window, TableView<S> table, ObservableList<S>
			data, Iterable<Function1<S,?>> dataFunctions) {
		var fns = CollectionConverters.asJava(dataFunctions);
		List<Function<S, ?>> fnsJava = new ArrayList<>();
		fns.forEach((x) -> fnsJava.add(FunctionConverters.asJavaFunction(x)));
		loadTableView(window, table, data, fnsJava);
	}
	
	
	default <S> void loadTreeTableView(TableViewWindow window, TreeTableView<S> table, ObservableList<S> data,
	                                   List<Function<S, ?>> dataFunctions, Function<S, ?> parentingFunction) {
		window.setDataTableCellFactories();


	}

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

	static <S, T> Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> tableCellDataCallback(
			Function<S, ?> propertyGetter) {
		return cellData -> {
				System.out.println("Table callback created, data: " + propertyGetter.apply(cellData.getValue()));
			// unchecked cast is necessary because the compiler doesn't know that the given
			// function will return a value of type T
			@SuppressWarnings("unchecked")
			T result = (T) propertyGetter.apply(cellData.getValue()); // yap
			return new SimpleObjectProperty<>(result);
		};
	}

	static <S, T> Callback<TreeTableColumn.CellDataFeatures<S, T>, ObservableValue<T>> treeTableCellDataCallback(
			Function<S, ?> propertyGetter) {
		return cellData -> {
				System.out.println("Table callback created, data: " + propertyGetter.apply(cellData.getValue().getValue()));
			// unchecked cast is necessary because the compiler doesn't know that the given
			// function will return a value of type T
			@SuppressWarnings("unchecked")
			T result = (T) propertyGetter.apply(cellData.getValue().getValue());
			return new SimpleObjectProperty<>(result);
		};
	}

	static <S> void updateColumnPercentBehavior(TableColumn<S, Double> column,
	                                            boolean displayPercentages) {
		// Set the cell factory for the column to a cell that can display
		// either integers or percentages
		column.setCellFactory(col -> {
			// Create a new cell instance that can display either integers or percentages
			DoubleOrPercentTableCell<S> cell = new DoubleOrPercentTableCell<>();
			cell.setDouble(!displayPercentages);
			cell.setPercent(displayPercentages);
			return cell;
		});
	}
}
