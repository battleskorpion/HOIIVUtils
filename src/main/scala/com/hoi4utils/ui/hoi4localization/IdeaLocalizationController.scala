package com.hoi4utils.ui.hoi4localization

import com.hoi4utils.HOIIVFiles
import com.hoi4utils.hoi4.idea.{Idea, IdeaFile}
import com.hoi4utils.localization.{Localization, Property}
import com.hoi4utils.ui.{HOIIVUtilsAbstractController, JavaFXUIManager}
import com.hoi4utils.ui.javafx_ui.table.TableViewWindow
import javafx.collections.{FXCollections, ObservableList}
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.paint.Color

import java.io.File
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*

/**
 * todo: modify design to work with new localization system
 */
class IdeaLocalizationController extends HOIIVUtilsAbstractController with TableViewWindow:

  @FXML private var numLocAddedLabel: Label = uninitialized
  @FXML private var ideaFileTextField: TextField = uninitialized
  @FXML private var ideaFileBrowseButton: Button = uninitialized
  @FXML private var ideaFileNameLabel: Label = uninitialized
  @FXML private var ideaLocFileTextField: TextField = uninitialized
  @FXML private var ideaLocFileBrowseButton: Button = uninitialized
  @FXML private var loadButton: Button = uninitialized
  @FXML private var saveButton: Button = uninitialized
  @FXML private var ideaListTable: TableView[Idea] = uninitialized
  @FXML private var ideaIDColumn: TableColumn[Idea, String] = uninitialized
  @FXML private var ideaNameColumn: TableColumn[Idea, String] = uninitialized

  private var ideaFile: IdeaFile = uninitialized

  private val ideaObservableList: ObservableList[Idea] = FXCollections.observableArrayList()

  // Constructor initialization
  setFxmlResource("IdeaLocalization.fxml")
  setTitle("HOIIVUtils Idea Localization")

  /**
   * Initializes the window.
   *
   * This method is automatically called after the FXML file has been loaded.
   * It sets up the table view and sets the save button to be disabled.
   */
  @FXML
  def initialize(): Unit =
    // Set up the table view
    loadTableView(this, ideaListTable, ideaObservableList, Idea.getDataFunctions)

    // Set the save button to be disabled
    saveButton.setDisable(true)

  def handleIdeaFileBrowseButtonAction(): Unit =
    val initialIdeaDirectory = HOIIVFiles.Mod.ideas_folder
    val selectedFile = JavaFXUIManager.openChooser(ideaFileBrowseButton, initialIdeaDirectory, false)

    println(selectedFile)

    if selectedFile != null then
      ideaFileTextField.setText(selectedFile.getAbsolutePath)
      ideaFile = IdeaFile(selectedFile) // todo?
      ideaFileNameLabel.setText(ideaFile.toString)
    else
      ideaFileNameLabel.setText("[not found]")

  def handleIdeaLocFileBrowseButtonAction(): Unit = ???
  // Commented out implementation as in original
  //        val initialIdeaLocDirectory = HOIIVUtilsFiles.mod_localization_folder
  //        val selectedFile = openChooser(ideaLocFileBrowseButton, initialIdeaLocDirectory, false)
  //
  //            println(selectedFile)
  //
  //        if selectedFile != null then
  //            ideaLocFileTextField.setText(selectedFile.getAbsolutePath)
  //            try
  //                ideaLocFile = LocalizationFile(selectedFile)
  //            catch
  //                case e: IllegalLocalizationFileTypeException =>
  //                    openError(e)
  //                    return
  //            // ideaFile.setLocalization(ideaLocFile) // todo ?
  //            println(s"Set localization file of $ideaFile to $ideaLocFile")

  /**
   * Handles the action of the load button.
   * Loads the localization file of the idea tree into the idea tree.
   * If the localization file or idea tree is not properly initialized, it will
   * open an error window.
   */
  def handleLoadButtonAction(): Unit = ???
  // Commented out implementation as in original
  //        if ideaLocFile == null || ideaFile == null then
  //            // Handle the case where ideaLocFile or ideaFile is not properly initialized
  //            val window = MessageController()
  //            window.open("Error: Idea localization or idea tree not properly initialized.")
  //            return
  //
  //        /* load idea loc */
  //        try
  //            // Load the localization file into the idea tree
  //            val numLocalizedIdeas = FixIdea.addIdeaLoc(ideaFile, ideaLocFile)
  //            // Update the number of localized ideas displayed on the window
  //            updateNumLocalizedIdeaes(numLocalizedIdeas)
  //        catch
  //            case e: IOException =>
  //                // If the file cannot be loaded, open an error window
  //                openError(e)
  //                return
  //
  //        // Update the observable list of ideas
  //        updateObservableIdeaList()
  //
  //        /* enable saving of localization */
  //        // After the localization file has been loaded, enable the save button
  //        saveButton.setDisable(false)

  /**
   * Updates the idea observable list by clearing the list and adding all
   * ideas from the idea file.
   */
  private def updateObservableIdeaList(): Unit =
    // Clear the list
    ideaObservableList.clear()

    // Add all ideas from the idea file to the list
    ideaObservableList.addAll(ideaFile.listIdeas.asJava)

  /**
   * Updates the idea observable list by replacing the idea with the given id with
   * the given idea.
   * If the idea is not found in the list, it will be added to the list.
   *
   * @param idea the idea to update in the list
   */
  private def updateObservableIdeaList(idea: Idea): Unit =
    if idea == null then
      println("Idea was null and therefore did not update idea list.")
      return

    // Find the index of the idea in the list
    val ideaList = ideaObservableList.asScala
    val existingIndex = ideaList.indexWhere(_.id == idea.id)

    if existingIndex >= 0 then
      // Update the idea in the list
      ideaObservableList.remove(existingIndex)
      ideaObservableList.add(existingIndex, idea)
    else
      // Idea not found in list, add it
      ideaObservableList.add(idea)

  private def updateNumLocalizedIdeaes(numLocalizedIdeaes: Int): Unit =
    numLocAddedLabel.setText(
      numLocAddedLabel.getText.replace("x", numLocalizedIdeaes.toString)
    )

  /**
   * Handles the action of the save button.
   * Saves the localization file.
   * If the localization file is not properly initialized, it will open an error
   * window.
   */
  def handleSaveButtonAction(): Unit = ???
  // Commented out implementation as in original
  //        if ideaLocFile == null then
  //            // Handle the case where ideaLocFile is not properly initialized
  //            val window = MessageController()
  //            window.open("Error: Idea localization file not properly initialized.")
  //            return
  //
  //        try
  //            // Saves the localization file
  //            ideaLocFile.writeLocalization()
  //        catch
  //            case e: IOException =>
  //                // If the file cannot be saved, open an error window
  //                openError(e)

  override def setDataTableCellFactories(): Unit =
    /* column factory */
    // ideaDescColumn.setCellFactory(TextFieldTableCell.forTableColumn()) // This
    // makes the column editable

    /* row factory */
    ideaListTable.setRowFactory { _ =>
      new TableRow[Idea]:
        override protected def updateItem(idea: Idea, empty: Boolean): Unit =
          super.updateItem(idea, empty)

          if idea == null || empty then
            setGraphic(null) // Clear any previous content
          else
            val textStatus = idea.localizationStatus(Property.valueOf("NAME"))
            val hasStatusUpdated = textStatus == Localization.Status.valueOf("UPDATED")
            val hasStatusNew = textStatus == Localization.Status.valueOf("NEW")

            // Apply text style
            if hasStatusUpdated || hasStatusNew then
              setTextFill(Color.BLACK) // Set text color to black
              setStyle("-fx-font-weight: bold; -fx-background-color: #328fa8;") // Apply bold text using CSS
            else
              setTextFill(Color.BLACK) // Set text color to black
              setStyle("-fx-background-color: transparent;") // Reset style
    }