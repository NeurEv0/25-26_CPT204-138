import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Analyzes data characteristics of the three candidate datasets.
 * Helps explain which sorting algorithm performs best on each dataset
 * by examining initial order, distribution, and duplicate patterns.
 */
public class DataAnalyzer {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String OUTPUT_DIR = "output";
    private static final String[] DATASETS = {"candidates_A.csv", "candidates_B.csv", "candidates_C.csv"};

    public static void main(String[] args) {
        StringBuilder report = new StringBuilder();
        report.append("==============================================\n");
        report.append("  Data Characteristics Analysis\n");
        report.append("==============================================\n\n");

        for (int d = 0; d < DATASETS.length; d++) {
            String filePath = DATA_DIR + "/" + DATASETS[d];
            Location[] data = readCSV(filePath);
            report.append("──────────────────────────────────────────────\n");
            report.append("  Dataset ").append((char)('A' + d)).append(": ").append(DATASETS[d]).append("\n");
            report.append("──────────────────────────────────────────────\n");
            analyze(data, report);
            report.append("\n");
        }

        // Print to console
        System.out.print(report.toString());

        // Write to file
        exportReport(report.toString());
    }

    private static Location[] readCSV(String filePath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Location[] locations = new Location[count];
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            int idx = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                locations[idx++] = new Location(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }

    private static void analyze(Location[] data, StringBuilder sb) {
        // ---- Basic Statistics ----
        int n = data.length;
        int minScore = Integer.MAX_VALUE, maxScore = Integer.MIN_VALUE;
        long sum = 0;
        int minId = Integer.MAX_VALUE, maxId = Integer.MIN_VALUE;

        for (Location loc : data) {
            int score = loc.getPriorityScore();
            if (score < minScore) minScore = score;
            if (score > maxScore) maxScore = score;
            sum += score;

            int id = extractIdNumber(loc.getLocationId());
            if (id < minId) minId = id;
            if (id > maxId) maxId = id;
        }

        double mean = (double) sum / n;

        sb.append("  Basic Statistics:\n");
        sb.append("    Total locations : ").append(n).append("\n");
        sb.append("    Score range      : ").append(minScore).append(" ~ ").append(maxScore).append("\n");
        sb.append("    Mean score       : ").append(String.format("%.2f", mean)).append("\n");
        sb.append("    Location ID range: L").append(String.format("%04d", minId))
          .append(" ~ L").append(String.format("%04d", maxId)).append("\n");

        // ---- Score Distribution ----
        int[] scoreFrequency = new int[maxScore - minScore + 1];
        for (Location loc : data) {
            scoreFrequency[loc.getPriorityScore() - minScore]++;
        }

        int uniqueScores = 0;
        int maxFreq = 0;
        int maxFreqScore = 0;
        for (int i = 0; i < scoreFrequency.length; i++) {
            if (scoreFrequency[i] > 0) uniqueScores++;
            if (scoreFrequency[i] > maxFreq) {
                maxFreq = scoreFrequency[i];
                maxFreqScore = i + minScore;
            }
        }

        sb.append("    Unique scores    : ").append(uniqueScores).append("\n");
        sb.append("    Most frequent    : score ").append(maxFreqScore)
          .append(" appears ").append(maxFreq).append(" times\n");

        // ---- Initial Order Analysis ----
        int descViolations = 0;
        int ascViolations = 0;

        for (int i = 0; i < n - 1; i++) {
            int curr = data[i].getPriorityScore();
            int next = data[i + 1].getPriorityScore();
            if (curr < next) descViolations++;
            if (curr > next) ascViolations++;
        }

        double descSortedness = (1.0 - (double) descViolations / (n - 1)) * 100;
        double ascSortedness = (1.0 - (double) ascViolations / (n - 1)) * 100;

        sb.append("\n  Initial Order Analysis:\n");
        sb.append("    Descending order : ").append(String.format("%.1f%%", descSortedness))
          .append(" sorted (").append(descViolations).append(" violations out of ")
          .append(n - 1).append(" adjacent pairs)\n");
        sb.append("    Ascending order  : ").append(String.format("%.1f%%", ascSortedness))
          .append(" sorted (").append(ascViolations).append(" violations out of ")
          .append(n - 1).append(" adjacent pairs)\n");

        // ---- Inversion Count (for first 200 elements as sample) ----
        int sampleSize = Math.min(n, 200);
        long inversions = countInversionsSample(data, sampleSize);
        long maxPossible = (long) sampleSize * (sampleSize - 1) / 2;
        double inversionDensity = (double) inversions / maxPossible * 100;
        sb.append("    Inversions (first ").append(sampleSize).append(" items) : ").append(inversions)
          .append(" (").append(String.format("%.1f%%", inversionDensity)).append(" of max possible)\n");

        // ---- Initial Order Classification ----
        String orderType;
        if (descSortedness > 95) {
            orderType = "NEARLY SORTED DESCENDING";
        } else if (descSortedness > 70) {
            orderType = "PARTIALLY SORTED DESCENDING";
        } else if (inversionDensity > 40) {
            orderType = "RANDOM / UNSORTED";
        } else {
            orderType = "PARTIALLY SORTED";
        }
        sb.append("    Classification  : ").append(orderType).append("\n");

        // ---- Duplicate / Tie Analysis ----
        int totalTies = 0;
        int tieGroups = 0;
        for (int freq : scoreFrequency) {
            if (freq > 1) {
                totalTies += freq;
                tieGroups++;
            }
        }

        sb.append("\n  Tie Analysis:\n");
        sb.append("    Score groups with ties : ").append(tieGroups).append("\n");
        sb.append("    Locations sharing a score: ").append(totalTies)
          .append(" (").append(String.format("%.1f%%", (double) totalTies / n * 100)).append(" of data)\n");

        // ---- Score Gap Analysis ----
        long gapSum = 0;
        int gapCount = 0;
        for (int i = 0; i < n - 1; i++) {
            int gap = data[i].getPriorityScore() - data[i + 1].getPriorityScore();
            if (gap != 0) {
                gapSum += Math.abs(gap);
                gapCount++;
            }
        }
        double avgGap = gapCount > 0 ? (double) gapSum / gapCount : 0;
        sb.append("\n  Score Gap Analysis:\n");
        sb.append("    Avg gap between distinct adjacent scores: ")
          .append(String.format("%.2f", avgGap)).append("\n");
    }

    private static void exportReport(String content) {
        java.io.File outputDir = new java.io.File(OUTPUT_DIR);
        outputDir.mkdirs();

        String filePath = OUTPUT_DIR + "/data_analysis_report.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.print(content);
            System.out.println("[Export] Data analysis report saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error exporting data analysis report: " + e.getMessage());
        }
    }

    /**
     * Counts inversions in a sample subset using a simple O(n²) method.
     * An inversion is a pair (i, j) where i < j but data[i] > data[j] in sort order.
     */
    private static long countInversionsSample(Location[] data, int sampleSize) {
        long inversions = 0;
        for (int i = 0; i < sampleSize; i++) {
            for (int j = i + 1; j < sampleSize; j++) {
                // Using compareTo: positive means data[i] should come AFTER data[j]
                // (i.e., data[i] has lower priority or same priority but larger id)
                if (data[i].compareTo(data[j]) > 0) {
                    inversions++;
                }
            }
        }
        return inversions;
    }

    /**
     * Extracts the numeric part from a location ID like "L0001" -> 1.
     */
    private static int extractIdNumber(String locationId) {
        return Integer.parseInt(locationId.substring(1));
    }
}
