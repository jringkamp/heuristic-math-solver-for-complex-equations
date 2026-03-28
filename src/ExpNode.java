import java.math.BigDecimal;

class ExpNode extends MathNode implements BigEvaluable {
    MathNode child;
    public ExpNode(MathNode child) { this.child = child; }
    @Override double evaluate(double x) { return Math.exp(child.evaluate(x)); }
    @Override int getEngineWeight() { return 4; }
    @Override double getDerivative(double x) {
        return Math.exp(child.evaluate(x)) * child.getDerivative(x);
    }
    @Override public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.exp(((BigEvaluable) child).evaluateBig(x));
    }
    @Override public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal u = ((BigEvaluable) child).evaluateBig(x);
        return BigMath.exp(u).multiply(((BigEvaluable) child).getDerivativeBig(x), BigMath.MC);
    }
}