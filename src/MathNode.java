abstract class MathNode {
    abstract int getEngineWeight();
    // NEW: Calculate the numerical value at point x
    abstract double evaluate(double x);
    abstract double getDerivative(double x);

}

class ConstantNode extends MathNode {
    double value;
    public ConstantNode(double value) { this.value = value; }

    @Override int getEngineWeight() { return 0; }
    @Override double evaluate(double x) { return value; }

    // THE FIX: The derivative of a constant is always 0
    @Override
    double getDerivative(double x) {
        return 0.0;
    }
}

class VariableNode extends MathNode {
    @Override int getEngineWeight() { return 1; }
    @Override double evaluate(double x) { return x; }

    // THE FIX: The derivative of x is always 1
    @Override
    double getDerivative(double x) {
        return 1.0;
    }

}
