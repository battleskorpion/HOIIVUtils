module com.HOIIVUtils {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires org.apache.logging.log4j;
    requires java.sql;
    requires java.desktop;
    requires org.jetbrains.annotations;
    requires org.jgrapht.core;
    requires org.apache.poi.poi;
    requires aparapi; // leave aparapi last cause aparapi has terrible config
    requires org.xerial.sqlitejdbc;
    requires io.github.javadiffutils;
    requires org.fxmisc.richtext;
    requires reactfx;

    opens com.HOIIVUtils.ui to javafx.fxml;
    opens com.HOIIVUtils.ui.buildings to javafx.fxml;
    opens com.HOIIVUtils.ui.clausewitz_gfx to javafx.fxml;
    opens com.HOIIVUtils.ui.colorgen to javafx.fxml;
    opens com.HOIIVUtils.ui.console to javafx.fxml;
    opens com.HOIIVUtils.ui.focus_view to javafx.fxml;
    opens com.HOIIVUtils.ui.hoi4localization to javafx.fxml;
    opens com.HOIIVUtils.ui.javafx.export to javafx.fxml;
    opens com.HOIIVUtils.ui.javafx.image to javafx.fxml;
    opens com.HOIIVUtils.ui.javafx.table to javafx.fxml;
    opens com.HOIIVUtils.ui.map to javafx.fxml;
    opens com.HOIIVUtils.ui.menu to javafx.fxml;
    opens com.HOIIVUtils.ui.message to javafx.fxml;
    opens com.HOIIVUtils.ui.parser to javafx.fxml;
    opens com.HOIIVUtils.ui.settings to javafx.fxml;
    opens com.HOIIVUtils.ui.statistics to javafx.fxml;
    opens com.HOIIVUtils.ui.units to javafx.fxml;

    // Export other necessary packages (adjust if needed)
    exports com.HOIIVUtils.clausewitz_parser;
    exports com.HOIIVUtils.hoi4utils;
    exports com.HOIIVUtils.ui;
    exports com.HOIIVUtils.ui.buildings;
    exports com.HOIIVUtils.ui.clausewitz_gfx;
    exports com.HOIIVUtils.ui.colorgen;
    exports com.HOIIVUtils.ui.console;
    exports com.HOIIVUtils.ui.focus_view;
    exports com.HOIIVUtils.ui.hoi4localization;
    exports com.HOIIVUtils.ui.javafx.export;
    exports com.HOIIVUtils.ui.javafx.image;
    exports com.HOIIVUtils.ui.javafx.table;
    exports com.HOIIVUtils.ui.map;
    exports com.HOIIVUtils.ui.menu;
    exports com.HOIIVUtils.ui.message;
    exports com.HOIIVUtils.ui.parser;
    exports com.HOIIVUtils.ui.settings;
    exports com.HOIIVUtils.ui.statistics;
    exports com.HOIIVUtils.ui.units;

}