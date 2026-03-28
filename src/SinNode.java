import java.math.BigDecimal;

class SinNode extends MathNode implements BigEvaluable {
    MathNode child;
    public SinNode(MathNode child) { this.child = child; }

    @Override double evaluate(double x) { return Math.sin(child.evaluate(x)); }

    @Override int getEngineWeight() { return 2; }

    @Override double getDerivative(double x) {
        return Math.cos(child.evaluate(x)) * child.getDerivative(x);
    }

    // BIG ENGINE
    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        return BigMath.sin(((BigEvaluable) child).evaluateBig(x));
    }
    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal u = ((BigEvaluable) child).evaluateBig(x);
        return BigMath.cos(u).multiply(((BigEvaluable) child).getDerivativeBig(x), BigMath.MC);
    }
}