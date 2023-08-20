import gui.hellofx.Main;
import hoi4utils.HOIIVUtils;

import java.io.IOException;

public class Launcher {
	public static void main(String[] args) throws RuntimeException,IOException {
		String whatToRun;
//		System.out.print("Press 1 and Enter to test test\nPress anything and enter to run normally:\n");
//		Scanner scanner = new Scanner(System.in);
//		whatToRun = scanner.nextLine();
//		scanner.close();
		whatToRun = "";
		if (whatToRun == "1") {
			Main.main(args);
		}
		else {
			HOIIVUtils.main(args);
		}
	}
}
