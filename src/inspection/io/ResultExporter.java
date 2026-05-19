package inspection.io;

import inspection.model.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Consolidates all file-export operations for the inspection system.
 */
public class ResultExporter {

    private ResultExporter() {
        // Utility class — not instantiable.
    }

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
