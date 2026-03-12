OmniSolver: Heuristic Numerical Engine (v1.0-Stable)
Project Overview

This solver is looking at a different way to approach this problem outside of Lambert W

This is a specialized Java-based numerical engine designed to solve transcendental equations where variables exist in both the base and the exponent (e.g., x^x+x=k and x^x^x+x=k). Because these equations lack a standard algebraic solution, this solver utilizes a custom "Stripping" heuristic to achieve convergence where traditional iterative methods (like Newton's Method) often fail due to extreme growth gradients.
The Strategy: Factoring & Stripping

Instead of relying on a "good guess," this engine uses a Growth-Rate Heuristic:

    Variable Stripping: The algorithm identifies the dominant growth term (e.g., the x^x^x tower) and "strips" the equation to analyze the primary growth rate.

    Search-Space Pruning: By factoring the equation based on transcendental properties, the solver narrows the search bounds before the refinement process begins.

    Hybrid Convergence: Combines bisection principles with high-precision refinement to ensure stability even as x scales rapidly.

Human-AI Collaboration

This project represents a "Force Multiplier" approach to development:

    Architect (Human): Engineered the high-level logic, the two-factor growth-rate factoring strategy, and the "Phase 1" seed improvement logic.

    Implementation (AI): Leveraged LLMs to assist in generating boilerplate multi-threading structures and managing the heavy lifting of Java's syntax for the Big-Decimal math requirements.

Proof of Concept: The Exponential Tower

The following terminal output demonstrates the solver taming a violent exponential curve. Starting from a "bad" initial guess of 2.0 (which produced an error value of -82.0), the Phase 1 logic corrected the trajectory to converge in a single high-precision iteration.
Plaintext

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

Development Roadmap

    [In Progress] BigInt Integration: Fully connecting the BigMode classes to handle 34-digit arbitrary precision across all equation types.

    [Planned] Mid-Point Iteration Reduction: Implementing an optimization layer to further reduce the computational overhead in Phase 2.

    [Planned] Advanced Class Systems: Fine-tuning the solver to handle specific edge-case transcendental approaches it currently finds challenging.
