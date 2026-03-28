import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class BigMath {

    public static final MathContext MC = new MathContext(50, RoundingMode.HALF_UP);

    // High-precision PI to ensure modulo doesn't lose digits for a 50-digit result
    private static final BigDecimal PI = new BigDecimal(
            "3.1415926535897932384626433832795028841971693993751058209749445923078164062"
    );
    private static final BigDecimal TWO_PI = PI.multiply(BigDecimal.valueOf(2), MC);

    // ────────────────────────────────────────────────
    // Trigonometric functions (Taylor series with Range Reduction)
    // ────────────────────────────────────────────────

    public static BigDecimal sin(BigDecimal x) {
        // Range reduction: sin(x) = sin(x % 2pi)
        BigDecimal reducedX = x.remainder(TWO_PI, MC);

        // Adjust to [-pi, pi] for better convergence
        if (reducedX.compareTo(PI) > 0) reducedX = reducedX.subtract(TWO_PI, MC);
        if (reducedX.compareTo(PI.negate()) < 0) reducedX = reducedX.add(TWO_PI, MC);

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal term = reducedX;
        BigDecimal x2 = reducedX.multiply(reducedX, MC).negate();
        int k = 1;
        while (term.abs().compareTo(new BigDecimal("1E-55")) > 0 && k < 200) {
            sum = sum.add(term, MC);
            term = term.multiply(x2, MC).divide(BigDecimal.valueOf(2L * k * (2L * k + 1)), MC);
            k++;
        }
        return sum.round(MC);
    }

    public static BigDecimal cos(BigDecimal x) {
        // Range reduction
        BigDecimal reducedX = x.remainder(TWO_PI, MC);

        if (reducedX.compareTo(PI) > 0) reducedX = reducedX.subtract(TWO_PI, MC);
        if (reducedX.compareTo(PI.negate()) < 0) reducedX = reducedX.add(TWO_PI, MC);

        BigDecimal sum = BigDecimal.ONE;
        BigDecimal xx = reducedX.multiply(reducedX, MC);
        BigDecimal term = xx.negate().divide(BigDecimal.valueOf(2), MC);
        int k = 1;
        while (term.abs().compareTo(new BigDecimal("1E-55")) > 0 && k < 200) {
            sum = sum.add(term, MC);
            term = term.multiply(xx.negate(), MC).divide(BigDecimal.valueOf((2L * k + 1) * (2L * k + 2)), MC);
            k++;
        }
        return sum.round(MC);
    }

    // ────────────────────────────────────────────────
    // Exponential (Taylor series with extra guard digits)
    // ────────────────────────────────────────────────

    public static BigDecimal exp(BigDecimal x) {
        return exp(x, MC);
    }

    public static BigDecimal exp(BigDecimal x, MathContext ctx) {
        if (x.abs().compareTo(BigDecimal.valueOf(700)) > 0) {
            throw new ArithmeticException("exp argument too large");
        }

        MathContext work = new MathContext(ctx.getPrecision() + 12, RoundingMode.HALF_EVEN);

        BigDecimal sum = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        int n = 1;

        while (term.abs().compareTo(new BigDecimal("1E-" + (ctx.getPrecision() + 8))) > 0 && n < 400) {
            term = term.multiply(x, work).divide(BigDecimal.valueOf(n), work);
            sum = sum.add(term, work);
            n++;
        }
        return sum.round(ctx);
    }

    // ────────────────────────────────────────────────
    // Natural logarithm (range reduction + Newton)
    // ────────────────────────────────────────────────

    private static final BigDecimal LN2 = new BigDecimal(
            "0.6931471805599453094172321214581765680755001343602552541206800094933936219696947156058633269964186875"
    );

    public static BigDecimal ln(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArithmeticException("ln of non-positive");
        }

        MathContext work = new MathContext(72, RoundingMode.HALF_EVEN);  // more guard digits

        BigDecimal y = x;
        int exp2 = 0;
        BigDecimal TWO = BigDecimal.TWO;

        // Bring into [0.5, 2) — wider but safer interval
        while (y.compareTo(TWO) >= 0) {
            y = y.divide(TWO, work);
            exp2++;
        }
        while (y.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            y = y.multiply(TWO, work);
            exp2--;
        }

        // Initial approximation — very accurate near 1
        BigDecimal u = y.subtract(BigDecimal.ONE).divide(y.add(BigDecimal.ONE), work);
        BigDecimal z = u.multiply(BigDecimal.valueOf(2), work);          // 2u/(1+u²) series start

        // Newton-Raphson for z where exp(z) = y
        for (int i = 0; i < 50; i++) {   // generous limit
            BigDecimal ez = exp(z, work);
            BigDecimal delta = y.subtract(ez).divide(ez, work);   // = (y/ez - 1)
            z = z.add(delta, work);

            if (delta.abs().compareTo(new BigDecimal("1E-62")) < 0) {
                break;
            }
        }

        // Re-attach binary exponent
        BigDecimal correction = BigDecimal.valueOf(exp2).multiply(LN2, work);
        BigDecimal result = z.add(correction, work);

        return result.round(MC);   // back to your 50-digit context
    }

    // ────────────────────────────────────────────────
    // Square root (Newton's method)
    // ────────────────────────────────────────────────

    public static BigDecimal sqrt(BigDecimal x) {
        return sqrt(x, MC);
    }

    public static BigDecimal sqrt(BigDecimal x, MathContext ctx) {
        if (x.compareTo(BigDecimal.ZERO) < 0) {
            throw new ArithmeticException("sqrt of negative");
        }
        if (x.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        MathContext work = new MathContext(ctx.getPrecision() + 8, RoundingMode.HALF_EVEN);

        // Initial guess using double
        BigDecimal guess = new BigDecimal(Math.sqrt(x.doubleValue()));

        for (int i = 0; i < 40; i++) {
            BigDecimal prev = guess;
            guess = guess.add(x.divide(guess, work)).divide(BigDecimal.valueOf(2), work);
            if (guess.subtract(prev).abs().compareTo(new BigDecimal("1E-" + (ctx.getPrecision() + 4))) < 0) {
                break;
            }
        }
        return guess.round(ctx);
    }

    // ────────────────────────────────────────────────
    // Other helpers
    // ────────────────────────────────────────────────

    public static BigDecimal abs(BigDecimal x) {
        return x.abs();
    }

    public static BigDecimal atan(BigDecimal x) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal term = x;
        BigDecimal x2 = x.multiply(x, MC).negate();
        int n = 1;
        while (term.abs().compareTo(new BigDecimal("1E-40")) > 0 && n < 200) {
            sum = sum.add(term, MC);
            term = term.multiply(x2, MC).divide(BigDecimal.valueOf(2L * n + 1), MC);
            n++;
        }
        return sum.round(MC);
    }

    public static BigDecimal tan(BigDecimal x) {
        BigDecimal c = cos(x);
        if (c.abs().compareTo(new BigDecimal("1E-30")) < 0) {
            throw new ArithmeticException("tan singularity");
        }
        return sin(x).divide(c, MC);
    }
}