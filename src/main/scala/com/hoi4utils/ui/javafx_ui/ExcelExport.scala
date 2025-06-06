package com.hoi4utils.ui.javafx_ui

import com.typesafe.scalalogging.LazyLogging
import javafx.scene.control.TableView
import org.apache.poi.hssf.usermodel.{HSSFRow, HSSFSheet, HSSFWorkbook}

import java.io.{FileOutputStream, IOException}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Exports a JavaFX TableView to an Excel (.xls) file.
 *
 * Adapted from StackOverflow: https://stackoverflow.com/a/58074839/15639400
 *
 * @tparam T The type of data in the TableView.
 */
class ExcelExport[T] extends LazyLogging {
  /**
   * Exports the given T to an Excel file.
   *
   * @param tableView The TableView to export.
   */
  def hExport(tableView: TableView[T]): Unit = {
    val workbookName = generateWorkbookName
    val workbook = new HSSFWorkbook
    val sheet = workbook.createSheet(workbookName)
    createHeaderRow(sheet, tableView)
    populateSheetWithData(sheet, tableView)
    val xls = ".xls"
    saveWorkbook(workbook, s"$workbookName.xls")
  }

  /**
   * Generates a unique workbook name based on the current date.
   *
   * @return A formatted workbook name.
   */
  private def generateWorkbookName: String = {
    s"HOIIVUtils_Sheet_${LocalDateTime.now.format(DateTimeFormatter.BASIC_ISO_DATE)}"
  }

  /**
   * Creates the header row in the Excel sheet.
   *
   * @param sheet     The sheet to write the header row to.
   * @param tableView The TableView providing column names.
   */
  private def createHeaderRow(sheet: HSSFSheet, tableView: TableView[T]): Unit = {
    val headerRow = sheet.createRow(0)
    for (col <- 0 until tableView.getColumns.size) {
      headerRow.createCell(col).setCellValue(tableView.getColumns.get(col).getText)
    }
  }

  /**
   * Populates the Excel sheet with data from the TableView.
   *
   * @param sheet     The sheet to populate.
   * @param tableView The TableView providing data.
   */
  private def populateSheetWithData(sheet: HSSFSheet, tableView: TableView[T]): Unit = {
    for (row <- 0 until tableView.getItems.size) {
      val dataRow = sheet.createRow(row + 1)
      for (col <- 0 until tableView.getColumns.size) {
        val cellValue = tableView.getColumns.get(col).getCellObservableValue(row).getValue
        setCellValue(dataRow, col, cellValue)
      }
    }
  }

  /**
   * Sets the cell value based on its type.
   *
   * @param row       The row to modify.
   * @param col       The column index.
   * @param cellValue The value to set.
   */
  private def setCellValue(row: HSSFRow, col: Int, cellValue: Any): Unit = {
    if (cellValue == null) return
    cellValue match {
      case number: Number => row.createCell(col).setCellValue(number.doubleValue)
      case _ => row.createCell(col).setCellValue(cellValue.toString)
    }
  }

  /**
   * Saves the workbook to a file and handles errors.
   *
   * @param workbook The workbook to save.
   * @param fileName The file name.
   */
  private def saveWorkbook(workbook: HSSFWorkbook, fileName: String): Unit = {
    var fileOut: FileOutputStream = null
    try {
      fileOut = new FileOutputStream(fileName)
      workbook.write(fileOut)
      logger.info("Excel file saved: {}", fileName)
    } catch {
      case e: IOException =>
        logger.error("Failed to save Excel file: {}", fileName, e)
    } finally {
      try {
        if (fileOut != null) fileOut.close()
      } catch {
        case e: IOException =>
          logger.error("Failed to close FileOutputStream for: {}", fileName, e)
      }
      try {
        workbook.close()
      } catch {
        case e: IOException =>
          logger.error("Failed to close workbook: {}", fileName, e)
      }
    }
  }
}