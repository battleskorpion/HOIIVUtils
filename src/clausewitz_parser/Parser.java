package clausewitz_parser;

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
        } catch (FileNotFoundException exc) {
            throw new RuntimeException(exc);
        }

        ArrayList<String> data = new ArrayList<>();
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("{")) {
                /*
                if text [a-z] after '{', replace with "{\n\t + text"
                 */
                line = line.replaceAll("\\{\\s*([a-z]+)", "{\n\t$1");        // maybe change [a-z] to [a-z|0-9]
                line = line.replaceAll("([a-z|0-9]+\\s)+(})", "$1\n$2");
                String[] lines = line.split("\n");
                for (String s : lines) {
                    s += "\n";
                    data.add(s);
                }
            } else {
                data.add(line);
            }
        }
        System.out.println("Lines parsed: " + data.size() + ", file: " + file.getName());
        fileExpressions = new Expression(data.toArray(new String[]{}));

        scanner.close();
    }

    public Parser(String filename) {
        this(new File(filename));
    }

    /**
     * Returns expressions found by parser in file
     * @return expressions in file parsed
     */
    public Expression expression() {
        return fileExpressions;
    }

    public Expression find(String s) {
        if (fileExpressions == null) {
            return null;
        }

        return fileExpressions.get(s);
    }

    public Expression[] findAll(String s) {
        if (fileExpressions == null) {
            return null;
        }

        return fileExpressions.getAll(s);
    }

    public Expression findImmediate(String s) {
        if (fileExpressions == null) {
            return null;
        }

        return fileExpressions.getImmediate(s);
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
