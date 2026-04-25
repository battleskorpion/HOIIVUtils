package com.hoi4utils.ui.script

import com.hoi4utils.main.HOIIVUtilsConfig
import com.hoi4utils.script2.{PDXProperty, PDXPropertyList, PDXScript}
import com.typesafe.scalalogging.LazyLogging
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import org.controlsfx.control.SearchableComboBox

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Displays an editor pane for a [[PDXScript]].
 */
class PDXEditorPane(val pdxScript: PDXScript[?], var onUpdate: Option[Runnable]) extends AnchorPane with LazyLogging:

  val rootVBox: VBox = new VBox()
  val nullProperties: ListBuffer[PDXScript[?]] = ListBuffer.empty
  val nullPropertyNodes: ListBuffer[Node] = ListBuffer.empty
  var displayNullProperties: Boolean = false


  /* init */
  this.getChildren.add(rootVBox)

  // Anchor the vbox to all sides of the PDXEditorPane
  AnchorPane.setTopAnchor(rootVBox, 0.0);
  AnchorPane.setBottomAnchor(rootVBox, 0.0);
  AnchorPane.setLeftAnchor(rootVBox, 0.0);
  AnchorPane.setRightAnchor(rootVBox, 0.0);

  // Set padding and spacing for the vbox
  rootVBox.setPadding(new Insets(10));
  rootVBox.setSpacing(10);

  // Initialize the editor with the properties of the PDXScript
  drawEditor(pdxScript, rootVBox);

  def drawEditor(pdxScript: PDXScript[?], pane: Pane): Unit =
    val editorPDXNode = createEditorPDXNode(pdxScript, false, true)

    if (editorPDXNode != null) {
        pane.getChildren.add(editorPDXNode)
    } else {
        nullProperties += pdxScript
    }

    // Add a plus sign button to add new properties
    if (nullProperties.nonEmpty) {
        val addButton: Button = new Button("+")
        addButton.setOnAction(event => {
            if (displayNullProperties) hideNullProperties()
            else showNullProperties()
            displayNullProperties = !displayNullProperties
        });
        pane.getChildren.add(addButton)
        // initial
        if (displayNullProperties) {
            showNullProperties()
        }
    }

    /* post ui construction */
    if (HOIIVUtilsConfig.get("debug.colors").equals("true")) applyDebugBorders(pane)

  private def createEditorPDXNode(property: PDXScript[?], allowNull: Boolean, withLabel: Boolean): Node =
    val editorPropertyPane: Pane = property match
      case pdxList: PDXPropertyList[?] =>
        val vbox = VBox()
        vbox.setSpacing(4)
        vbox
      case pdx: PDXProperty[?] =>
        val hbox = HBox()
        hbox.setSpacing(4)
        hbox
      case _ =>
        val hbox = HBox()
        hbox.setSpacing(4)
        hbox
    val label: Option[Label] =
      if withLabel then
        val labelText = property match
          case pdxList: PDXPropertyList[?] => pdxList.pdxKey + " :="
          case pdx: PDXProperty[?] => pdx.pdxKey + " ="
          case pdx => pdx.pdxKey + " ="
        val label = new Label(labelText)
        label.setFont(Font.font("Monospaced"));
        label.setMinWidth(10)
        label.setPrefHeight(25)
        Some(label)
      else None

    val editorNode: Node = property match
      case pdx: PDXPropertyList[?] => visualizePDXList(pdx)
      case pdx: PDXProperty[String] => visualizeStringPDX(pdx)
      case _ =>
        logger.warn("Ui node unknown for property type: " + property.getClass)
        HBox()

    label.map(l => editorPropertyPane.getChildren.add(l))
    editorPropertyPane.getChildren.add(editorNode)
    editorPropertyPane

  private def createEditorNullPDXNode(property: PDXScript[?]): Node =
    val nullPropertyHBox: HBox = HBox()
    nullPropertyHBox.setSpacing(10)
    nullPropertyHBox.setPadding(new Insets(6, 6, 6, 20)); // Indent the null properties
    var allowNull = true

    val editorNode: Node = property match
      case pdx: PDXPropertyList[?] => visualizePDXList(pdx)
      case pdx: PDXProperty[String] => visualizeStringPDX(pdx)
      case _ =>
        logger.warn("Ui node unknown for property type: " + property.getClass)
        HBox()

    nullPropertyHBox.getChildren.add(editorNode)
    nullPropertyHBox

  private def visualizePDXList[T](pdxList: PDXPropertyList[T], allowNull: Boolean = false): Node =
    val subVBox: VBox = VBox()
    subVBox.setSpacing(10)
    if pdxList.nonEmpty then
      /* sub PDX visualization */
      pdxList.foreach { pdx => 
        // always allow null child to appear visually
        val subNode = createSubNode(true, pdx)
        subNode match
          case Some(node) =>
            val container: HBox = HBox()
            container.setSpacing(6)
            container.getChildren.add(subNode)

            // Create the remove button for this sub-element.
            val removeButton: Button = Button("Remove")
            removeButton.setOnAction(event => {
              // Remove this specific sub-element.
              pdxList.remove(pdx)
              reloadEditor()
            })
            container.getChildren.add(removeButton)

            subVBox.getChildren.add(container)
          case None => ()
      }

      /* new sub pdx button */
      val addPDXButton: Button = new Button("Add " + pdxList.getPDXTypeName())
      addPDXButton.setPrefWidth(200)
      addPDXButton.setOnAction(event => {
          pdx.addNewPDX()
          this.reloadEditor()
      })
      subVBox.getChildren.add(addPDXButton)

      subVBox
    else if allowNull then
