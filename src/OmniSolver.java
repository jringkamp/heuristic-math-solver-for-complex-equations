class OmniSolver {

    /**
     * Phase 1: Seed Improvement (The "Stripper")
     */
    private static double improveSeed(MathNode f, double target, double guess) {
        double x = guess;
        System.out.println("[INFO] Phase 1: Improving Seed...");
        for (int i = 0; i < 5; i++) {
            double val = f.evaluate(x);
            double error = target - val;

            if (Double.isNaN(val) || Double.isInfinite(val)) return x / 2.0;

            double slope = f.getDerivative(x);

            if (Math.abs(slope) > 1e-8) {
                double delta = error / slope;
                double maxJump = Math.abs(x) * 0.2;
                delta = Math.max(-maxJump, Math.min(delta, maxJump));
                x += delta;
            } else {
                x += Math.signum(error) * 0.1;
            }
        }
        System.out.println("[INFO] Seed improved to: " + x);
        return x;
    }

    /**
     * Phase 2: The Main Solver Loop
     */
    public static double findRoot(MathNode equation, double target, double startX) {
        double epsilon = 1e-10;
        int maxIter = 100;
        double prevError = Double.POSITIVE_INFINITY;

        System.out.println("[INFO] Starting OmniSolver...");

        // ───────────────────────────────────────────────
        // Phase 1: Dual-Factoring Seed Improvement
        // ───────────────────────────────────────────────
        System.out.println("[Phase 1] Computing forward seed (fast growth dominates)...");
        double forwardSeed = improveSeed(equation, target, startX);

        System.out.println("[Phase 1] Computing reverse seed (slow/linear dominates)...");
        double reverseSeed = target;  // Naive reverse guess: linear wants x = target
        // Refine reverse seed a bit (mini-Newton on reverse view)
        for (int j = 0; j < 3; j++) {  // short refinement
            double val = equation.evaluate(reverseSeed);
            double err = target - val;
            double sl = equation.getDerivative(reverseSeed);
            if (Math.abs(sl) > 1e-8) {
                double d = err / sl;
                reverseSeed += d;
            } else {
                reverseSeed += Math.signum(err) * 0.1;
            }
        }

        // Evaluate both seeds
        double valForward = equation.evaluate(forwardSeed);
        double errForward = Math.abs(target - valForward);

        double valReverse = equation.evaluate(reverseSeed);
        double errReverse = Math.abs(target - valReverse);

        // Choose the better seed (or blend if close)
        double x;
        if (errForward < errReverse * 0.5) {
            x = forwardSeed;
            System.out.println("[Phase 1] Forward seed chosen (better fit)");
        } else if (errReverse < errForward * 0.5) {
            x = reverseSeed;
            System.out.println("[Phase 1] Reverse seed chosen (better fit)");
        } else {
            // Blend if both reasonable
            x = (forwardSeed + reverseSeed) / 2;
            System.out.println("[Phase 1] Blended seeds (comparable fit)");
        }

        System.out.println("[Phase 1] Starting x = " + x);

        // ───────────────────────────────────────────────
        // Phase 2: Main Newton with adaptive clamping
        // ───────────────────────────────────────────────
        System.out.println("[Phase 2] Starting High-Precision Loop...");
        for (int i = 0; i < maxIter; i++) {
            double currentVal = equation.evaluate(x);
            double error = target - currentVal;

            if (Math.abs(error) < epsilon) {
                System.out.println("[SUCCESS] Converged in " + i + " iterations.");
                return x;
            }

            // Divergence / stall checks (your existing code)
            if (Math.abs(error) > prevError * 10 || Double.isInfinite(error) || Double.isNaN(error)) {
                System.out.println("[WARNING] Divergence detected - switching to Bisection");
                return bisectionFallback(equation, target, x * 0.8, x * 1.2, 40);
            }

            double slope = equation.getDerivative(x);

            if (Math.abs(slope) < 1e-8) {
                x += Math.signum(error) * 0.01 * (1 + Math.abs(x));
            } else {
                double delta = error / slope;

                int weight = equation.getEngineWeight();
                System.out.println("Engine weight detected: " + weight);

                double maxStepFactor = (weight >= 3) ? 0.05 : (weight == 2 ? 0.2 : 0.5);
                double maxJump = Math.abs(x) * maxStepFactor + 0.1;
                delta = Math.max(-maxJump, Math.min(delta, maxJump));

                if (i > 0 && Math.abs(error) > prevError) {
                    delta *= 0.5;
                }

                x += delta;
            }

            prevError = Math.abs(error);

            System.out.printf("Iteration %d: x = %.6f | Val = %.4f | Error = %.4f (Weight: %d)\n",
                    i, x, currentVal, error, equation.getEngineWeight());
        }

        System.out.println("[WARNING] Max iterations reached.");
        // At end of findRoot(), before return x
        double expoApprox = Math.pow(2, -Math.pow(x, x)); // rough for 2^{-x^x}
        if (Math.abs(expoApprox) < 1e-8) {
            System.out.println("[INFO] Negative tower is dead (<1e-8) - answer is essentially x = " + x);
        }
        return x;
    }

    /**
     * Phase 3: Bisection Fallback (Safety Net)
     */
    private static double bisectionFallback(MathNode f, double target, double a, double b, int steps) {
        double fa = f.evaluate(a) - target;
        double fb = f.evaluate(b) - target;

        if (fa * fb > 0) {
            System.out.println("[ERROR] No sign change in fallback bracket. Returning best guess.");
            return (a + b) / 2;
        }

        for (int i = 0; i < steps; i++) {
            double mid = (a + b) / 2;
            double fMid = f.evaluate(mid) - target;

            if (Math.abs(fMid) < 1e-10) return mid;

            if (fa * fMid < 0) {
                b = mid;
                fb = fMid;
            } else {
                a = mid;
                fa = fMid;
            }
        }
        return (a + b) / 2;
    }
}