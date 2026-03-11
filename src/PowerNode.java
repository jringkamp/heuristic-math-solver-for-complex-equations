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

    // PowerNode.getEngineWeight() currently always returns 4
// FIX — check if exponent is constant (polynomial) vs variable (exponential)
    @Override
    int getEngineWeight() {
        if (exponent.getEngineWeight() == 0) return 2; // x^2 = polynomial
        if (base.getEngineWeight() == 0) return 3;     // 2^x = exponential
        return 4;                                        // x^x = tower
    }

    @Override
    double getDerivative(double x) {
        double b = base.evaluate(x);
        double e = exponent.evaluate(x);

        // If exponent is constant, simple power rule works for negative base too
        if (exponent instanceof ConstantNode) {
            return e * Math.pow(b, e - 1) * base.getDerivative(x);
        }

        // General rule requires ln(b) — only valid for positive base
        if (b <= 0) return 0;

        double val = evaluate(x);
        return val * (exponent.getDerivative(x) * Math.log(b) +
                e * base.getDerivative(x) / b);
    }
}