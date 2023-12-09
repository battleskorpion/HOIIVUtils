package hoi4utils.fileIO;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
/*
 * FileRead File
 */
public class FileRead 
{
	
	public static String readFile(File f) throws IOException 
	{
		// scan file and read from it 
		Scanner file_scan = new Scanner(f); 
		StringBuilder buffer = new StringBuilder();
		while (file_scan.hasNextLine()) 
		{
			buffer.append(file_scan.nextLine()).append(System.lineSeparator());
		}
		String fileContents = buffer.toString();
		// testing 
		System.out.println("next file: " +fileContents);
		//closing the Scanner object
		file_scan.close();
		
		return fileContents;
	}
}
