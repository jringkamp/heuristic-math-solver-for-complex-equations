import java.math.BigDecimal;

class AbsNode extends MathNode implements BigEvaluable {
    MathNode child;

    public AbsNode(MathNode child) {
        this.child = child;
    }

    @Override
    double evaluate(double x) {
        return Math.abs(child.evaluate(x));
    }

    @Override
    int getEngineWeight() {
        return 1;
    }

    @Override
    double getDerivative(double x) {
        double v = child.evaluate(x);
        if (v == 0) return 0;
        return (v > 0 ? 1.0 : -1.0) * child.getDerivative(x);
    }

    // ─────────────────────────────────────────
    // BigDecimal support
    // ─────────────────────────────────────────
    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.abs(((BigEvaluable) child).evaluateBig(x));
    }

    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal v = ((BigEvaluable) child).evaluateBig(x);
        BigDecimal dv = ((BigEvaluable) child).getDerivativeBig(x);

        int cmp = v.compareTo(BigDecimal.ZERO);
        if (cmp == 0) {
            return BigDecimal.ZERO;
        }
        return (cmp > 0 ? BigDecimal.ONE : BigDecimal.ONE.negate())
                .multiply(dv, BigMath.MC);
    }
}