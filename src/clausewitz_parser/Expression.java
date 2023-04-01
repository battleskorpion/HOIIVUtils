package clausewitz_parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static clausewitz_parser.Parser.usefulData;

public class Expression {
    private static Iterator<String> it;
    String expression;
    List<Expression> subexpressions;

    public Expression(String[] expressions) {
        subexpressions = new ArrayList<Expression>();

        for (it = Arrays.stream(expressions).iterator(); it.hasNext(); ) {
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
                if (exp.contains("=") && exp.contains("{")){
                    subexpressions.add(new Expression(exp, true));
                }
                else {
                    subexpressions.add(new Expression(exp));
                }
            }
        }

    }

    // for adding subexpressions with subexpressions
    private Expression(String exp, boolean iterator) {
//        exp = exp.replaceAll(" ", "");
        expression = exp;
        subexpressions = new ArrayList<>();

        while(it.hasNext()) {
            exp = it.next();
//            System.out.println(exp);

            if (!usefulData(exp)) {
                continue;
            }
            if(exp.trim().equals("}")) {
                break;
            }

            if (exp.contains("=") && exp.contains("{")) {
                subexpressions.add(new Expression(exp, true));
            }
            else {
                subexpressions.add(new Expression(exp));
            }
        }
    }

    public Expression(String expression) {
        expression = expression.replaceAll("= ", "=");
        expression = expression.replaceAll(" =", "=");
        this.expression = expression;
        this.subexpressions = null;
    }

    public Expression(Expression exp) {
        this.expression = exp.expression;
        this.subexpressions = exp.subexpressions;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof String) {
            return expression.trim().equals(((String) obj).trim());
        }
        if(obj instanceof Expression) {
            return expression.trim().equals(((Expression) obj).expression.trim());
            // shouldnt check subexpression equality - enables easy search by string -> expression stuff
//                    && subexpressions.equals(((Expression) obj).subexpressions);
        }

        return false;
    }

    // fuck HOI4
    // schizophrenics
    // hi gamerz

    /**
     * Gets the first instance of an expression in parsed file
     * @param s - expression to find
     * @return
     */
    public Expression get(String s) {
        Expression exp = new Expression(s);
        if (expression != null && expression.trim().contains(s)) {
            return new Expression(expression);
        }
        else {
//            if (subexpressions != null && subexpressions.contains(exp)) {
//                return subexpressions.get(subexpressions.indexOf(exp));
//            }
//            else {
                if (subexpressions != null) {
                    for (Expression subexp : subexpressions) {
                        if (subexp.get(s) != null) {
                            return subexp.get(s);
                        }
                    }
                }
//            }
        }

        return null;
    }
    public Expression getSubexpression(String s) {
        Expression exp = new Expression(s);

        if (subexpressions != null) {
            for (Expression subexp : subexpressions) {
                if (subexp.get(s) != null) {
                    return subexp.get(s);
                }
            }
        }

        return null;
    }

    public int getValue() {
        if (subexpressions != null) {
            return Integer.MIN_VALUE;
        }

        String exp = expression;
        if (expression.contains("#")) {
            exp = expression.substring(0, expression.indexOf("#"));
        }

        return Integer.parseInt(exp.substring(exp.indexOf("=") + 1).trim().replace(",", ""));
    }

    public double getDoubleValue() {
        if (subexpressions != null) {
            return Double.NaN;
        }

        String exp = expression;
        if (expression.contains("#")) {
            exp = expression.substring(0, expression.indexOf("#"));
        }

        return Double.parseDouble(exp.substring(exp.indexOf("=") + 1).trim().replace(",", ""));
    }


    public String getText() {
        if (subexpressions != null) {
            return null;
        }

        return expression.substring(expression.indexOf("=") + 1).trim();
    }

    /**
     * Gets all instances of an expression in parsed file
     * @param s expression to find
     * @return all instances of expression s
     */
    public Expression[] getAll(String s) {
        ArrayList<Expression> expressions = new ArrayList<>();

        Expression exp = new Expression(s);
        if(expression != null && expression.trim().contains(s)) {
            expressions.add(new Expression(this));
        } else if (subexpressions != null) {
            for (Expression subexp : subexpressions) {
                Expression[] subexpexps = subexp.getAll(s);
                if (subexpexps != null) {
                    expressions.addAll(Arrays.asList(subexpexps));
                }
            }
        }

        // if none found return null
        if (expressions.size() == 0) {
            return null;
        }

        return expressions.toArray(new Expression[]{});
    }

    public String toString() {
        String s = "";
        s += expression;
        //s += "\n";
        if (subexpressions != null) {
            for (Expression exp : subexpressions) {
                s += exp.toString();
                s += "\n";
            }
        }
        return s;
    }

    public Expression[] getSubexpressions() {
    }
}
