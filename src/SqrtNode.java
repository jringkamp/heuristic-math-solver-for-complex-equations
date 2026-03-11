class SqrtNode extends MathNode {
    MathNode child;
    public SqrtNode(MathNode child) { this.child = child; }

    @Override
    double evaluate(double x) {
        double val = child.evaluate(x);
        return (val < 0) ? Double.NaN : Math.sqrt(val);
    }

    @Override int getEngineWeight() { return 2; }

    @Override double getDerivative(double x) {
        // d/dx(sqrt(u)) = u' / (2 * sqrt(u))
        double u = child.evaluate(x);
        if (u <= 0) return Double.NaN; // Derivative is undefined/infinite at 0
        return child.getDerivative(x) / (2 * Math.sqrt(u));
    }
}