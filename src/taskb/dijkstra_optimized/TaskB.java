package taskb.dijkstra_optimized;

import taskb.graph.DijkstraResult;
import taskb.graph.Graph;
import taskb.graph.GraphIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Task B – optimized bidirectional Dijkstra.
 * Run from project root: {@code java -cp out taskb.dijkstra_optimized.TaskB}
 */
public class TaskB {

    private static final String DATA_DIR = "Group Project Datasets";
    private static final String OUTPUT_DIR = "output";
    private static final String PATHS_FILE = DATA_DIR + "/paths.csv";
    private static final String TOP_TARGETS_FILE = OUTPUT_DIR + "/taskA_top30_targets.csv";
    private static final String RESULT_FILE = OUTPUT_DIR + "/taskB_dijkstra_optimized_shortest_paths.txt";
    private static final String ALGORITHM_NAME = "Bidirectional Dijkstra (optimized)";
    private static final int WARMUP_RUNS = 2;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Task B - Graph Algorithm Evaluation");
        System.out.println("  Algorithm: " + ALGORITHM_NAME);
        System.out.println("==============================================\n");

        long loadStart = System.nanoTime();
        Graph graph = Graph.readFromCSV(PATHS_FILE);
        GraphIndex index = GraphIndex.fromGraph(graph);
        OptimizedDijkstraAlgorithm algorithm = new OptimizedDijkstraAlgorithm(index);
        Map<String, String[]> selectedTargets = readSelectedTargets(TOP_TARGETS_FILE);
        long loadNanos = System.nanoTime() - loadStart;

        String a1 = selectedTargets.get("A")[0];
        String a10 = selectedTargets.get("A")[9];
        String b1 = selectedTargets.get("B")[0];
        String b5 = selectedTargets.get("B")[4];
        String c1 = selectedTargets.get("C")[0];
        String c5 = selectedTargets.get("C")[4];

        System.out.printf("Loaded graph locations: %d (load time: %.3f ms)%n%n",
                graph.getLocationCount(), loadNanos / 1_000_000.0);

        List<PathCase> cases = new ArrayList<PathCase>();
        cases.add(new PathCase(1, a1, a1));
        cases.add(new PathCase(2, a1, a10));
        cases.add(new PathCase(3, a1, b5, b1));
        cases.add(new PathCase(4, a1, c5, c1, b5));

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
        reportLines.add("----------------------------------------------");

