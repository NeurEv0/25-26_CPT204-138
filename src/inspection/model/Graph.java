package inspection.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Weighted undirected graph representing the urban infrastructure network.
 *
 * <p>Internally stored as an adjacency list ({@code HashMap<String, List<Edge>>}).
 * Each undirected edge in paths.csv is inserted in both directions so that
 * graph traversal only needs to follow outgoing {@link Edge} objects.
 *
 * <p>Graph construction (CSV loading) is deliberately kept outside this class
 * and delegated to {@link inspection.io.GraphLoader}, keeping this class a
 * pure domain model with no I/O dependencies.
 */
public class Graph {

    private final Map<String, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<String, List<Edge>>();
    }

    /**
     * Adds an undirected weighted edge between {@code from} and {@code to}.
     * Both endpoints are registered as nodes even if they have no other edges.
     */
    public void addEdge(String from, String to, int weight) {
        adjacencyList.putIfAbsent(from, new ArrayList<Edge>());
        adjacencyList.putIfAbsent(to, new ArrayList<Edge>());
        adjacencyList.get(from).add(new Edge(to, weight));
        adjacencyList.get(to).add(new Edge(from, weight));
    }

    /**
     * Returns the outgoing edges for {@code locationId}, or an empty list if
     * the location is not present in the graph.
     */
    public List<Edge> getEdges(String locationId) {
        List<Edge> edges = adjacencyList.get(locationId);
        return edges == null ? new ArrayList<Edge>() : edges;
    }

    public boolean containsLocation(String locationId) {
        return adjacencyList.containsKey(locationId);
    }

    public int getLocationCount() {
        return adjacencyList.size();
    }

    /** Returns an unmodifiable view of all location IDs in this graph. */
    public Set<String> getAllLocations() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }
}
