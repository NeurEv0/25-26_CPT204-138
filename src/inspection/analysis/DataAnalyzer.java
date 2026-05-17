package inspection.analysis;

import inspection.io.CandidateLoader;
import inspection.io.ResultExporter;
import inspection.model.Location;

/**
 * Analyses the structural characteristics of a candidate-location dataset.
 *
 * <p>Can be used in two ways:
 * <ol>
 *   <li><b>Standalone</b> — run {@link #main(String[])} to analyse all three
 *       datasets and export a report to {@code output/data_analysis_report.txt}.</li>
 *   <li><b>Integrated</b> — call {@link #analyze(Location[], String)} from
 *       {@link inspection.app.TaskARunner} to embed per-dataset analysis in the
 *       sorting workflow output.</li>
 * </ol>
 */
public class DataAnalyzer {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String OUTPUT_DIR = "output";
    private static final String[] DATASETS =
            {"candidates_A.csv", "candidates_B.csv", "candidates_C.csv"};

    /** Standalone entry point: analyses all three datasets and exports a report. */
    public static void main(String[] args) {
        StringBuilder report = new StringBuilder();
        report.append("==============================================\n");
        report.append("  Data Characteristics Analysis\n");
        report.append("==============================================\n\n");

        for (int d = 0; d < DATASETS.length; d++) {
            String filePath = DATA_DIR + "/" + DATASETS[d];
            Location[] data = CandidateLoader.load(filePath);
            String datasetName = "Dataset " + (char) ('A' + d) + ": " + DATASETS[d];
            report.append("──────────────────────────────────────────────\n");
            report.append("  ").append(datasetName).append("\n");
            report.append("──────────────────────────────────────────────\n");
            report.append(analyze(data, datasetName));
            report.append("\n");
        }

        System.out.print(report);
        ResultExporter.exportDataAnalysis(report.toString(),
                OUTPUT_DIR + "/data_analysis_report.txt");
    }

    /**
     * Analyses {@code data} and returns a formatted multi-line report string.
     *
     * @param data        pre-loaded array in original file order
     * @param datasetName label used in the report header
     * @return formatted analysis text (does not include the section header)
     */
    public static String analyze(Location[] data, String datasetName) {
        StringBuilder sb = new StringBuilder();
        int n = data.length;

        // ---- Basic Statistics ----
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
        int[] scoreFreq = new int[maxScore - minScore + 1];
        for (Location loc : data) {
            scoreFreq[loc.getPriorityScore() - minScore]++;
        }
        int uniqueScores = 0, maxFreq = 0, maxFreqScore = 0;
        for (int i = 0; i < scoreFreq.length; i++) {
            if (scoreFreq[i] > 0) uniqueScores++;
            if (scoreFreq[i] > maxFreq) {
                maxFreq = scoreFreq[i];
                maxFreqScore = i + minScore;
            }
        }
        sb.append("    Unique scores    : ").append(uniqueScores).append("\n");
        sb.append("    Most frequent    : score ").append(maxFreqScore)
          .append(" appears ").append(maxFreq).append(" times\n");

        // ---- Initial Order Analysis ----
        int descViolations = 0, ascViolations = 0;
        for (int i = 0; i < n - 1; i++) {
            int curr = data[i].getPriorityScore();
            int next = data[i + 1].getPriorityScore();
            if (curr < next) descViolations++;
            if (curr > next) ascViolations++;
        }
        double descSortedness = (1.0 - (double) descViolations / (n - 1)) * 100;
        double ascSortedness  = (1.0 - (double) ascViolations  / (n - 1)) * 100;

        sb.append("\n  Initial Order Analysis:\n");
        sb.append("    Descending order : ").append(String.format("%.1f%%", descSortedness))
          .append(" sorted (").append(descViolations).append(" violations)\n");
        sb.append("    Ascending order  : ").append(String.format("%.1f%%", ascSortedness))
          .append(" sorted (").append(ascViolations).append(" violations)\n");

        int sampleSize = Math.min(n, 200);
        long inversions = countInversionsSample(data, sampleSize);
        long maxPossible = (long) sampleSize * (sampleSize - 1) / 2;
        double inversionDensity = (double) inversions / maxPossible * 100;
        sb.append("    Inversions (first ").append(sampleSize).append("): ").append(inversions)
          .append(" (").append(String.format("%.1f%%", inversionDensity)).append(" of max)\n");

        String orderType;
        if (descSortedness > 95) orderType = "NEARLY SORTED DESCENDING";
        else if (descSortedness > 70) orderType = "PARTIALLY SORTED DESCENDING";
        else if (inversionDensity > 40) orderType = "RANDOM / UNSORTED";
        else orderType = "PARTIALLY SORTED";
        sb.append("    Classification  : ").append(orderType).append("\n");

        // ---- Tie Analysis ----
        int totalTies = 0, tieGroups = 0;
        for (int freq : scoreFreq) {
            if (freq > 1) { totalTies += freq; tieGroups++; }
        }
        sb.append("\n  Tie Analysis:\n");
        sb.append("    Score groups with ties   : ").append(tieGroups).append("\n");
        sb.append("    Locations sharing a score: ").append(totalTies)
          .append(" (").append(String.format("%.1f%%", (double) totalTies / n * 100)).append(")\n");

        return sb.toString();
    }

    /** Counts inversions in a sample using O(n²) brute force. */
    private static long countInversionsSample(Location[] data, int sampleSize) {
        long inversions = 0;
        for (int i = 0; i < sampleSize; i++) {
            for (int j = i + 1; j < sampleSize; j++) {
                if (data[i].compareTo(data[j]) > 0) inversions++;
            }
        }
        return inversions;
    }

    private static int extractIdNumber(String locationId) {
        return Integer.parseInt(locationId.substring(1));
    }
}
