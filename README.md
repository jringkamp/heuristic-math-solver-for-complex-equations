Prototype working solver. More will be added to it. Will be adding mid point iteration reduction to lessen the amounts of iterations requires for optimization. Will add more class systems
to fine tune the solver to handle approaches it current struggles in. Using a 2 factor approach of logic stripping to view growth equations. Will be adding BigInt later to class system and solver

Project Overview

This project is a specialized Java-based numerical engine designed to solve transcendental equations where variables exist in both the base and the exponent (e.g., x^x+x=k). Because these equations often lack a standard algebraic solution, this solver utilizes a custom heuristic approach to achieve high-precision results where traditional iterative methods may struggle with convergence.

Logic: Factoring & Stripping

Unlike standard solvers that rely solely on brute-force iteration or Newton’s Method (which requires a near-perfect initial guess), this engine uses a Growth-Rate Heuristic:

    Variable Stripping: The algorithm identifies the dominant term (such as xx) and "strips" the equation to analyze the primary growth rate.

    Search-Space Pruning: By factoring the equation based on its transcendental properties, the solver narrows the bounds of the solution before beginning the refinement process.

    Hybrid Convergence: The engine utilizes a combination of bisection and high-precision refinement to ensure stability, even as x scales rapidly.

Technical Specifications

    Language: Java

    Heuristic Lambert W Solver

    Original Logic: Engineered a unique heuristic approach to solve transcendental equations of the form xx+x=k. Rather than relying on standard iterative libraries, I developed a "stripping" method that isolates variables based on their dominant growth rates.

    Human-AI Collaboration: Leveraged Large Language Models (LLMs) to bridge the gap between conceptual mathematical theory and Java implementation.

        The Division of Labor: I provided the high-level logic and "growth-rate factoring" strategy; the AI assisted in generating the boilerplate multi-threading code and managing the high-precision Big-Decimal math requirements.

    Technical Result: Successfully implemented a 34-digit precision solver that bypasses common convergence failures found in traditional Newton-Raphson applications for exponential functions.

Sample results: I have not added the big version yet to github, but the github version runs these same results. Big version requires more troubling shooting in the code to connect the classes properly. 

"C:\Program Files\Java\jdk-24\bin\java.exe" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2\lib\idea_rt.jar=60207" -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath C:\Users\J\IdeaProjects\MathPower\out\production\MathPower OmniTest

========================================
SELECT MODE: [1] Classic (Hardware Fast) | [2] Big (Arbitrary Precision)
Choice: 1
Enter function: x^x^x + x - 100
Enter guess (seed): 2
--- Solving: x^x^x + x - 100 ---
[INFO] Starting OmniSolver...
[Phase 1] Computing forward seed (fast growth dominates)...
[DEBUG] Testing seed: 2.0
[DEBUG] Initial Value: -82.0
[INFO] Phase 1: Improving Seed...
[INFO] Seed improved to: 2.2168991921486314
[Phase 1] Computing reverse seed (slow/linear dominates)...
[Phase 1] Reverse seed chosen (better fit)
[Phase 1] Starting x = 2.21073678406093
[Phase 2] Starting High-Precision Loop...
Engine weight detected: 4
Iteration 0: x = 2.210737 | Val = 0.0000 | Error = -0.0000 (Weight: 4)
[SUCCESS] Converged in 1 iterations.

FINAL RESULT: 2.2107367840

========================================
SELECT MODE: [1] Classic (Hardware Fast) | [2] Big (Arbitrary Precision)
Choice: 
