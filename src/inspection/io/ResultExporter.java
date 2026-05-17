package inspection.io;

import inspection.model.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Consolidates all file-export operations for the inspection system.
 *
 * <p>Previously, export methods were scattered across {@code TaskA.java} and
 * {@code TaskB.java} as private static methods. Gathering them here means that
 * any future change to the output format — column names, file paths, encoding —
 * is made in exactly one place.
 */
public class ResultExporter {

    private ResultExporter() {
        // Utility class — not instantiable.
    }

    /**
     * Exports the top-10 selected locations from each dataset to a single CSV.
     *
     * @param top10s   three-element array: {@code top10s[0]} = Dataset A,
     *                 {@code top10s[1]} = Dataset B, {@code top10s[2]} = Dataset C
     * @param filePath destination file path
     */
    public static void exportTop30(Location[][] top10s, String filePath) {
        ensureParentDirs(filePath);
        String[] labels = {"A", "B", "C"};
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("rank,dataset,location_id,priority_score");
            for (int d = 0; d < top10s.length; d++) {
                for (int i = 0; i < top10s[d].length; i++) {
                    Location loc = top10s[d][i];
                    pw.println((i + 1) + "," + labels[d] + ","
                            + loc.getLocationId() + "," + loc.getPriorityScore());
                }
            }
            System.out.println("[Export] Top-30 targets  -> " + filePath);
        } catch (IOException e) {
            System.err.println("[ResultExporter] Error writing top-30: " + e.getMessage());
        }
    }

    /**
     * Exports per-dataset average sort times (in nanoseconds) to a CSV.
     *
     * @param bubbleTimes average Bubble Sort time per dataset (ns)
     * @param quickTimes  average Quick Sort time per dataset (ns)
     * @param mergeTimes  average Merge Sort time per dataset (ns)
     * @param filePath    destination file path
     */
    public static void exportTimingReport(long[] bubbleTimes,
                                          long[] quickTimes,
                                          long[] mergeTimes,
                                          String filePath) {
        ensureParentDirs(filePath);
        String[] labels = {"A", "B", "C"};
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("dataset,bubble_sort_ns,quick_sort_ns,merge_sort_ns");
            for (int d = 0; d < 3; d++) {
                pw.println(labels[d] + "," + bubbleTimes[d] + ","
                        + quickTimes[d] + "," + mergeTimes[d]);
            }
            System.out.println("[Export] Timing report   -> " + filePath);
        } catch (IOException e) {
            System.err.println("[ResultExporter] Error writing timing report: " + e.getMessage());
        }
    }

    /**
     * Exports shortest-path results (formatted text lines) to a plain-text file.
     *
     * @param reportLines ordered list of text lines to write
     * @param algorithmName name of the algorithm used (written in the file header)
     * @param filePath    destination file path
     */
    public static void exportPathResults(List<String> reportLines,
                                         String algorithmName,
                                         String filePath) {
        ensureParentDirs(filePath);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("Task B - Shortest Path Results");
            pw.println("Algorithm: " + algorithmName);
            pw.println("==============================================");
            for (String line : reportLines) {
                pw.println(line);
            }
            System.out.println("[Export] Path results    -> " + filePath);
        } catch (IOException e) {
            System.err.println("[ResultExporter] Error writing path results: " + e.getMessage());
        }
    }

    /**
     * Exports the dataset-analysis report to a plain-text file.
     *
     * @param content  full text content to write
     * @param filePath destination file path
     */
    public static void exportDataAnalysis(String content, String filePath) {
        ensureParentDirs(filePath);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.print(content);
            System.out.println("[Export] Data analysis   -> " + filePath);
        } catch (IOException e) {
            System.err.println("[ResultExporter] Error writing data analysis: " + e.getMessage());
        }
    }

    private static void ensureParentDirs(String filePath) {
        File parent = new File(filePath).getParentFile();
        if (parent != null) parent.mkdirs();
    }
}
