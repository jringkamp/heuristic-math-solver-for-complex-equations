class AbsNode extends MathNode {
    MathNode child;
    public AbsNode(MathNode child) { this.child = child; }
    @Override double evaluate(double x) { return Math.abs(child.evaluate(x)); }
    @Override int getEngineWeight() { return 1; }
    @Override double getDerivative(double x) {
        double v = child.evaluate(x);
        if (v == 0) return 0; // The "Undefined" trap point
        return (v > 0 ? 1.0 : -1.0) * child.getDerivative(x);
    }
}