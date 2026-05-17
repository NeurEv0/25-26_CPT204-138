# Chapter 2 – Graph Algorithms (Task B)

## 2.1 Task Overview

After Task A, we obtain **30 inspection targets** (the top 10 by priority from datasets A, B, and C). Task B solves shortest paths on the **undirected weighted graph** defined in `paths.csv`: each edge has a positive integer weight, and nodes are labelled such as `L0001`. The graph has about **1000 nodes** and just over two thousand undirected edges. The 30 targets are only important nodes in the full graph; start, end, and waypoints for Cases 1–4 are chosen from them. **Search runs on the full graph**, not on a subgraph induced by those 30 nodes alone.

**Current implementation:** bidirectional Dijkstra in package `taskb.dijkstra_optimized` (`OptimizedDijkstraAlgorithm`). Run from the project root: `java -cp out taskb.dijkstra_optimized.TaskB`; results are written to `output/taskB_dijkstra_optimized_shortest_paths.txt`.

**Code layout:**


| Package / class            | File                              | Role                                    |
| -------------------------- | --------------------------------- | --------------------------------------- |
| `taskb.graph`              | `Graph.java`                      | Read CSV, build adjacency list          |
| `taskb.graph`              | `GraphIndex.java`                 | Node indexing, integer adjacency arrays |
| `taskb.graph`              | `DijkstraResult.java`             | Path, cost, reachability                |
| `taskb.dijkstra_optimized` | `OptimizedDijkstraAlgorithm.java` | Bidirectional Dijkstra core             |
| `taskb.dijkstra_optimized` | `TaskB.java`                      | Task A input, 4 cases, timing, export   |


---

## 2.2 Question 1: Which graph algorithm is used? Is it suitable for the weighted graph in this assignment?

### 2.2.1 What we use and why it fits

#### What we use

The program uses **bidirectional Dijkstra**, implemented in `OptimizedDijkstraAlgorithm` and invoked from `TaskB`.

- **Bidirectional search:** Relax edges outward from both the start and the end following Dijkstra’s rule. When the two frontiers **meet** at a node, we obtain the shortest-path length for that pair; the path is rebuilt from predecessor arrays. The main loop stops early when `forwardBest + backwardBest ≥ bestDistance`, and the side with the smaller heap top is expanded first so the two frontiers stay balanced.
- **Implementation notes:** `GraphIndex` maps nodes to integer ids; distances and predecessors are stored in `int[]`. One `OptimizedDijkstraAlgorithm` is created at startup; each segment query reuses buffers via `resetSearchState()`. The priority queues use a lazy strategy to skip stale heap entries (see §2.3).

#### Why it fits this assignment

Task B requires, on the network in `paths.csv`, the **shortest path, total cost, and search time** for four fixed route scenarios. Bidirectional Dijkstra aligns with the graph type, query shape, and program goals as follows.

**(1) Weighted undirected graph — suitable for Dijkstra’s family**

`paths.csv` describes **undirected** links between nodes with **positive integer** weights. In Case 2, the optimal route from L0001 to L0010 has cost 27, showing that the shortest route is determined by the **sum of edge weights**. Dijkstra (including bidirectional variants) requires non-negative weights; when a node is expanded after skipping stale heap entries, its distance is final, yielding an **exact optimum** on this data.

**(2) The assignment asks for shortest paths between given start and end — naturally source–sink queries**

All four cases reduce to several shortest-path queries from node A to node B:


| Case | Route (illustrative)            | Queries in the program               |
| ---- | ------------------------------- | ------------------------------------ |
| 1    | L0001 → L0001                   | 1 segment (same start/end, cost 0)   |
| 2    | L0001 → L0010                   | 1 segment                            |
| 3    | L0001 → L0101, via L0105        | 2 segments: L0001→L0105, L0105→L0101 |
| 4    | L0001 → L0201, via L0105, L0205 | 3 segments                           |


