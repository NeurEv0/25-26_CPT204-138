package inspection.app;

import inspection.model.Location;

/**
 * Entry point for the Urban Infrastructure Inspection System.
 */
public class InspectionSystem {

    public static void main(String[] args) {
        printBanner();

        // ── Phase 1: Sorting & Candidate Selection ────────────────────────────
        TaskARunner taskA = new TaskARunner();
        Location[][] topTargets = taskA.run();

        // ── Phase 2: Graph Construction & Route Planning ──────────────────────
        TaskBRunner taskB = new TaskBRunner();
        taskB.run(topTargets);

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
