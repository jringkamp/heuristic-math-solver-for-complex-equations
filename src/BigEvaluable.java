import java.math.BigDecimal;

public interface BigEvaluable {
    BigDecimal evaluateBig(BigDecimal x);
    BigDecimal getDerivativeBig(BigDecimal x);
}