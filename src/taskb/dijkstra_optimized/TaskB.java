package taskb.dijkstra_optimized;

import taskb.TaskBRunner;
import taskb.graph.Graph;
import taskb.graph.GraphIndex;
import taskb.graph.ShortestPathFinder;

/**
 * Task B entry point using optimized bidirectional Dijkstra.
 * Run from project root: {@code java -cp out taskb.dijkstra_optimized.TaskB}
 */
public class TaskB {

    public static void main(String[] args) {
        Graph graph = Graph.readFromCSV("Group Project Datasets/paths.csv");
        GraphIndex index = GraphIndex.fromGraph(graph);
        OptimizedDijkstraAlgorithm algorithm = new OptimizedDijkstraAlgorithm(index);

        ShortestPathFinder finder = (ignoredGraph, start, destination) ->
                algorithm.findShortestPath(start, destination);

        TaskBRunner.run(
                "Bidirectional Dijkstra (optimized)",
                finder,
                "taskB_dijkstra_optimized_shortest_paths.txt");
    }
}
