import static java.lang.Double.isNaN;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
class OmniSolver {

    /**
     * Phase 1: Seed Improvement (The "Stripper")
     */
    public void startSolver(String function, double target, double seed) {
        if (!isSafeToSolve(function)) {
            System.out.println("[CRITICAL ERROR] Illegal mathematical expression. Solver aborted.");
            return;
        }

        try {
            MathNode equation = ExpressionParser.parse(function);
            System.out.println("--- Solving: " + function + " = " + target + " ---");
            double root = findRoot(function, equation, target, seed);
            System.out.printf("\nFINAL RESULT: %.10f\n", root);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to parse or solve: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
// Walks the top-level expression tree and collects
// all additive terms into a flat list.
// e.g. 2^x + x - 5  →  [2^x, x, ConstantNode(-5)]
// ─────────────────────────────────────────
    public static List<MathNode> getAdditiveTerms(MathNode node) {
        List<MathNode> terms = new ArrayList<>();
        collectTerms(node, terms, false);
        return terms;
    }

    public static void collectTerms(MathNode node, List<MathNode> terms, boolean negate) {
        if (node instanceof OperatorNode) {
            OperatorNode op = (OperatorNode) node;
            if (op.operator == '+') {
                collectTerms(op.left,  terms, negate);
                collectTerms(op.right, terms, negate);
                return;
            }
            if (op.operator == '-') {
                collectTerms(op.left,  terms, negate);
                collectTerms(op.right, terms, !negate);  // right side is negated
                return;
            }
        }
        // For NegateNode from the parser (unary minus)
        if (node instanceof ExpressionParser.NegateNode) {
            collectTerms(((ExpressionParser.NegateNode) node).child, terms, !negate);
            return;
        }
        // Leaf or non-additive node — wrap in NegateNode if it came from a subtraction
        terms.add(negate ? new ExpressionParser.NegateNode(node) : node);
    }

    // ─────────────────────────────────────────
// Runs Newton steps on a single isolated term
// to find where that term alone equals the target.
// This is the "factored" seed for that term's growth rate.
// ─────────────────────────────────────────
    public static double seedFromTerm(MathNode term, double target, double startGuess) {
        double x = Math.abs(startGuess) < 1e-6 ? 1.0 : startGuess;  // avoid zero start
        for (int i = 0; i < 8; i++) {
            double val = term.evaluate(x);
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                x = Math.abs(x) + 0.5;  // step away from undefined territory
                continue;
            }
            double slope = term.getDerivative(x);
            if (Math.abs(slope) < 1e-10) break;
            double delta = (target - val) / slope;
            double maxJump = Math.max(Math.abs(x) * 0.3, 1.0);
            delta = Math.max(-maxJump, Math.min(delta, maxJump));
            double newX = x + delta;
            // If the step produces NaN, don't take it
            if (Double.isNaN(term.evaluate(newX)) || Double.isInfinite(term.evaluate(newX))) {
                x = Math.abs(x) + 0.5;
                continue;
            }
            x = newX;
            if (Double.isNaN(x) || Double.isInfinite(x)) return startGuess;
        }
        return x;
    }
    // ─────────────────────────────────────────
    // Factors the equation by growth rate and derives
    // two seeds — one from the fastest-growing term,
    // one from the slowest non-constant term.
    // Returns [fastSeed, slowSeed].
    // findRoot compares them and picks the best.
    // ─────────────────────────────────────────
    public static double[] improveSeed(MathNode f, double target, double guess) {
        double initialVal = f.evaluate(guess);
        System.out.println("[DEBUG] Testing seed: " + guess);
        System.out.println("[DEBUG] Initial Value: " + initialVal);

        if (Double.isNaN(initialVal) || Double.isInfinite(initialVal)) {
            System.out.println("[WARNING] Initial seed is in an undefined region.");
            return new double[]{Double.NaN, Double.NaN};
        }

        // Split into additive terms and sort by engine weight descending
        List<MathNode> terms = getAdditiveTerms(f);
        terms.sort((a, b) -> Integer.compare(b.getEngineWeight(), a.getEngineWeight()));

        // Filter out pure constants — they contribute nothing to seed derivation
        List<MathNode> activeTerms = new ArrayList<>();
        for (MathNode t : terms) {
            if (t.getEngineWeight() > 0) activeTerms.add(t);
        }

        System.out.println("[INFO] Phase 1: Factoring " + activeTerms.size()
                + " active terms (+" + (terms.size() - activeTerms.size()) + " constants)...");

        // Fast growth seed — dominant term (highest weight)
        double fastSeed;
        if (!activeTerms.isEmpty()) {
            MathNode fastTerm = activeTerms.get(0);
            System.out.println("[INFO] Fast term weight: " + fastTerm.getEngineWeight());
            fastSeed = seedFromTerm(fastTerm, target, guess);
        } else {
            fastSeed = guess;
        }

        // Slow growth seed — weakest non-constant term (lowest weight > 0)
        double slowSeed;
        if (activeTerms.size() > 1) {
            MathNode slowTerm = activeTerms.get(activeTerms.size() - 1);
            System.out.println("[INFO] Slow term weight: " + slowTerm.getEngineWeight());
            slowSeed = seedFromTerm(slowTerm, target, guess);
        } else {
            // Only one active term — use a simple offset as the slow seed
            slowSeed = (Math.abs(fastSeed) > 1e-6) ? 1.0 / fastSeed : fastSeed + 1.0;
        }

        // Validate — if a seed landed in undefined territory, fall back
        if (Double.isNaN(fastSeed) || Double.isInfinite(fastSeed)) fastSeed = guess;
        if (Double.isNaN(slowSeed) || Double.isInfinite(slowSeed)) slowSeed = guess + 1.0;

        System.out.println("[INFO] Fast seed: " + fastSeed
                + " | f(x) error: " + Math.abs(f.evaluate(fastSeed) - target));
        System.out.println("[INFO] Slow seed: " + slowSeed
                + " | f(x) error: " + Math.abs(f.evaluate(slowSeed) - target));

        return new double[]{fastSeed, slowSeed};
    }

    // ─────────────────────────────────────────
// Dual test: run both classic and Big — automatic smart seed if null
// ─────────────────────────────────────────

    // ─────────────────────────────────────────
// Factors the equation by growth rate and derives
// two seeds — one from the fastest-growing term,
// one from the slowest non-constant term.
// Returns [fastSeed, slowSeed].
// findRoot compares them and picks the best.
// ─────────────────────────────────────────
    /**
     * Phase 2: The Main Solver Loop
     */
    public static double findRoot(String originalFunction, MathNode equation, double target, double startX) {
        int totalNewtonSteps = 0;

        if (!isSafeToSolve(originalFunction)) {
            System.out.println("[CRITICAL ERROR] Illegal mathematical expression (Division by Zero).");
            return Double.NaN;
        }

        double epsilon = Math.max(1e-10, Math.abs(target) * 1e-4);

        int maxIter = 500;
        double prevError = Double.POSITIVE_INFINITY;

        int stallCount = 0;
        double bestError = Double.POSITIVE_INFINITY;
        int noImprovementCount = 0;

        System.out.println("[INFO] Starting OmniSolver...");

        // ───────────────────────────────────────────────
        // Phase 1: Dual-Factoring Seed Improvement
        // ───────────────────────────────────────────────
        System.out.println("[Phase 1] Computing seeds via factoring...");
        double[] seeds = improveSeed(equation, target, startX);
        if (isNaN(seeds[0]) && isNaN(seeds[1])) return Double.NaN;

        double forwardSeed = Double.isNaN(seeds[0]) ? seeds[1] : seeds[0];
        double reverseSeed = Double.isNaN(seeds[1]) ? seeds[0] : seeds[1];

        double valForward = equation.evaluate(forwardSeed);
        double errForward = Math.abs(target - valForward);

        double valReverse = equation.evaluate(reverseSeed);
        double errReverse = (isNaN(valReverse) || Double.isInfinite(valReverse))
                ? Double.MAX_VALUE : Math.abs(target - valReverse);

        double x;
        if (errForward < errReverse * 0.5) {
            x = forwardSeed;
            System.out.println("[Phase 1] Fast seed chosen (better fit)");
        } else if (errReverse < errForward * 0.5) {
            x = reverseSeed;
            System.out.println("[Phase 1] Slow seed chosen (better fit)");
        } else {
            x = (forwardSeed + reverseSeed) / 2;
            System.out.println("[Phase 1] Blended seeds (comparable fit)");
        }

        // Also consider the original startX — improveSeed may have drifted to a worse region
        double errStartX = Math.abs(equation.evaluate(startX) - target);
        if (!Double.isNaN(errStartX) && !Double.isInfinite(errStartX) && errStartX < Math.abs(equation.evaluate(x) - target)) {
            x = startX;
            System.out.println("[Phase 1] Original startX kept (better fit than factored seeds)");
        }
        System.out.println("[Phase 1] Starting x = " + x);
        // Phase 1.5: Bracket expansion
        double spread = Math.max(5.0, Math.abs(x) * 0.5);
        double low  = x - spread;
        double high = x + spread;

        double fLow  = equation.evaluate(low)  - target;
        double fHigh = equation.evaluate(high) - target;

        if (Double.isNaN(fLow) || Double.isInfinite(fLow)) {
            low  = x - 1.0;
            fLow = equation.evaluate(low) - target;
        }
        if (Double.isNaN(fHigh) || Double.isInfinite(fHigh)) {
            high  = x + 1.0;
            fHigh = equation.evaluate(high) - target;
        }

        int expandCount = 0;
        while (fLow * fHigh > 0 && expandCount < 40) {
            // Expand outward
            double leftStep  = Math.max(10.0, Math.abs(low)  * 0.5);
            double rightStep = Math.max(10.0, Math.abs(high) * 0.5);

            double newLow  = low  - leftStep;
            double newHigh = high + rightStep;

            double newFLow  = equation.evaluate(newLow)  - target;
            double newFHigh = equation.evaluate(newHigh) - target;

            if (!Double.isNaN(newFLow) && !Double.isInfinite(newFLow)) {
                low  = newLow;
                fLow = newFLow;
            }
            if (!Double.isNaN(newFHigh) && !Double.isInfinite(newFHigh)) {
                high  = newHigh;
                fHigh = newFHigh;
            }

            // Also probe inward: the root may be between the seed and zero
            // (e.g. x^2 - 4 = 0 has sign change between 0 and 2, not outward)
            if (fLow * fHigh > 0 && expandCount < 10) {
                double mid = (low + high) / 2.0;
                double fMid = equation.evaluate(mid) - target;
                if (!Double.isNaN(fMid) && !Double.isInfinite(fMid)) {
                    if (fMid * fHigh <= 0) {
                        // Prefer the right/positive half when both halves work
                        low  = mid;
                        fLow = fMid;
                    } else if (fLow * fMid <= 0) {
                        high  = mid;
                        fHigh = fMid;
                    }
                }
            }

            expandCount++;
            if (expandCount % 5 == 0) {
                System.out.println("[DEBUG] Expanding bracket to [" + low + ", " + high + "]");
            }
        }

        if (fLow * fHigh <= 0) {
            // ─────────────────────────────────────────
            // Phase 2a: Bisection to coarse precision
            // ─────────────────────────────────────────
            System.out.println("[INFO] Bracket found: [" + low + ", " + high
                    + "] after " + expandCount + " expansions");
            System.out.println("[Phase 2a] Bracket valid — running bisection...");
            double coarseEpsilon = Math.max(1e-4, epsilon * 100);

            for (int i = 0; i < 60; i++) {
                double mid  = (low + high) / 2;
                double fMid = equation.evaluate(mid) - target;

                if (Double.isNaN(fMid) || Double.isInfinite(fMid)) {
                    System.out.println("[WARNING] Singularity at bisection midpoint.");
                    break;
                }

                if (Math.abs(fMid) < coarseEpsilon) {
                    x = mid;
                    System.out.println("[Phase 2a] Bisection converged early at iteration " + i);
                    break;
                }

                if (fLow * fMid < 0) {
                    high  = mid;
                    fHigh = fMid;
                } else {
                    low  = mid;
                    fLow = fMid;
                }
                x = mid;
            }

            System.out.println("[Phase 2a] Bisection complete. Handing off to Newton at x = " + x);

            // ─────────────────────────────────────────
            // Phase 2b: Newton refinement
            // ─────────────────────────────────────────
            System.out.println("[Phase 2b] Newton refinement...");

            for (int i = 0; i < 40; i++) {
                double val   = equation.evaluate(x);
                double error = target - val;

                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    System.out.println("[WARNING] Singularity during Newton refinement at x = " + x);
                    break;
                }

                if (Math.abs(error) < epsilon) {
                    System.out.println("[SUCCESS] Newton refined in " + i + " iterations.");
                    // Added: check for better/alternative root & domain
                    return checkAlternativeRoot(equation, target, x, epsilon, originalFunction);
                }

                double slope = equation.getDerivative(x);

                if (Math.abs(slope) < 1e-12) {
                    System.out.println("[INFO] Flat slope during refinement — bisection result is best answer.");
                    break;
                }

                x += error / slope;  // pure Newton, no clamping needed
            }

            // Added: even if Newton didn't fully converge, check alternative
            return checkAlternativeRoot(equation, target, x, epsilon, originalFunction);

        } else {
            // ─────────────────────────────────────────
            // Phase 2c: No bracket — adaptive Newton
            // ─────────────────────────────────────────
            System.out.println("[WARNING] No sign change after aggressive expansion - using best seed");

            // ──────── NEW: POLY FIX for small-target even powers ────────
            if (equation.getEngineWeight() == 2 && Math.abs(target) < 1e-5) {
                // signum(0) = 0, which would send Newton to x=0 (flat slope) — use 1.0 instead
                x = (target >= 0 ? 1.0 : -1.0) * 2.0;
                System.out.println("[POLY FIX] Forcing wider start x = " + x + " for small target");
            }

            int weight = equation.getEngineWeight();

            int stallThreshold = (weight >= 3) ? 12 : 5;
            System.out.println("[INFO] Engine weight: " + weight);

            for (int i = 0; i < maxIter; i++) {
                double currentVal = equation.evaluate(x);
                double error = target - currentVal;
                double slope = equation.getDerivative(x);

                if (Double.isInfinite(currentVal) || isNaN(currentVal)) {
                    // For domain-restricted functions (sqrt, ln), Newton may wander negative.
                    // Try nudging back toward positive territory before giving up.
                    String funcLower = originalFunction.toLowerCase();
                    boolean hasDomainRestriction = funcLower.contains("sqrt") || funcLower.contains("ln") || funcLower.contains("log");
                    if (hasDomainRestriction && x <= 0) {
                        x = Math.abs(x) + 0.5;
                        System.out.println("[DOMAIN NUDGE] Reflected x to " + x);
                        continue;
                    }
                    System.out.println("[WARNING] Function undefined at x = " + x + " (complex or singularity).");
                    System.out.println("[CONCLUSION] No real solution found — equation may have no real roots.");
                    return Double.NaN;
                }

                if (Math.abs(error) < epsilon) {
                    totalNewtonSteps++;
                    System.out.println("[SUCCESS] Converged in " + i + " iterations.");
                    // Added: check for better/alternative root & domain
                    return checkAlternativeRoot(equation, target, x, epsilon, originalFunction);
                }

                if (Math.abs(error) < bestError) {
                    bestError = Math.abs(error);
                    noImprovementCount = 0;
                } else {
                    noImprovementCount++;
                }
                if (noImprovementCount >= 80 && bestError > 0.1) {
                    System.out.println("[WARNING] No improvement detected - equation may have no real solution.");
                    return checkAlternativeRoot(equation, target, x, epsilon, originalFunction);
                }

                if (Math.abs(error) > prevError * 10 || Double.isInfinite(error) || isNaN(error)) {
                    System.out.println("[WARNING] Divergence detected - switching to Bisection");
                    double fbResult = bisectionFallback(equation, target,
                            x - Math.max(10.0, Math.abs(x) * 2.0),
                            x + Math.max(10.0, Math.abs(x) * 2.0), 60);
                    return checkAlternativeRoot(equation, target, fbResult, epsilon, originalFunction);
                }

                if (Math.abs(slope) < 1e-8) {
                    double nudge = Math.max(
                            Math.min(0.1, Math.abs(x) * 0.5 + 1e-7),
                            Math.abs(error) * 0.005
                    );
                    x += Math.signum(error) * nudge * (1 + Math.abs(x));
                } else {
                    double delta = error / slope;

                    if (Math.abs(error) > 0.1 && Math.abs(delta) < 0.05) {
                        System.out.println("[INFO] Kick-starting: flat zone detected.");
                        delta *= 5.0;
                    }

                    double maxStepFactor = (weight >= 3) ? 0.3 : (weight == 2 ? 0.4 : 0.6);

                    double additive = (weight >= 3)
                            ? Math.max(Math.min(1.0, Math.abs(x) * 0.3 + 1e-7), Math.abs(error) * 0.005)
                            : Math.max(Math.min(0.5, Math.abs(x) * 0.5 + 1e-7), Math.abs(error) * 0.01);

                    double maxJump = Math.abs(x) * maxStepFactor + additive;
                    delta = Math.max(-maxJump, Math.min(delta, maxJump));

                    if (i > 0 && Math.abs(error) > prevError) {
                        delta *= 0.5;
                    }

                    x += delta;
                }

                if (i > 0 && Math.abs(error) > Math.abs(prevError) * 0.95) {
                    stallCount++;
                    if (stallCount >= stallThreshold) {
                        System.out.println("[WARNING] Stall detected - switching to Bisection");
                        double fbResult = bisectionFallback(equation, target,
                                x - Math.max(5.0, Math.abs(x)),
                                x + Math.max(5.0, Math.abs(x)), 60);
                        return checkAlternativeRoot(equation, target, fbResult, epsilon, originalFunction);
                    }
                } else {
                    stallCount = 0;
                }

                prevError = Math.abs(error);
                if (i % 10 == 0 || Math.abs(error) < 0.01) {
                    System.out.printf("[Phase 2c] Iteration %d: x = %.6f | Error = %.6f\n", i, x, error);
                }
            }

            System.out.println("[WARNING] Max iterations reached.");
            System.out.println("[SUMMARY] Total Newton iterations (main loop): " + totalNewtonSteps);
            System.out.println("[SUMMARY] Best x found: " + x + " | f(x) = " + equation.evaluate(x));
            return checkAlternativeRoot(equation, target, x, epsilon, originalFunction);
        }
    }

