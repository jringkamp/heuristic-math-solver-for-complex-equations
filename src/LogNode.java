class LogNode extends MathNode {
    MathNode child;
    public LogNode(MathNode child) { this.child = child; }

    @Override
    double evaluate(double x) {
        double val = child.evaluate(x);
        if (val <= 0) return Double.NaN; // The Forbidden Zone
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