//      val newPDX = applySomeSupplier()
//      createEditorPDXNode(newPDX.asInstanceOf[PDXScript[?]], allowNull, false)
      VBox()  // todo
    else
      /* modify sub pdx buttons */
      val modifySubPDXHBox = HBox()
      // add sub pdx
      val addPDXButton: Button = Button("Add " + pdxList.getPDXTypeName())
      addPDXButton.setPrefWidth(200)
      addPDXButton.setOnAction(event => {
//        val newPDX = pdx.applySomeSupplier()
//        // always allow null child to appear visually
//        var newPDXNode = createEditorPDXNode((PDXScript<?, ?>) newPDX, true, false);
//        if (newPDXNode != null) {
//            subVBox.getChildren().add(subVBox.getChildren().size() - 1, newPDXNode); // Add before the add button
//        }
        VBox() // todo
      })
      // remove sub pdx
      val removePDXButton: Button = Button("Remove")
      removePDXButton.setPrefWidth(80)
      removePDXButton.setOnAction(event => {
        // hover over pdx (highlights), remove on click

      })
      modifySubPDXHBox.getChildren.add(addPDXButton)

      subVBox

  private def visualizeStringPDX(pdx: PDXScript[String]): HBox =
    val hbox: HBox = HBox()
    val textField: TextField = TextField(pdx.getOrElse(""))
    textField.setPrefWidth(200)
    textField.setPrefHeight(25)
    textField.textProperty().addListener((observable, oldValue, newValue) => {
        pdx.setNode(newValue)
        if (newValue.nonEmpty && nullProperties.contains(property)) {
            reloadEditor()
        }
        else onPropertyUpdate()
    })
    hbox.getChildren.add(textField)
    hbox

  private def visualizeBooleanPDX(pdx: PDXScript[Boolean]): HBox =
    val hbox: HBox = HBox()
    val customCheckBox: Label = Label()
    customCheckBox.setText(pdx.$ ? "yes" : "no")
    customCheckBox.setFont(Font.font("Monospaced"))
    customCheckBox.setPrefHeight(25)
    customCheckBox.getStyleClass.add("custom-check-box")
    customCheckBox.setOnMouseClicked(event -> {
        pdx.invert()
        customCheckBox.setText(pdx.$() ? "yes" : "no")
        if nullProperties contains pdx then reloadEditor()
        else onPropertyUpdate()
    })
    hbox.getChildren.add(customCheckBox)
    hbox

  private def visualizeDoublePDX(pdx: PDXScript[Double]): HBox =
    new HBox() // todo
