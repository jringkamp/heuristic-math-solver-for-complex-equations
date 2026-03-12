Project Overview

This project is a specialized Java-based numerical engine designed to solve transcendental equations where variables exist in both the base and the exponent (e.g., xx+x=k). Because these equations often lack a standard algebraic solution, this solver utilizes a custom heuristic approach to achieve high-precision results where traditional iterative methods may struggle with convergence.
The "Free Thinker" Logic: Factoring & Stripping

Unlike standard solvers that rely solely on brute-force iteration or Newton’s Method (which requires a near-perfect initial guess), this engine uses a Growth-Rate Heuristic:

    Variable Stripping: The algorithm identifies the dominant term (such as xx) and "strips" the equation to analyze the primary growth rate.

    Search-Space Pruning: By factoring the equation based on its transcendental properties, the solver narrows the bounds of the solution before beginning the refinement process.

    Hybrid Convergence: The engine utilizes a combination of bisection and high-precision refinement to ensure stability, even as x scales rapidly.

Technical Specifications

    Language: Java

    Core Mechanics: Multi-threaded execution for parallel bound-searching.

    Precision: Targeted for 34-digit accuracy using a custom "Big Engine" branch to prevent floating-point overflow in exponential calculations.

    Scope: Optimized specifically for xx and similar transcendental growth patterns.
