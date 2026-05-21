# CPT204 Group 138 Presentation Script (Around 8 Minutes)

## Slide 1 – Title
Good morning everyone. We are Group 138, and our project is the Urban Infrastructure Inspection System. Today we will show how we rank high-priority inspection locations, how we compute efficient routes on a weighted city graph, and how object-oriented design connects these parts into one Java application.

## Slide 2 – Presentation Overview
This presentation has five parts. First, we will introduce the project and the roles of the three candidate datasets and the weighted graph. Next, we will explain Task A and how we select the top 10 locations from each dataset. Then we will discuss Task B and our shortest-path queries. After that, we will show the OOP design of the system. Finally, we will reflect on teamwork, EDI, and future improvement.

## Slide 3 – Project Introduction
The purpose of this project is to help inspectors identify important infrastructure targets and travel between them efficiently. We use three datasets, A, B, and C, each with 1,000 candidate locations. They have different data patterns: Dataset A is nearly sorted, Dataset B is random, and Dataset C contains many ties. These differences matter because they affect sorting behaviour.

The second input is `paths.csv`, a weighted city network with about 1,000 nodes and 2,600 edges. After Task A selects the top 10 locations from each dataset, Task B uses those 30 targets in route planning. However, the shortest paths are still searched on the full graph, not only on those 30 nodes.

## Slide 4 – Task A: Sorting Workflow
In Task A, every location is ranked by one shared rule: higher `priority_score` first, and if scores are equal, smaller `location_id` first. We implement this once in `Location.compareTo()`, so all sorting algorithms follow the same order.

To evaluate performance fairly, we load each dataset once, make fresh copies, and run Bubble Sort, Quick Sort, and Merge Sort three times each using `System.nanoTime()`. After sorting, we take the first 10 items as the selected targets. In the program, Quick Sort is used to generate the exported top-10 list, but from the evaluation we recommend Merge Sort for production use because it is more stable.

## Slide 5 – Task A: Timing Results and Top 10 Output
The timing results show that input order strongly affects performance. On Dataset A, which is 98.3 percent close to the target order, Bubble Sort is fastest at 0.421 milliseconds because early termination works very well. Quick Sort is worst there because its first-element pivot causes very unbalanced partitions.

On Dataset B, which is random, Bubble Sort slows down to 13.076 milliseconds, while Quick Sort and Merge Sort stay near `O(n log n)`. Merge Sort is slightly fastest at 1.848 milliseconds. On Dataset C, which has 41 tie groups, Merge Sort is again best at 0.298 milliseconds.

The final top 10 outputs are `L0001` to `L0010` for Dataset A, `L0101` to `L0110` for Dataset B, and `L0201` to `L0210` for Dataset C.

## Slide 6 – Task B: Graph Construction and Algorithm
In Task B, we first load `paths.csv` into an undirected weighted graph using an adjacency list. Then we build `GraphIndex`, which converts string IDs into integer indices and stores neighbours in arrays. This makes shortest-path search faster because the inner loop uses direct array access instead of repeated string lookups.

For routing, we use bidirectional Dijkstra. It searches from the start and the destination at the same time. When the two search frontiers meet, we get a candidate shortest path, and the algorithm can stop when no better path is possible. This fits our problem because all weights are positive and the graph has no coordinates, so A-star is not directly applicable.

## Slide 7 – Task B: Query Cases and Results
We define four required query cases from the Task A targets. Case 1 is from `L0001` to itself, so the path is trivial and the cost is 0. Case 2 is a direct query from `L0001` to `L0010`, and the total cost is 27.

Cases 3 and 4 include ordered waypoints, so we solve them one segment at a time. Case 3 is `L0001` to `L0105` to `L0101`. The first segment costs 22, the second costs 17, and after joining the two segment paths we get a final cost of 39.

Case 4 is `L0001` to `L0105` to `L0205` to `L0201`. Its three segment costs are 22, 18, and 8, so the total is 48. Across all seven legs, the total measured search time is about 1.228 milliseconds. This shows that bidirectional Dijkstra is efficient at this graph scale.

## Slide 8 – Task C: Application Architecture
Task C turns the project into one coherent application. `InspectionSystem.main()` is the single entry point. It runs Task A first and passes the result directly to Task B as a `Location[3][10]` array. This in-memory handoff means the two tasks are integrated as one system rather than two separate programs.

The architecture is layered: model classes represent data, I/O classes handle files, algorithm classes perform sorting and routing, and the app layer coordinates the workflow. This keeps responsibilities clear and reduces coupling.

## Slide 9 – Task C: OOP Principles in Practice
Several object-oriented principles appear throughout the system. Encapsulation is used in `Location` and `Edge`, where fields are private and accessed through methods. Abstraction appears in `CandidateLoader` and `GraphLoader`, which hide CSV parsing behind simple interfaces.

We also apply the Single Responsibility Principle. Each class has one main job, such as data loading, graph storage, sorting, shortest-path search, or workflow control. In addition, we prefer composition over deep inheritance: `InspectionSystem` combines reusable components instead of relying on a complex class hierarchy.

A key design choice is that `Location` implements `Comparable<Location>`, so the ranking rule is defined once and reused consistently by all three sorting algorithms.

## Slide 10 – Reflection: Planning, Collaboration, EDI, and Future Work
Our reflection focuses on planning, collaboration, EDI, and future work. In planning, we divided the project early into Task A, Task B, and later integration, which made progress easier to manage. In collaboration, we used GitHub and IntelliJ, reviewed each other’s work, and solved debugging problems together, especially for waypoint ordering.

For equality, diversity, and inclusion, we considered both the team and the system. Within the team, we aimed for fair contribution, shared speaking time, and clear credit for both members. In the software itself, we recognise that accessibility and fairness matter. Future versions could provide more accessible output, such as structured or audio-friendly results, and should also explain how priority scores are generated so possible bias can be examined.

For future development, we would use Merge Sort as the default production sorter, improve Quick Sort with a better pivot strategy, and extend the system with dynamic graph updates or a web API.

## Slide 11 – Summary
To conclude, our project combines ranking and routing in one integrated Java system. We evaluated sorting algorithms, selected the top 10 locations from each dataset, built a weighted graph, and solved the required shortest-path cases efficiently. The project also shows how OOP design, teamwork, and reflection support a practical smart-city application. Thank you for listening.
