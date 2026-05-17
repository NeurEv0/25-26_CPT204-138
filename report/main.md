## Chapter 1 – Sorting Algorithm (Task A)

### 1.1 Overview

This CPT204 Coursework 3 project is an infrastructure inspection planning system for a specific city, designed to rapidly identify critical inspection targets within large-scale infrastructure networks and compute efficient inspection routes, which focuses on practicing Object-Oriented Programming (OOP), utilizing advanced data structures, and enhancing comprehensive skills in problem-solving, team cooperation, and EDI principles. 

Our system employs a **modular**, **Object-Oriented design** to ensure maintainability and scalability.


### 1.2 Algorithm Implementations

**Bubble Sort** iterates through adjacent pairs and swaps them if they are out of order, repeating until no swap occurs in a full pass. An early-termination flag (`swapped`) is used: if a complete pass produces no swaps, the array is already sorted and the algorithm exits immediately. This optimisation makes Bubble Sort particularly sensitive to — and fast on — nearly sorted input.

**Quick Sort** uses a two-pointer partitioning scheme with the **first element as pivot**. The left pointer advances to find an element larger than the pivot, the right pointer retreats to find one smaller, and the two are swapped. After partitioning, the pivot is placed at its correct position and the algorithm recurses on both sub-arrays. Choosing the first element as pivot is efficient for random data but degrades to O(n²) when the array is already sorted (or nearly sorted) in the target order, because every partition is heavily imbalanced.

**Merge Sort** divides the array in half recursively until sub-arrays have one element, then merges adjacent pairs into sorted order using temporary arrays. Its divide-and-conquer structure guarantees O(n log n) regardless of input order, at the cost of O(n) auxiliary memory for the temporary arrays.

### 1.3 Dataset Characteristics

Before analysing the timing results it is important to understand the properties of each dataset. The `DataAnalyzer` utility class performs a full structural analysis of each CSV file.

| Property | Dataset A | Dataset B | Dataset C |
|---|---|---|---|
| Total locations | 1,000 | 1,000 | 1,000 |
| Score range | 9,001 – 10,000 | 9,001 – 10,000 | 4,951 – 5,000 |
| Mean score | 9,500.50 | 9,500.50 | 4,970.99 |
| Unique scores | 1,000 | 1,000 | 41 |
| Score groups with ties | 0 | 0 | 41 (100% of data) |
| Inversion density (first 200) | 0.1% | 49.2% | 5.6% |
| Initial order | **Nearly sorted descending** (98.3%) | **Random / Unsorted** (~49% desc.) | **Perfectly sorted descending** (100%) |

**Dataset A** is already 98.3% in the target descending order with only 17 adjacent violations and no duplicate scores. It can be thought of as an almost-finished sort.

**Dataset B** has 49.1% descending sortedness and 49.2% inversion density in the first 200 elements — statistically indistinguishable from a uniformly random permutation. Scores are all unique and span the same range as Dataset A.

**Dataset C** is already in **perfect** descending score order (0 violations), but every one of its 1,000 locations shares one of only 41 distinct scores. This means tie-breaking by location ID is required within every score group, and the within-group order may not be sorted ascending, requiring additional work even though the score ordering is already correct.

### 1.4 Timing Results and Top-10 Selection

Each algorithm was run **3 times** per dataset using `System.nanoTime()` and the average was recorded. Results are shown below.

| Dataset | Bubble Sort | Quick Sort | Merge Sort | Top 10 Selected Locations |
|---|---|---|---|---|
| Dataset A | 420,533 ns (0.421 ms) | 7,709,766 ns (7.710 ms) | 787,366 ns (0.787 ms) | L0001, L0002, L0003, L0004, L0005, L0006, L0007, L0008, L0009, L0010 |
| Dataset B | 13,076,033 ns (13.076 ms) | 1,871,233 ns (1.871 ms) | 1,848,166 ns (1.848 ms) | L0101, L0102, L0103, L0104, L0105, L0106, L0107, L0108, L0109, L0110 |
| Dataset C | 1,798,766 ns (1.799 ms) | 3,637,733 ns (3.638 ms) | 298,233 ns (0.298 ms) | L0201, L0202, L0203, L0204, L0205, L0206, L0207, L0208, L0209, L0210 |

