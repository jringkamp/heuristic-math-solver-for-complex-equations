import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

class OperatorNode extends MathNode implements BigEvaluable {
    char operator;
    MathNode left, right;

    private static final MathContext MC = new MathContext(34, RoundingMode.HALF_UP);

    public OperatorNode(char operator, MathNode left, MathNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    double evaluate(double x) {
        double l = left.evaluate(x);
        double r = right.evaluate(x);
        return switch (operator) {
            case '+' -> l + r;
            case '-' -> l - r;
            case '*' -> l * r;
            case '/' -> l / r;
            case '^' -> Math.pow(l, r);
            default -> 0;
        };
    }

    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        if (left instanceof BigEvaluable && right instanceof BigEvaluable) {
            BigDecimal l = ((BigEvaluable) left).evaluateBig(x);
            BigDecimal r = ((BigEvaluable) right).evaluateBig(x);
            return switch (operator) {
                case '+' -> l.add(r, MC);
                case '-' -> l.subtract(r, MC);
                case '*' -> l.multiply(r, MC);
                case '/' -> l.divide(r, MC);
                case '^' -> l.pow(r.intValue(), MC);
                default -> BigDecimal.ZERO;
            };
        }
        throw new UnsupportedOperationException("Sub-nodes do not support Big Mode");
    }

    @Override
    int getEngineWeight() {
        if (operator == '^') {
            if (right.getEngineWeight() >= 1) {
                if (right instanceof OperatorNode && ((OperatorNode)right).operator == '^') return 4;
                return 3;
            }
            return 2;
        }
        return Math.max(left.getEngineWeight(), right.getEngineWeight());
    }

    @Override
    double getDerivative(double x) {
        double u = left.evaluate(x);
        double v = right.evaluate(x);
        double du = left.getDerivative(x);
        double dv = right.getDerivative(x);

        return switch (operator) {
            case '+' -> du + dv;
            case '-' -> du - dv;
            case '*' -> du * v + u * dv;
            case '/' -> (du * v - u * dv) / (v * v);
            case '^' -> {
                if (left instanceof ConstantNode) {
                    yield Math.pow(u, v) * Math.log(u) * dv;
                } else {
                    yield v * Math.pow(u, v - 1) * du;
                }
            }
            default -> 0;
        };
    }

    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        if (left instanceof BigEvaluable && right instanceof BigEvaluable) {
            BigEvaluable bLeft = (BigEvaluable) left;
            BigEvaluable bRight = (BigEvaluable) right;

            BigDecimal u = bLeft.evaluateBig(x);
            BigDecimal v = bRight.evaluateBig(x);
            BigDecimal du = bLeft.getDerivativeBig(x);
            BigDecimal dv = bRight.getDerivativeBig(x);

            return switch (operator) {
                case '+' -> du.add(dv, MC);
                case '-' -> du.subtract(dv, MC);
                case '*' -> (du.multiply(v, MC)).add(u.multiply(dv, MC), MC);
                case '/' -> {
                    BigDecimal num = (du.multiply(v, MC)).subtract(u.multiply(dv, MC), MC);
                    BigDecimal den = v.multiply(v, MC);
                    yield num.divide(den, MC);
                }
                case '^' -> {
                    if (left instanceof ConstantNode) {
                        double logBase = Math.log(u.doubleValue());
                        BigDecimal bigLogBase = BigDecimal.valueOf(logBase);
                        yield u.pow(v.intValue(), MC).multiply(bigLogBase, MC).multiply(dv, MC);
                    } else {
                        BigDecimal vMinusOne = v.subtract(BigDecimal.ONE, MC);
                        yield v.multiply(u.pow(vMinusOne.intValue(), MC), MC).multiply(du, MC);
                    }
                }
                default -> BigDecimal.ZERO;
            };
        }
        throw new UnsupportedOperationException("Sub-nodes do not support Big Mode");
    }
}