There are **7** point-to-point queries in total — **not** single-source shortest paths, and **not** a tour over all 30 inspection targets. Each call handles one start–end pair, which suits bidirectional Dijkstra.

**(3) Bidirectional search fits queries where both endpoints are known**

Each case specifies start and end (waypoints are split into point-to-point segments). Bidirectional Dijkstra expands from **both** ends until the frontiers meet, which helps fix the optimum length early and stop when pruning applies. With about 1000 nodes, this helps limit visited nodes in practice; measured total search time for 7 segments is about **0.443 ms** (§2.7), meeting the assignment’s timing requirement.

**(4) Search on the full graph, few queries — build once, query many times**

The 30 Task A targets only pick landmarks for the cases; shortest paths are computed on the **full graph of about 1000 nodes**. One query costs O(m log n) (§2.3.2), acceptable at this scale; `GraphIndex` is built once at startup and all seven segments reuse the same arrays and algorithm object.

**(5) No planar coordinates — no geometric heuristic**

`paths.csv` has no (x, y) coordinates, so Euclidean distance cannot be used as an O(1) geometric heuristic. Weighted bidirectional Dijkstra uses only edge weights and topology, matching the data format.

**(6) Matches required output**

`DijkstraResult` supplies the path sequence, total cost, and reachability; Case 1 returns cost 0 when start equals end. On non-negative weights the algorithm is optimal for shortest paths, satisfying the assignment.

**Summary:** The task is **a small number of source–sink shortest paths on a positively weighted undirected graph**. Bidirectional Dijkstra is appropriate in correctness, query shape, implementation cost, and measured runtime, and matches the four cases, seven segments, and timed output structure.

### 2.2.2 Quick comparison (table)


| Aspect     | This assignment                          | Bidirectional Dijkstra                           |
| ---------- | ---------------------------------------- | ------------------------------------------------ |
| Graph type | Undirected, positive integer weights     | Valid relaxations, optimal result                |
| Query type | 4 cases, 7 point-to-point shortest paths | Source–sink bidirectional search + early pruning |
| Graph size | ~1000 nodes, ~2000 edges                 | O(m log n) per query; index built once at start  |
| Data       | No node coordinates                      | No geometric heuristic; simple and reliable      |


### 2.2.3 Worth noting in the code

Within the bidirectional Dijkstra framework, the submitted code uses the following to support **one graph build and many point-to-point queries** (aligned with §2.3):


| Design                      | Purpose                                                                                |
| --------------------------- | -------------------------------------------------------------------------------------- |
| `GraphIndex`                | Integer node ids; array-backed neighbours for O(1) index access in search              |
| `int[]` distances / parents | Four arrays for bidirectional distances and predecessors                               |
| Reused algorithm object     | One `new OptimizedDijkstraAlgorithm` in `TaskB.main`; buffers shared across 7 segments |
| Lazy priority queues        | Skip stale entries on pop to avoid useless expansions                                  |
| `(long)` pruning test       | Sum heap-top distances in `long` to avoid `int` overflow in the stop test              |
| Case splitting + warmup     | `PathCase` splits routes; 2 warmup runs per segment, 3rd run timed                     |


---

## 2.3 Question 2: How is the algorithm implemented? What are the time and space complexities?

### 2.3.1 How the code is laid out

The pipeline has four layers: **build graph → integer index → bidirectional shortest-path search → case splitting and timing**.

#### (1) Read CSV and build the graph

Each row of `paths.csv` is `from_location,to_location,weight`. Undirected edges are stored **in both directions** in the adjacency list:

```java
adjacencyList.get(from).add(new Edge(to, weight));
adjacencyList.get(to).add(new Edge(from, weight));
```

Node ids remain `String` at this stage to match the CSV for debugging; hot-path search uses the integer index layer below, concentrating string cost in the **one-time** indexing step.

#### (2) Map nodes to integers

After loading the graph, `TaskB.main` builds the index and creates the algorithm object once:

