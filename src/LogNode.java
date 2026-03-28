import java.math.BigDecimal;

class LogNode extends MathNode implements BigEvaluable {
    MathNode child;
    public LogNode(MathNode child) { this.child = child; }
    @Override double evaluate(double x) { return Math.log(child.evaluate(x)); }
    @Override int getEngineWeight() { return 3; }
    @Override double getDerivative(double x) {
        double u = child.evaluate(x);
        if (u <= 0) return Double.NaN;
        return child.getDerivative(x) / u;
    }
    @Override public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.ln(((BigEvaluable) child).evaluateBig(x));
    }
    @Override public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal u = ((BigEvaluable) child).evaluateBig(x);
        return ((BigEvaluable) child).getDerivativeBig(x).divide(u, BigMath.MC);
    }
}