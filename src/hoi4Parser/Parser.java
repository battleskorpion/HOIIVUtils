package hoi4Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
    private final Expression fileExpressions;

    public Parser(File file) {
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String[] data = scanner.next().split("\n");
        fileExpressions = new Expression(data);
//        while(scanner.hasNext()) {
//            String data = scanner.nextLine();
//            if(!usefulData(data)) {
//               continue;
//            }
//
//            if(data.contains("={"))
//        }

    }

    public Parser(String filename) {
        this(new File(filename));
    }

    public Expression expressions() {
        return fileExpressions;
    }

    protected static boolean usefulData(String data) {
        if (!data.isBlank()) {
            if (data.trim().charAt(0) == '#') {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

}
