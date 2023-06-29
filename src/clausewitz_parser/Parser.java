package clausewitz_parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            //int tabAmt = numMatches(line, "\t");
            if (line.contains("{")) {
                int tabAmt = numMatches(line, "\t");

                /* if text [a-z] after '{', replace with "{\n\t + text" */
                if (line.matches("(?i)\\{[^\\S\\t\\r\\n]*(\\t*)([a-z]+)")) {
                    line = line.replaceAll("(?i)\\{[^\\S\\t\\r\\n]*(\\t*)([a-z]+)", "\\{" + System.lineSeparator() + "\t$1$2");        // old: \{\s*([a-z]+)

                    /* add tabs to new lines */
                    String replacement = "$1\t";
                    for (int i = tabAmt; tabAmt > 0; tabAmt--) {
                        replacement = "\t" + replacement;
                    }
                    line = line.replaceAll("(\n+)", replacement);
                }
                line = line.replaceAll("(?i)(([a-z0-9]+|\\{)[^\\S\\n\\r]*)+(})", "$1" + System.lineSeparator() + "$3");
                String[] lines = line.split(System.lineSeparator());
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

    public Expression[] findAll() {
         return findAll("");
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

    private int numMatches(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }
}
