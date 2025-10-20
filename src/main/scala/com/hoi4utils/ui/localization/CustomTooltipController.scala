package com.hoi4utils.ui.localization

import com.hoi4utils.hoi4mod.tooltip.CustomTooltip
import com.hoi4utils.main.{HOIIVFiles, HOIIVUtils}
import com.hoi4utils.ui.javafx.application.{HOIIVUtilsAbstractController, HOIIVUtilsAbstractController2, JavaFXUIManager}
import com.hoi4utils.ui.javafx.scene.control.TableViewWindow
import javafx.application.Platform
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.*
import javafx.scene.layout.{AnchorPane, BorderPane, GridPane}
import scalafx.collections.ObservableBuffer

import java.io.File
import java.net.URL
import java.util.ResourceBundle
import scala.compiletime.uninitialized

/// // * todo: have to redo some functionality to work with new localization system
class CustomTooltipController extends HOIIVUtilsAbstractController2 with TableViewWindow with Initializable:
  setFxmlFile("CustomTooltip.fxml")
  setTitle("Custom Tooltips")

  @FXML var contentContainer: GridPane = uninitialized
  @FXML var tooltipAnchorPane: AnchorPane = uninitialized
  @FXML var ctClose: Button = uninitialized
  @FXML var ctSquare: Button = uninitialized
  @FXML var ctMinimize: Button = uninitialized

  @FXML var idVersion: Label = uninitialized
  @FXML var tooltipIdTableColumn: TableColumn[CustomTooltip,String] = uninitialized
  @FXML var tooltipTextTableColumn: TableColumn[CustomTooltip,String] = uninitialized
  @FXML var tooltipFileComboBox: ComboBox[File]          = uninitialized
  @FXML var tooltipLocalizationFileComboBox: ComboBox[File] = uninitialized
  @FXML var tooltipFileBrowseButton: Button              = uninitialized
  @FXML var tooltipLocalizationFileBrowseButton: Button  = uninitialized
  @FXML var customTooltipTableView: TableView[CustomTooltip] = uninitialized

  private var tooltipFile: Option[File] = None

  private var isEmbedded: Boolean = false

  // ScalaFX‐friendly backing list
  private val customTooltipBuf: ObservableBuffer[CustomTooltip] = ObservableBuffer.empty

  override def initialize(location: URL, resources: ResourceBundle): Unit =
    Platform.runLater(() =>
      isEmbedded = primaryScene == null
      ctClose.setVisible(!isEmbedded)
      ctSquare.setVisible(!isEmbedded)
      ctMinimize.setVisible(!isEmbedded)
    )
    idVersion.setText(HOIIVUtils.get("version"))
    // wire up the JavaFX TableView using your existing helper:
    loadTableView(this, customTooltipTableView, customTooltipBuf, CustomTooltip.dataFunctions())

  override def preSetup(): Unit = setupWindowControls(contentContainer, ctClose, ctSquare, ctMinimize, tooltipAnchorPane)
  // --- action handlers ---
  @FXML def handleTooltipFileBrowseAction(): Unit =
    val initial = HOIIVFiles.Mod.common_folder
    val selectedFile = JavaFXUIManager.openChooser(tooltipFileBrowseButton, initial, false)
    if selectedFile != null then
      tooltipFileComboBox.setValue(selectedFile)
      tooltipFile = Some(selectedFile)
      CustomTooltip.loadTooltips(selectedFile)
      CustomTooltip.getTooltips.foreach: tooltip =>
        customTooltipBuf += tooltip

  @FXML def handleTooltipLocalizationFileBrowseAction(): Unit = throw new NotImplementedError("Localization file handling not implemented yet")

  // TableViewWindow stub TODO implement
  override def setDataTableCellFactories(): Unit = ()