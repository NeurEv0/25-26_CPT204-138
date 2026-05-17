package inspection.app;

import inspection.model.Location;

/**
 * Entry point for the Urban Infrastructure Inspection System.
 *
 * <p>This class is the single {@code main} method for the whole application.
 * It orchestrates two sequential phases:
 *
 * <ol>
 *   <li><b>Phase 1 – Candidate Selection (Task A):</b>
 *       {@link TaskARunner} loads the three candidate-location CSV files,
 *       evaluates the runtime of Bubble Sort, Quick Sort, and Merge Sort on
 *       each dataset, and selects the top 10 highest-priority locations per
 *       dataset, yielding 30 inspection targets in total.</li>
 *
 *   <li><b>Phase 2 – Route Planning (Task B):</b>
 *       {@link TaskBRunner} receives the 30 selected targets directly as Java
 *       objects from Phase 1 (no CSV file read required), loads the
 *       infrastructure graph from {@code paths.csv}, and computes shortest
 *       paths for four specified query cases using Bidirectional Dijkstra.</li>
 * </ol>
 *
 * <p>The two phases are connected through the return value of
 * {@link TaskARunner#run()}, which passes {@code Location[][]} directly to
 * {@link TaskBRunner#run(Location[][])}. This object-level handoff makes the
 * system a coherent end-to-end pipeline rather than two independent programs
 * that communicate through intermediate files.
 *
 * <h3>How to run</h3>
 * <pre>
 *   # Compile (from project root)
 *   javac -d out src/inspection/model/*.java \
 *               src/inspection/sorting/*.java \
 *               src/inspection/graph/*.java \
 *               src/inspection/io/*.java \
 *               src/inspection/analysis/*.java \
 *               src/inspection/app/*.java
 *
 *   # Execute
 *   java -cp out inspection.app.InspectionSystem
 * </pre>
 */
public class InspectionSystem {

    public static void main(String[] args) {
        printBanner();

        // ── Phase 1: Sorting & Candidate Selection ────────────────────────────
        TaskARunner taskA = new TaskARunner();
        Location[][] topTargets = taskA.run();
        // topTargets[0] = top 10 from Dataset A
        // topTargets[1] = top 10 from Dataset B
        // topTargets[2] = top 10 from Dataset C

        // ── Phase 2: Graph Construction & Route Planning ──────────────────────
        // topTargets is passed directly — no CSV intermediary
        TaskBRunner taskB = new TaskBRunner();
        taskB.run(topTargets);

        // ── Done ──────────────────────────────────────────────────────────────
        System.out.println("==============================================");
        System.out.println("  Inspection system workflow complete.");
        System.out.println("  Output files written to: output/");
        System.out.println("==============================================");
    }

    private static void printBanner() {
        System.out.println("==============================================");
        System.out.println("  Urban Infrastructure Inspection System");
        System.out.println("  CPT204 Group Project – AY2526");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("  Phase 1 : Candidate Selection   (Task A)");
        System.out.println("  Phase 2 : Route Planning        (Task B)");
        System.out.println();
    }
}
