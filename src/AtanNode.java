import java.math.BigDecimal;

class AtanNode extends MathNode implements BigEvaluable {
    MathNode child;

    public AtanNode(MathNode child) {
        this.child = child;
    }

    @Override
    double evaluate(double x) {
        return Math.atan(child.evaluate(x));
    }

    @Override
    int getEngineWeight() {
        return 2;
    }

    @Override
    double getDerivative(double x) {
        double v = child.evaluate(x);
        return (1.0 / (1.0 + v * v)) * child.getDerivative(x);
    }

    // ─────────────────────────────────────────
    // BigDecimal support
    // ─────────────────────────────────────────
    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.atan(((BigEvaluable) child).evaluateBig(x));
    }

    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal v = ((BigEvaluable) child).evaluateBig(x);
        BigDecimal denom = BigDecimal.ONE.add(
                v.multiply(v, BigMath.MC),
                BigMath.MC
        );
        return ((BigEvaluable) child).getDerivativeBig(x)
                .divide(denom, BigMath.MC);
    }
}