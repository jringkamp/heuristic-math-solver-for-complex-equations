import java.math.BigDecimal;

class CosNode extends MathNode implements BigEvaluable {
    MathNode child;
    public CosNode(MathNode child) { this.child = child; }
    @Override double evaluate(double x) { return Math.cos(child.evaluate(x)); }
    @Override int getEngineWeight() { return 2; }
    @Override double getDerivative(double x) {
        return -Math.sin(child.evaluate(x)) * child.getDerivative(x);
    }

    @Override public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.cos(((BigEvaluable) child).evaluateBig(x));
    }
    @Override public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal u = ((BigEvaluable) child).evaluateBig(x);
        return BigMath.sin(u).negate().multiply(((BigEvaluable) child).getDerivativeBig(x), BigMath.MC);
    }
}