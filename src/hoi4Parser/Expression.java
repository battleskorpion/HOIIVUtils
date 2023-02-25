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
        this.expression = expression;
        this.subexpressions = null;
    }

    // fuck HOI4
    // schizophrenics
    // hi gamerz
    public Expression get(String s) {
        Expression exp = new Expression(s);
        if(expression.equals(s)) {
            return exp;
        }
        else {
            if (subexpressions.contains(exp)) {
                return subexpressions.get(subexpressions.indexOf(exp));
            }
            else {
                for (Expression subexp : subexpressions) {
                    if (subexp.get(s) != null) {
                        return subexp.get(s);
                    }
                }
            }
        }

        return null;
    }

    public int getValue()
}