class SinNode extends MathNode {
    MathNode child;
    public SinNode(MathNode child) { this.child = child; }

    @Override double evaluate(double x) { return Math.sin(child.evaluate(x)); }

    @Override int getEngineWeight() { return 2; } // Oscillatory, treat like Atan

    @Override double getDerivative(double x) {
        // Chain Rule: d/dx(sin(u)) = cos(u) * u'
        return Math.cos(child.evaluate(x)) * child.getDerivative(x);
    }
}