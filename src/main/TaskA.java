import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Task A - Algorithm Evaluation: Sorting
 * Reads three candidate datasets, sorts them using Bubble Sort, Quick Sort,
 * and Merge Sort, measures performance, and identifies the top 10 locations.
 */
public class TaskA {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String[] DATASETS = {"candidates_A.csv", "candidates_B.csv", "candidates_C.csv"};
    private static final int RUNS = 3;

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
}