The top 10 locations and their priority scores are detailed below.

**Dataset A — Top 10:**

| Rank | Location ID | Priority Score |
|---|---|---|
| 1 | L0001 | 10,000 |
| 2 | L0002 | 9,999 |
| 3 | L0003 | 9,998 |
| 4 | L0004 | 9,997 |
| 5 | L0005 | 9,996 |
| 6 | L0006 | 9,995 |
| 7 | L0007 | 9,994 |
| 8 | L0008 | 9,993 |
| 9 | L0009 | 9,992 |
| 10 | L0010 | 9,991 |

**Dataset B — Top 10:**

| Rank | Location ID | Priority Score |
|---|---|---|
| 1 | L0101 | 10,000 |
| 2 | L0102 | 9,999 |
| 3 | L0103 | 9,998 |
| 4 | L0104 | 9,997 |
| 5 | L0105 | 9,996 |
| 6 | L0106 | 9,995 |
| 7 | L0107 | 9,994 |
| 8 | L0108 | 9,993 |
| 9 | L0109 | 9,992 |
| 10 | L0110 | 9,991 |

**Dataset C — Top 10:**

| Rank | Location ID | Priority Score |
|---|---|---|
| 1 | L0201 | 5,000 |
| 2 | L0202 | 5,000 |
| 3 | L0203 | 5,000 |
| 4 | L0204 | 5,000 |
| 5 | L0205 | 5,000 |
| 6 | L0206 | 5,000 |
| 7 | L0207 | 5,000 |
| 8 | L0208 | 5,000 |
| 9 | L0209 | 5,000 |
| 10 | L0210 | 5,000 |

The top 10 locations from Dataset C all share a priority score of 5,000 — the maximum in that dataset. Because all ties are resolved by ascending location ID, the program correctly selects L0201 through L0210 as the first ten IDs at that score level.

### 1.5 Analysis and Discussion

#### 1.5.1 How does the initial order of input data affect each algorithm?

The initial order has a profound and algorithm-specific effect on performance.

**Bubble Sort** benefits enormously from nearly sorted data. Dataset A is 98.3% sorted in the target order, so the early-termination flag fires after very few passes — the algorithm finishes in just 420,533 ns, making it the fastest on that dataset. Dataset B is random, so no early termination occurs and every pair may need swapping, pushing the time up to 13.1 ms (a 31× slowdown). Dataset C is perfectly sorted by score but has many tied scores whose IDs must be reordered within groups; those within-group inversions prevent full early termination and produce a 1.8 ms runtime.

**Quick Sort** is critically hurt by nearly sorted input when the first element is used as the pivot. In Dataset A, which is already almost fully sorted in descending order, the first element is almost always the largest remaining value. Nearly all other elements therefore fall into the right partition, creating maximally unbalanced sub-arrays and pushing the algorithm toward O(n²) behaviour — observed as a 7.7 ms runtime, by far its worst result. Dataset B is random, giving the pivot a statistically average split and producing a near-optimal 1.87 ms. Dataset C also triggers partial degradation (3.64 ms) because the sorted score order again front-loads larger values, and the heavy tie density further disrupts balanced partitioning.

**Merge Sort** is the least sensitive to initial order. Its divide-and-conquer structure always produces exactly ⌊n/2⌋ and ⌈n/2⌉ sub-arrays, independent of data values. The three runtimes — 0.787 ms, 1.848 ms, and 0.298 ms — reflect differences in merge-step work (more swaps on random data, fewer on ordered data) but never approach the worst-case degradation seen in the other two algorithms.

#### 1.5.2 Which algorithm performs best on each dataset?

**Dataset A — Bubble Sort (420,533 ns).** Because the data is already 98.3% in the correct descending order, the early-termination optimisation fires almost immediately. Bubble Sort completes in fewer than 20 effective passes, far outperforming the O(n log n) algorithms whose constant overhead becomes dominant.

