package hoi4Parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static hoi4Parser.Parser.usefulData;

public class Expression {
    String expression;
    List<Expression> subexpressions;

    public Expression(String[] expressions) {
        for (Iterator<String> it = Arrays.stream(expressions).iterator(); it.hasNext(); ) {
            String exp = it.next();
            if (!usefulData(exp)) {
                continue;
            }
            if(exp.trim().equals("}")) {
                continue;
            }

            if (expression == null && exp.contains("={")) {
                expression = exp;
            }
            else {
                if (exp.contains("={")) {
                    subexpressions.add(new Expression(exp, it));
                }
                else {
                    subexpressions.add(new Expression(exp));
                }
            }
        }

    }

    // for adding subexpressions with subexpressions
    private Expression(String exp, Iterator<String> it) {
        expression = exp;
        while(it.hasNext()) {
            exp = it.next();
            if (!usefulData(exp)) {
                continue;
            }
            if(exp.trim().equals("}")) {
                continue;
            }

            if (exp.contains("={")) {
                subexpressions.add(new Expression(exp, it));
            }
            else {
                subexpressions.add(new Expression(exp));
            }
        }
    }

    public Expression(String expression) {

    }


}
