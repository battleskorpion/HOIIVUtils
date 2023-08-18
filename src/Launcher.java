import clausewitz_coding.HOI4Fixes;
import hellofx.Main;

import java.io.IOException;
import java.util.Scanner;

public class Launcher {
    public static void main(String[] args) throws RuntimeException,IOException {
        String whatToRun;
        System.out.print("Press 1 and Enter to test test\nPress anything and enter to run normally:\n");
        Scanner scanner = new Scanner(System.in);
        whatToRun = scanner.nextLine();
        scanner.close();
        if (whatToRun == "1") {
            Main.main(args);
        }
        else {
            HOI4Fixes.main(args);
        }
    }
}
