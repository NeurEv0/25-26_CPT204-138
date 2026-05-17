package inspection.io;

import inspection.model.Graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads a weighted-edge CSV file into a {@link Graph}.
 *
 * <p>Previously, {@code Graph.readFromCSV()} mixed file-I/O into the domain
 * model class. Extracting the loading logic here keeps {@link Graph} a pure
 * in-memory data structure with no I/O dependencies, and follows the
 * Single Responsibility Principle.
 *
 * <p>Expected CSV format (paths.csv):
 * <pre>
 *   from_location,to_location,weight
 *   L0001,L0002,15
 *   L0001,L0003,22
 *   ...
 * </pre>
 * Each row is treated as an undirected edge; both directions are inserted
 * into the {@link Graph} via {@link Graph#addEdge}.
 */
public class GraphLoader {

    private GraphLoader() {
        // Utility class — not instantiable.
    }

    /**
     * Reads {@code filePath} and returns a fully constructed {@link Graph}.
     *
     * @param filePath path to the edge-list CSV file (e.g., {@code paths.csv})
     * @return an undirected weighted {@link Graph} containing all edges in the file
     */
    public static Graph load(String filePath) {
        Graph graph = new Graph();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                String from = parts[0].trim();
                String to = parts[1].trim();
                int weight = Integer.parseInt(parts[2].trim());
                graph.addEdge(from, to, weight);
            }
        } catch (IOException e) {
            System.err.println("[GraphLoader] Error reading: " + filePath);
            e.printStackTrace();
        }

        return graph;
    }
}
