class LogNode extends MathNode {
    MathNode child;
    public LogNode(MathNode child) { this.child = child; }

    @Override
    double evaluate(double x) {
        double val = child.evaluate(x);
        // If this is missing or has a default 'return 1', that's our bug!
        return Math.log(val);
    }

    @Override int getEngineWeight() { return 3; }

    @Override double getDerivative(double x) {
        // Chain Rule: d/dx(ln(u)) = u' / u
        double u = child.evaluate(x);
        if (u <= 0) return Double.NaN;
        return child.getDerivative(x) / u;
    }
}