package com.hoi4utils.ui.javafx_ui.export;

import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exports a {@link TableView} to an Excel (.xls) file.
 * Adapted from <a href="https://stackoverflow.com/a/58074839/15639400">StackOverflow</a>.
 *
 * @param <T> The type of data in the {@link TableView}.
 */
public class ExcelExport<T> {
	private static final Logger logger = LogManager.getLogger(ExcelExport.class);
	private static final String FILE_EXTENSION = ".xls";

	/**
	 * Exports the given {@link TableView} to an Excel file.
	 *
	 * @param tableView The TableView to export.
	 */
	public void export(TableView<T> tableView) {
		String workbookName = generateWorkbookName();
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet(workbookName);

		createHeaderRow(sheet, tableView);
		populateSheetWithData(sheet, tableView);

		saveWorkbook(workbook, workbookName + FILE_EXTENSION);
	}

	/**
	 * Generates a unique workbook name based on the current date.
	 *
	 * @return A formatted workbook name.
	 */
	private String generateWorkbookName() {
		return "HOIIVUtils_Sheet_" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	/**
	 * Creates the header row in the Excel sheet.
	 *
	 * @param sheet     The sheet to write the header row to.
	 * @param tableView The TableView providing column names.
	 */
	private void createHeaderRow(HSSFSheet sheet, TableView<T> tableView) {
		HSSFRow headerRow = sheet.createRow(0);
		for (int col = 0; col < tableView.getColumns().size(); col++) {
			headerRow.createCell(col).setCellValue(tableView.getColumns().get(col).getText());
		}
	}

	/**
	 * Populates the Excel sheet with data from the TableView.
	 *
	 * @param sheet     The sheet to populate.
	 * @param tableView The TableView providing data.
	 */
	private void populateSheetWithData(HSSFSheet sheet, TableView<T> tableView) {
		for (int row = 0; row < tableView.getItems().size(); row++) {
			HSSFRow dataRow = sheet.createRow(row + 1);
			for (int col = 0; col < tableView.getColumns().size(); col++) {
				Object cellValue = tableView.getColumns().get(col).getCellObservableValue(row).getValue();
				setCellValue(dataRow, col, cellValue);
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
	private void setCellValue(HSSFRow row, int col, Object cellValue) {
		if (cellValue == null) return;
		if (cellValue instanceof Number) {
			row.createCell(col).setCellValue(((Number) cellValue).doubleValue());
		} else {
			row.createCell(col).setCellValue(cellValue.toString());
		}
	}

	/**
	 * Saves the workbook to a file and handles errors.
	 *
	 * @param workbook The workbook to save.
	 * @param fileName The file name.
	 */
	private void saveWorkbook(HSSFWorkbook workbook, String fileName) {
		try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
			workbook.write(fileOut);
			logger.info("Excel file saved: {}", fileName);
		} catch (IOException e) {
			logger.error("Failed to save Excel file: {}", fileName, e);
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				logger.error("Failed to close workbook: {}", fileName, e);
			}
		}
	}
}
