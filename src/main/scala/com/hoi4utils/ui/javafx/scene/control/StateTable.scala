package com.hoi4utils.ui.javafx.scene.control

import com.hoi4utils.hoi4mod.map.state.State
import com.hoi4utils.ui.javafx.application.JavaFXUIManager
import com.typesafe.scalalogging.LazyLogging
import javafx.beans.property.{IntegerProperty, ObjectProperty, ReadOnlyObjectWrapper, StringProperty}
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.control.{TableCell, TableColumn, TableView}
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import org.apache.logging.log4j.{LogManager, Logger}
import scalafx.collections.ObservableBuffer
import scalafx.util.converter.IntStringConverter

import java.util.Comparator
import java.util.function.Function

class StateTable extends TableView[State] with LazyLogging {
	final private val stateColumn = new TableColumn[State, String]("State")
	final private val populationColumn = new TableColumn[State, Integer]("Population")
	final private val civFactoryColumn = new TableColumn[State, Integer]("Civilian Factories")
	final private val milFactoryColumn = new TableColumn[State, Integer]("Military Factories")
	final private val dockyardsColumn = new TableColumn[State, Integer]("Dockyards")
	final private val airfieldsColumn = new TableColumn[State, Integer]("Airfields")
	final private val civMilRatioColumn = new TableColumn[State, java.lang.Double]("Civ/Mil Ratio")
	final private val popFactoryRatioColumn = new TableColumn[State, java.lang.Double]("Pop/Factory Ratio")
	final private val popCivRatioColumn = new TableColumn[State, java.lang.Double]("Pop/Civ Ratio")
	final private val popMilRatioColumn = new TableColumn[State, java.lang.Double]("Pop/Mil Ratio")
	final private val popAirCapacityRatioColumn = new TableColumn[State, java.lang.Double]("Pop/Air Cap. Ratio")
	final private val aluminiumColumn = new TableColumn[State, java.lang.Double]("Aluminium")
	final private val chromiumColumn = new TableColumn[State, java.lang.Double]("Chromium")
	final private val oilColumn = new TableColumn[State, java.lang.Double]("Oil")
	final private val rubberColumn = new TableColumn[State, java.lang.Double]("Rubber")
	final private val steelColumn = new TableColumn[State, java.lang.Double]("Steel")
	final private val tungstenColumn = new TableColumn[State, java.lang.Double]("Tungsten")

	/* constructor */
	initialize()

	private def initialize(): Unit = {
		// make columns fill the width
		setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS)

		// bind every column
//		bindColumn(stateColumn, (s -> s.name().getOrElse("[Unknown]")), String::compareTO);
//		bindColumn(populationColumn, State::population, Integer::compareTo);
//		bindColumn(civFactoryColumn, State::civilianFactories, Integer::compareTo);
//		bindColumn(milFactoryColumn, State::militaryFactories, Integer::compareTo);
//		bindColumn(dockyardsColumn, State::navalDockyards, Integer::compareTo);
//		bindColumn(airfieldsColumn, State::airfields, Integer::compareTo);
//		bindColumn(civMilRatioColumn, State::civMilFactoryRatio, java.lang.Double::compareTo);
//		bindColumn(popFactoryRatioColumn, State::populationFactoryRatio, java.lang.Double::compareTo);
//		bindColumn(popCivRatioColumn, State::populationCivFactoryRatio, java.lang.Double::compareTo);
//		bindColumn(popMilRatioColumn, State::populationMilFactoryRatio, java.lang.Double::compareTo);
//		bindColumn(popAirCapacityRatioColumn, State::populationAirCapacityRatio, java.lang.Double::compareTo);

		// ----- CELL FACTORIES (rendering/editing) -----
		populationColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter))
		civFactoryColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter))
		milFactoryColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter))
		dockyardsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter))
		airfieldsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter))
//		civMilRatioColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleTableCell[State])   // TODO:
//		popFactoryRatioColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleTableCell[State])
//		popCivRatioColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleTableCell[State])
//		popMilRatioColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleTableCell[State])
//		popAirCapacityRatioColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleTableCell[State])
		//        aluminiumColumn.setCellFactory(TextFieldTableCell.forTableColumn(new java.lang.DoubleStringConverter()));
		//        chromiumColumn.setCellFactory(TextFieldTableCell.forTableColumn(new java.lang.DoubleStringConverter()));
		//        oilColumn.setCellFactory(TextFieldTableCell.forTableColumn(new java.lang.DoubleStringConverter()));
		//        rubberColumn.setCellFactory(TextFieldTableCell.forTableColumn(new java.lang.DoubleStringConverter()));
		//        steelColumn.setCellFactory(TextFieldTableCell.forTableColumn(new java.lang.DoubleStringConverter()));
		//        tungstenColumn.setCellFactory(TextFieldTableCell.forTableColumn(new java.lang.DoubleStringConverter()));
