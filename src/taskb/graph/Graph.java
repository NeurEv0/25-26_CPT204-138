package taskb.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Weighted undirected infrastructure network (adjacency list).
 */
public class Graph {
    private final Map<String, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<String, List<Edge>>();
    }

    public void addEdge(String from, String to, int weight) {
        adjacencyList.putIfAbsent(from, new ArrayList<Edge>());
        adjacencyList.putIfAbsent(to, new ArrayList<Edge>());
        adjacencyList.get(from).add(new Edge(to, weight));
        adjacencyList.get(to).add(new Edge(from, weight));
    }

    public List<Edge> getEdges(String locationId) {
        List<Edge> edges = adjacencyList.get(locationId);
        if (edges == null) {
            return new ArrayList<Edge>();
        }
        return edges;
    }

    public boolean containsLocation(String locationId) {
        return adjacencyList.containsKey(locationId);
    }

    public int getLocationCount() {
        return adjacencyList.size();
    }

    public Set<String> getAllLocations() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    public static Graph readFromCSV(String filePath) {
        Graph graph = new Graph();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }

                String from = parts[0].trim();
                String to = parts[1].trim();
                int weight = Integer.parseInt(parts[2].trim());
                graph.addEdge(from, to, weight);
            }
        } catch (IOException e) {
            System.err.println("Error reading graph file: " + filePath);
            e.printStackTrace();
        }

        return graph;
    }
}
