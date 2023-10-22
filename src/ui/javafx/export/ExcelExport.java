package ui.javafx.export;

import javafx.scene.control.TableView;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * adapted from <a href="https://stackoverflow.com/a/58074839/15639400">stackoverflow.com/a/58074839/15639400</a>
 * @param <T>
 */
public class ExcelExport<T> {
	public void export(TableView<T> tableView) {        // todo have one with String workBookName
		String workbookName = "HOIIVUtils Sheet" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);

		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		HSSFSheet hssfSheet = hssfWorkbook.createSheet(workbookName);
		HSSFRow firstRow = hssfSheet.createRow(0);

		///set titles of columns
		for (int i=0; i<tableView.getColumns().size();i++) {
			firstRow.createCell(i).setCellValue(tableView.getColumns().get(i).getText());
		}


		for (int row=0; row<tableView.getItems().size();row++){
			HSSFRow hssfRow= hssfSheet.createRow(row + 1);

			for (int col=0; col<tableView.getColumns().size(); col++){
				Object cellValue = tableView.getColumns().get(col).getCellObservableValue(row).getValue();

				if (cellValue != null) {
					if (cellValue instanceof Number) {
						hssfRow.createCell(col).setCellValue(((Number) cellValue).doubleValue());
					} else {
						hssfRow.createCell(col).setCellValue(cellValue.toString());
					}
				}
			}
		}

		// Save Excel file and close the workbook
		try (FileOutputStream fileOut = new FileOutputStream(workbookName + ".xls")) {
			hssfWorkbook.write(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}