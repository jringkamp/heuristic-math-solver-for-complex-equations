class PowerNode extends MathNode {
    MathNode base;
    MathNode exponent;

    public PowerNode(MathNode base, MathNode exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    double evaluate(double x) {
        return Math.pow(base.evaluate(x), exponent.evaluate(x));
    }

    @Override
    int getEngineWeight() { return 4; }

    @Override
    double getDerivative(double x) {
        double b = base.evaluate(x);
        double e = exponent.evaluate(x);
        if (b <= 0) return 0; // Avoid log of negative/zero in general rule

        // General Power Rule: d/dx(u^v) = u^v * (v' * ln(u) + v * u' / u)
        double val = evaluate(x);
        return val * (exponent.getDerivative(x) * Math.log(b) +
                e * base.getDerivative(x) / b);
    }
}