    // ─────────────────────────────────────────
    // NEW HELPER: Check alternative root & enforce domain
    // ─────────────────────────────────────────
    private static double checkAlternativeRoot(MathNode equation, double target, double x,
                                               double epsilon, String originalFunction) {
        // Domain enforcement for ln, log, sqrt — solution must be positive
        String funcLower = originalFunction.toLowerCase();
        if ((funcLower.contains("ln") || funcLower.contains("log") || funcLower.contains("sqrt")) && x <= 0) {
            System.out.println("[DOMAIN] Solution in invalid region for ln/log/sqrt → returning NaN");
            return Double.NaN;
        }

        // Validate: if the solution doesn't actually satisfy f(x)=target, it's a false convergence
        double val = equation.evaluate(x);
        if (!Double.isNaN(x) && (Double.isNaN(val) || Math.abs(val - target) > Math.max(1e-4, Math.abs(target) * 1e-3 + 1e-4))) {
            System.out.println("[VALIDATE] Solution x=" + x + " gives f(x)=" + val + ", not close to target=" + target + " → NaN");
            return Double.NaN;
        }

        return x;
    }

    // ... rest of your class (bisectionFallback, isSafeToSolve, findRootBig, etc.) unchanged ...

    /**
     * Phase 3: Bisection Fallback (Safety Net)
     */
    private static double bisectionFallback(MathNode f, double target, double a, double b, int steps) {
        double fa = f.evaluate(a) - target;
        double fb = f.evaluate(b) - target;

        if (isNaN(fa) || isNaN(fb)) {
            System.out.println("[ERROR] NaN in bisection bounds. No real solution.");
            return Double.NaN;  // not (a + b) / 2
        }

        if (fa * fb > 0) {
            System.out.println("[ERROR] No sign change in fallback bracket. No real solution.");
            return Double.NaN;  // not (a + b) / 2
        }


        double bestX = (a + b) / 2;
        double bestErr = Double.MAX_VALUE;

        for (int i = 0; i < steps; i++) {
            double mid  = (a + b) / 2;
            double fMid = f.evaluate(mid) - target;

            if (isNaN(fMid) || Double.isInfinite(fMid)) {
                System.out.println("[WARNING] Singularity at bisection midpoint, shrinking step.");
                b = mid + (b - mid) * 0.5;
                continue;
            }

            if (Math.abs(fMid) < Math.abs(bestErr)) {
                bestErr = fMid;
                bestX   = mid;
            }

            if (Math.abs(fMid) < 1e-10) return mid;

            if (fa * fMid < 0) {
                b = mid;
                fb = fMid;
            } else {
                a = mid;
                fa = fMid;
            }
        }
        return bestX;
    }

