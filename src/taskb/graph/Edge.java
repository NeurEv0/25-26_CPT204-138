package taskb.graph;

/**
 * Represents one weighted edge in the infrastructure graph.
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
}
