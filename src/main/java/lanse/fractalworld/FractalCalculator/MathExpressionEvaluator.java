package lanse.fractalworld.FractalCalculator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathExpressionEvaluator {

    private static final Set<String> FUNCTIONS = Set.of(
            //6.0.0
            "sin", "cos", "tan", "sqrt", "log", "exp", "abs",

            //6.1.0
            "rand", "ceil", "floor", "round", "toDegrees", "toRadians",
            "asin", "acos", "atan", "sinh", "cosh", "tanh"
    );

    private static final Set<Character> OPERATORS = Set.of('+', '-', '*', '/', '^', '%');

    public static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();

        // wtf did I make (this line took me 3 days, I hate regex)-
        Matcher matcher = Pattern.compile("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?|[a-zA-Z][a-zA-Z0-9]*|[+\\-*/%^()]").matcher(expression);

        String previousToken = null;

        while (matcher.find()) {
            String token = matcher.group();

            // If the previous token is a number and the current token is a variable, insert a multiplication operator
            if (previousToken != null && isNumeric(previousToken) && token.matches("[a-zA-Z][a-zA-Z0-9]*")) {
                tokens.add("*");
            }

            tokens.add(token);
            previousToken = token; // Update the previous token
        }
        return tokens;
    }

    public static double evaluateInfix(List<String> tokens, Map<String, Double> variables) {
        Stack<Double> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (isNumeric(token)) {
                values.push(Double.parseDouble(token));
            } else if (FUNCTIONS.contains(token)) {
                operators.push(token);
            } else if ("(".equals(token)) {
                operators.push(token);
            } else if (")".equals(token)) {
                while (!operators.isEmpty() && !"(".equals(operators.peek())) {
                    applyOperator(values, operators.pop());
                }
                operators.pop(); // Remove the '('
                if (!operators.isEmpty() && FUNCTIONS.contains(operators.peek())) {
                    applyFunction(values, operators.pop());
                }
            } else if (isOperator(token)) {
                while (!operators.isEmpty() && precedence(token) <= precedence(operators.peek())) {
                    applyOperator(values, operators.pop());
                }
                operators.push(token);
            } else {
                // Handle variables
                if (variables.containsKey(token)) {
                    values.push(variables.get(token));
                } else {
                    throw new IllegalArgumentException("Unknown variable: " + token);
                }
            }
        }
        while (!operators.isEmpty()) {
            applyOperator(values, operators.pop());
        }

        if (values.size() != 1) {
            throw new IllegalArgumentException("Invalid expression: leftover elements in values stack");
        }
        return values.pop();
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isOperator(String token) {
        return token.length() == 1 && OPERATORS.contains(token.charAt(0));
    }

    private static int precedence(String op) {
        if ("+".equals(op) || "-".equals(op)) return 1;
        if ("*".equals(op) || "/".equals(op) || "%".equals(op)) return 2;
        if ("^".equals(op)) return 3;
        return 0;
    }

    private static void applyOperator(Stack<Double> values, String operator) {
        if (values.size() < 2) {
            throw new IllegalArgumentException("Invalid expression: insufficient operands for operator " + operator);
        }
        double b = values.pop();
        double a = values.pop();
        double result = switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "%" -> a % b;
            case "^" -> Math.pow(a, b);
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
        values.push(result);
    }

    private static void applyFunction(Stack<Double> values, String function) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression: insufficient operands for function " + function);
        }
        double value = values.pop();
        double result = switch (function) {
            //6.0.0
            case "sin" -> Math.sin(value);
            case "cos" -> Math.cos(value);
            case "tan" -> Math.tan(value);
            case "sqrt" -> Math.sqrt(value);
            case "log" -> Math.log(value);
            case "exp" -> Math.exp(value);
            case "abs" -> Math.abs(value);

            //6.1.0

            case "ceil" -> Math.ceil(value);
            case "floor" -> Math.floor(value);
            case "round" -> (double) Math.round(value);
            case "rand" -> Math.random() * Math.abs(value);
            case "toDegrees" -> Math.toDegrees(value);
            case "toRadians" -> Math.toRadians(value);
            case "asin" -> Math.asin(value);
            case "acos" -> Math.acos(value);
            case "atan" -> Math.atan(value);
            case "sinh" -> Math.sinh(value);
            case "cosh" -> Math.cosh(value);
            case "tanh" -> Math.tanh(value);
            default -> throw new IllegalArgumentException("Unsupported function: " + function);
        };
        values.push(result);
    }
}