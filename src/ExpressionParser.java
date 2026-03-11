import java.util.*;
import java.util.regex.*;

public class ExpressionParser {

    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "+", 1, "-", 1,
            "*", 2, "/", 2,
            "^", 3
    );

    public static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("([0-9]*\\.?[0-9]+|[a-zA-Z]+|[+\\-*/^()])").matcher(expression);
        while (m.find()) {
            tokens.add(m.group());
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
            } else if (isFunction(token) || token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    processOperator(operators.pop(), nodes);
                }
                operators.pop(); // Pop the "("
                if (!operators.isEmpty() && isFunction(operators.peek())) {
                    processFunction(operators.pop(), nodes);
                }
            } else if (PRECEDENCE.containsKey(token)) {
                while (!operators.isEmpty() && !operators.peek().equals("(") &&
                        PRECEDENCE.getOrDefault(operators.peek(), 0) >= PRECEDENCE.get(token)) {
                    processOperator(operators.pop(), nodes);
                }
                operators.push(token);
            }
        }
        while (!operators.isEmpty()) {
            processOperator(operators.pop(), nodes);
        }
        return nodes.pop();
    }

    private static void processOperator(String op, Stack<MathNode> nodes) {
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