```java
Graph graph = Graph.readFromCSV(PATHS_FILE);
GraphIndex index = GraphIndex.fromGraph(graph);
OptimizedDijkstraAlgorithm algorithm = new OptimizedDijkstraAlgorithm(index);
```

`GraphIndex` collects all `location_id` values, **sorts them lexicographically**, maps them to `0 … n-1`, and stores `neighborIds[i][]` and `neighborWeights[i][]` per node. Relaxation then uses array indices without repeatedly allocating `Edge` objects in the inner loop.

#### (3) Bidirectional Dijkstra search

**Structures (built once, shared by all 7 segments):** four arrays of length `n` — `distForward`, `distBackward`, `parentForward`, `parentBackward` — and two `PriorityQueue<HeapNode>` (node id and current distance).

**Steps for one query:**


| Step | Action                                                                            |
| ---- | --------------------------------------------------------------------------------- |
| 1    | Map start and end to integer ids; unreachable if either id is missing             |
| 2    | If start equals end, return cost 0                                                |
| 3    | `resetSearchState()`: distances to `INF`, parents to -1, clear both heaps         |
| 4    | Push start on forward heap and end on backward heap; set parents for backtracking |
| 5    | Run the bidirectional main loop (below)                                           |
| 6    | `buildPath` from the meeting node; wrap in `DijkstraResult`                       |


**Main loop:** While both heaps are non-empty, read heap-top distances `forwardBest` and `backwardBest`. If their sum is at least the current best full-path distance `bestDistance`, stop. Otherwise **expand the side with the smaller heap top** so the two frontiers stay aligned.

```java
while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {
    int forwardBest = forwardQueue.peek().distance;
    int backwardBest = backwardQueue.peek().distance;
    if ((long) forwardBest + (long) backwardBest >= bestDistance) break;
    if (forwardBest <= backwardBest)
        expandFrontier(forwardQueue, distForward, distBackward, parentForward, ...);
    else
        expandFrontier(backwardQueue, distBackward, distForward, parentBackward, ...);
}
```

`(long)` addition avoids overflow when summing two large `int` distances and misjudging the prune condition. `expandFrontier` reports whether a better meeting point was found; the main loop updates `bestDistance` and `meetingPoint`.

**Inside `expandFrontier`, three main steps:**

1. **Stale heap entries:** A node may be pushed several times. On pop, use `do { poll } while (distance > ownDistances[node])` to discard outdated entries before expanding.
2. **Meeting check:** If the current node was reached from the other side, try to improve the best meeting point using the sum of both distances.
3. **Relax neighbours:** Walk `index.neighbors` and weights; after a successful relaxation, if the neighbour is on the opposite frontier, check for a better full path there too.

**Path reconstruction (`buildPath`):** From the meeting node, follow `parentForward` to the start and reverse for the first half; follow `parentBackward` to the end. `locationOf` converts integer ids back to labels such as `L0001` for output.

#### (4) Case splitting and timing: `TaskB.solveCase`

Each assignment case is a `PathCase`. For example `new PathCase(3, a1, b1, b5)` yields route points `[L0001, L0105, L0101]` and calls `findShortestPath` on each consecutive pair:

```java
cases.add(new PathCase(3, a1, b1, b5));  // two segments: L0001→L0105, L0105→L0101
```

The first segment’s path is appended in full; later segments use `subList(1, size)` to drop the duplicate join node. Case total cost is the sum of segment costs. Timing: 2 warmup runs per segment, then the 3rd run is recorded, to reduce JVM cold-start bias on millisecond timings.

### 2.3.2 Time and space complexity

> Complexities are written in plain text (e.g. `O(m log n)`, `O(k * m log n)`), not LaTeX.

**Notation:** n = number of nodes, m = number of undirected edges in `paths.csv`. In the adjacency list each undirected edge appears twice; total traversal work remains O(n+m); we do not distinguish directed vs undirected storage below.

Complexity is split into **startup** (once), **one shortest-path query**, and **a case with multiple segments**.