    private static boolean isSafeToSolve(String function) {
        if (function == null || function.isBlank()) return false;

        String norm = function.replaceAll("\\s+", "");

        if (norm.matches(".*/\\(?0+\\.?0*\\)?([^0-9.].*|$)")) {
            System.out.println("[SAFETY] Division by zero detected.");
            return false;
        }

        if (norm.matches(".*[^0-9a-zA-Z().+\\-*/^].*")) {
            System.out.println("[SAFETY] Illegal character in expression.");
            return false;
        }

        int depth = 0;
        for (char c : norm.toCharArray()) {
            if (c == '(') depth++;
            else if (c == ')') depth--;
            if (depth < 0) {
                System.out.println("[SAFETY] Mismatched parentheses.");
                return false;
            }
        }
        if (depth != 0) {
            System.out.println("[SAFETY] Unclosed parentheses.");
            return false;
        }

        return true;
    }

    /**
     * Phase 4: The Big Engine (Arbitrary Precision Path)
     */
    public static BigDecimal findRootBig(MathNode equation, double targetValue, BigDecimal startX) {
        if (!(equation instanceof BigEvaluable)) {
            System.out.println("[CRITICAL] This equation contains nodes that do not support Big Mode.");
            System.out.println("[TIP] Use only +, -, *, /, ^, x, and constants for Big Mode.");
            return null;
        }

        System.out.println("[INFO] Big Engine active. Precision: 34 digits.");
        MathContext mc = new MathContext(34, RoundingMode.HALF_UP);
        BigDecimal target = BigDecimal.valueOf(targetValue);
        BigDecimal x = startX;

        BigEvaluable bigEq = (BigEvaluable) equation;
        System.out.println("[FORCE FRESH DERIV CHECK] Before big loop starts | x = " + x.toPlainString() +
                " | fresh slope = " + bigEq.getDerivativeBig(x).toPlainString());

        // ← ADD THESE TWO LINES
        BigDecimal prevBigError = new BigDecimal("1E+999");
        int noImproveBig = 0;

        for (int i = 0; i < 40; i++) {
            try {
                BigDecimal currentVal = bigEq.evaluateBig(x);
                BigDecimal error = target.subtract(currentVal);

                if (error.abs().compareTo(new BigDecimal("1E-22")) < 0) {
                    System.out.println("[SUCCESS] Big Engine converged in " + i + " iterations.");
                    return x;
                }

                // Replace the stall check block with this:
                BigDecimal absError = error.abs();
                BigDecimal threshold = prevBigError.multiply(new BigDecimal("0.99"), mc);
                if (absError.compareTo(threshold) < 0) {   // ← threshold here, NOT prevBigError
                    prevBigError = absError;
                    noImproveBig = 0;
                } else {
                    noImproveBig++;
                }
                if (i > 3 && noImproveBig >= 8) {
                    System.out.println("[WARNING] Big Engine stalled — equation may have no real solution.");
                    return null;
                }
                prevBigError = error.abs();

                BigDecimal slope = bigEq.getDerivativeBig(x);

                System.out.printf("[CHECK] iter %2d | x = %.12f | expected_slope ≈ %.3f | actual_slope = %.12f | f(x) = %.12f%n",
                        i, x.doubleValue(),
                        2.0 * x.doubleValue(),
                        slope.doubleValue(),
                        currentVal.doubleValue());
                System.out.printf("[DERIV] iter %2d | x = %20s | slope = %20s | f(x) = %20s | target = %s | error = %s%n",
                        i,
                        x.toPlainString(),
                        slope.toPlainString(),
                        currentVal.toPlainString(),
                        target.toPlainString(),
                        error.toPlainString());

                if (slope.abs().compareTo(new BigDecimal("1E-18")) <= 0) {
                    System.out.println("[WARNING] Slope near zero at iter " + i);
                    BigDecimal safeStep = error.signum() >= 0 ? new BigDecimal("0.01") : new BigDecimal("-0.01");
                    x = x.add(safeStep);
                    continue;
                }

                BigDecimal delta = error.divide(slope, mc);

                BigDecimal maxAbsDelta = new BigDecimal("100");
                delta = delta.min(maxAbsDelta).max(maxAbsDelta.negate());

                x = x.add(delta);

                System.out.printf("Big Iteration %d: x = %s\n", i, x.toPlainString());

            } catch (ArithmeticException | NumberFormatException e) {
                System.out.println("[ERROR] Math error in Big Engine: " + e.getMessage());
                return null;
            }
        }

        // ==================== SAFETY CHECK FOR NO-REAL-ROOT CASES ====================
        BigDecimal finalF = bigEq.evaluateBig(x);

        if (finalF.abs().compareTo(new BigDecimal("1E-20")) > 0) {
            // Quick test: does the function ever cross zero?
            BigDecimal fLeft  = bigEq.evaluateBig(new BigDecimal("-1000"));
            BigDecimal fRight = bigEq.evaluateBig(new BigDecimal("1000"));

            if (fLeft.signum() == fRight.signum() && fLeft.signum() != 0) {
                System.out.println("[BIG] No real root detected (function never crosses zero)");
                return null;                     // ← this makes it match Classic "NaN"
            }
        }

        System.out.println("[WARNING] Did not converge within 40 iterations.");
        return x;
    }
}