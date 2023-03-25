package clausewitz_coding.localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class reader {

    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("C:\\Users\\daria\\Documents\\Paradox Interactive\\Hearts of Iron IV\\mod\\nadivided-dev\\common\\national_focus\\alaskanew.txt");

        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            System.out.println(line.contains("\t"));
        }
    }
}
