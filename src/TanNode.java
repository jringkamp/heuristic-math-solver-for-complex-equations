import java.math.BigDecimal;

class TanNode extends MathNode implements BigEvaluable {
    MathNode child;
    public TanNode(MathNode child) { this.child = child; }

    @Override double evaluate(double x) { return Math.tan(child.evaluate(x)); }
    @Override int getEngineWeight() { return 4; }
    @Override double getDerivative(double x) {
        double cosVal = Math.cos(child.evaluate(x));
        if (Math.abs(cosVal) < 1e-10) return Double.NaN;
        return (1.0 / (cosVal * cosVal)) * child.getDerivative(x);
    }

    @Override public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.tan(((BigEvaluable) child).evaluateBig(x));
    }
    @Override public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal u = ((BigEvaluable) child).evaluateBig(x);
        BigDecimal c = BigMath.cos(u);
        return BigDecimal.ONE.divide(c.multiply(c, BigMath.MC), BigMath.MC)
                .multiply(((BigEvaluable) child).getDerivativeBig(x), BigMath.MC);
    }
}