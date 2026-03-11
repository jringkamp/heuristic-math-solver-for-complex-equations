class OperatorNode extends MathNode {
    char operator;
    MathNode left, right;

    public OperatorNode(char operator, MathNode left, MathNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    // 1. EVALUATION (The "Nerves")
    @Override
    double evaluate(double x) {
        double l = left.evaluate(x);
        double r = right.evaluate(x);
        double result = 0;

        switch (operator) {
            case '+': result = l + r; break;
            case '-': result = l - r; break;
            case '*': result = l * r; break;
            case '/':
                result = l / r;
                // DEBUG: See the division happen
                System.out.println("[DEBUG] Node Eval: " + l + " / " + r + " = " + result);
                break;
            case '^': result = Math.pow(l, r); break;
        }
        return result;
    }

    // 2. WEIGHTING (The "Eyes" - Identifying Engines)
    @Override
    int getEngineWeight() {
        if (operator == '^') {
            if (right.getEngineWeight() >= 1) {
                // Check for Tower Engine (x^x^x)
                if (right instanceof OperatorNode && ((OperatorNode)right).operator == '^') return 4;
                return 3; // Exponential (2^x)
            }
            return 2; // Polynomial (x^2)
        }
        return Math.max(left.getEngineWeight(), right.getEngineWeight());
    }

    // 3. DERIVATIVE (The "Velocity" - Finding the Slope)
    @Override
    double getDerivative(double x) {
        switch (operator) {
            // ... (other cases)
            case '/': // ADD THIS (Quotient Rule)
                double u = left.evaluate(x);
                double v = right.evaluate(x);
                double du = left.getDerivative(x);
                double dv = right.getDerivative(x);
                return (du * v - u * dv) / (v * v);

            case '*':
                return left.getDerivative(x) * right.evaluate(x)
                        + left.evaluate(x) * right.getDerivative(x);

            case '^':
                if (left instanceof ConstantNode) {
                    // Exponential: b^x -> b^x * ln(b) * x'
                    double base = left.evaluate(x);
                    return evaluate(x) * Math.log(base) * right.getDerivative(x);
                } else {
                    // Power: f(x)^n -> n * f(x)^(n-1) * f'(x)   (chain rule)
                    double n = right.evaluate(x);
                    double base = left.evaluate(x);                       // FIX: was Math.pow(x, ...)
                    return n * Math.pow(base, n - 1) * left.getDerivative(x); // FIX: chain rule added
                }
            default: return 0;
        }
    }
}