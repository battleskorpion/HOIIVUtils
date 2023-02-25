package hoi4Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static hoi4Parser.Parser.usefulData;

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
    public Expression get(String s) {
        Expression exp = new Expression(s);
        if(expression != null && expression.trim().contains(s)) {
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


    public String getName() {
        if (subexpressions != null) {
            return null;
        }

        return expression.substring(expression.indexOf("=") + 1).trim();
    }
}
