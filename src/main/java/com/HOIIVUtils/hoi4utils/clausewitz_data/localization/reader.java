package com.HOIIVUtils.hoi4utils.clausewitz_data.localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
/**
 * This is the reader localization file.
 */
public class reader {

	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("C:\\Users\\daria\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\nadivided-dev\\common\\national_focus\\alaskanew.txt");

		Scanner scanner = new Scanner(file);

		for (int lineI = 0; scanner.hasNextLine() && lineI < 1000000; lineI++) {
			String line = scanner.nextLine();

			System.out.println(line.contains("\t"));
		}
		scanner.close();
	}
}
