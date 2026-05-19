package inspection.io;

import inspection.model.Graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads a weighted-edge CSV file into a {@link Graph}.
 */
public class GraphLoader {

    private GraphLoader() {
        // Utility class — not instantiable.
    }
    
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
