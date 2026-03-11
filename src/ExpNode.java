class ExpNode extends MathNode {
    MathNode child;
    public ExpNode(MathNode child) { this.child = child; }
    @Override double evaluate(double x) { return Math.exp(child.evaluate(x)); }
    @Override int getEngineWeight() { return 3; }
    @Override double getDerivative(double x) {
        return Math.exp(child.evaluate(x)) * child.getDerivative(x);
    }
}