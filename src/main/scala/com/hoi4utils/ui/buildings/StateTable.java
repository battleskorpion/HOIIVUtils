package com.hoi4utils.ui.buildings;

import com.hoi4utils.ui.JavaFXUIManager;
import com.hoi4utils.ui.javafx_ui.table.DoubleOrPercentTableCell;
import com.hoi4utils.ui.javafx_ui.table.DoubleTableCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.map.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.function.Function;

public class StateTable extends TableView<State> {
    public static final Logger logger = LogManager.getLogger(StateTable.class);

    private final TableColumn<State, String> stateColumn                = new TableColumn<>("State");
    private final TableColumn<State, Integer> populationColumn          = new TableColumn<>("Population");
    private final TableColumn<State, Integer> civFactoryColumn          = new TableColumn<>("Civ Factories");
    private final TableColumn<State, Integer> milFactoryColumn          = new TableColumn<>("Mil Factories");
    private final TableColumn<State, Integer> dockyardsColumn           = new TableColumn<>("Dockyards");
    private final TableColumn<State, Integer> airfieldsColumn           = new TableColumn<>("Airfields");
    private final TableColumn<State, Double> civMilRatioColumn          = new TableColumn<>("Civ/Mil Ratio");
    private final TableColumn<State, Double> popFactoryRatioColumn      = new TableColumn<>("Pop/Factory Ratio");
    private final TableColumn<State, Double> popCivRatioColumn          = new TableColumn<>("Pop/Civ Ratio");
    private final TableColumn<State, Double> popMilRatioColumn          = new TableColumn<>("Pop/Mil Ratio");
    private final TableColumn<State, Double> popAirCapacityRatioColumn  = new TableColumn<>("Pop/Air Cap Ratio");
    private final TableColumn<State, Double> aluminiumColumn            = new TableColumn<>("Aluminium");
    private final TableColumn<State, Double> chromiumColumn             = new TableColumn<>("Chromium");
    private final TableColumn<State, Double> oilColumn                  = new TableColumn<>("Oil");
    private final TableColumn<State, Double> rubberColumn               = new TableColumn<>("Rubber");
    private final TableColumn<State, Double> steelColumn                = new TableColumn<>("Steel");
    private final TableColumn<State, Double> tungstenColumn             = new TableColumn<>("Tungsten");

    public StateTable() {
        initialize();
    }

    private void initialize() {
        // make columns fill the width
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // bind every column
        bindColumn(stateColumn, (s -> s.name().getOrElse("[Unknown]")), String::compareTo);
        bindColumn(populationColumn, State::population, Integer::compareTo);
        bindColumn(civFactoryColumn, State::civilianFactories, Integer::compareTo);
        bindColumn(milFactoryColumn, State::militaryFactories, Integer::compareTo);
        bindColumn(dockyardsColumn, State::navalDockyards, Integer::compareTo);
        bindColumn(airfieldsColumn, State::airfields, Integer::compareTo);
        bindColumn(civMilRatioColumn, State::civMilFactoryRatio, Double::compareTo);
        bindColumn(popFactoryRatioColumn, State::populationFactoryRatio, Double::compareTo);
        bindColumn(popCivRatioColumn, State::populationCivFactoryRatio, Double::compareTo);
        bindColumn(popMilRatioColumn, State::populationMilFactoryRatio, Double::compareTo);
        bindColumn(popAirCapacityRatioColumn, State::populationAirCapacityRatio, Double::compareTo);
        
        aluminiumColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
        chromiumColumn .setCellFactory(col -> new DoubleOrPercentTableCell<>());
        oilColumn      .setCellFactory(col -> new DoubleOrPercentTableCell<>());
        rubberColumn   .setCellFactory(col -> new DoubleOrPercentTableCell<>());
        steelColumn    .setCellFactory(col -> new DoubleOrPercentTableCell<>());
        tungstenColumn .setCellFactory(col -> new DoubleOrPercentTableCell<>());

        // comparators on those too
        Comparator<Double> dblCmp = Double::compareTo;
        aluminiumColumn.setComparator(dblCmp);
        chromiumColumn.setComparator(dblCmp);
        oilColumn.setComparator(dblCmp);
        rubberColumn.setComparator(dblCmp);
        steelColumn.setComparator(dblCmp);
        tungstenColumn.setComparator(dblCmp);
        
        // add columns to table
        getColumns().addAll(
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
        );
    }

    <T> void bindColumn(
            TableColumn<State,T> col,
            Function<State,T> mapper,
            Comparator<T> comparator
    ) {
        col.setCellFactory(c -> new TableCell<State,T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(mapper.apply(getTableRow().getItem())));
                }
            }
        });
        col.setComparator(comparator);
    }

    public void updateResourcesColumnsPercentBehavior(boolean resourcesPercent) {
        JavaFXUIManager.updateColumnPercentBehavior(aluminiumColumn, resourcesPercent);
        JavaFXUIManager.updateColumnPercentBehavior(chromiumColumn, resourcesPercent);
        JavaFXUIManager.updateColumnPercentBehavior(oilColumn, resourcesPercent);
        JavaFXUIManager.updateColumnPercentBehavior(rubberColumn, resourcesPercent);
        JavaFXUIManager.updateColumnPercentBehavior(steelColumn, resourcesPercent);
        JavaFXUIManager.updateColumnPercentBehavior(tungstenColumn, resourcesPercent);
    }

    public void setDataTableCellFactories() {
        // table cell factories
        civMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
        popFactoryRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
        popCivRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
        popMilRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
        popAirCapacityRatioColumn.setCellFactory(col -> new DoubleTableCell<>());
        aluminiumColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
        chromiumColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
        oilColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
        rubberColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
        steelColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
        tungstenColumn.setCellFactory(col -> new DoubleOrPercentTableCell<>());
    }
    
    public void setStates(State... states) {
        getItems().setAll(states); 
    }
    
    public void clearStates() {
        getItems().clear();
    }
}
