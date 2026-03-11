class TanNode extends MathNode {
    MathNode child;
    public TanNode(MathNode child) { this.child = child; }

    @Override
    double evaluate(double x) {
        return Math.tan(child.evaluate(x));
    }

    @Override int getEngineWeight() { return 4; } // High weight because it's volatile

    @Override double getDerivative(double x) {
        // d/dx(tan(u)) = sec^2(u) * u'
        // Since Java doesn't have sec, we use 1 / cos^2(u)
        double cosVal = Math.cos(child.evaluate(x));
        if (Math.abs(cosVal) < 1e-10) return Double.NaN; // Avoid division by zero
        return (1.0 / (cosVal * cosVal)) * child.getDerivative(x);
    }
}