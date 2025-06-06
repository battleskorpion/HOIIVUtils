package com.hoi4utils.ui.parser

import com.hoi4utils.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.hoi4.country.{Country, CountryTag}
import com.hoi4utils.hoi4.focus.FocusTree
import com.hoi4utils.parser.{Node, Parser, ParserException}
import com.hoi4utils.script.{AbstractPDX, PDXScript}
import com.hoi4utils.ui.{HOIIVUtilsAbstractController, JavaFXUIManager}
import com.hoi4utils.ui.pdxscript.{PDXTreeViewFactory, StratRegionPDXEditorController}
import com.map.{ResourcesFile, State, StrategicRegion}
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane

import javax.swing.JOptionPane
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

class ParserViewerController extends HOIIVUtilsAbstractController:

  @FXML var idVersion: Label = uninitialized
  @FXML var parsePDXFileTextField: TextField = uninitialized
  @FXML var browseButton: Button = uninitialized
  @FXML var pdxIdentifierLabel: Label = uninitialized
  @FXML var pdxTreeViewPane: AnchorPane = uninitialized
  @FXML private var searchTextField: TextField = uninitialized // user enters search text here
  @FXML private var filesListView: ListView[PDXScript[?]] = uninitialized
  @FXML private var pdxNodeTextArea: TextArea = uninitialized
  @FXML private var saveMenuItem: MenuItem = uninitialized

  private val pdxScripts: ListBuffer[PDXScript[?]] = ListBuffer.empty

  setFxmlResource("ParserViewer.fxml")
  setTitle("HOIIVUtils Parser Viewer")

  @FXML
  def initialize(): Unit =
    includeVersion()

    // Handle list selection
    filesListView.getSelectionModel.selectedItemProperty().addListener { (_, _, newVal) =>
      Option(newVal).foreach { selectedScript =>
        // Create a tree view for the newly selected script
        val pdxTreeView = PDXTreeViewFactory.createPDXTreeView(selectedScript)
        pdxTreeViewPane.getChildren.removeIf(_.isInstanceOf[TreeView[?]])
        pdxTreeViewPane.getChildren.add(pdxTreeView)

        AnchorPane.setTopAnchor(pdxTreeView, 25.0)
        AnchorPane.setBottomAnchor(pdxTreeView, 0.0)
        AnchorPane.setLeftAnchor(pdxTreeView, 0.0)
        AnchorPane.setRightAnchor(pdxTreeView, 0.0)

        searchTextField.setOnAction { _ =>
          val searchTerm = searchTextField.getText
          PDXTreeViewFactory.searchAndSelect(pdxTreeView, searchTerm)
        }

        pdxTreeView.setOnMouseClicked { _ =>
          Option(pdxTreeView.getSelectionModel.getSelectedItem).foreach { selectedItem =>
            val selectedPDX = selectedItem.getValue
            if selectedPDX != null then
              pdxNodeTextArea.setText(selectedPDX.toScript)
          }
        }
      }
    }

  private def includeVersion(): Unit =
    idVersion.setText(HOIIVUtils.get("version").toString)

  @FXML
  private def handlePDXFileBrowseAction(): Unit =
    val initialDirectory = HOIIVFiles.Mod.folder
    val selected = JavaFXUIManager.openChooser(browseButton, initialDirectory, true)

    println(selected)

    Option(selected) match
      case Some(file) =>
        pdxScripts.clear()

        if file.isDirectory then
          val allPDXFiles = PDXScript.allPDXFilesInDirectory(file).asJava.asScala.toList

          allPDXFiles.foreach { pdxFile =>
            Try {
              val parser = Parser(pdxFile)
              parser.parse
            } match
              case Success(rootNode) if rootNode != null =>
                val firstChild = rootNode.toList.head
                val pdxIdentifier = firstChild.name

                val pdx: Option[AbstractPDX[?]] = pdxIdentifier match
                  case "focus_tree"       => Some(FocusTree(pdxFile))
                  case "state"            => Some(State(false, pdxFile))
                  case "strategic_region" => Some(StrategicRegion(pdxFile))
                  case _                  => None

                pdx.foreach { p =>
                  if !p.isUndefined then
                    pdxScripts += p
                }
              case Failure(_: ParserException) =>
              // handle errors silently
              case _ =>
            // handle null rootNode or other cases
          }
        else
          parsePDXFileTextField.setText(file.getAbsolutePath)
          val pdxParser = Parser(file)

          Try(pdxParser.parse) match
            case Success(rootNode) if rootNode != null =>
              if !rootNode.isParent then
                pdxIdentifierLabel.setText("[empty]")
              else
                rootNode.$list() match
                  case Some(childList) if childList.nonEmpty =>
                    val childPDXNode = childList.apply(0)
                    val pdxIdentifier = childPDXNode.name

                    if childList.length == 1 then
                      pdxIdentifierLabel.setText(pdxIdentifier)
                    else
                      pdxIdentifierLabel.setText(file.getName)

                    val pdx: Option[AbstractPDX[?]] = pdxIdentifier match
                      case "focus_tree" =>
                        Some(FocusTree(file))
                      case "state" =>
                        Some(State(false, file))
                      case _ if file.getParent.endsWith("countries") &&
                        file.getParentFile.getParent.endsWith("history") =>
                        Some(Country(file, CountryTag.get(file.getName.substring(0, 3))))
                      case "resources" =>
                        Some(ResourcesFile(file))
                      case "strategic_region" =>
                        Some(StrategicRegion(file))
                      case _ =>
                        None

                    pdx.foreach { p =>
                      if p != null && !p.isUndefined then
                        pdxScripts += p
                    }
                  case _ =>
                    pdxIdentifierLabel.setText("[empty]")
            case Success(_) =>
              JOptionPane.showMessageDialog(null, "Error: Selected focus tree not found in loaded focus trees.")
            case Failure(e: ParserException) =>
              throw RuntimeException(e)
            case Failure(e) =>
              throw RuntimeException(e)

        // After loading all PDX scripts
        filesListView.setItems(FXCollections.observableList(pdxScripts.asJava))

        // Show pdx's node in side view when pdx item is selected in list view
        filesListView.setOnMouseClicked { _ =>
          Option(filesListView.getSelectionModel.getSelectedItem).foreach { selectedPDX =>
            pdxNodeTextArea.setText(selectedPDX.toScript)
          }
        }

        if pdxScripts.isEmpty then return

      /* do stuff with the pdx scripts */

      case None =>
        pdxIdentifierLabel.setText("[not found]")

  @FXML
  private def handleSaveAction(): Unit =
    savePDX(pdxScripts.toList)

  private def savePDX(pdxScripts: List[PDXScript[?]]): Unit =
    pdxScripts.foreach { pdx =>
      pdx.savePDX(File("Parser Viewer PDXScripts"))
    }

  // @FXML
  // private def handleOpenReplAction(): Unit =
  //   // Launch an Ammonite REPL, passing in the pdxScripts list
  //   ReplUIController().open(pdxScripts.toList)

  @FXML
  private def handlePDXListViewEditAll(): Unit =
    if pdxScripts.nonEmpty then
      pdxScripts.head match
        case _: StrategicRegion =>
          // Open the strategic region editor
          val stratRegionEditor = StratRegionPDXEditorController()
          val strategicRegions = pdxScripts.collect {
            case sr: StrategicRegion => sr
          }.toList
          stratRegionEditor.open(strategicRegions.asJava)
        case _ =>
          JOptionPane.showMessageDialog(null, "No editor available for this PDX type.")