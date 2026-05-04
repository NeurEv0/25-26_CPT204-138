import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Analyzes data characteristics of the three candidate datasets.
 * Helps explain which sorting algorithm performs best on each dataset
 * by examining initial order, distribution, and duplicate patterns.
 */
public class DataAnalyzer {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String[] DATASETS = {"candidates_A.csv", "candidates_B.csv", "candidates_C.csv"};

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Data Characteristics Analysis");
        System.out.println("==============================================\n");

        for (int d = 0; d < DATASETS.length; d++) {
            String filePath = DATA_DIR + "/" + DATASETS[d];
            Location[] data = readCSV(filePath);
            System.out.println("──────────────────────────────────────────────");
            System.out.println("  Dataset " + (char)('A' + d) + ": " + DATASETS[d]);
            System.out.println("──────────────────────────────────────────────");
            analyze(data);
            System.out.println();
        }
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

    private static void analyze(Location[] data) {
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

        System.out.println("  Basic Statistics:");
        System.out.println("    Total locations : " + n);
        System.out.println("    Score range      : " + minScore + " ~ " + maxScore);
        System.out.println("    Mean score       : " + String.format("%.2f", mean));
        System.out.println("    Location ID range: L" + String.format("%04d", minId) +
                " ~ L" + String.format("%04d", maxId));

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

        System.out.println("    Unique scores    : " + uniqueScores);
        System.out.println("    Most frequent    : score " + maxFreqScore +
                " appears " + maxFreq + " times");

        // ---- Initial Order Analysis ----
        int descViolations = 0;     // violations of descending order (adjacent)
        int ascViolations = 0;      // violations of ascending order (adjacent)

        for (int i = 0; i < n - 1; i++) {
            int curr = data[i].getPriorityScore();
            int next = data[i + 1].getPriorityScore();
            if (curr < next) descViolations++;
            if (curr > next) ascViolations++;
        }

        double descSortedness = (1.0 - (double) descViolations / (n - 1)) * 100;
        double ascSortedness = (1.0 - (double) ascViolations / (n - 1)) * 100;

        System.out.println("\n  Initial Order Analysis:");
        System.out.println("    Descending order : " + String.format("%.1f%%", descSortedness) +
                " sorted (" + descViolations + " violations out of " + (n - 1) + " adjacent pairs)");
        System.out.println("    Ascending order  : " + String.format("%.1f%%", ascSortedness) +
                " sorted (" + ascViolations + " violations out of " + (n - 1) + " adjacent pairs)");

        // ---- Inversion Count (for first 200 elements as sample) ----
        int sampleSize = Math.min(n, 200);
        long inversions = countInversionsSample(data, sampleSize);
        long maxPossible = (long) sampleSize * (sampleSize - 1) / 2;
        double inversionDensity = (double) inversions / maxPossible * 100;
        System.out.println("    Inversions (first " + sampleSize + " items) : " + inversions +
                " (" + String.format("%.1f%%", inversionDensity) + " of max possible)");

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
        System.out.println("    Classification  : " + orderType);

        // ---- Duplicate / Tie Analysis ----
        int totalTies = 0;
        int tieGroups = 0;
        for (int freq : scoreFrequency) {
            if (freq > 1) {
                totalTies += freq;
                tieGroups++;
            }
        }

        System.out.println("\n  Tie Analysis:");
        System.out.println("    Score groups with ties : " + tieGroups);
        System.out.println("    Locations sharing a score: " + totalTies +
                " (" + String.format("%.1f%%", (double) totalTies / n * 100) + " of data)");

        // ---- Consistency across dataset (variance of gaps) ----
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
        System.out.println("\n  Score Gap Analysis:");
        System.out.println("    Avg gap between distinct adjacent scores: " +
                String.format("%.2f", avgGap));
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
