package inspection.model;

/**
 * An immutable, weighted directed edge in the infrastructure graph.
 *
 * <p>Because the graph in paths.csv is undirected, every logical connection
 * is represented by two {@code Edge} objects — one in each direction — so
 * that shortest-path traversal only needs to follow outgoing edges.
 */
public class Edge {

    private final String target;
    private final int weight;

    public Edge(String target, int weight) {
        this.target = target;
        this.weight = weight;
    }

    public String getTarget() {
        return target;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "-> " + target + " (w=" + weight + ")";
    }
}