//    double minValue = pdx.isDefaultRange() ? pdx.minValue() : pdx.minValueNonInfinite();    // todo simplify?
//    double maxValue = pdx.isDefaultRange() ? pdx.maxValue() : pdx.maxValueNonInfinite();
//    double value = pdx.getOrElse(pdx.defaultValue());
//    Spinner<Double> spinner = new Spinner<>(
//            new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, value, 1));
//    return newSpinnerHBox(pdx, spinner);

  private def visualizeReferencePDX(pdx: PDXScript[Reference[?]]): ComboBox[String] =
    pdx match
      case pdx: PDXPropertyList[Reference[?]] =>
        val subVBox: VBox = new ReferencePDXListVBox(pdx, this::reloadEditor)
        subVBox.setSpacing(2)
        subVBox
      case _ =>
        val comboBox = new SearchableComboBox[String]()
        comboBox.setPrefWidth(200)
        comboBox.setPrefHeight(25)
        comboBox.getSelectionModel.select(pdx.getReferenceName())
        comboBox.setItems(FXCollections.observableArrayList(CollectionConverters.asJavaCollection(pdx.getReferenceCollectionNames())))
        comboBox.valueProperty().addListener((observable, oldValue, newValue) => {
          pdx.setReferenceName(newValue)
          if nullProperties contains pdx then reloadEditor
          else onPropertyUpdate()
        })
        comboBox

  /**
   * Perform some onUpdate action when the properties may have been updated
   */
  private def onPropertyUpdate(): Unit =
    onUpdate foreach(_.run())

  private def addLabelToHBox(pdx: PDXScript[?], hbox: HBox): Unit =
    val label: Label = new Label(pdx.pdxIdentifier() + " =")
    label.setFont(Font.font("Monospaced"))
    label.setMinWidth(10)
    label.setPrefHeight(25)
    hbox.getChildren.add(label)

  private def createSubNode(allowNull: Boolean, pdxScript: PDXScript[?]): Node =
    createEditorPDXNode(pdxScript, allowNull, false);

  /**
   * Clears the null properties list and nodes, and redraws the editor.
   */
  private def reloadEditor(): Unit =
    nullProperties.clear()
    nullPropertyNodes.clear()
    rootVBox.getChildren.clear()     // Clear existing children to reset the editor
    onPropertyUpdate()     // Properties may have been updated
    drawEditor(pdxScript, rootVBox)

  private def showNullProperties(): Unit =
    nullProperties.foreach(property => {
      //            HBox hbox = new HBox();
      //            hbox.setSpacing(10);
      //            hbox.setPadding(new Insets(0, 0, 0, 20)); // Indent the null properties
      //            Label label = new Label(property.getPDXIdentifier() + " =");
      //            label.setFont(Font.font("Monospaced"));
      //            label.setMinWidth(10);
      //            label.setPrefHeight(25);
      //            label.setStyle("-fx-text-fill: grey;");

      val editorPDXNode: Node = createEditorNullPDXNode(property)
      if (editorPDXNode != null) {
        //                hbox.getChildren().addAll(label, editorPDXNode);
        rootVBox.getChildren.add(rootVBox.getChildren.size() - 1, editorPDXNode) // Add before the add button
        nullPropertyNodes.add(editorPDXNode)
      }
    })

  private def hideNullProperties(): Unit =
    nullPropertyNodes.foreach(node => {
      rootVBox.getChildren.remove(node)
    })
    nullPropertyNodes.clear()

//  private HBox newSpinnerHBox[T](ValPDXScript<?, ?> pdx, Spinner<T> spinner): HBox =
//    HBox hbox = new HBox();
//    spinner.setPrefHeight(25);
//    spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
//        pdx.setNode(newValue);
//        if (nullProperties.contains(pdx)) {
//            reloadEditor();
//        }
//        else onPropertyUpdate()
//    })
//    hbox.getChildren().add(spinner)
//    hbox

  private def applyDebugBorders(parent: Parent): Unit =
    parent.getChildrenUnmodifiable.forEach {
      case vbox: VBox => vbox.setBorder(Border.stroke(Paint.valueOf("blue")))
      case hbox: HBox => hbox.setBorder(Border.stroke(Paint.valueOf("lightblue")))
      case label: Label => label.setBorder(Border.stroke(Paint.valueOf("orange")))
      case region: Region => region.setBorder(Border.stroke(Paint.valueOf("green")))
      case childParent: Parent => applyDebugBorders(childParent)
    }

  def showSaveButton(): Unit =
    val saveButton: Button = new Button("Save Script");
    saveButton.setOnAction(event => {
      logger.info("Saving PDXScript...")
      pdxScript match
        case pdxFile: PDXFile => pdxFile.save();
        case _ =>
          logger.warn("Cannot save PDXScript of type: " + pdxScript.getClass)
    })
    rootVBox.getChildren.add(saveButton)







