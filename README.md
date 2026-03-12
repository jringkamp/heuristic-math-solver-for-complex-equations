OmniSolver: Heuristic Numerical Engine (v1.0-Stable)

Project Overview
This solver approaches numerical solving differently from established methods like Lambert W. This is a specialized Java-based numerical engine designed to solve transcendental equations where variables exist in both the base and the exponent (e.g., x^x+x=k and x^x^x+x=k). Because these equations lack a standard algebraic solution, this solver utilizes a custom "Stripping" heuristic to achieve convergence where traditional iterative methods (like Newton's Method) often fail due to extreme growth gradients.
Through testing it has been discovered that the solver generalizes well beyond its original design scope, functioning as a general nonlinear equation solver across multiple equation classes.
The Strategy: Factoring & Stripping
Instead of relying on a "good guess," this engine uses a Growth-Rate Heuristic:

Variable Stripping: The algorithm identifies the dominant growth term (e.g., the x^x^x tower) and "strips" the equation to analyze the primary growth rate.
Search-Space Pruning: By factoring the equation based on transcendental properties, the solver narrows the search bounds before the refinement process begins.
Hybrid Convergence: Combines bisection principles with high-precision refinement to ensure stability even as x scales rapidly.
Dual Seed Competition: Both a forward seed (dominant growth drives search) and a reverse seed (slower growth drives from the other factored form) are computed independently. The better fitting seed wins. This dual approach was critical to achieving reliable convergence across all equation types — single seed approaches failed on equations where growth direction was ambiguous.

Supported Equation Types
Testing has confirmed the solver handles a much broader class of equations than the original transcendental use case:

Exponential towers (x^x, x^x^x, x^(x^(1/x)))
Mixed polynomial-exponential (x^x + x^2, x^3 - x^x)
Pure polynomials (x^7 + x^5 + x^3 + x)
Trigonometric combinations (sin(x) + x^2)
Trig multiplied with polynomials (sin(x)*cos(x) + x^4 + x^3)
Nested trig with polynomial arguments (sin(x^2) + cos(x^3) + x^5)
Nested fractional exponents (x^(x/2) + x^3)

In all tested cases the solver returns correct approximations within tolerance. The stripping logic treats bounded trig terms as noise against dominant polynomial or exponential growth, naturally handling mixed equations without special casing.
Known Limitations

Negative seeds with x^x type terms enter complex number territory and are currently unsupported in real-number mode. The solver will detect the singularity and abort cleanly rather than return a wrong answer.
Trig terms multiplied directly into exponential towers (e.g., x^x * sin(x)) can cause Phase 2 oscillation where Newton steps overshoot due to the competing periodic behavior. The bisection fallback handles this but the midpoint seed optimization will address it more cleanly.
BigDecimal arbitrary precision mode is functional but not yet fully connected across all equation types (in progress).

Performance Notes
Phase 1 seed approximation is the core innovation of this engine. In typical cases the growth-rate stripping produces a seed close enough that Phase 2 converges in 0-2 iterations. The planned midpoint optimization will address edge cases where Phase 1 lands further from the root, currently capped at 99 iterations. A bisection fallback is already implemented as a last resort safety net in Phase 2.
All results are approximations — the solver returns x within a tolerance band. Classic mode targets 10 decimal places of precision. BigDecimal mode will extend this to 34+ digits for applications where approximation is insufficient.
Human-AI Collaboration
This project represents a "Force Multiplier" approach to development:

Architect (Human): Engineered the high-level logic, the two-factor growth-rate factoring strategy, the dual forward/reverse seed competition, and the "Phase 1" seed improvement logic.
Implementation (AI): Leveraged LLMs to assist in generating boilerplate multi-threading structures and managing the heavy lifting of Java's syntax for the BigDecimal math requirements.

Proof of Concept: The Exponential Tower
The following terminal output demonstrates the solver taming a violent exponential curve. Starting from a "bad" initial guess of 2.0 (which produced an error value of -82.0), the Phase 1 logic corrected the trajectory to converge in a single high-precision iteration.
Enter function: x^x^x + x - 100
Enter guess (seed): 2
--- Solving: x^x^x + x - 100 ---
[INFO] Starting OmniSolver...
[Phase 1] Computing forward seed (fast growth dominates)...
[DEBUG] Initial Value: -82.0
[INFO] Phase 1: Improving Seed...
[INFO] Seed improved to: 2.2168991921486314
[Phase 2] Starting High-Precision Loop...
[SUCCESS] Converged in 1 iterations.
FINAL RESULT: 2.2107367840
Proof of Concept: Nested Trig + Polynomial
Demonstrating the solver handling a complex mixed equation with nested trig arguments:
Enter function: sin(x^2) + cos(x^3) + x^5 + x^3 + x - 200
Enter guess (seed): 3
[SUCCESS] Converged in 0 iterations.
FINAL RESULT: 2.8093672396
Development Roadmap

[In Progress] BigDecimal Integration: Fully connecting the BigMode classes to handle 34-digit arbitrary precision across all equation types.
[Planned] Mid-Point Seed Optimization: Adding a third seed computed from the midpoint of the forward/reverse bracket, implementing true binary search behavior in Phase 1 to reduce Phase 2 iterations to consistently 0-2 across all equation types.
[Planned] Advanced Class Systems: Fine-tuning the solver to handle specific edge-case transcendental approaches it currently finds challenging, including oscillating trig-exponential multiplications.
