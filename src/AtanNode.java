class AtanNode extends MathNode {
    MathNode child;
    public AtanNode(MathNode child) { this.child = child; }
    @Override double evaluate(double x) { return Math.atan(child.evaluate(x)); }
    @Override int getEngineWeight() { return 2; }
    @Override double getDerivative(double x) {
        double v = child.evaluate(x);
        return (1.0 / (1.0 + v * v)) * child.getDerivative(x);
    }
}