package taskb.graph;

/**
 * Strategy for one shortest-path query (used by Task B runners).
 */
@FunctionalInterface
public interface ShortestPathFinder {

    DijkstraResult findShortestPath(Graph graph, String start, String destination);
}
