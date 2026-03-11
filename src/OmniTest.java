import java.util.*;
import java.util.Scanner;
public class OmniTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n========================================");
            System.out.print("Enter function (or 'exit'): ");
            String input = sc.nextLine();

            if (input.equalsIgnoreCase("exit")) break;

            try {
                MathNode equation = ExpressionParser.parse(input);

                System.out.print("Enter guess (seed): ");
                double seed = Double.parseDouble(sc.nextLine());

                System.out.println("--- Solving: " + input + " ---");
                double root = OmniSolver.findRoot(input, equation, 0.0, seed);

                System.out.printf("\nFINAL RESULT: %.10f\n", root);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Check your syntax! (e.g., use 'x^2' or 'sin(x)')");
            }
        }
        sc.close();
    }
}