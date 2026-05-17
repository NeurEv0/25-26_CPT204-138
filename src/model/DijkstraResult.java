package inspection.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable result of a single shortest-path query.
 *
 * <p>Encapsulates whether the destination was reachable, the ordered list of
 * location IDs forming the path, and the total accumulated edge weight.
 * Returning a dedicated result object — rather than {@code null} or a raw list
 * — makes the caller's reachability check explicit and avoids
 * {@code NullPointerException} in unreachable cases.
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

    /** Returns a defensive copy of the path node list. */
    public List<String> getPath() {
        return new ArrayList<String>(path);
    }

    public int getTotalCost() {
        return totalCost;
    }

    public boolean isReachable() {
        return reachable;
    }

    /** Formats the path as {@code "L0001 -> L0002 -> ..."} for reporting. */
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
