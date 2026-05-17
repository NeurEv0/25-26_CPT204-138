package taskb.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the result of one shortest-path query.
 */
public class DijkstraResult {
    private final List<String> path;
    private final int totalCost;
    private final boolean reachable;

    public DijkstraResult(List<String> path, int totalCost, boolean reachable) {
        this.path = path;
        this.totalCost = totalCost;
        this.reachable = reachable;
    }

    public List<String> getPath() {
        return new ArrayList<String>(path);
    }

    public int getTotalCost() {
        return totalCost;
    }

    public boolean isReachable() {
        return reachable;
    }

    public String getPathString() {
        if (!reachable || path.isEmpty()) {
            return "No path found";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }
}
