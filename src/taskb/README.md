# Task B – Optimized Bidirectional Dijkstra

## Compile (project root)

```bat
javac -encoding UTF-8 -d out src\main\*.java src\taskb\graph\*.java src\taskb\dijkstra_optimized\*.java src\taskb\TaskBRunner.java src\utils\*.java
```

## Run

Run Task A first to generate `output/taskA_top30_targets.csv`, then:

```bat
java -cp out taskb.dijkstra_optimized.TaskB
```

Output: `output/taskB_dijkstra_optimized_shortest_paths.txt`
