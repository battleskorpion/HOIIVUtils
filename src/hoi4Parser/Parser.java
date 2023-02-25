package hoi4Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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

        ArrayList<String> data = new ArrayList<>();
        while(scanner.hasNextLine()) {
             data.add(scanner.nextLine());
        }
        System.out.println("Lines parsed: " + data.size() + ", file: " + file.getName());
        fileExpressions = new Expression(data.toArray(new String[]{}));

        // testing, print expressions, tests for state 1-cape cod
//        for(int i = 0; i < fileExpressions.subexpressions.size(); i++) {
//            System.out.println(fileExpressions.subexpressions.get(i).expression);
//        }
        // history
//        for(int i = 0; i < fileExpressions.subexpressions.get(3).subexpressions.size(); i++) {
//            System.out.println(fileExpressions.subexpressions.get(3).subexpressions.get(i).expression);
//        }
        // buildings
//        for(int i = 0; i < fileExpressions.subexpressions.get(3).subexpressions.get(1).subexpressions.size(); i++) {
//            System.out.println(fileExpressions.subexpressions.get(3).subexpressions.get(1).subexpressions.get(i).expression);
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
