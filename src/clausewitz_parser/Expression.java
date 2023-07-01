package clausewitz_parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static clausewitz_parser.Parser.usefulData;

public class Expression {
    private Iterator<String> it;
    String expression;
    List<Expression> subexpressions;

    public Expression(String[] expressions) {
        subexpressions = new ArrayList<Expression>();

        for (it = Arrays.stream(expressions).iterator(); it.hasNext(); ) {
            String exp = it.next();
            if (!usefulData(exp)) {
//                System.out.println(exp);
                continue;
            }
            if(exp.trim().matches("}+")) {
                continue;
            }

            exp = exp.replaceAll("= ", "=");
            exp = exp.replaceAll(" =", "=");

            if (expression == null && exp.contains("={")) {
                expression = exp;
            }
            else {
                if (exp.contains("=") && exp.contains("{")) {
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
//        exp = exp.replaceAll(" ", "");
        expression = exp;
        expression = expression.replaceAll("= ", "=");
        expression = expression.replaceAll(" =", "=");
        subexpressions = new ArrayList<>();

        while(it.hasNext()) {
            exp = it.next();
//            System.out.println(exp);

            if (!usefulData(exp)) {
                continue;
            }
            if(exp.trim().matches("}+")) {      // was exp.trim().equals("{");
                break;
            }

            if (exp.contains("=") && exp.contains("{")) {
                subexpressions.add(new Expression(exp, it));
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
     * @return First expression found matching s
     * @implNote Whitespace is removed surrounding "=" when expressions are instantiated.
     */
    public Expression get(String s) {
        Expression exp = new Expression(s);
        if (expression != null && expression.trim().contains(s)) {
            return new Expression(this);        // copy
        }
        else {
//            if (subexpressions != null && subexpressions.contains(exp)) {
//                return subexpressions.get(subexpressions.indexOf(exp));
//            }
//            else {
                if (subexpressions != null) {
                    for (Expression subexp : subexpressions) {
                        if (subexp.get(s) != null) {
                            return new Expression(subexp.get(s));
                        }
                    }
                }
//            }
        }

        return null;
    }

    public Expression getImmediate(String s) {
        Expression exp = new Expression(s);
        if (expression != null && expression.trim().contains(s)) {
            return new Expression(this);
        }

        if (subexpressions != null) {
            for (Expression subexp : subexpressions) {
                if (subexp.expression != null && subexp.expression.trim().contains(s)) {
                    return new Expression(subexp);
                }
            }
        }

        return null;
    }

    public Expression getSubexpression(String s) {
        Expression exp = new Expression(s);

        if (subexpressions != null) {
            for (Expression subexp : subexpressions) {
                if (subexp.get(s) != null) {
                    return new Expression(subexp.get(s));
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

    public Expression[] getAll() {
        return getAll("");
    }

    public Expression[] getAllSubexpressions(String s) {
        ArrayList<Expression> expressions = new ArrayList<>();

        for (Expression subexp : subexpressions) {
            Expression[] subexpexps = subexp.getAll(s);
            if (subexpexps != null) {
                expressions.addAll(Arrays.asList(subexpexps));
            }
        }

        // if none found return null
        if (expressions.size() == 0) {
            return null;
        }

        return expressions.toArray(new Expression[]{});
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int l) {
        StringBuilder s = new StringBuilder("");
        s.append(l + ":" + expression);
        //s += "\n";
        if (subexpressions != null) {
            for (Expression exp : subexpressions) {
                s.append(exp.toString(l + 1));
                s.append("\n");
            }
        }
        return s.toString();
    }

    public Expression[] getSubexpressions() {
        // todo implement!!! subexpressions, not subexpression subexpressions necessarily
        if (subexpressions == null) {
            return null;
        }

        return this.subexpressions.toArray(new Expression[]{});
    }

    public List<String> subexpressionSplit(String s, boolean whitespace) {
        Expression[] subexps = getAllSubexpressions(s);
        ArrayList<String> subexpsSplit = new ArrayList<>();

        for (Expression subexp : subexps) {
            subexpsSplit.addAll(Arrays.asList(subexp.getText().split(s)));
        }

        if (!whitespace) {
            subexpsSplit.removeIf((str) -> str.matches("\s+"));
        }
        return subexpsSplit;
    }
}