#### At startup (runs once)


| Step                   | Time   | Space  | Notes                                  |
| ---------------------- | ------ | ------ | -------------------------------------- |
| `Graph.readFromCSV`    | O(m)   | O(n+m) | Read each edge into the adjacency list |
| `GraphIndex.fromGraph` | O(n+m) | O(n+m) | Build integer ids and array adjacency  |


Startup does not depend on the number of cases; all four cases and seven segments share one graph and index.

#### One query (`findShortestPath`)


|       | Complexity |
| ----- | ---------- |
| Time  | O(m log n) |
| Space | O(n)       |


**Time:** Bidirectional Dijkstra has the same asymptotic order as standard Dijkstra. Each edge is relaxed a constant number of times in the worst case; each relaxation may insert into the heap in O(log n), giving O(m log n). The `forwardBest + backwardBest ≥ bestDistance` prune reduces **actual** work (§2.7) but not worst-case Big-O.

**Space:** Four O(n) arrays plus two heaps that may hold O(n) entries each — still O(n) overall.

#### Multi-segment cases (e.g. Case 3, 4)

If a case is split into k segments (one `findShortestPath` per consecutive pair in `routePoints`):


|       | Complexity     |
| ----- | -------------- |
| Time  | O(k * m log n) |
| Space | O(n)           |


k is small here (7 segments in total). `OptimizedDijkstraAlgorithm` is constructed once in `main`; `resetSearchState()` reuses the same arrays and heaps — **no** k copies of O(n) structures.

---

## 2.4 Question 3: If every segment is shortest, is the whole inspection plan optimal?

### 2.4.1 Short answer first

**It depends on which problem is being asked.**

For Cases 1–4, each `findShortestPath(from, to)` returns the **shortest path on the full `paths.csv` graph** (minimum sum of edge weights) for that pair. For the subproblem **with only a given start and end**, every query is optimal.

If the problem is restricted to: **the assignment fixes the start, waypoint order, and end** (`routePoints` fixed), then the sum of segment shortest-path costs is the **global optimum under that route constraint** (Case 3 cost 39 and Case 4 cost 48 are examples).

If the problem is enlarged to: **reorder waypoints, choose which nodes to visit, or tour all 30 Task A targets with minimum total cost**, segment Dijkstra results **do not** imply overall optimality. These are different mathematical problems.

### 2.4.2 What the two levels mean


| Level                                    | Meaning in this project                                                               | Solved by the program?                                 |
| ---------------------------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------ |
| **Level A: segmented shortest paths**    | Shortest path between each consecutive pair on the prescribed route, then concatenate | Yes (`TaskB.solveCase` + `OptimizedDijkstraAlgorithm`) |
| **Level B: overall inspection planning** | e.g. visit multiple Task A targets in some order with minimum total travel cost       | No (not required by Task B, not implemented)           |


**Level A** is the classical **shortest path problem** (single pair, non-negative weights). Dijkstra and its bidirectional variant are optimal here.

**Level B**, if interpreted as visiting many nodes and returning to the start or fixing only the end, is closer to **TSP or constrained routing**: besides edge costs, one must choose **visit order**, which nodes to include, whether to revisit nodes, etc. That is not the same difficulty as one shortest-path query, and repeated Dijkstra runs do not automatically yield a global optimum.

Task A picks 10 high-priority targets per dataset (30 in total). Task B only uses some of them as endpoints and waypoints for Cases 1–4; it does **not** require visiting all 30 with minimum total cost.

### 2.4.3 What the program is really solving

`PathCase` turns an assignment route into an ordered list `routePoints`, e.g. Case 3:

```text
routePoints = [L0001, L0105, L0101]
               start  waypoint  end
```

`solveCase` calls shortest path on each adjacent pair `(routePoints[i], routePoints[i+1])`, concatenates paths (dropping the duplicate join node from the second segment onward), and sets **total cost = sum of segment costs**.

The program therefore solves:

