import java.util.*;
import java.util.regex.*;

public class ExpressionParser {

    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "+", 1, "-", 1,
            "*", 2, "/", 2,
            "^", 3, "u-", 4   // unary minus has higher precedence////
            );

    public static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder number = new StringBuilder();
        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Number (including decimal)
            if (Character.isDigit(c) || c == '.') {
                number.setLength(0);
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    number.append(expression.charAt(i++));
                }
                tokens.add(number.toString());
                continue;
            }

            // Identifier (x, sin, ln, etc.)
            if (Character.isLetter(c)) {
                number.setLength(0);
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    number.append(expression.charAt(i++));
                }
                tokens.add(number.toString());
                continue;
            }

            // Operators, parentheses, unary minus
            if ("+-*/^()".indexOf(c) != -1) {
                // Detect unary minus (after operator or start or '(')
                if (c == '-' && (i == 0 || "+-*/^(".indexOf(expression.charAt(i-1)) != -1)) {
                    tokens.add("u-");  // special token for unary minus
                } else {
                    tokens.add(String.valueOf(c));
                }
                i++;
                continue;
            }

            throw new IllegalArgumentException("Invalid character: " + c);
        }
        return tokens;
    }

    private static boolean isFunction(String s) {
        return s.equals("sin") || s.equals("ln") || s.equals("sqrt") || s.equals("cos");
    }

    // --- STEP 2 STARTS HERE ---
    public static MathNode parse(String expression) {
        List<String> tokens = tokenize(expression);
        Stack<MathNode> nodes = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.matches("[0-9]*\\.?[0-9]+")) {
                nodes.push(new ConstantNode(Double.parseDouble(token)));
            } else if (token.equals("x")) {
                nodes.push(new VariableNode());
            } else if (isFunction(token)) {
                operators.push(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    processOperator(operators.pop(), nodes);
                }
                if (operators.isEmpty() || !operators.pop().equals("(")) {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
                if (!operators.isEmpty() && isFunction(operators.peek())) {
                    processFunction(operators.pop(), nodes);
                }
            } else if (PRECEDENCE.containsKey(token) || token.equals("u-")) {
                // Right-associativity for ^ (only pop if precedence >= and not right-assoc)
                while (!operators.isEmpty() && !operators.peek().equals("(") &&
                        (PRECEDENCE.getOrDefault(operators.peek(), 0) > PRECEDENCE.get(token) ||
                                (PRECEDENCE.getOrDefault(operators.peek(), 0) == PRECEDENCE.get(token) && !token.equals("^")))) {
                    processOperator(operators.pop(), nodes);
                }
                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {
            processOperator(operators.pop(), nodes);
        }

        if (nodes.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }
        return nodes.pop();
    }
    static class NegateNode extends MathNode {
        MathNode child;

        public NegateNode(MathNode child) { this.child = child; }

        @Override
        double evaluate(double x) {
            return -child.evaluate(x);
        }

        @Override
        int getEngineWeight() {
            return child.getEngineWeight();
        }

        @Override
        double getDerivative(double x) {
            return -child.getDerivative(x);
        }
    }

    private static void processOperator(String op, Stack<MathNode> nodes) {
        if (op.equals("u-")) {
            MathNode child = nodes.pop();
            nodes.push(new NegateNode(child));  // You'll need to add a NegateNode class
            return;
        }

        MathNode right = nodes.pop();
        MathNode left = nodes.pop();
        if (op.equals("^")) {
            nodes.push(new PowerNode(left, right));
        } else {
            nodes.push(new OperatorNode(op.charAt(0), left, right));
        }
    }

    private static void processFunction(String func, Stack<MathNode> nodes) {
        MathNode child = nodes.pop();
        if (func.equals("sin")) nodes.push(new SinNode(child));
        if (func.equals("ln")) nodes.push(new LogNode(child));
        if (func.equals("sqrt")) nodes.push(new SqrtNode(child));
    }


}