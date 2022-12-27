package hoi4_localization.province;

import fileIO.FileRead;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FixDupProvinces {
	
	// removes all duplicate provinces in each file in the directory
	public static boolean RemoveDuplicates(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			
			String fContents = FileRead.readFile(f);
			
			// select only the provinces 
			int prov_dec_index = fContents.indexOf("provinces"); 
			int prov_index = fContents.indexOf("{", prov_dec_index); 
			int end_prov_index = fContents.indexOf("}", prov_index);
			String old_provs_dec = fContents.substring(prov_dec_index, end_prov_index + 1); 	// for later so can replace
			String provinces = fContents.substring(prov_index, end_prov_index + 1).replaceAll("[^0-9]+", " ");
			
			provinces = provinces.trim(); 
			// test
			System.out.println("provinces (string): " + provinces); 
			
			// make a list of duplicate province numbers 
			ArrayList<String> provinces_list = new ArrayList<String>(Arrays.asList(provinces.split(" ")));
			
			// unnecessary 
			//for (int i = 0; i < provinces_list.size(); i++) {
			//	for (int j = 0; j < provinces_list.get(i).length(); j++) {
			//		if (!Character.isDigit(provinces_list.get(i).charAt(j))) {
			//			provinces_list.remove(i); 
			//			i--; 
			//			j = provinces_list.get(i).length(); 
			//		}
			//	}
			//}
				
			ArrayList<Integer> duplicates = new ArrayList<Integer>(); 		// indexes of duplicate provinces
			//test
			System.out.println("List of provinces: " + provinces_list); 
			//System.out.println("provinces list size: " + provinces_list.size()); 
			// add duplicates to duplicates list 
			for (int i = 1; i < provinces_list.size(); i++ ) {
				//System.out.println(provinces_list.get(i)); 
				if (provinces_list.get(i).equals(provinces_list.get(i - 1))) {
					duplicates.add(i); 
				}
			}
			// test
			System.out.println(duplicates); 
			System.out.println(duplicates.size()); 
			
			// remove duplicates
			for (int i = 0; i < duplicates.size(); i++) {
				provinces_list.remove(i); 
				System.out.println("Removed province (" + i + ")"); 
			}
			
			// make new provinces list
			String new_provinces = ""; 
			for (int i = 0; i < provinces_list.size(); i++) {
				new_provinces += provinces_list.get(i) + " "; 
			}
			new_provinces = new_provinces.trim();
			
			// finally replace provinces in file with unduped list
			fContents = fContents.replace(old_provs_dec, "provinces={" + "\n\t\t" + new_provinces + "\n\t" + "}"); 
			
			FileWriter writer = new FileWriter(f);
		    System.out.println("");
		    System.out.println("new data: " + fContents);
		    //fixing crlf to lf (did not work lol) 
		    //fContents = fContents.replaceAll("\r\n", "\n");
		    //fContents = fContents.replaceAll("\r", "\n");
		    writer.append(fContents);
		    writer.close(); 	// says it also flushes 
		}
		return true; 
	}
	
}
