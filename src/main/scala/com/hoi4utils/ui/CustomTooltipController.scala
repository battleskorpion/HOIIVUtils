package com.hoi4utils.ui

import com.hoi4utils.hoi4.tooltip.CustomTooltip
import com.hoi4utils.ui.javafx_ui.table.TableViewWindow
import com.hoi4utils.{HOIIVFiles, HOIIVUtils}
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.*
import scalafx.collections.ObservableBuffer

import java.io.File
import java.net.URL
import java.util.ResourceBundle

/// // * todo: have to redo some functionality to work with new localization system
class CustomTooltipController extends HOIIVUtilsAbstractController with TableViewWindow with Initializable {
  
  @FXML var idVersion: Label = _
  @FXML var tooltipIdTableColumn: TableColumn[CustomTooltip,String] = _
  @FXML var tooltipTextTableColumn: TableColumn[CustomTooltip,String] = _
  @FXML var tooltipFileComboBox: ComboBox[File]          = _
  @FXML var tooltipLocalizationFileComboBox: ComboBox[File] = _
  @FXML var tooltipFileBrowseButton: Button              = _
  @FXML var tooltipLocalizationFileBrowseButton: Button  = _
  @FXML var customTooltipTableView: TableView[CustomTooltip] = _

  private var tooltipFile: Option[File] = None 
  
  /* default */
  setFxmlResource("CustomTooltip.fxml")
  setTitle("Custom Tooltips")

  // ScalaFX‐friendly backing list
  private val customTooltipBuf: ObservableBuffer[CustomTooltip] = ObservableBuffer.empty
  
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    idVersion.setText(HOIIVUtils.get("version"))
    // wire up the JavaFX TableView using your existing helper:
    loadTableView(this, customTooltipTableView, customTooltipBuf, CustomTooltip.dataFunctions())
  }

  // --- action handlers ---
  @FXML def handleTooltipFileBrowseAction(): Unit = {
    val initial = HOIIVFiles.Mod.common_folder
    val selectedFile = JavaFXUIManager.openChooser(tooltipFileBrowseButton, initial, false)
    if (selectedFile != null) {
      tooltipFileComboBox.setValue(selectedFile)
      tooltipFile = Some(selectedFile)
      CustomTooltip.loadTooltips(selectedFile)
      CustomTooltip.getTooltips.foreach { tooltip =>
        customTooltipBuf += tooltip
      }
    }
    
  }

  @FXML def handleTooltipLocalizationFileBrowseAction(): Unit = {
    // todo 
  }

  // TableViewWindow stub
  override def setDataTableCellFactories(): Unit = {
  }
}
