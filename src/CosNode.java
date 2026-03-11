class CosNode extends MathNode {
    MathNode child;
    public CosNode(MathNode child) { this.child = child; }
    @Override double evaluate(double x) { return Math.cos(child.evaluate(x)); }
    @Override int getEngineWeight() { return 2; }
    @Override double getDerivative(double x) {
        // d/dx(cos(u)) = -sin(u) * u'
        return -Math.sin(child.evaluate(x)) * child.getDerivative(x);
    }
}