//		aluminiumColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleOrPercentTableCell[State])  // TODO:
//		chromiumColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleOrPercentTableCell[State])
//		oilColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleOrPercentTableCell[State])
//		rubberColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleOrPercentTableCell[State])
//		steelColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleOrPercentTableCell[State])
//		tungstenColumn.setCellFactory((col: TableColumn[State, java.lang.Double]) => new java.lang.DoubleOrPercentTableCell[State])
		// ----- COMPARATORS -----
		// TODO: was not working for scala

		// ----- EDIT COMMIT HANDLERS -----
		populationColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, Integer]) => ev.getRowValue.population_=?(ev.getNewValue))
		civFactoryColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, Integer]) => ev.getRowValue.civilianFactories_=?(ev.getNewValue))
		milFactoryColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, Integer]) => ev.getRowValue.militaryFactories_=?(ev.getNewValue))
		dockyardsColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, Integer]) => ev.getRowValue.navalDockyards_=?(ev.getNewValue))
		airfieldsColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, Integer]) => ev.getRowValue.airfields_=?(ev.getNewValue))
		/** resources */
		aluminiumColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, java.lang.Double]) => ev.getRowValue.setResource("aluminium", ev.getNewValue))
		chromiumColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, java.lang.Double]) => ev.getRowValue.setResource("chromium", ev.getNewValue))
		oilColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, java.lang.Double]) => ev.getRowValue.setResource("oil", ev.getNewValue))
		rubberColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, java.lang.Double]) => ev.getRowValue.setResource("rubber", ev.getNewValue))
		steelColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, java.lang.Double]) => ev.getRowValue.setResource("steel", ev.getNewValue))
		tungstenColumn.setOnEditCommit((ev: TableColumn.CellEditEvent[State, java.lang.Double]) => ev.getRowValue.setResource("tungsten", ev.getNewValue))
		// add columns to table
		this.getColumns.addAll(
			stateColumn, 
			populationColumn, 
			civFactoryColumn, 
			milFactoryColumn,
			dockyardsColumn,
			airfieldsColumn,
			civMilRatioColumn,
			popFactoryRatioColumn,
			popCivRatioColumn,
			popMilRatioColumn,
			popAirCapacityRatioColumn,
			aluminiumColumn,
			chromiumColumn,
			oilColumn,
			rubberColumn,
			steelColumn,
			tungstenColumn
		)
	}

//	private[buildings] def bindColumn[T](col: TableColumn[State, T], mapper: Function[State, T], comparator: Comparator[T]): Unit = {
//		col.setCellFactory((c: TableColumn[State, T]) => new TableCell[State, T]() {
//			override protected def updateItem(item: T, empty: Boolean): Unit = {
//				super.updateItem(item, empty)
//				if (empty || getTableRow.getItem == null) setText(null)
//				else setText(String.valueOf(mapper.apply(getTableRow.getItem)))
//			}
//		})
//		col.setComparator(comparator)
//	}

	def updateResourcesColumnsPercentBehavior(resourcesPercent: Boolean): Unit = {
		JavaFXUIManager.updateColumnPercentBehavior(aluminiumColumn, resourcesPercent)
		JavaFXUIManager.updateColumnPercentBehavior(chromiumColumn, resourcesPercent)
		JavaFXUIManager.updateColumnPercentBehavior(oilColumn, resourcesPercent)
		JavaFXUIManager.updateColumnPercentBehavior(rubberColumn, resourcesPercent)
		JavaFXUIManager.updateColumnPercentBehavior(steelColumn, resourcesPercent)
		JavaFXUIManager.updateColumnPercentBehavior(tungstenColumn, resourcesPercent)
	}

	def setDataTableCellFactories(): Unit = ???

	def setEditableColumns(editable: Boolean): Unit = {
		this.setEditable(true)
		// stateColumn is not editable
		populationColumn.setEditable(editable)
		civFactoryColumn.setEditable(editable)
		milFactoryColumn.setEditable(editable)
		dockyardsColumn.setEditable(editable)
		airfieldsColumn.setEditable(editable)
		//civMilRatioColumn is not editable
		//popFactoryRatioColumn is not editable
		//popCivRatioColumn is not editable
		//popMilRatioColumn is not editable
		//popAirCapacityRatioColumn is not editable
		aluminiumColumn.setEditable(editable)
		chromiumColumn.setEditable(editable)
		oilColumn.setEditable(editable)
		rubberColumn.setEditable(editable)
		steelColumn.setEditable(editable)
		tungstenColumn.setEditable(editable)
	}

	def setStates(states: State*): Unit = {
		setItems(FXCollections.observableArrayList(states*))
	}

	def clearStates(): Unit = {
		this.getItems.clear()
	}
}

