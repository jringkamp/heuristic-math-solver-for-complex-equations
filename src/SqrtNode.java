import java.math.BigDecimal;

class SqrtNode extends MathNode implements BigEvaluable {
    MathNode child;

    public SqrtNode(MathNode child) {
        this.child = child;
    }

    @Override
    double evaluate(double x) {
        double val = child.evaluate(x);
        return (val < 0) ? Double.NaN : Math.sqrt(val);
    }

    @Override
    int getEngineWeight() {
        return 2;
    }

    @Override
    double getDerivative(double x) {
        double u = child.evaluate(x);
        if (u <= 0) return Double.NaN;
        return child.getDerivative(x) / (2 * Math.sqrt(u));
    }

    // ─────────────────────────────────────────
    // BigDecimal support
    // ─────────────────────────────────────────
    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.sqrt(((BigEvaluable) child).evaluateBig(x));
    }

    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal u = ((BigEvaluable) child).evaluateBig(x);
        BigDecimal twoSqrtU = BigDecimal.valueOf(2).multiply(BigMath.sqrt(u), BigMath.MC);
        return ((BigEvaluable) child).getDerivativeBig(x).divide(twoSqrtU, BigMath.MC);
    }
}