**Dataset B — Merge Sort (1,848,166 ns) and Quick Sort (1,871,233 ns) are essentially tied.** For random input, both algorithms operate close to their O(n log n) average case. Quick Sort's cache-friendly in-place access gives it an edge in theory, but the 23,000 ns gap is negligible at this scale. Bubble Sort is 7× slower due to its O(n²) quadratic behaviour on unsorted data.

**Dataset C — Merge Sort (298,233 ns).** Despite the data being perfectly sorted by score, the 41 distinct score groups each require internal sorting by location ID. Merge Sort handles equal elements stably and efficiently within its merge step. Quick Sort degrades due to imbalanced pivot splits on equal-valued sequences. Bubble Sort must perform many within-group swaps and cannot fully exploit its early-exit flag, leaving it 6× slower than Merge Sort.

#### 1.5.3 Which algorithm behaves most consistently across the three datasets?

**Merge Sort** is the most consistent. Its three runtimes span from 0.298 ms to 1.848 ms — a ratio of about 6×, and the variation is attributable to natural differences in the volume of merge-step work, not algorithmic degradation. Bubble Sort varies by a factor of 31× (0.421 ms to 13.076 ms) and Quick Sort by 4× (1.871 ms to 7.710 ms), but Quick Sort's worst case (Dataset A) is already approaching quadratic. The theoretical guarantee of O(n log n) in all cases is what gives Merge Sort its stable profile.

#### 1.5.4 If only one sorting algorithm could be used in the final system, which would you choose?

The best single choice would be **Merge Sort**, and the justification depends on the evaluation criterion:

- *Best average runtime:* Quick Sort has a slightly lower average across the three datasets but its worst case (7.71 ms on Dataset A) is nearly 4× its best case, introducing unpredictable latency.
- *Most stable behaviour:* Merge Sort, as argued above.
- *Simplest implementation:* Bubble Sort is trivially simple but quadratic — unsuitable for production use with large datasets.

Merge Sort strikes the best balance: its O(n log n) guarantee eliminates worst-case surprises regardless of how future data arrives, its stability correctly handles tie-breaking (equal-score locations are kept in their original order before location-ID ordering is applied), and its performance is already competitive or best on two of the three datasets. For an infrastructure inspection system where reliability and predictability matter, algorithmic stability outweighs the slight average-case speed advantage Quick Sort offers on random data.

#### 1.5.5 If the number of candidate locations became significantly larger, which algorithm would be most suitable?

With significantly more locations (e.g., tens or hundreds of thousands), **Merge Sort or Quick Sort** with an improved pivot strategy would be appropriate; Bubble Sort must be ruled out entirely. At 10,000 locations, Bubble Sort's O(n²) would take roughly 100× longer than at 1,000 — extrapolating from Dataset B, this reaches around 1.3 seconds per sort, which is unacceptable in an interactive system.

Between the two O(n log n) options, **Quick Sort with randomised or median-of-three pivot selection** would be preferable for very large datasets due to its in-place nature (O(log n) stack space) and superior cache locality. The current first-element pivot strategy would need to be replaced to avoid worst-case degradation on sorted inputs. If stability is required (as it is here, to preserve tie-breaking determinism), Merge Sort remains the safer choice despite its O(n) auxiliary space cost.

#### 1.5.6 If both runtime efficiency and memory usage are considered, how would your algorithm choice be affected?

Factoring in memory narrows the choice to **Quick Sort (with improved pivot selection)**. Merge Sort allocates O(n) auxiliary arrays during the merge phase — at 1,000 locations this is negligible, but at millions of locations these temporary arrays can put significant pressure on the heap, potentially triggering garbage collection pauses in Java. Quick Sort is in-place, using only O(log n) stack space for recursion frames. However, the trade-off is stability: Quick Sort as implemented is not stable, so tie-breaking by location ID would rely solely on the `compareTo` comparison rather than insertion order. Since `compareTo` already encodes the location ID as a secondary key, correctness is maintained, but the developer must ensure this is always the case when the algorithm is modified or reused. In a memory-constrained environment, an in-place O(n log n) Quick Sort with randomised pivot is therefore the preferred solution.

## Chapter 2 – Graph Algorithm (Task B)

## Design of the Overall Application

## Project Reflection

## Program Code

## Appendix


## Contribution Form