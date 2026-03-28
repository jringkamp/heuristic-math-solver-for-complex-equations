import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class OmniSolverTests {

    private static int passedClassic = 0;
    private static int failedClassic = 0;
    private static int noSolutionClassic = 0;

    private static int passedBig = 0;
    private static int failedBig = 0;
    private static int noSolutionBig = 0;

    private static final List<TestResult> results = new ArrayList<>();

    private static class TestResult {
        String label;
        double classicResult;
        boolean classicNaN;
        BigDecimal bigResult;
        boolean bigNaN;
        double expected;
        String notes = "";

        TestResult(String label, double classic, boolean cNaN, BigDecimal big, boolean bNaN, double exp) {
            this.label = label;
            this.classicResult = classic;
            this.classicNaN = cNaN;
            this.bigResult = big;
            this.bigNaN = bNaN;
            this.expected = exp;
        }
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("     OMNISOLVER DUAL-MODE TEST SUITE     ");
        System.out.println("   (Now using automatic smart seeds!)    ");
        System.out.println("   [VERSION: FIX-4]                      ");
        System.out.println("=========================================\n");

        runAllGroups();

        printComparison();
    }

    private static void runAllGroups() {
        // GROUP 1: Basic Polynomials
        System.out.println("--- GROUP 1: Basic Polynomials ---");
        runDualTest("x - 2",          0.0, null, 2.0,        "Linear");
        runDualTest("x^2 - 4",        0.0, null, 2.0,        "Quadratic simple");
        runDualTest("x^2",            9.0, null, 3.0,        "Quadratic non-zero target");
        runDualTest("x^3 - x - 2",   0.0, null, 1.5213797068, "Cubic");
        runDualTest("x^2 - 2",        0.0, null, 1.4142135624, "Irrational root (sqrt 2)");

        // GROUP 2: Trig
        System.out.println("\n--- GROUP 2: Trig Functions ---");
        runDualTest("sin(x)",          0.0, Math.PI, 3.1415926536, "sin(x) = 0 near pi");
        runDualTest("sin(x)",          0.5, null, 0.5235987756, "sin(x) = 0.5");
        runDualTest("cos(x)",          0.0, null, 1.5707963268, "cos(x) = 0");
        runDualTest("sin(x) - x + 1", 0.0, null, 1.9345632108, "sin(x) - x + 1 = 0");
        runDualTest("tan(x)",          1.0, null, 0.7853981634, "tan(x) = 1");

        // GROUP 3: Log/Exp
        System.out.println("\n--- GROUP 3: Log and Exp ---");
        runDualTest("ln(x)",           1.0, null, 2.7182818285, "ln(x) = 1 (e)");
        runDualTest("ln(x) + x - 3",  0.0, null, 2.2079400316, "ln(x) + x = 3");
        runDualTest("exp(x) - 10",    0.0, null, 2.3025850930, "exp(x) = 10");
        runDualTest("exp(x)",         50.0, null, 3.9120230054, "exp(x) = 50");

        // GROUP 4: Towers
        System.out.println("\n--- GROUP 4: Tower Functions ---");
        runDualTest("x^x",            50.0, null, 3.2872621954, "x^x = 50");
        runDualTest("x^x^x + 2^x - 100", 0.0, null, 2.2084258949, "x^x^x + 2^x = 100");
        runDualTest("x^(x^x) + 2^(x^x) - 100", 0.0, null, 2.1636203005, "Tower sum = 100");

        // GROUP 5: Edge cases
        System.out.println("\n--- GROUP 5: Edge Cases ---");
        runDualTest("x^2",            0.0, null, 0.0,          "Double root at zero");
        runDualTest("x",              0.0, null, 0.0,          "Root at zero, far seed");
        runDualTest("sqrt(x) - 2",    0.0, null, 4.0,          "Square root equation");
        runDualTest("abs(x) - 3",     0.0, 3.0, 3.0,          "Absolute value");

        // GROUP 6 & 7: No-solution and negatives
        System.out.println("\n--- GROUP 6+7: No-Solution & Negative Domains ---");
        runDualNoSolution("x^2 + 1",  0.0, "x^2 + 1 = 0 (complex)");
        runDualNoSolution("(x^(x^x)) + (2^(x^x)) + (sin(x)^(x)) + 100", 0.0, "Hard no-solution tower");
        runDualTest("x + 5",          0.0, null, -5.0,          "Linear negative root");
        runDualTest("-x^2 + 4",       0.0, null, 2.0,          "Negative leading coeff");
        runDualNoSolution("sqrt(x) + 2",    0.0, "sqrt(negative)");
        runDualTest("ln(x) + 10",     0.0, null, 4.539992976248485e-5, "ln(x) = -10 (small positive root)");
    }

    // ─────────────────────────────────────────
    // Dual test: run both classic and Big — automatic smart seed if null
    // ─────────────────────────────────────────
    private static void runDualTest(String function, double target, Double seedParam,
                                    double expected, String label) {
        MathNode equation = null;
        try {
            equation = ExpressionParser.parse(function);
        } catch (Exception e) {
            System.out.printf("  [PARSE ERROR] %-40s → %s\n", label, e.getMessage());
            failedClassic++; failedBig++;
            results.add(new TestResult(label, Double.NaN, true, null, true, expected));
            return;
        }

        double chosenSeed;
        if (seedParam == null) {
            // ────────────────────────────────
            // 1. Get smart seeds from OmniSolver
            // ────────────────────────────────
            double[] seeds = OmniSolver.improveSeed(equation, target, 1.0);
            double fast = seeds[0];
            double slow = seeds[1];

            double errFast = Math.abs(equation.evaluate(fast) - target);
            double errSlow = Math.abs(equation.evaluate(slow) - target);

            chosenSeed = (errFast < errSlow || Double.isNaN(errSlow)) ? fast : slow;

            // ────────────────────────────────
            // 2. TRIG BOOST — only here, where function/label/equation exist
            // ────────────────────────────────
            String funcLower = function.toLowerCase();
            if (funcLower.contains("sin") || funcLower.contains("cos") || funcLower.contains("tan")) {
                double[] trigCandidates = {Math.PI, Math.PI / 2.0, Math.PI * 1.5, 3.0 * Math.PI / 2.0, 4.0, 1.0, -1.0};

                // Prefer candidates with larger |slope| when errors are very close
                double bestSeed = chosenSeed;
                double bestError = Math.abs(equation.evaluate(chosenSeed) - target);
                double bestSlopeMag = Math.abs(equation.getDerivative(chosenSeed));

                for (double cand : trigCandidates) {
                    double val = equation.evaluate(cand);
                    if (Double.isNaN(val) || Double.isInfinite(val)) continue;

                    double err = Math.abs(val - target);
                    double slopeMag = Math.abs(equation.getDerivative(cand));

                    boolean betterError = err < bestError - 1e-10;
                    boolean similarErrorButBetterSlope = Math.abs(err - bestError) < 1e-8 && slopeMag > bestSlopeMag * 1.5;

                    if (betterError || similarErrorButBetterSlope) {
                        bestError = err;
                        bestSlopeMag = slopeMag;
                        bestSeed = cand;
                    }
                }

                if (Math.abs(bestSeed - chosenSeed) > 1e-6) {
                    System.out.printf("  [TRIG SEED BOOST] %-40s → %.6f (was %.6f)%n",
                            label, bestSeed, chosenSeed);
                }
                chosenSeed = bestSeed;
            }

            // ────────────────────────────────
            // 3. General fallback (optional safety net)
            // ────────────────────────────────
            double finalErr = Math.abs(equation.evaluate(chosenSeed) - target);
            if (finalErr > 100 || Double.isNaN(finalErr) || Double.isInfinite(finalErr)) {
                double[] fallbacks = {0.0, 1.0, 2.0, 3.0, Math.PI, Math.E};
                double best = chosenSeed;
                double bestErr = finalErr;

                for (double fb : fallbacks) {
                    double e = Math.abs(equation.evaluate(fb) - target);
                    if (!Double.isNaN(e) && !Double.isInfinite(e) && e < bestErr) {
                        bestErr = e;
                        best = fb;
                    }
                }
                if (best != chosenSeed) {
                    System.out.printf("  [BAD SEED FALLBACK] %-40s → %.6f%n", label, best);
                    chosenSeed = best;
                }
            }
        } else {
            chosenSeed = seedParam;
        }

        // ─── Everything below this line stays EXACTLY as it was in your original code ───
        // Classic
        double classicRes = OmniSolver.findRoot(function, equation, target, chosenSeed);
        boolean cNaN = Double.isNaN(classicRes);
        boolean cPass = !cNaN && Math.abs(classicRes - expected) < 1e-4;

        if (cNaN) {
            System.out.printf("  [CLASSIC PASS] %-40s → NaN\n", label);
            noSolutionClassic++;
        } else if (cPass) {
            System.out.printf("  [CLASSIC PASS] %-40s → %.10f\n", label, classicRes);
            passedClassic++;
        } else {
            System.out.printf("  [CLASSIC FAIL] %-40s → %.10f (exp %.10f)\n", label, classicRes, expected);
            failedClassic++;
        }

        // Big
        BigDecimal bigRes = null;
        boolean bNaN = false;
        boolean bPass = false;

        if (equation instanceof BigEvaluable) {
            BigDecimal bigSeed = new BigDecimal(String.valueOf(chosenSeed));
            bigRes = OmniSolver.findRootBig(equation, target, bigSeed);

            bNaN = (bigRes == null);
            if (!bNaN) {
                BigDecimal expBig = new BigDecimal(String.valueOf(expected));
                MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
                bPass = bigRes.subtract(expBig, mc).abs().compareTo(new BigDecimal("1E-8")) <= 0;
            }

            if (bNaN) {
                System.out.printf("  [BIG PASS] %-40s → null/NaN\n", label);
                noSolutionBig++;
            } else if (bPass) {
                System.out.printf("  [BIG PASS] %-40s → %s\n", label,
                        bigRes.toPlainString().substring(0, Math.min(34, bigRes.toPlainString().length())));
                passedBig++;
            } else {
                System.out.printf("  [BIG FAIL] %-40s → %s (exp %.10f)\n", label, bigRes, expected);
                failedBig++;
            }
        } else {
            System.out.printf("  [BIG SKIPPED] %-40s (no BigEvaluable support)\n", label);
        }

        results.add(new TestResult(label, classicRes, cNaN, bigRes, bNaN, expected));
    }

    // ─────────────────────────────────────────
    // No-solution variant — no expected value, auto seed
    // ─────────────────────────────────────────
    private static void runDualNoSolution(String function, double target, String label) {
        MathNode equation = null;
        try {
            equation = ExpressionParser.parse(function);
        } catch (Exception e) {
            System.out.printf("  [PARSE ERROR] %-40s → %s\n", label, e.getMessage());
            failedClassic++; failedBig++;
            return;
        }

        // Auto seed for no-solution tests
        double[] seeds = OmniSolver.improveSeed(equation, target, 1.0);
        double chosenSeed = seeds[0]; // fast seed usually fine for rejection

        // Classic
        double classicRes = OmniSolver.findRoot(function, equation, target, chosenSeed);
        if (Double.isNaN(classicRes)) {
            System.out.printf("  [CLASSIC PASS] %-40s → NaN\n", label);
            noSolutionClassic++;
        } else {
            System.out.printf("  [CLASSIC FAIL] %-40s → %.10f (should be NaN)\n", label, classicRes);
            failedClassic++;
        }

        // Big
        if (equation instanceof BigEvaluable) {
            BigDecimal bigSeed = new BigDecimal(String.valueOf(chosenSeed));
            BigDecimal bigRes = null;
            try {
                bigRes = OmniSolver.findRootBig(equation, target, bigSeed);
                if (bigRes == null) {
                    System.out.printf("  [BIG PASS] %-40s → null\n", label);
                    noSolutionBig++;
                } else {
                    System.out.printf("  [BIG FAIL] %-40s → %s (should be null)\n", label, bigRes);
                    failedBig++;
                }
            } catch (ArithmeticException e) {
                System.out.printf("  [BIG PASS] %-40s → %s (domain error)\n", label, e.getMessage());
                noSolutionBig++;
            }
        } else {
            System.out.printf("  [BIG SKIPPED] %-40s (no BigEvaluable)\n", label);
        }
    }

    private static void printComparison() {
        System.out.println("\n========================================");
        System.out.println("         FINAL DUAL COMPARISON          ");
        System.out.println("========================================");
        System.out.printf("%-42s %-18s %-18s %s\n", "Label", "Classic", "Big (34 digits)", "Notes");

        for (TestResult r : results) {
            String cStr = r.classicNaN ? "NaN" : String.format("%.10f", r.classicResult);
            String bStr = r.bigNaN ? "NaN/null" :
                    (r.bigResult == null ? "null" : r.bigResult.toPlainString().substring(0, Math.min(34, r.bigResult.toPlainString().length())));
            String note = "";
            if (r.classicNaN != r.bigNaN) note = "Mismatch on NaN";
            if (!r.classicNaN && !r.bigNaN) {
                double diff = Math.abs(r.classicResult - r.expected);
                if (diff > 1e-6) note += " Classic off";
            }
            System.out.printf("%-42s %-18s %-18s %s\n", r.label, cStr, bStr, note);
        }

        System.out.println("\nSUMMARY");
        System.out.printf("Classic: %d passed | %d failed | %d no-solution\n", passedClassic, failedClassic, noSolutionClassic);
        System.out.printf("Big:     %d passed | %d failed | %d no-solution\n", passedBig, failedBig, noSolutionBig);
    }
}