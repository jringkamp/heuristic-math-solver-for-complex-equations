import java.math.BigDecimal;
abstract class MathNode {
    abstract int getEngineWeight();
    // NEW: Calculate the numerical value at point x
    abstract double evaluate(double x);
    abstract double getDerivative(double x);

}



// Add "implements BigEvaluable" here!
class ConstantNode extends MathNode implements BigEvaluable {
    double value;
    public ConstantNode(double value) { this.value = value; }

    @Override int getEngineWeight() { return 0; }
    @Override double evaluate(double x) { return value; }
    @Override double getDerivative(double x) { return 0.0; }

    // This @Override now refers to the INTERFACE
    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        return BigDecimal.valueOf(value);
    }

    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        return BigDecimal.ZERO;
    }
}

// ADD "implements BigEvaluable" RIGHT HERE
class VariableNode extends MathNode implements BigEvaluable {
    @Override int getEngineWeight() { return 1; }
    @Override double evaluate(double x) { return x; }
    @Override double getDerivative(double x) { return 1.0; }

    // This @Override is now valid because of the Interface!
    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        return x;
    }

    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        return BigDecimal.ONE;
    }
}


