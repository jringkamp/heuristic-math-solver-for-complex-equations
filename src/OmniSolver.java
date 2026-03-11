class OmniSolver {

    /**
     * Phase 1: Seed Improvement (The "Stripper")
     * Performs 5 quick, aggressive iterations to get into the neighborhood of the root.
     */
    private static double improveSeed(MathNode f, double target, double guess) {
        double x = guess;
        System.out.println("[INFO] Phase 1: Improving Seed...");
        for (int i = 0; i < 5; i++) {
            double val = f.evaluate(x);
            double error = target - val;

            // SAFETY: If we already hit NaN or Infinity, stop and let Phase 2/3 handle it
            if (Double.isNaN(val) || Double.isInfinite(val)) return x / 2.0;

            double slope = f.getDerivative(x);

            if (Math.abs(slope) > 1e-8) {
                double delta = error / slope;

                // APPLY CLAMPING HERE TOO:
                // Don't let the seed jump more than 20% of the current x
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
     * Uses Derivative-based jumps with adaptive clamping based on Engine Weights.
     */
    public static double findRoot(MathNode equation, double target, double startX) {
        double x = startX;
        double epsilon = (Math.abs(target) > 1e-8) ? 1e-10 : Math.abs(target) * 1e-4;
        int maxIter = 100;
        double prevError = Double.POSITIVE_INFINITY;
        int stallCount = 0;
        double bestError = Double.POSITIVE_INFINITY;
        int noImprovementCount = 0;

        // Run Phase 1
        x = improveSeed(equation, target, x);

        System.out.println("\n[INFO] Phase 2: Starting High-Precision Loop...");
        for (int i = 0; i < maxIter; i++) {
            double currentVal = equation.evaluate(x);
            double error = target - currentVal;

            // Success Check

                if (Math.abs(error) < epsilon) {
                    System.out.println("[SUCCESS] Converged in " + i + " iterations.");
                    return x;
                }

            // Add this INSIDE the loop, right after the success check
            if (i > 0 && Math.abs(error) > Math.abs(prevError) * 0.9 && equation.getEngineWeight() < 3) {
                stallCount++;
                if (stallCount >= 5) {
                    System.out.println("[WARNING] Stall detected - switching to Bisection Fallback");
                    return bisectionFallback(equation, target, x * 0.1, x * 2.0, 60);
                }
            } else {
                stallCount = 0;

            }
            // Add INSIDE the loop, right after the stall check
            if (Math.abs(error) < bestError) {
                bestError = Math.abs(error);
                noImprovementCount = 0;
            } else {
                noImprovementCount++;
            }
            if (noImprovementCount >= 15 && bestError > 0.1) {
                System.out.println("[WARNING] No improvement detected - equation may have no real solution.");
                return x;
            }

            // Safety: Detect if we are flying off into space (Divergence)
            if (Math.abs(error) > prevError * 10 || Double.isInfinite(error) || Double.isNaN(error)) {
                System.out.println("[WARNING] Divergence detected - switching to Bisection Fallback");
                return bisectionFallback(equation, target, x * 0.8, x *1.2, 40);
            }

            double slope = equation.getDerivative(x);

            // Handle flat spots
            if (Math.abs(slope) < 1e-8) {
                double nudge = (equation.getEngineWeight() < 3)
                        ? Math.max(Math.min(0.05, Math.abs(x) * 0.5 + 1e-7), Math.abs(error) * 0.001)
                        : Math.min(0.05, Math.abs(x) * 0.5 + 1e-7);
                x += Math.signum(error) * nudge;
            } else {
                double delta = error / slope;

                if (Math.abs(error) > 0.1 && Math.abs(delta) < 0.05 && equation.getEngineWeight() < 3) {
                    delta *= 5.0;
                }

                // CONCEPT 2: Weight-based Clamping
                int weight = equation.getEngineWeight();
                double maxStepFactor;

                if (weight >= 3) { // Exponential / Tower
                    maxStepFactor = 0.05;
                } else if (weight == 2) { // Polynomial
                    maxStepFactor = 0.2;
                } else { // Linear
                    maxStepFactor = 2.0;
                }

                // Apply the physical jump limit
                double additive = (equation.getEngineWeight() < 3)
                        ? Math.max(Math.min(0.1, Math.abs(x) * 0.5 + 1e-7), Math.abs(error) * 0.001)
                        : Math.min(0.1, Math.abs(x) * 0.5 + 1e-7);
                double maxJump = (weight == 1) ? 1.0 : Math.abs(x) * maxStepFactor + additive;
                delta = Math.max(-maxJump, Math.min(delta, maxJump));

                // If error increased, dampen the move
                if (i > 0 && Math.abs(error) > prevError) {
                    delta *= 0.5;
                }

                x += delta;
            }

            prevError = Math.abs(error);
            System.out.printf("Iteration %d: x = %.6f | Val = %.4f | Error = %.4f (Weight: %d)\n",
                    i, x, currentVal, error, equation.getEngineWeight());
        }

        System.out.println("[WARNING] Max iterations reached without perfect convergence.");
        return x;
    }

    /**
     * Phase 3: Bisection Fallback (The "Safety Net")
     * Slowly but surely squeezes the answer if the main loop fails.
     */
    private static double bisectionFallback(MathNode f, double target, double a, double b, int steps) {
        double fa = f.evaluate(a) - target;
        double fb = f.evaluate(b) - target;

        // Ensure we have a sign change; if not, expand the search
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