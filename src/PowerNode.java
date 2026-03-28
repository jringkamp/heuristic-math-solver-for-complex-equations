import java.math.BigDecimal;

class PowerNode extends MathNode implements BigEvaluable {
    MathNode base;
    MathNode exponent;

    public PowerNode(MathNode base, MathNode exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    double evaluate(double x) {
        return Math.pow(base.evaluate(x), exponent.evaluate(x));
    }

    @Override
    int getEngineWeight() {
        // 0 usually means ConstantNode
        if (exponent.getEngineWeight() == 0) return 2;  // x^k        → polynomial-like → fast
        if (base.getEngineWeight() == 0)     return 3;  // c^x, e^x   → exponential   → medium
        return 4;                                       // x^x, x^{x+1} → tower/general → slow
    }

    @Override
    double getDerivative(double x) {
        double b = base.evaluate(x);
        double e = exponent.evaluate(x);

        // Simple power rule when exponent is constant (works even if base negative, if e integer)
        if (exponent instanceof ConstantNode) {
            return e * Math.pow(b, e - 1) * base.getDerivative(x);
        }

        // General case: logarithmic derivative → only valid for positive base
        if (b <= 0) {
            return Double.NaN;  // or 0 — your choice; NaN is common for undefined
        }

        double val = evaluate(x);
        return val * (exponent.getDerivative(x) * Math.log(b) +
                e * base.getDerivative(x) / b);
    }

    // ────────────────────────────────────────────────
    //                  BIG DECIMAL ENGINE
    // ────────────────────────────────────────────────

    @Override
    public BigDecimal evaluateBig(BigDecimal x) {
        BigDecimal b = ((BigEvaluable) base).evaluateBig(x);
        BigDecimal e = ((BigEvaluable) exponent).evaluateBig(x);

        boolean isIntegerExp = e.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;

        // Negative base handling
        if (b.compareTo(BigDecimal.ZERO) < 0) {
            if (!isIntegerExp) {
                throw new ArithmeticException("negative base with non-integer exponent in Big Mode");
            }

            int expInt = e.intValueExact();
            BigDecimal absBase = b.abs();
            BigDecimal absPow;

            if (expInt >= 0) {
                absPow = absBase.pow(expInt, BigMath.MC);
            } else {
                absPow = BigDecimal.ONE.divide(
                        absBase.pow(-expInt, BigMath.MC),
                        BigMath.MC);
            }

            boolean oddExponent = (expInt % 2 != 0);
            return oddExponent ? absPow.negate() : absPow;
        }

        // Zero base
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            if (e.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ArithmeticException("0^negative or 0^0 in Big Mode");
            }
            return BigDecimal.ZERO;
        }

        // Positive base — fast path for integer exponent
        if (isIntegerExp && e.signum() >= 0) {
            return b.pow(e.intValueExact(), BigMath.MC);
        }

        // General real exponent using exp(ln(b) * e)
        return BigMath.exp(e.multiply(BigMath.ln(b), BigMath.MC));
    }

    @Override
    public BigDecimal getDerivativeBig(BigDecimal x) {
        BigDecimal b  = ((BigEvaluable) base).evaluateBig(x);
        BigDecimal e  = ((BigEvaluable) exponent).evaluateBig(x);
        BigDecimal db = ((BigEvaluable) base).getDerivativeBig(x);

        boolean isIntegerExp = e.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
        BigDecimal de = ((BigEvaluable) exponent).getDerivativeBig(x);
        boolean isConstantExp = de.signum() == 0;

        // Fast path: constant integer exponent → power rule
        // Safe even for negative base (since exponent is integer)
        if (isConstantExp && isIntegerExp) {
            int expInt = e.intValueExact();
            if (expInt == 0) return BigDecimal.ZERO;

            BigDecimal pMinus1 = b.pow(expInt - 1, BigMath.MC);
            return e.multiply(pMinus1, BigMath.MC).multiply(db, BigMath.MC);
        }

        // General case: u^v  →  requires b > 0
        if (b.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;   // or throw — your convention
        }

        // f' = v * u^(v-1) * u' + u^v * ln(u) * v'
        BigDecimal term1 = e.multiply(db, BigMath.MC).divide(b, BigMath.MC);
        BigDecimal lnB   = BigMath.ln(b);
        BigDecimal term2 = de.multiply(lnB, BigMath.MC);

        // Compute u^v efficiently
        BigDecimal uPowV;
        if (e.scale() == 0 && e.signum() >= 0) {
            uPowV = b.pow(e.intValueExact(), BigMath.MC);
        } else {
            uPowV = BigMath.exp(e.multiply(lnB, BigMath.MC));
        }

        return uPowV.multiply(term1.add(term2, BigMath.MC), BigMath.MC);
    }
}