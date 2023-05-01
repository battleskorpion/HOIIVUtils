package clausewitz_coding.localization;

import clausewitz_coding.HOI4Fixes;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class LocalizationFile extends File {
    private static String loc_key = ":0";
    private static String language = "l_english:";
    private List<Localization> localizationList;

    public LocalizationFile(File file) {
        super(file.toURI());

        localizationList = new ArrayList<>();
    }

    public File getFile() { return this; }

    public String toString() {
        if (super.isFile()) {
            return super.toString();
        }
        return super.toString();
    }

    public void readLocalization() {
        if (!exists()) {
            return;
        }

        /* file reader */
        Scanner reader;
        try {
            reader = new Scanner(this);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        /* language declaration */
        boolean found_lang = false;
        while(!found_lang) {
            if (reader.hasNextLine()) {
                String data = reader.nextLine().replaceAll("\\s", "");
                if (HOI4Fixes.usefulData(data)) {
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
            //String data = reader.nextLine().replaceAll("\\s", "");
            String data = reader.nextLine().trim();
            if (HOI4Fixes.usefulData(data)) {
                if (data.contains(loc_key)) {
                    String id = data.substring(0, data.indexOf(loc_key)).trim();
                    String text = data.substring(data.indexOf("\""), data.lastIndexOf("\"") + 1).trim();
                    if (text.charAt(text.length() - 1) == '\n') {
                        text = text.substring(0, text.length() - 1);
                    }
                    if(text.length() == 0) {
                        continue;
                    }
                    if (text.charAt(0) == '\"') {
                        text = text.substring(1, text.length());
                    }
                    if(text.length() == 0) {
                        continue;
                    }
                    if (text.charAt(text.length() - 1) == '\"') {
                        text = text.substring(0, text.length() - 1);
                    }
                    if(text.length() == 0) {
                        continue;
                    }
                    text = text.replaceAll("(Â§)", "§");
                    Localization localization = new Localization(id, text);
                    localizationList.add(localization);
                    // print to statistics?
                    System.out.println("localization: " + text);
                } else if (data.contains(":")) {
                    System.err.println("Fix incorrect loc key: " + data);
                    System.exit(-1);
                }
            }
        }
    }

    public void writeLocalization() throws IOException {
        FileWriter writer = new FileWriter(this, false);		// true = append
        BufferedWriter BWriter = new BufferedWriter(writer);
        PrintWriter PWriter = new PrintWriter(BWriter); 		        // for println syntax

        /* bom */
        PWriter.write(0xef); // emits 0xef
        PWriter.write(0xbb); // emits 0xbb
        PWriter.write(0xbf); // emits 0xbf

        String localization_line;

        PWriter.println(language);

        for (Localization localization : localizationList) {
            String loc = localization.toString();
            loc = loc.replaceAll("§", "Â§");        // necessary with UTF-8 BOM
            System.out.println(loc);
            PWriter.println("");
            PWriter.print("    " + loc + "\n");
        }

        PWriter.close();
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

    public void setLocalization(String key, String text) {
        ArrayList<Localization> listTemp = new ArrayList<>(localizationList);

        for (Localization loc : localizationList) {
            if (loc.ID().equals(key)) {
                listTemp.remove(loc);      // record is final
                listTemp.add(new Localization(key, text));
                localizationList = listTemp;
                return;
            }
        }

        localizationList.add(new Localization(key, text));
    }

    public boolean isLocalized(String ID) {
        return getLocalization(ID) != null;
    }

}