        exportResults(reportLines);
    }

    private static Map<String, String[]> readSelectedTargets(String filePath) {
        Map<String, String[]> selectedTargets = new LinkedHashMap<String, String[]>();
        selectedTargets.put("A", new String[10]);
        selectedTargets.put("B", new String[10]);
        selectedTargets.put("C", new String[10]);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                int rank = Integer.parseInt(parts[0].trim());
                String dataset = parts[1].trim();
                String locationId = parts[2].trim();

                selectedTargets.get(dataset)[rank - 1] = locationId;
            }
        } catch (IOException e) {
            System.err.println("Error reading selected targets: " + filePath);
            e.printStackTrace();
        }

        return selectedTargets;
    }

    private static CaseResult solveCase(PathCase pathCase, OptimizedDijkstraAlgorithm algorithm) {
        List<String> routePoints = pathCase.getRoutePoints();
        List<String> combinedPath = new ArrayList<String>();
        List<SegmentTiming> segmentTimings = new ArrayList<SegmentTiming>();
        int totalCost = 0;
        long caseSearchNanos = 0;

        for (int i = 0; i < routePoints.size() - 1; i++) {
            String from = routePoints.get(i);
            String to = routePoints.get(i + 1);

            for (int w = 0; w < WARMUP_RUNS; w++) {
                algorithm.findShortestPath(from, to);
            }

            long start = System.nanoTime();
            DijkstraResult segment = algorithm.findShortestPath(from, to);
            long elapsed = System.nanoTime() - start;
            caseSearchNanos += elapsed;

            if (!segment.isReachable()) {
                return CaseResult.unreachable(pathCase, "No path found from " + from + " to " + to);
            }

            segmentTimings.add(new SegmentTiming(from, to, segment.getTotalCost(), elapsed));

            List<String> segmentPath = segment.getPath();
            if (i == 0) {
                combinedPath.addAll(segmentPath);
            } else {
                combinedPath.addAll(segmentPath.subList(1, segmentPath.size()));
            }
            totalCost += segment.getTotalCost();
        }

        return CaseResult.reachable(pathCase, combinedPath, totalCost, caseSearchNanos, segmentTimings);
    }

    private static void printCaseResult(CaseResult result) {
        PathCase pathCase = result.getPathCase();

        System.out.println("Case " + pathCase.getCaseNumber());
        System.out.println("  Start       : " + pathCase.getStart());
        System.out.println("  Destination : " + pathCase.getDestination());
        System.out.println("  Waypoints   : " + pathCase.getWaypointString());
        System.out.println("  Path        : " + result.getPathString());
        System.out.println("  Total cost  : " + result.getCostString());

        for (SegmentTiming seg : result.getSegmentTimings()) {
            System.out.printf("  Segment %s -> %s : cost %d, time %.3f ms%n",
                    seg.from, seg.to, seg.cost, seg.elapsedNanos / 1_000_000.0);
        }
        System.out.printf("  Case search time: %.3f ms%n%n",
                result.getSearchTimeNanos() / 1_000_000.0);
    }

    private static String formatCaseResult(CaseResult result) {
        PathCase pathCase = result.getPathCase();
        StringBuilder sb = new StringBuilder();

        sb.append("Case ").append(pathCase.getCaseNumber()).append("\n");
        sb.append("Start: ").append(pathCase.getStart()).append("\n");
        sb.append("Destination: ").append(pathCase.getDestination()).append("\n");
        sb.append("Waypoints: ").append(pathCase.getWaypointString()).append("\n");
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

    private static void exportResults(List<String> reportLines) {
        java.io.File outputDir = new java.io.File(OUTPUT_DIR);
        outputDir.mkdirs();

        try (PrintWriter pw = new PrintWriter(new FileWriter(RESULT_FILE))) {
            pw.println("Task B - Shortest Path Results");
            pw.println("Algorithm: " + ALGORITHM_NAME);
            pw.println("==============================================");
            for (String line : reportLines) {
                pw.println(line);
            }
            System.out.println("[Export] Results saved to: " + RESULT_FILE);
        } catch (IOException e) {
            System.err.println("Error exporting Task B results: " + e.getMessage());
        }
    }

    private static String joinPath(List<String> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }

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

    private static class PathCase {
        private final int caseNumber;
        private final String start;
        private final String destination;
        private final String[] waypoints;

        private PathCase(int caseNumber, String start, String destination, String... waypoints) {
            this.caseNumber = caseNumber;
            this.start = start;
            this.destination = destination;
            this.waypoints = waypoints;
        }

        private int getCaseNumber() {
            return caseNumber;
        }

        private String getStart() {
            return start;
        }

        private String getDestination() {
            return destination;
        }

        private List<String> getRoutePoints() {
            List<String> routePoints = new ArrayList<String>();
            routePoints.add(start);
            for (String waypoint : waypoints) {
                routePoints.add(waypoint);
            }
            routePoints.add(destination);
            return routePoints;
        }

        private String getWaypointString() {
            if (waypoints.length == 0) {
                return "None";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < waypoints.length; i++) {
                sb.append(waypoints[i]);
                if (i < waypoints.length - 1) {
                    sb.append(" -> ");
                }
            }
            return sb.toString();
        }
    }

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

        private static CaseResult reachable(PathCase pathCase, List<String> path, int totalCost,
                                            long searchTimeNanos, List<SegmentTiming> segmentTimings) {
            return new CaseResult(pathCase, path, totalCost, true, "", searchTimeNanos, segmentTimings);
        }

        private static CaseResult unreachable(PathCase pathCase, String message) {
            return new CaseResult(pathCase, new ArrayList<String>(), 0, false, message, 0,
                    new ArrayList<SegmentTiming>());
        }

        private PathCase getPathCase() {
            return pathCase;
        }

        private long getSearchTimeNanos() {
            return searchTimeNanos;
        }

        private int getSegmentCount() {
            return segmentTimings.size();
        }

        private List<SegmentTiming> getSegmentTimings() {
            return segmentTimings;
        }

        private String getPathString() {
            if (!reachable) {
                return message;
            }
            return joinPath(path);
        }

        private String getCostString() {
            if (!reachable) {
                return "N/A";
            }
            return String.valueOf(totalCost);
        }
    }
}