> With **waypoint order fixed by the assignment**, minimize the path length between each consecutive pair of route points, then join the segments end to end.

On a graph with non-negative weights, any path from start through the waypoints in order to the end can be split into segments; total cost equals the sum of segment costs. For a **fixed-order** `routePoints`, shortest paths per segment and concatenation give the **global minimum** under that constraint; there is no extra freedom that requires joint optimization across segments.

### 2.4.4 When it is globally optimal — and when it is not

**(1) Fixed waypoint order: optimal segments ⇒ globally optimal route**

For Case 3 the program computes L0001→L0105 (22) and L0105→L0101 (17), total 39. Each segment is a shortest path on the full graph. Because the assignment requires visiting **L0105 in order**, any feasible path splits at L0105 into those two legs, so **the minimum total cost from L0001 via L0105 to L0101 equals the sum of the two segment shortest-path costs**; 39 is both the optimal segment sum and the global optimum under that fixed order. Case 4 (22+18+8=48) is analogous.

**(2) Task B only answers the four prescribed routes**

The program outputs shortest paths and costs for Cases 1–4 on `paths.csv`. It does **not** solve problems such as starting at one node, visiting all 30 high-priority Task A targets with minimum mileage. Those are tour/combinatorial problems (e.g. TSP), not the same as one or many shortest-path queries.

**(3) Task A’s 30 targets vs Task B routing are two layers of decisions**

Task A ranks which nodes matter for inspection; Task B finds shortest routes for given starts, waypoints, and ends. Even when all route nodes come from Task A, **which nodes matter** and **how to walk a given route as short as possible** are different questions; this program only addresses the latter.

### 2.4.5 Wrap-up

1. **Single query (guaranteed):** Each `findShortestPath(a, b)` is a shortest path from `a` to `b` on the graph — correctness of Dijkstra on non-negative weights.
2. **Case total cost (guaranteed, and globally optimal for the fixed route):** On the assignment’s `routePoints`, the sum of adjacent segment shortest-path costs matches the program output (e.g. Case 3: 39, Case 4: 48). With waypoint order fixed, that total is the **global optimum for that case’s route constraint** (see §2.4.4 (1)).
3. **Overall inspection planning (not guaranteed):** One cannot infer an optimal tour of all 30 targets, or global optimality when **waypoints may be reordered or the visit set is free**; see §2.4.4 (2)–(3) — mainly **different problem types** (Level A fixed-route shortest paths vs Level B combinatorial/tour planning), not a bug in the implementation.

---

## 2.5 Question 4: If the graph were unweighted, what alternatives could be considered? How do they compare with the current choice?

### 2.5.1 How unweighted and weighted graphs differ

- **Unweighted:** Each edge often has cost 1; shortest means **fewest edges**.
- **Weighted (this assignment):** Shortest means **minimum sum of edge weights**; edges may have different lengths, so hop count is not a valid proxy.

Using BFS and treating every edge as one step wrongly equates long and short edges; **results are generally wrong** on this data. With non-negative weights, a Dijkstra-style method (here, bidirectional Dijkstra) is required.

### 2.5.2 If the graph were unweighted

**Breadth-first search (BFS)**

- Idea: Expand in layers from the start; the **first time** a node is reached gives a shortest path in edge count.
- Pros: Simple queue, O(n+m) time, no log factor from a heap, small constants.
- Cons: Only for unweighted or uniform-weight graphs; not for arbitrary positive integers as in this assignment.

**Bidirectional BFS**

- Idea: Expand in layers from start and end; similar to bidirectional Dijkstra but meetings are by **hop count**, not sum of weights.
- Pros: Often smaller search region than one-way BFS for point-to-point queries; still O(n+m) scale.
- Cons: Assumes unit edge weight; wrong on a weighted graph.

**0–1 BFS (deque BFS)**

