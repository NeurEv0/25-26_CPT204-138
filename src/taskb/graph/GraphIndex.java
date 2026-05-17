package taskb.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integer-indexed view of {@link Graph} for fast shortest-path algorithms.
 */
public final class GraphIndex {

    private final String[] idByIndex;
    private final Map<String, Integer> indexById;
    private final int[][] neighborIds;
    private final int[][] neighborWeights;

    private GraphIndex(
            String[] idByIndex,
            Map<String, Integer> indexById,
            int[][] neighborIds,
            int[][] neighborWeights) {
        this.idByIndex = idByIndex;
        this.indexById = indexById;
        this.neighborIds = neighborIds;
        this.neighborWeights = neighborWeights;
    }

    public static GraphIndex fromGraph(Graph graph) {
        List<String> ids = new ArrayList<String>(graph.getAllLocations());
        ids.sort(String::compareTo);

        int n = ids.size();
        String[] idByIndex = ids.toArray(new String[0]);
        Map<String, Integer> indexById = new HashMap<String, Integer>(n * 2);
        for (int i = 0; i < n; i++) {
            indexById.put(idByIndex[i], i);
        }

        int[][] neighborIds = new int[n][];
        int[][] neighborWeights = new int[n][];

        for (int i = 0; i < n; i++) {
            List<Edge> edges = graph.getEdges(idByIndex[i]);
            int m = edges.size();
            int[] nIds = new int[m];
            int[] nWeights = new int[m];
            for (int j = 0; j < m; j++) {
                Edge edge = edges.get(j);
                nIds[j] = indexById.get(edge.getTarget());
                nWeights[j] = edge.getWeight();
            }
            neighborIds[i] = nIds;
            neighborWeights[i] = nWeights;
        }

        return new GraphIndex(idByIndex, indexById, neighborIds, neighborWeights);
    }

    public int size() {
        return idByIndex.length;
    }

    public int idOf(String locationId) {
        Integer id = indexById.get(locationId);
        return id == null ? -1 : id;
    }

    public String locationOf(int id) {
        return idByIndex[id];
    }

    public int[] neighbors(int id) {
        return neighborIds[id];
    }

    public int[] weights(int id) {
        return neighborWeights[id];
    }
}
