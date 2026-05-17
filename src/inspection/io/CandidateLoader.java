package inspection.io;

import inspection.model.Location;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads a candidate-location CSV file into a {@link Location} array.
 */
public class CandidateLoader {

    private CandidateLoader() {
        // Utility class — not instantiable.
    }

    /**
     * Reads {@code filePath} and returns a {@link Location} array in file order.
     * The header row is skipped; blank lines are ignored.
     *
     * @param filePath path to the candidate CSV file
     * @return array of {@link Location} objects preserving the original row order
     */
    public static Location[] load(String filePath) {
        int count = countDataLines(filePath);
        Location[] locations = new Location[count];

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                String locId = parts[0].trim();
                int score = Integer.parseInt(parts[1].trim());
                locations[idx++] = new Location(locId, score);
            }
        } catch (IOException e) {
            System.err.println("[CandidateLoader] Error reading: " + filePath);
            e.printStackTrace();
        }

        return locations;
    }

    /** Two-pass helper: counts non-empty data rows (excluding header). */
    private static int countDataLines(String filePath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) count++;
            }
        } catch (IOException e) {
            System.err.println("[CandidateLoader] Error counting lines: " + filePath);
            e.printStackTrace();
        }
        return count;
    }
}