- Idea: For edges of weight only 0 or 1, use a deque to move between “current distance” and “current distance + 1” efficiently.
- Pros: Between BFS and Dijkstra in speed, O(n+m).
- Cons: Weights must be 0/1 only; **not** for arbitrary positive weights here; included for context.

### 2.5.3 Compared to what we use now

- **What is optimized:** Unweighted methods minimize **edge count**; this program minimizes **sum of weights**. The problem settings differ; BFS’s O(n+m) does not justify replacing Dijkstra on a weighted graph.
- **Implementation:** BFS uses a queue by layer; Dijkstra uses a priority queue for the smallest tentative distance, typically O(m log n). If data became unweighted, `GraphIndex` and the bidirectional framework could stay; heaps would become queues.

---

## 2.6 Question 5: If the graph were much larger or nodes had coordinates, what else could be used? Pros and cons?

### 2.6.1 When the graph gets huge

**Bidirectional Dijkstra (extension of current approach)**

- Pros: Still optimal for shortest paths; close to existing code.
- Cons: One query may still touch many nodes; latency grows with graph size; limited benefit when query count is small.

**A***

- Pros: With a good heuristic h, may expand fewer nodes than plain Dijkstra.
- Cons: Hard to design h without coordinates; precomputing distances to the goal adds per-query cost, poor fit when there are only a few queries.

**Contraction Hierarchies (CH), Hub Labeling, etc.**

- Pros: Very fast queries after heavy preprocessing; suited to many thousands of queries.
- Cons: Costly preprocessing and implementation; rebuild when the graph changes. With only 7 segments and n ≈ 1000, preprocessing is **not worthwhile**.

**Hierarchical / highway road networks**

- Pros: On large road networks with road classes, can trade a small amount of optimality for speed.
- Cons: Needs hierarchy such as arterials vs local roads; `paths.csv` has no such fields.

### 2.6.2 If nodes have coordinates (x, y)

*A with Euclidean straight-line distance as heuristic h**

- Pros: Fast h; on road networks embedded in the plane, Euclidean distance is often an admissible underestimate, guiding search toward the goal.
- Cons: Real routes may detour; h must not overestimate; slightly more complex than Dijkstra.

**Dijkstra + geographic pruning (e.g. sector limits)**

- Pros: Still graph-based; coordinates may shrink the searched region.
- Cons: Sensitive parameters; must prove no optimal path is missed — higher engineering risk.

**Visibility graphs, continuous motion planning**

- Pros: For obstacles and open terrain.
- Cons: Does not match a discrete node/edge CSV road network; a different problem class.

**Bidirectional Dijkstra (no coordinates)**

- Pros: Correct without coordinates, as in this project.
- Cons: May expand more nodes than coordinate-guided A*.

### 2.6.3 Back to our project

`paths.csv` has **no node coordinates**, about 1000 nodes, and only **7** segment queries. **Bidirectional Dijkstra + GraphIndex** is a good balance of correctness, implementation effort, and measured time (§2.7). If future data included coordinates and many queries, **A*** with Euclidean h would be a natural next step; if the graph grew to millions of nodes with huge query volume, consider **CH / Hub Labeling**.

---

## 2.7 Experimental Results

Results from `OptimizedDijkstraAlgorithm`, supporting §2.2 and the report Required output.

### Table 1: shortest-path results


| Case | Start | End   | Waypoints     | Total cost |
| ---- | ----- | ----- | ------------- | ---------- |
| 1    | L0001 | L0001 | —             | **0**      |
| 2    | L0001 | L0010 | —             | **27**     |
| 3    | L0001 | L0101 | L0105         | **39**     |
| 4    | L0001 | L0201 | L0105 → L0205 | **48**     |


### Table 2: search time (ms)


| Case               | Search time |
| ------------------ | ----------- |
| 1                  | 0.001       |
| 2                  | 0.141       |
| 3                  | 0.133       |
| 4                  | 0.169       |
| **Total (7 seg.)** | **0.443**   |


*Full paths: `output/taskB_dijkstra_optimized_shortest_paths.txt` or console output.*