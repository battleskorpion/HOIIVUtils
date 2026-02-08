//package com.hoi4utils.ui.javafx.scene.layout
//
//import com.hoi4utils.databases.effect.{Effect, EffectDatabase}
//import com.hoi4utils.script.PDXScript
//import com.hoi4utils.script.seq.CollectionPDX
//import com.typesafe.scalalogging.LazyLogging
//import javafx.beans.value.ObservableValue
//import javafx.collections.FXCollections
//import javafx.collections.transformation.FilteredList
//import javafx.event.ActionEvent
//import javafx.geometry.Insets
//import javafx.scene.control.*
//import javafx.scene.layout.*
//import javafx.scene.paint.Paint
//import javafx.scene.{Node, Parent}
//import org.jetbrains.annotations.Nullable
//
///**
// * A Pane that displays an editor for a PDXScript.
// */
//// todo may make abstract instead of type Effect
//class CollectionPDXSearchPane[T <: PDXScript[?, ?]](private val pdxScript: CollectionPDX[T]) extends AnchorPane with LazyLogging:
//  /* UI */
//  private val showDebugBorders = false // todo
//
//  final private var rootVBox: VBox = new VBox
//  // UI Controls for filtering
//  private var searchField: TextField = new TextField
//  private var effectListView: ListView[T] = null
//
//  this.getChildren.add(rootVBox)
//  // Anchor the vbox to all sides of the PDXEditorPane
//  AnchorPane.setTopAnchor(rootVBox, 0.0)
//  AnchorPane.setBottomAnchor(rootVBox, 0.0)
//  AnchorPane.setLeftAnchor(rootVBox, 0.0)
//  AnchorPane.setRightAnchor(rootVBox, 0.0)
//  // Set padding and spacing for the vbox
//  rootVBox.setPadding(new Insets(10))
//  rootVBox.setSpacing(10)
//  drawEditor(rootVBox)
//
//  private def drawEditor(rootVBox: VBox): Unit =
//    searchField.setPromptText("Search by name...")
//    var categoryCombo: ComboBox[String] = null
//    categoryCombo = new ComboBox[String]
//    categoryCombo.setPromptText("Category")
//    categoryCombo.getItems.addAll("All", "Economy", "Military", "Diplomacy")
//    categoryCombo.setValue("All") // Default
//
//    pdxScript match
//      case effect: CollectionPDX[Effect] =>
//        val allEffects = FXCollections.observableArrayList(EffectDatabase.effects*)
//        val filteredEffects = new FilteredList[Effect](allEffects, (e: Effect) => true)
//        searchField.textProperty.addListener((obs: ObservableValue[? <: String], oldVal: String, newVal: String) => applyFilter(categoryCombo, filteredEffects))
//        categoryCombo.valueProperty.addListener((obs: ObservableValue[? <: String], oldVal: String, newVal: String) => applyFilter(categoryCombo, filteredEffects))
//        effectListView = new ListView[Effect](filteredEffects)
//        effectListView.setPrefHeight(300)
//        effectListView.setCellFactory: (listView: ListView[Effect]) =>
//          new ListCell[Effect]():
//            override protected def updateItem(@Nullable effect: Effect, empty: Boolean): Unit =
//              super.updateItem(effect, empty)
//              if empty || effect == null then
//                setText(null)
//                setGraphic(null)
//              else setText(effect.pdxIdentifier)
//
//        effectListView.getSelectionModel.selectedItemProperty.addListener: (obs: ObservableValue[? <: Effect], oldItem: Effect, newEffect: Effect) =>
//          if newEffect != null then
//            logger.debug("Selected: " + newEffect)
//            searchField.setMaxHeight(100)
//            var newPDX: Effect = null
//            try newPDX = newEffect.clone.asInstanceOf[Effect]
//            catch
//              case e: CloneNotSupportedException =>
//                throw new RuntimeException(e)
//
//            val pdxEditorPane = new PDXEditorPane(newPDX)
//            rootVBox.getChildren.removeIf((node: Node) => node.isInstanceOf[PDXEditorPane])
//            rootVBox.getChildren.add(pdxEditorPane)
//            // OK button
//            val okButton = new Button("OK")
//            okButton.setOnAction: (event: ActionEvent) =>
//              pdxScript += newPDX // this is okay
//              reloadEditor() // Reload the editor to reflect changes
//
//            rootVBox.getChildren.add(okButton)
//
//        val filterBar = new HBox(10, new Label("Search:"), searchField, new Label("Category:"), categoryCombo)
//        rootVBox.getChildren.addAll(filterBar, effectListView)
//
//      case null =>
//        throw new IllegalArgumentException("CollectionPDXSearchPane can only be used with CollectionPDX[Effect]")
//
//  /**
//   * Clears the null properties list and nodes, and redraws the editor
//   */
//  private def reloadEditor(): Unit =
//    rootVBox.getChildren.clear() // Clear existing children to reset the editor
//  //        onPropertyUpdate();     // Properties may have been updated
//  //        drawEditor(pdxScript, rootVBox);
//
//  /**
//   * The filtering logic. Called whenever the search text or category changes.
//   */
//  private def applyFilter(categoryCombo: ComboBox[String], filteredEffects: FilteredList[Effect]): Unit =
//    val searchText = if searchField.getText == null then ""
//    else searchField.getText.trim.toLowerCase
//    val selectedCategory = categoryCombo.getValue
//    filteredEffects.setPredicate: (effectItem: Effect) =>
//      def foo(effectItem: Effect): Boolean =
//        // Category filter
//        if !("All" == selectedCategory) && !effectItem.effectCategory.equalsIgnoreCase(selectedCategory) then return false
//        // Search filter
//        if searchText.isEmpty then return true // No search text => all pass
//        effectItem.pdxIdentifier.toLowerCase.contains(searchText)
//
//      foo(effectItem)
//
//  private def applyDebugBorders(parent: Parent): Unit =
//    for node <- parent.getChildrenUnmodifiable.toArray do
//      node match
//        case region: Region => region match
//          case vbox: VBox => vbox.setBorder(Border.stroke(Paint.valueOf("blue")))
//          case hbox: HBox => hbox.setBorder(Border.stroke(Paint.valueOf("lightblue")))
//          case label: Label => label.setBorder(Border.stroke(Paint.valueOf("orange")))
//          case _ => region.setBorder(Border.stroke(Paint.valueOf("green")))
//        case childParent: Parent =>
//          applyDebugBorders(childParent)  // Recursively apply to children