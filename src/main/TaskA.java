import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Task A - Algorithm Evaluation: Sorting
 * Reads three candidate datasets, sorts them using Bubble Sort, Quick Sort,
 * and Merge Sort, measures performance, and identifies the top 10 locations.
 */
public class TaskA {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String OUTPUT_DIR = "output";
    private static final String[] DATASETS = {"candidates_A.csv", "candidates_B.csv", "candidates_C.csv"};
    private static final int RUNS = 3;
    private static long[] bubbleTimes = new long[3];
    private static long[] quickTimes = new long[3];
    private static long[] mergeTimes = new long[3];

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Task A - Sorting Algorithm Evaluation");
        System.out.println("==============================================\n");

        System.out.printf("%-15s %-20s %-20s %-20s%n", "Dataset", "Bubble Sort", "Quick Sort", "Merge Sort");
        System.out.println("--------------------------------------------------------------");

        Location[] finalA = null, finalB = null, finalC = null;

        for (int d = 0; d < DATASETS.length; d++) {
            String datasetName = DATASETS[d];
            String filePath = DATA_DIR + "/" + datasetName;

            Location[] originalData = readCSV(filePath);
            System.out.printf("\n[%s] Loaded %d locations.%n", datasetName, originalData.length);

            long bubbleAvg = testSort(originalData, "Bubble", d);
            long quickAvg  = testSort(originalData, "Quick", d);
            long mergeAvg  = testSort(originalData, "Merge", d);

            bubbleTimes[d] = bubbleAvg;
            quickTimes[d] = quickAvg;
            mergeTimes[d] = mergeAvg;

            Location[] sortedCopy = copyArray(originalData);
            SortingAlgorithms.quickSort(sortedCopy);
            Location[] top10 = new Location[10];
            System.arraycopy(sortedCopy, 0, top10, 0, 10);

            if (d == 0) finalA = copyArray(top10);
            else if (d == 1) finalB = copyArray(top10);
            else finalC = copyArray(top10);

            String datasetLabel = "Dataset " + (char) ('A' + d);
            System.out.printf("%-15s %-20s %-20s %-20s%n",
                    datasetLabel,
                    formatTime(bubbleAvg),
                    formatTime(quickAvg),
                    formatTime(mergeAvg));

            System.out.println("  Top 10 Selected Locations:");
            for (int i = 0; i < top10.length; i++) {
                System.out.printf("    %2d. %s (score: %d)%n",
                        i + 1, top10[i].getLocationId(), top10[i].getPriorityScore());
            }
            System.out.println();
        }

        System.out.println("\n==============================================");
        System.out.println("  Top 10 Locations Summary (for Task B)");
        System.out.println("==============================================");

        printTop10Summary("Dataset A", finalA);
        printTop10Summary("Dataset B", finalB);
        printTop10Summary("Dataset C", finalC);

        // Export results to CSV files
        exportTop30ToCSV(finalA, finalB, finalC);
        exportTimingReport();
    }

    private static Location[] readCSV(String filePath) {
        // First pass: count lines (excluding header and empty lines)
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) count++;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }

        // Second pass: fill array
        Location[] locations = new Location[count];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header
            int idx = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                String locId = parts[0].trim();
                int score = Integer.parseInt(parts[1].trim());
                locations[idx++] = new Location(locId, score);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return locations;
    }

    private static long testSort(Location[] original, String algorithm, int datasetIdx) {
        long totalTime = 0;

        for (int run = 0; run < RUNS; run++) {
            Location[] copy = copyArray(original);
            long start = System.nanoTime();

            switch (algorithm) {
                case "Bubble": SortingAlgorithms.bubbleSort(copy); break;
                case "Quick":  SortingAlgorithms.quickSort(copy);  break;
                case "Merge":  SortingAlgorithms.mergeSort(copy);  break;
            }

            long end = System.nanoTime();
            totalTime += (end - start);

            if (run == RUNS - 1) {
                verifySort(copy, datasetIdx, algorithm);
            }
        }

        return totalTime / RUNS;
    }

    private static Location[] copyArray(Location[] src) {
        Location[] dest = new Location[src.length];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

    private static void verifySort(Location[] arr, int datasetIdx, String algorithm) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i].compareTo(arr[i + 1]) > 0) {
                System.err.printf("  WARNING: %s sort failed validation on dataset %c!%n",
                        algorithm, 'A' + datasetIdx);
                return;
            }
        }
    }

    private static String formatTime(long nanos) {
        if (nanos < 1_000_000) {
            return String.format("%,d ns", nanos);
        } else if (nanos < 1_000_000_000) {
            return String.format("%.3f ms", nanos / 1_000_000.0);
        } else {
            return String.format("%.3f s", nanos / 1_000_000_000.0);
        }
    }

    private static void printTop10Summary(String label, Location[] locations) {
        System.out.print(label + ": ");
        for (int i = 0; i < locations.length; i++) {
            System.out.print(locations[i].getLocationId());
            if (i < locations.length - 1) System.out.print(", ");
        }
        System.out.println();
    }

    /**
     * Exports the top 10 locations from each dataset to a CSV file.
     */
    private static void exportTop30ToCSV(Location[] finalA, Location[] finalB, Location[] finalC) {
        java.io.File outputDir = new java.io.File(OUTPUT_DIR);
        outputDir.mkdirs();

        String filePath = OUTPUT_DIR + "/taskA_top30_targets.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("rank,dataset,location_id,priority_score");
            for (int i = 0; i < 10; i++) {
                pw.println((i + 1) + ",A," + finalA[i].getLocationId() + "," + finalA[i].getPriorityScore());
            }
            for (int i = 0; i < 10; i++) {
                pw.println((i + 1) + ",B," + finalB[i].getLocationId() + "," + finalB[i].getPriorityScore());
            }
            for (int i = 0; i < 10; i++) {
                pw.println((i + 1) + ",C," + finalC[i].getLocationId() + "," + finalC[i].getPriorityScore());
            }
            System.out.println("\n[Export] Top 30 targets saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error exporting top 30 targets: " + e.getMessage());
        }
    }

    /**
     * Exports the timing comparison report to a CSV file.
     */
    private static void exportTimingReport() {
        java.io.File outputDir = new java.io.File(OUTPUT_DIR);
        outputDir.mkdirs();

        String filePath = OUTPUT_DIR + "/taskA_timing_report.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("dataset,bubble_sort_ns,quick_sort_ns,merge_sort_ns");
            String[] labels = {"A", "B", "C"};
            for (int d = 0; d < 3; d++) {
                pw.println(labels[d] + "," + bubbleTimes[d] + "," + quickTimes[d] + "," + mergeTimes[d]);
            }
            System.out.println("[Export] Timing report saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error exporting timing report: " + e.getMessage());
        }
    }
}
