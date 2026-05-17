package taskb.dijkstra_optimized;

import taskb.graph.DijkstraResult;
import taskb.graph.GraphIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Bidirectional Dijkstra with integer node ids, array-backed distances/parents,
 * reusable buffers, and stale heap entry skipping.
 */
public class OptimizedDijkstraAlgorithm {

    private static final int INF = Integer.MAX_VALUE / 4;

    private final GraphIndex index;
    private final int[] distForward;
    private final int[] distBackward;
    private final int[] parentForward;
    private final int[] parentBackward;
    private final PriorityQueue<HeapNode> forwardQueue;
    private final PriorityQueue<HeapNode> backwardQueue;

    public OptimizedDijkstraAlgorithm(GraphIndex index) {
        int n = index.size();
        this.index = index;
        this.distForward = new int[n];
        this.distBackward = new int[n];
        this.parentForward = new int[n];
        this.parentBackward = new int[n];
        this.forwardQueue = new PriorityQueue<HeapNode>();
        this.backwardQueue = new PriorityQueue<HeapNode>();
    }

    public DijkstraResult findShortestPath(String start, String destination) {
        int startId = index.idOf(start);
        int destId = index.idOf(destination);

        if (startId < 0 || destId < 0) {
            return new DijkstraResult(new ArrayList<String>(), INF, false);
        }

        if (startId == destId) {
            List<String> path = new ArrayList<String>();
            path.add(start);
            return new DijkstraResult(path, 0, true);
        }

        resetSearchState();

        distForward[startId] = 0;
        distBackward[destId] = 0;
        parentForward[startId] = startId;
        parentBackward[destId] = destId;
        forwardQueue.add(new HeapNode(startId, 0));
        backwardQueue.add(new HeapNode(destId, 0));

        int bestDistance = INF;
        int meetingPoint = -1;

        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
            int forwardBest = forwardQueue.peek().distance;
            int backwardBest = backwardQueue.peek().distance;
            if ((long) forwardBest + (long) backwardBest >= bestDistance) {
                break;
            }

            if (forwardBest <= backwardBest) {
                MeetingUpdate update = expandFrontier(
                        forwardQueue, distForward, distBackward, parentForward, true);
                if (update.meetingPoint >= 0 && update.totalDistance < bestDistance) {
                    bestDistance = update.totalDistance;
                    meetingPoint = update.meetingPoint;
                }
            } else {
                MeetingUpdate update = expandFrontier(
                        backwardQueue, distBackward, distForward, parentBackward, false);
                if (update.meetingPoint >= 0 && update.totalDistance < bestDistance) {
                    bestDistance = update.totalDistance;
                    meetingPoint = update.meetingPoint;
                }
            }
        }

        if (meetingPoint < 0) {
            return new DijkstraResult(new ArrayList<String>(), INF, false);
        }

        List<String> path = buildPath(startId, destId, meetingPoint);
        return new DijkstraResult(path, bestDistance, true);
    }

    private void resetSearchState() {
        Arrays.fill(distForward, INF);
        Arrays.fill(distBackward, INF);
        Arrays.fill(parentForward, -1);
        Arrays.fill(parentBackward, -1);
        forwardQueue.clear();
        backwardQueue.clear();
    }

    private MeetingUpdate expandFrontier(
            PriorityQueue<HeapNode> queue,
            int[] ownDistances,
            int[] oppositeDistances,
            int[] previous,
            boolean isForward) {

        HeapNode current;
        do {
            if (queue.isEmpty()) {
                return MeetingUpdate.none();
            }
            current = queue.poll();
        } while (current.distance > ownDistances[current.nodeId]);

        int meetingPoint = -1;
        int bestTotal = INF;

        int oppositeAtCurrent = oppositeDistances[current.nodeId];
        if (oppositeAtCurrent < INF) {
            long total = (long) current.distance + (long) oppositeAtCurrent;
            if (total < bestTotal) {
                bestTotal = (int) total;
                meetingPoint = current.nodeId;
            }
        }

        int[] neighbors = index.neighbors(current.nodeId);
        int[] weights = index.weights(current.nodeId);
        for (int i = 0; i < neighbors.length; i++) {
            int neighborId = neighbors[i];
            int newDistance = current.distance + weights[i];
            if (newDistance >= ownDistances[neighborId]) {
                continue;
            }

            ownDistances[neighborId] = newDistance;
            previous[neighborId] = current.nodeId;
            queue.add(new HeapNode(neighborId, newDistance));

            int oppositeAtNeighbor = oppositeDistances[neighborId];
            if (oppositeAtNeighbor < INF) {
                long total = (long) newDistance + (long) oppositeAtNeighbor;
                if (total < bestTotal) {
                    bestTotal = (int) total;
                    meetingPoint = neighborId;
                }
            }
        }

        return new MeetingUpdate(meetingPoint, bestTotal);
    }

    private List<String> buildPath(int startId, int destId, int meetingPoint) {
        List<String> path = new ArrayList<String>();

        int current = meetingPoint;
        while (current != startId) {
            path.add(index.locationOf(current));
            current = parentForward[current];
            if (current < 0) {
                return new ArrayList<String>();
            }
        }
        path.add(index.locationOf(startId));
        reverseInPlace(path);

        current = meetingPoint;
        int next = parentBackward[current];
        while (next != destId) {
            if (next < 0) {
                return new ArrayList<String>();
            }
            path.add(index.locationOf(next));
            current = next;
            next = parentBackward[current];
        }
        path.add(index.locationOf(destId));
        return path;
    }

    private static void reverseInPlace(List<String> list) {
        for (int i = 0, j = list.size() - 1; i < j; i++, j--) {
            String tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private static final class HeapNode implements Comparable<HeapNode> {
        private final int nodeId;
        private final int distance;

        private HeapNode(int nodeId, int distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(HeapNode other) {
            int cmp = Integer.compare(this.distance, other.distance);
            return cmp != 0 ? cmp : Integer.compare(this.nodeId, other.nodeId);
        }
    }

    private static final class MeetingUpdate {
        private final int meetingPoint;
        private final int totalDistance;

        private MeetingUpdate(int meetingPoint, int totalDistance) {
            this.meetingPoint = meetingPoint;
            this.totalDistance = totalDistance;
        }

        private static MeetingUpdate none() {
            return new MeetingUpdate(-1, INF);
        }
    }
}
