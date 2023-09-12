package hoi4utils.clausewitz_coding.localization;

import ui.HOIUtilsWindow;

import java.io.*;
import java.util.*;

import hoi4utils.HOIIVFile;
/**
 * This is the LocalizationFile file.
 */
public class LocalizationFile extends File {
	private static String loc_key = ":0";
	private static String language = "l_english:";
	protected List<Localization> localizationList;
//	private HashMap<Integer, String> comments;

	public LocalizationFile(File file) throws IOException {
		super(file.toURI());

		if (!file.getPath().endsWith(".yml")) {
			throw new IOException("Localization files can only be of type .yml");
		}

		localizationList = new ArrayList<>();
//		comments = new HashMap<Integer, String>();
	}

	public File getFile() { return this; }

	public String toString() {
		if (super.isFile()) {
			return super.toString();
		}
		return super.toString();
	}

	public void readLocalization() {
//		int line = 0;

		if (!exists()) {
			return;
		}

		/* file reader */
		Scanner reader = null;
		try {
			reader = new Scanner(this);
		} catch (Exception exception) {
			HOIUtilsWindow.openError(exception);
		}
		if (reader == null) {
			return;
		}

		/* language declaration */
		boolean found_lang = false;
		while(!found_lang) {
			if (reader.hasNextLine()) {
				String data = reader.nextLine().replaceAll("\\s", "");
//				line++;
				if (HOIIVFile.usefulData(data)) {
					if (data.trim().contains("l_")) {
						if(!data.contains(language)) {
							// todo
							System.err.println("language not english, not presently supported.");
							return;
						}
						found_lang = true;
					}
				}
			}
			else {
				break;
			}
		}

		/* exit if no loc */
		if (!reader.hasNextLine()) {
			return;
		}

		while (reader.hasNextLine()) {
			String data = reader.nextLine().trim();
//			line++;

			if (HOIIVFile.usefulData(data)) {
				if (data.contains(loc_key)) {
					String id = data.substring(0, data.indexOf(loc_key)).trim();
					String text = data.substring(data.indexOf("\""), data.lastIndexOf("\"") + 1).trim();
					if (text.charAt(text.length() - 1) == '\n') {
						text = text.substring(0, text.length() - 1);
					}
					if (text.length() == 0) {
						continue;
					}
					if (text.charAt(0) == '\"') {
						text = text.substring(1, text.length());
					}
					if (text.length() == 0) {
						continue;
					}
					if (text.charAt(text.length() - 1) == '\"') {
						text = text.substring(0, text.length() - 1);
					}
					if (text.length() == 0) {
						continue;
					}
					text = text.replaceAll("(Â§)", "§");
					Localization localization = new Localization(id, text, Localization.Status.EXISTS);
					localizationList.add(localization);
					// print to statistics?
					System.out.println("localization: " + text);
				} else if (data.contains(":")) {
					System.err.println("Fix incorrect loc key: " + data);
					System.exit(-1);
				}
			}
//			} else {
//				// comments, etc.
//				comments.put(line, data);
//			}
		}
		reader.close();
	}

	public void writeLocalization() throws IOException {
		/* load file data, before writer init so data not disappeared */
		Scanner scanner = new Scanner(this);
		StringBuilder fileBuffer = new StringBuilder();
		try {
			while (scanner.hasNextLine()) {
				fileBuffer.append(scanner.nextLine()).append(System.lineSeparator());
			}
	//		System.out.println(fileBuffer);
	
	//		FileWriter writer = new FileWriter(this, false);		// true = append
			FileWriter writer = new FileWriter(this, false);
			BufferedWriter BWriter = new BufferedWriter(writer);
			PrintWriter PWriter = new PrintWriter(BWriter);				// for println syntax
	
	//		String localization_line;
	
	//		PWriter.println(language);
	
			for (Localization localization : localizationList) {
				if (localization.status() == Localization.Status.UPDATED) {
					/* replace loc */
					int start = fileBuffer.indexOf(localization.ID());
					if (start < 0) {
						System.err.println("Start of localization id is negative!");
					}
					int temp = fileBuffer.indexOf("\"", start);
					int end = 1;
					// end char must be literally " and not \"
					do {
						end = fileBuffer.indexOf("\"", temp + 1);
					} while (fileBuffer.charAt(end - 1) == '\\');
					if (end < 0) {
						System.err.println("end of localization id is negative!");
					}
	
					String loc = localization.toString();
					loc = loc.replaceAll("§", "Â§");		// necessary with UTF-8 BOM
					fileBuffer.replace(start, end + 1, loc);
					System.out.println("replaced " + localization.ID());
				} else if (localization.status() == Localization.Status.NEW) {
					/* append loc */
					String loc = localization.toString();
					loc = loc.replaceAll("§", "Â§");		// necessary with UTF-8 BOM
					fileBuffer.append("\t").append(loc).append(System.lineSeparator());
					System.out.println("append " + localization.ID());
				} else {
	
				}
			}
	
			/* print */
			PWriter.print(fileBuffer);
			PWriter.close();
			scanner.close();
		}
		catch (Exception exception) {
			HOIUtilsWindow.openError(exception);
		}
	}

	public void addLocalization(Localization newLocalization) {
		if (newLocalization == null) {
			return;
		}

		for (Localization loc : localizationList) {
			if (loc.ID().equals(newLocalization.ID())) {
				System.err.println("Localization " + newLocalization.ID() + " already exists.");
				// dont overwrite // todo - for now
				return;
			}
		}

		localizationList.add(newLocalization);
	}

	/**
	 * Gets localization from this file corresponding to the ID
	 * @param ID
	 * @return Localization corresponding with ID, or null if not found
	 */
	public Localization getLocalization(String ID) {
		if(ID == null) {
			System.err.println("Null localization ID in " + this);
			return null;
		}

		for (Localization loc : localizationList) {
			if (loc.ID().equals(ID)) {
				return loc;
			}
			System.out.println(loc.ID());
		}

		return null;
	}

	public Localization setLocalization(String key, String text) {
		ArrayList<Localization> listTemp = new ArrayList<>(localizationList);

		for (Localization loc : localizationList) {
			if (loc.ID().equals(key)) {
				Localization newLocalization = new Localization(key, text, Localization.Status.UPDATED);

				listTemp.remove(loc);	  // record is final
				listTemp.add(newLocalization);
				localizationList = listTemp;

				return newLocalization;
			}
		}

		Localization newLocalization = new Localization(key, text, Localization.Status.NEW);
		localizationList.add(newLocalization);

		return newLocalization;
	}

	public boolean isLocalized(String ID) {
		return getLocalization(ID) != null;
	}

}
