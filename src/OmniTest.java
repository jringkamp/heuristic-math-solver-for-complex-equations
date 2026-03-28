import java.util.Scanner;
import java.math.BigDecimal;
import java.math.MathContext;

public class OmniTest {
    private static final MathContext MC = new MathContext(34);

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("   OMNISOLVER INTERACTIVE v2.1          ");
        System.out.println("   Smart automatic seeds — no guess needed! ");
        System.out.println("========================================");

        while (true) {
            System.out.println("\n[1] Classic (double precision, fast)");
            System.out.println("[2] Big (34-digit arbitrary precision)");
            System.out.println("[exit] to quit");
            System.out.print("Mode choice: ");
            String mode = sc.nextLine().trim().toLowerCase();

            if (mode.equals("exit")) break;
            if (!mode.equals("1") && !mode.equals("2")) {
                System.out.println("Invalid. Use 1, 2, or exit.");
                continue;
            }

            System.out.print("Enter equation (e.g. x^x + sin(x) - 3): ");
            String equationStr = sc.nextLine().trim();

            System.out.print("Target value f(x) = ");
            double target = readDouble(sc, "");

            // Parse equation once
            MathNode equation;
            try {
                equation = ExpressionParser.parse(equationStr);
            } catch (Exception e) {
                System.out.println("Parse error: " + e.getMessage());
                continue;
            }

            // ────────────────────────────────
            // Compute smart seeds automatically
            // ────────────────────────────────
            double initialGuessForSeed = 1.0;  // fallback — almost never used
            double[] seeds = OmniSolver.improveSeed(equation, target, initialGuessForSeed);

            double fastSeed = seeds[0];
            double slowSeed = seeds[1];

            // Show what we found (educational + debug)
            System.out.println("\n[SMART SEEDS]");
            System.out.printf("  Fast seed (dominant term): %.12f   error: %g%n",
                    fastSeed, Math.abs(equation.evaluate(fastSeed) - target));
            System.out.printf("  Slow seed (weaker term)   : %.12f   error: %g%n",
                    slowSeed, Math.abs(equation.evaluate(slowSeed) - target));

            double chosenSeed;
            double errFast = Math.abs(equation.evaluate(fastSeed) - target);
            double errSlow = Math.abs(equation.evaluate(slowSeed) - target);
            chosenSeed = (errFast < errSlow || Double.isNaN(errSlow)) ? fastSeed : slowSeed;

            System.out.printf("[AUTO] Using better seed: %.12f%n", chosenSeed);

            if (mode.equals("1")) {
                // Classic mode
                System.out.println("\n--- Classic Solver (double) ---");
                new OmniSolver().startSolver(equationStr, target, chosenSeed);
            } else {
                // Big mode
                System.out.println("\n--- Big Precision Solver (34 digits) ---");
                BigDecimal bigSeed = BigDecimal.valueOf(chosenSeed);

                BigDecimal root = OmniSolver.findRootBig(equation, target, bigSeed);

                if (root != null) {
                    System.out.println("\nSOLUTION (34 digits): " + root.toPlainString());
                    BigDecimal fAtRoot = ((BigEvaluable) equation).evaluateBig(root);
                    System.out.printf("f(root) ≈ %s   (error ≈ %g)%n",
                            fAtRoot.toPlainString(),
                            Math.abs(fAtRoot.doubleValue() - target));
                } else {
                    System.out.println("\nNo real solution found or computation unsupported.");
                    // Only ask for override on failure (rare)
                    System.out.print("Try a custom starting guess? (y/n): ");
                    if (sc.nextLine().trim().toLowerCase().startsWith("y")) {
                        System.out.print("Enter custom seed: ");
                        bigSeed = readBigDecimal(sc, "");
                        root = OmniSolver.findRootBig(equation, target, bigSeed);
                        if (root != null) {
                            System.out.println("Custom seed result: " + root.toPlainString());
                        } else {
                            System.out.println("Still no convergence.");
                        }
                    }
                }
            }
        }

        sc.close();
        System.out.println("Goodbye!");
    }

    // ─────────────────────────────────────────
    // Helpers (unchanged)
    // ─────────────────────────────────────────

    private static double readDouble(Scanner sc, String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) System.exit(0);
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private static BigDecimal readBigDecimal(Scanner sc, String prompt) {
        while (true) {
            if (!prompt.isEmpty()) System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) System.exit(0);
            try {
                return new BigDecimal(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid decimal.");
            }
        }
    }
}