Prototype working solver. More will be added to it. Will be adding mid point iteration reduction to lesson the amounts of iterations requires for optimization. Will add more class systems
to fine tune the solver to handle approaches it current struggles in. Using a 2 factor apprach of logic stripping to view growth equations. 

Project Overview

This project is a specialized Java-based numerical engine designed to solve transcendental equations where variables exist in both the base and the exponent (e.g., xx+x=k). Because these equations often lack a standard algebraic solution, this solver utilizes a custom heuristic approach to achieve high-precision results where traditional iterative methods may struggle with convergence.

Logic: Factoring & Stripping

Unlike standard solvers that rely solely on brute-force iteration or Newton’s Method (which requires a near-perfect initial guess), this engine uses a Growth-Rate Heuristic:

    Variable Stripping: The algorithm identifies the dominant term (such as xx) and "strips" the equation to analyze the primary growth rate.

    Search-Space Pruning: By factoring the equation based on its transcendental properties, the solver narrows the bounds of the solution before beginning the refinement process.

    Hybrid Convergence: The engine utilizes a combination of bisection and high-precision refinement to ensure stability, even as x scales rapidly.

Technical Specifications

    Language: Java

    Core Mechanics: Will be adding big functions later when project is more fined tune. Will be adding a mid point search approximation function to direct the approximations to
    reduce interactions down after discussing different algorithm logic with proffessor. 

    Heuristic Lambert W Solver

    Original Logic: Engineered a unique heuristic approach to solve transcendental equations of the form xx+x=k. Rather than relying on standard iterative libraries, I developed a "stripping" method that isolates variables based on their dominant growth rates.

    Human-AI Collaboration: Leveraged Large Language Models (LLMs) to bridge the gap between conceptual mathematical theory and Java implementation.

        The Division of Labor: I provided the high-level logic and "growth-rate factoring" strategy; the AI assisted in generating the boilerplate multi-threading code and managing the high-precision Big-Decimal math requirements.

    Technical Result: Successfully implemented a 34-digit precision solver that bypasses common convergence failures found in traditional Newton-Raphson applications for exponential functions.
