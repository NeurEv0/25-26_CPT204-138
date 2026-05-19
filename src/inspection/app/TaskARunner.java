package inspection.app;

import inspection.analysis.DataAnalyzer;
import inspection.io.CandidateLoader;
import inspection.io.ResultExporter;
import inspection.model.Location;
import inspection.sorting.SortingAlgorithms;

/**
 * Executes the Task A workflow: loads the three candidate datasets, sorts each
 * one with Bubble Sort, Quick Sort, and Merge Sort, measures average runtimes,
 * and extracts the top 10 highest-priority locations per dataset.
 */
public class TaskARunner {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String OUTPUT_DIR = "output";
    private static final String[] DATASET_FILES = {
            "candidates_A.csv", "candidates_B.csv", "candidates_C.csv"
    };
    private static final int RUNS = 3;  // timing runs per algorithm per dataset

    /**
     * Runs the full Task A workflow.
     */
    public Location[][] run() {
        System.out.println("==============================================");
        System.out.println("  Task A – Sorting Algorithm Evaluation");
        System.out.println("==============================================\n");

        long[] bubbleTimes = new long[3];
        long[] quickTimes  = new long[3];
        long[] mergeTimes  = new long[3];
        Location[][] top10s = new Location[3][10];

        for (int d = 0; d < DATASET_FILES.length; d++) {
            String filePath = DATA_DIR + "/" + DATASET_FILES[d];
            String datasetLabel = "Dataset " + (char) ('A' + d);

            // Load original data once; copies are made before each sort
            Location[] originalData = CandidateLoader.load(filePath);
            System.out.printf("[%s] Loaded %d locations from %s%n",
                    datasetLabel, originalData.length, DATASET_FILES[d]);

            // Optional: embed dataset characteristics analysis
            System.out.println(DataAnalyzer.analyze(originalData, datasetLabel));

            // Time each algorithm (3 runs, report average)
            bubbleTimes[d] = timeSort(originalData, "Bubble");
            quickTimes[d]  = timeSort(originalData, "Quick");
            mergeTimes[d]  = timeSort(originalData, "Merge");

            System.out.printf("  %-12s  Bubble: %-18s  Quick: %-18s  Merge: %s%n",
                    datasetLabel,
                    formatTime(bubbleTimes[d]),
                    formatTime(quickTimes[d]),
                    formatTime(mergeTimes[d]));

            // Extract top 10 using Quick Sort on a fresh copy
            Location[] sorted = copyArray(originalData);
            SortingAlgorithms.quickSort(sorted);
            System.arraycopy(sorted, 0, top10s[d], 0, 10);

            System.out.println("  Top 10 selected locations:");
            for (int i = 0; i < 10; i++) {
                System.out.printf("    %2d. %s (score: %d)%n",
                        i + 1,
                        top10s[d][i].getLocationId(),
                        top10s[d][i].getPriorityScore());
            }
            System.out.println();
        }

        // Export timing and selection results as audit files
        ResultExporter.exportTop30(top10s, OUTPUT_DIR + "/taskA_top30_targets.csv");
        ResultExporter.exportTimingReport(bubbleTimes, quickTimes, mergeTimes,
                OUTPUT_DIR + "/taskA_timing_report.csv");

        System.out.println();
        return top10s;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private long timeSort(Location[] original, String algorithm) {
        long total = 0;
        for (int run = 0; run < RUNS; run++) {
            Location[] copy = copyArray(original);
            long start = System.nanoTime();
            switch (algorithm) {
                case "Bubble": SortingAlgorithms.bubbleSort(copy); break;
                case "Quick":  SortingAlgorithms.quickSort(copy);  break;
                case "Merge":  SortingAlgorithms.mergeSort(copy);  break;
            }
            total += System.nanoTime() - start;

            // Verify sort correctness on the last run
            if (run == RUNS - 1) {
                verifySorted(copy, algorithm);
            }
        }
        return total / RUNS;
    }

    /** Asserts that {@code arr} is fully sorted; prints a warning otherwise. */
    private void verifySorted(Location[] arr, String algorithm) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i].compareTo(arr[i + 1]) > 0) {
                System.err.printf("  WARNING: %s sort produced an incorrect result!%n", algorithm);
                return;
            }
        }
    }

    private Location[] copyArray(Location[] src) {
        Location[] dest = new Location[src.length];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

    private String formatTime(long nanos) {
        if (nanos < 1_000_000L) {
            return String.format("%,d ns", nanos);
        } else if (nanos < 1_000_000_000L) {
            return String.format("%.3f ms", nanos / 1_000_000.0);
        } else {
            return String.format("%.3f s", nanos / 1_000_000_000.0);
        }
    }
}
