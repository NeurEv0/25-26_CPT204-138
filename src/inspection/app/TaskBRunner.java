package inspection.app;

import inspection.graph.OptimizedDijkstraAlgorithm;
import inspection.io.GraphLoader;
import inspection.io.ResultExporter;
import inspection.model.DijkstraResult;
import inspection.model.Graph;
import inspection.model.GraphIndex;
import inspection.model.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes the Task B workflow: builds the infrastructure graph, then solves
 * four shortest-path query cases using the top-10 inspection targets selected
 * in Task A.
 *
 * <p><b>Key integration point:</b> {@link #run(Location[][])} receives the
 * sorted target arrays directly from {@link TaskARunner#run()} as Java objects.
 * There is no intermediate CSV file read; the two tasks are connected through
 * the Java call stack rather than through the file system.
 *
 * <p><b>Bug fix:</b> The original {@code TaskB.java} had swapped arguments in
 * the {@code PathCase} constructor calls for Cases 3 and 4:
 * <ul>
 *   <li>Case 3 was producing the route {@code a1 → b1 → b5} instead of the
 *       required {@code a1 → b5 → b1}.</li>
 *   <li>Case 4 was producing {@code a1 → c1 → b5 → c5} instead of
 *       {@code a1 → b5 → c5 → c1}.</li>
 * </ul>
 * Both cases are corrected here by passing waypoints in the right order
 * to the {@link PathCase} constructor.
 */
public class TaskBRunner {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String OUTPUT_DIR = "output";
    private static final String PATHS_FILE = DATA_DIR + "/paths.csv";
    private static final String ALGORITHM_NAME = "Bidirectional Dijkstra (optimized)";
    private static final int WARMUP_RUNS = 2;

    /**
     * Runs the full Task B workflow.
     *
     * @param topTargets the 3×10 location arrays returned by {@link TaskARunner#run()};
     *                   index 0 = Dataset A, 1 = Dataset B, 2 = Dataset C
     */
    public void run(Location[][] topTargets) {
        System.out.println("==============================================");
        System.out.println("  Task B – Graph Algorithm Evaluation");
        System.out.println("  Algorithm: " + ALGORITHM_NAME);
        System.out.println("==============================================\n");

        // Build graph and integer index
        long loadStart = System.nanoTime();
        Graph graph = GraphLoader.load(PATHS_FILE);
        GraphIndex index = GraphIndex.fromGraph(graph);
        OptimizedDijkstraAlgorithm algorithm = new OptimizedDijkstraAlgorithm(index);
        long loadMs = (System.nanoTime() - loadStart) / 1_000_000;
        System.out.printf("Graph loaded: %d locations, %d ms%n%n",
                graph.getLocationCount(), loadMs);

        // Resolve the six named nodes from the top-target arrays
        // (received directly from TaskARunner — no CSV re-read needed)
        String a1  = topTargets[0][0].getLocationId();  // 1st of A
        String a10 = topTargets[0][9].getLocationId();  // 10th of A
        String b1  = topTargets[1][0].getLocationId();  // 1st of B
        String b5  = topTargets[1][4].getLocationId();  // 5th of B
        String c1  = topTargets[2][0].getLocationId();  // 1st of C
        String c5  = topTargets[2][4].getLocationId();  // 5th of C

        // Define the four required query cases
        // PathCase constructor: PathCase(number, start, destination, waypoints...)
        // Waypoints are visited in the order they appear, before the destination.
        List<PathCase> cases = new ArrayList<PathCase>();
        cases.add(new PathCase(1, a1,  a1));   // no waypoints
        cases.add(new PathCase(2, a1,  a10));  // no waypoints
        cases.add(new PathCase(3, a1,  b1,  b5));        // a1 -> b5 -> b1
        cases.add(new PathCase(4, a1,  c1,  b5, c5));    // a1 -> b5 -> c5 -> c1

        List<String> reportLines = new ArrayList<String>();
        reportLines.add("Algorithm: " + ALGORITHM_NAME);
        reportLines.add("");

        long totalSearchNanos = 0;
        int totalSegments = 0;

        for (PathCase pathCase : cases) {
            CaseResult result = solveCase(pathCase, algorithm);
            totalSearchNanos += result.getSearchTimeNanos();
            totalSegments += result.getSegmentCount();
            printCaseResult(result);
            reportLines.add(formatCaseResult(result));
        }

        System.out.println("----------------------------------------------");
        System.out.printf("Total path-search time (%d segments): %.3f ms%n",
                totalSegments, totalSearchNanos / 1_000_000.0);
        System.out.println("----------------------------------------------\n");

        reportLines.add("----------------------------------------------");
        reportLines.add(String.format("Total path-search time (%d segments): %.3f ms",
                totalSegments, totalSearchNanos / 1_000_000.0));

        ResultExporter.exportPathResults(reportLines, ALGORITHM_NAME,
                OUTPUT_DIR + "/taskB_dijkstra_optimized_shortest_paths.txt");
    }

    // -------------------------------------------------------------------------
    // Solving and printing
    // -------------------------------------------------------------------------

    private CaseResult solveCase(PathCase pathCase, OptimizedDijkstraAlgorithm algorithm) {
        List<String> routePoints = pathCase.getRoutePoints();
        List<String> combinedPath = new ArrayList<String>();
        List<SegmentTiming> segmentTimings = new ArrayList<SegmentTiming>();
        int totalCost = 0;
        long caseSearchNanos = 0;

        for (int i = 0; i < routePoints.size() - 1; i++) {
            String from = routePoints.get(i);
            String to   = routePoints.get(i + 1);

            // Warm-up runs to stabilise JIT before timing
            for (int w = 0; w < WARMUP_RUNS; w++) {
                algorithm.findShortestPath(from, to);
            }

            long start = System.nanoTime();
            DijkstraResult segment = algorithm.findShortestPath(from, to);
            long elapsed = System.nanoTime() - start;
            caseSearchNanos += elapsed;

            if (!segment.isReachable()) {
                return CaseResult.unreachable(pathCase,
                        "No path found from " + from + " to " + to);
            }

            segmentTimings.add(new SegmentTiming(from, to, segment.getTotalCost(), elapsed));

            List<String> segPath = segment.getPath();
            if (i == 0) {
                combinedPath.addAll(segPath);
            } else {
                // Drop the duplicate waypoint node that ends one segment and starts the next
                combinedPath.addAll(segPath.subList(1, segPath.size()));
            }
            totalCost += segment.getTotalCost();
        }

        return CaseResult.reachable(pathCase, combinedPath, totalCost,
                caseSearchNanos, segmentTimings);
    }

    private void printCaseResult(CaseResult result) {
        PathCase pc = result.getPathCase();
        System.out.println("Case " + pc.getCaseNumber());
        System.out.println("  Start       : " + pc.getStart());
        System.out.println("  Destination : " + pc.getDestination());
        System.out.println("  Waypoints   : " + pc.getWaypointString());
        System.out.println("  Path        : " + result.getPathString());
        System.out.println("  Total cost  : " + result.getCostString());
        for (SegmentTiming seg : result.getSegmentTimings()) {
            System.out.printf("  Segment %s -> %s : cost %d, time %.3f ms%n",
                    seg.from, seg.to, seg.cost, seg.elapsedNanos / 1_000_000.0);
        }
        System.out.printf("  Case search time: %.3f ms%n%n",
                result.getSearchTimeNanos() / 1_000_000.0);
    }

    private String formatCaseResult(CaseResult result) {
        PathCase pc = result.getPathCase();
        StringBuilder sb = new StringBuilder();
        sb.append("Case ").append(pc.getCaseNumber()).append("\n");
        sb.append("Start: ").append(pc.getStart()).append("\n");
        sb.append("Destination: ").append(pc.getDestination()).append("\n");
        sb.append("Waypoints: ").append(pc.getWaypointString()).append("\n");
        sb.append("Path: ").append(result.getPathString()).append("\n");
        sb.append("Total cost: ").append(result.getCostString()).append("\n");
        for (SegmentTiming seg : result.getSegmentTimings()) {
            sb.append(String.format("Segment %s -> %s: cost %d, time %.3f ms%n",
                    seg.from, seg.to, seg.cost, seg.elapsedNanos / 1_000_000.0));
        }
        sb.append(String.format("Case search time: %.3f ms%n",
                result.getSearchTimeNanos() / 1_000_000.0));
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Private value types
    // -------------------------------------------------------------------------

    /** Describes one shortest-path query with optional intermediate waypoints. */
    private static class PathCase {
        private final int caseNumber;
        private final String start;
        private final String destination;
        private final String[] waypoints;

        private PathCase(int caseNumber, String start, String destination,
                         String... waypoints) {
            this.caseNumber = caseNumber;
            this.start = start;
            this.destination = destination;
            this.waypoints = waypoints;
        }

        /** Returns the ordered sequence of nodes to traverse: start → waypoints → destination. */
        private List<String> getRoutePoints() {
            List<String> points = new ArrayList<String>();
            points.add(start);
            for (String wp : waypoints) points.add(wp);
            points.add(destination);
            return points;
        }

        private int getCaseNumber() { return caseNumber; }
        private String getStart() { return start; }
        private String getDestination() { return destination; }

        private String getWaypointString() {
            if (waypoints.length == 0) return "None";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < waypoints.length; i++) {
                sb.append(waypoints[i]);
                if (i < waypoints.length - 1) sb.append(" -> ");
            }
            return sb.toString();
        }
    }

    /** Holds the result of solving one {@link PathCase}. */
    private static class CaseResult {
        private final PathCase pathCase;
        private final List<String> path;
        private final int totalCost;
        private final boolean reachable;
        private final String message;
        private final long searchTimeNanos;
        private final List<SegmentTiming> segmentTimings;

        private CaseResult(PathCase pathCase, List<String> path, int totalCost,
                            boolean reachable, String message, long searchTimeNanos,
                            List<SegmentTiming> segmentTimings) {
            this.pathCase = pathCase;
            this.path = path;
            this.totalCost = totalCost;
            this.reachable = reachable;
            this.message = message;
            this.searchTimeNanos = searchTimeNanos;
            this.segmentTimings = segmentTimings;
        }

        private static CaseResult reachable(PathCase pc, List<String> path, int cost,
                                             long nanos, List<SegmentTiming> segs) {
            return new CaseResult(pc, path, cost, true, "", nanos, segs);
        }

        private static CaseResult unreachable(PathCase pc, String msg) {
            return new CaseResult(pc, new ArrayList<String>(), 0, false, msg, 0,
                    new ArrayList<SegmentTiming>());
        }

        private PathCase getPathCase() { return pathCase; }
        private long getSearchTimeNanos() { return searchTimeNanos; }
        private int getSegmentCount() { return segmentTimings.size(); }
        private List<SegmentTiming> getSegmentTimings() { return segmentTimings; }

        private String getPathString() {
            if (!reachable) return message;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i));
                if (i < path.size() - 1) sb.append(" -> ");
            }
            return sb.toString();
        }

        private String getCostString() {
            return reachable ? String.valueOf(totalCost) : "N/A";
        }
    }

    /** Timing and cost data for one segment of a multi-waypoint query. */
    private static final class SegmentTiming {
        private final String from;
        private final String to;
        private final int cost;
        private final long elapsedNanos;

        private SegmentTiming(String from, String to, int cost, long elapsedNanos) {
            this.from = from;
            this.to = to;
            this.cost = cost;
            this.elapsedNanos = elapsedNanos;
        }
    }
}
