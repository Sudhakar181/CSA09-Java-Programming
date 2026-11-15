# CO2 Java Assignment 2
## Strings, Regular Expressions, Collections Framework, Generics & Comparators

**Student Name:** Sudhakar R  
**Register Number:** 192311024  
**GitHub Link:** `https://github.com/<your-username>/CSA09-192311024-Assignment2`  
**Java Version:** JDK 17 (or JDK 8+)

> Run every program in `Q1`–`Q7` on your machine, paste your actual measured timings into the measurement tables below, and capture screenshots in each folder.

---

## Q1 – Building Text Under Load: String, StringBuilder or StringBuffer?

**Prediction:**
- `String` with `+` inside a loop will be the slowest and allocate the most memory as field count `n` and ticket count `T` grow. Because `String` is immutable, every `ticket = ticket + field` allocates a new `String` and copies all previously accumulated characters (`1 + 2 + ... + n` copies).
- `StringBuilder` and `StringBuffer` both use a mutable internal `char[]`/`byte[]` buffer that grows geometrically (doubling capacity), achieving `O(n)` amortized time per ticket.
- `StringBuilder` will be slightly faster than `StringBuffer` because `StringBuffer` synchronizes every `append()` call, adding monitor-acquisition overhead even when used by a single thread.

**Workloads (`Q1/TicketBenchmark.java`):**
1. **Workload 1 (Single Short Ticket):** 1 ticket, 8 fields — negligible difference (< 1 ms for all three).
2. **Workload 2 (Bulk Standard Tickets):** 10,000 tickets, 50 fields each — `String (+)` creates ~1,000,000 intermediate `String` objects and exhibits quadratic per-ticket copying; `StringBuilder` creates 10,000 final `String`s and finishes in a few milliseconds.
3. **Workload 3 (Heavy Detailed Tickets):** 5,000 tickets, 120 fields each — `String (+)` degrades sharply while `StringBuilder` stays linear.

**Design Decision (Multithreaded Booking Server):**
- **Per-ticket text:** Use `StringBuilder` as a local variable inside the ticket-generation method. Local variables live on each thread's stack, so no other thread can access them and synchronization is unnecessary.
- **Shared booking log:** Use `StringBuffer` (or a `synchronized` / `ConcurrentLinkedQueue` log writer) because multiple threads append concurrently to the same shared object; `StringBuilder` would suffer race conditions and corrupted indices.

**Alternative Concept and Complexity Analysis (`String.join()`, `StringJoiner`, `String.format()`):**
- `String.join(" | ", fields)` and `StringJoiner` internally use a `StringBuilder` (or `String` array sizing in Java 8+) in `O(n)` time and `O(n)` space—clean and readable when all fields are already in a collection/array.
- `String.format()` parses a format string with regex on every call, making it significantly slower (`O(n)` with high constant factor and extra `Formatter` allocations).
- **Complexity Comparison (1 ticket of `n` fields):**
  - Repeated `+`: Time `O(n^2)` character copies; Extra space `O(n^2)` total garbage allocated across `O(n)` intermediate `String` objects.
  - `StringBuilder` / `String.join`: Time `O(n)`; Extra space `O(n)` for the single internal buffer.

---

## Q2 – When Is ArrayList an Appropriate Engineering Choice?

**Experimental Design (`Q2/PlaylistBenchmark.java`):**
Tested at `n = 1,000`, `10,000`, and `50,000` songs for: adding at end, front insert/remove, middle insert/remove, 1,000 random `get(k)` calls, and full iteration.

**Operating Boundary:**
- `ArrayList` is the right engineering choice when the workload is dominated by **appending at the end**, **random access (`get(k)`)**, and **sequential iteration** (contiguous array gives CPU cache locality and `O(1)` indexing).
- Stop recommending `ArrayList` when the workload performs frequent **insertions or deletions near the front or middle** of large lists (`n >= 10,000`), because `System.arraycopy` shifts `O(n)` elements per operation.

**Changed Condition ("Up Next" Queue):**
- For adding at both ends and removing from the front, `ArrayDeque<Song>` is more appropriate than both `ArrayList` (which has `O(n)` `remove(0)`) and `LinkedList` (which allocates a `Node` object per element and has poor cache locality). `ArrayDeque` uses a resizable circular array with `O(1)` amortized `addFirst`, `addLast`, and `removeFirst`.

**Alternative Concept and Complexity Analysis:**

| Operation | `ArrayList` | `LinkedList` | `ArrayDeque` | Plain Array (Manual Shift) |
|---|---|---|---|---|
| Add at end | `O(1)` amortized | `O(1)` | `O(1)` amortized | `O(1)` if capacity remains, `O(n)` resize |
| Insert at front | `O(n)` | `O(1)` | `O(1)` amortized | `O(n)` |
| Insert in middle | `O(n)` | `O(n)` (traversal) | Not supported | `O(n)` |
| `get(k)` | `O(1)` | `O(n)` | Not supported | `O(1)` |
| Iterate all | `O(n)` (fast, cache-friendly) | `O(n)` (pointer chasing) | `O(n)` | `O(n)` |
| Space per element | 1 reference (4–8 B) | Node + 2 refs (~24–32 B) | 1 reference (4–8 B) | 1 reference (4–8 B) |

---

## Q3 – Engineer the hashCode–equals Contract

**Three Designed Tests (`Q3/AttendanceInvestigation.java`):**
1. **Test 1 (Appears to work):** Adding the exact same `Student` reference twice (`arun`, `divya`, `arun`) to `HashSet`. *Prediction:* Size = 2, because the same object has the same default `Object.hashCode()` and `this == o` returns `true`.
2. **Test 2 (Exposes duplicates):** Adding `new Student("192311001", "Arun")` twice as distinct objects without overriding `hashCode()`. *Prediction:* Size = 3 (bug!), because `Object.hashCode()` returns different memory-based hashes, placing them in different buckets so `equals()` is never called.
3. **Test 3 (Mutation after insertion):** Inserting a `Student` with `"192311001"`, then changing `regNo = "192311099"`. *Prediction:* `contains()` returns `false` for both `"192311099"` (wrong bucket) and `"192311001"` (`equals()` fails).

**Inquiry Answers:**
- **How `HashSet` locates elements:** It computes `hashCode()` to find the bucket index, then uses `equals()` only on entries inside that bucket.
- **Why duplicates appear:** If `a.equals(b)` is `true` but `a.hashCode() != b.hashCode()`, `HashSet` looks in different buckets and treats `a` and `b` as distinct.
- **Fix (`Q3/Attendance.java`):** Override `hashCode()` using `Objects.hash(regNo)` and keep `regNo` `final` (immutable). Before TODO 1, `Attendance` prints `Unique students: 3`; after TODO 1, it prints `Unique students: 2`.

**Alternative Concept and Complexity Analysis (`TreeSet<Student>` vs `HashMap<String, Student>`):**
- `TreeSet<Student>` with `Comparator.comparing(s -> s.regNo)`: Time per scan is `O(log n)` (red-black tree traversal); Space is `O(n)` tree nodes (~32–40 bytes overhead per node). Does not depend on `hashCode()`, and keeps students sorted.
- `HashMap<String, Student>` keyed by `regNo`: Time per scan is `O(1)` average; Space is `O(n)` entries + `String` key references.
- `HashSet<Student>`: Time per scan is `O(1)` average; Space is `O(n)` (backed internally by a `HashMap<Student, Object>`).

---

## Q4 – Choosing a Map for a Pharmacy Inventory

**Pre-Implementation Analysis:**

| Map | `put` | `get` | Iteration | Natural Report |
|---|---|---|---|---|
| `HashMap` | `O(1)` avg | `O(1)` avg | `O(n + capacity)` | Neither (unordered) |
| `TreeMap` | `O(log n)` | `O(log n)` | `O(n)` | Code-order report |
| `LinkedHashMap` | `O(1)` avg | `O(1)` avg | `O(n)` | Insertion-order report |

- If the "wrong" map is chosen (e.g., `HashMap` for code-order report), extra `O(n log n)` sorting of keys (`Collections.sort`) and `O(n)` list space are required every time the report is generated.

**Scalability Inquiry (`Q4/PharmacyInventoryBenchmark.java`):**
- **Acceptable lookup criterion:** `< 1 microsecond` (`0.001 ms`) per lookup (i.e., `< 100 ms` for 100,000 lookups).
- **Deployment choice:** `LinkedHashMap` for primary stock lookups + insertion-order evening report, or `HashMap` for fastest `O(1)` lookups if insertion-order is tracked separately. Since reports run only once per evening, sorting `n` keys (`O(n log n)`) once a day is cheaper than paying `O(log n)` on hundreds of daily lookups with `TreeMap`.
- **Remaining cost:** Hash computation (`String.hashCode()`), bucket array indexing, and CPU cache misses on large tables.
- **Why not `Hashtable`?** Every method in `Hashtable` is `synchronized`, causing unnecessary lock contention in single-threaded or read-heavy workloads, and it does not permit `null` keys/values. Use `HashMap` or `ConcurrentHashMap`.

**Alternative Concept (`Sorted Array + Arrays.binarySearch()`):**
- **Lookup:** `O(log n)` with good cache locality (faster than `TreeMap` for static data).
- **Insertion:** `O(n)` because elements must be shifted to keep the array sorted (much worse than `HashMap`'s `O(1)` or `TreeMap`'s `O(log n)`).
- **Code-order report:** `O(n)` direct scan.
- **Space:** `O(n)` contiguous references (lowest memory overhead, no `Map.Entry` or tree nodes).

---

## Q5 – Derive a Generic Ranking Component from a Principle

**Design Reasoning:**
- **Total-Order Principle:** A comparator `cmp` must be antisymmetric (`sgn(cmp(a,b)) == -sgn(cmp(b,a))`), transitive (`a > b` and `b > c` implies `a > c`), and consistent with equality.
- **`Comparable` vs `Comparator`:** Separate `Comparator` objects are better because `Leaderboard<T>` can rank any type (`Team`, `Player`, `College`) without modifying those classes, and multiple ranking rules can coexist.
- **Bounded Generic & Wildcard (`Q5/LeaderboardDemo.java`):** `Leaderboard<T>` accepts `Comparator<? super T>` (contravariant consumer) so a `Comparator<Object>` or `Comparator<Competitor>` works for `Team`. A method `addAll(List<? extends T> batch)` (covariant producer) allows adding `List<FootballTeam>` into `Leaderboard<Team>`.
- **Multi-field Comparator (TODO 2):**
  ```java
  Comparator<Team> byRank = Comparator
      .comparingInt((Team t) -> t.points).reversed()
      .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
      .thenComparing(t -> t.name);
  ```
- **Why Bisons ranks above Panthers:** Both have `points = 9` and `goalDiff = 4`, so the third tie-breaker (`t -> t.name`) compares `"Bisons"` vs `"Panthers"` alphabetically; `'B' < 'P'`, so `Bisons(9,4)` precedes `Panthers(9,4)`.
- **Why subtraction fails:** `b.points - a.points` can overflow `int` when comparing `Integer.MAX_VALUE` and `-10`, violating transitivity. `Comparator.comparingInt` uses `Integer.compare`, which never overflows.

**Alternative Concept and Complexity Analysis:**
- **`Comparable<Team>`:** Ties `Team` to one fixed natural order; requires `1` sort of `O(n log n)` and `O(n)` copy space, but cannot easily rank `Player` or `College` without duplicating `Leaderboard`.
- **Three-pass stable sort (name -> goalDiff -> points):** Runs `3` separate `O(n log n)` sorts (`3 * O(n log n)` = `O(n log n)` asymptotic, ~3x constant factor) and relies on TimSort stability. Single-pass `Comparator` chain is cleaner, faster, and reusable across any type `T`.

---

## Q6 – Priority, Order and Fairness in a Queue

**Design Reasoning (`Q6/Triage.java`):**
- A plain `Queue` (`LinkedList` / `ArrayDeque`) is strictly FIFO and ignores severity, so a critical S1 patient would wait behind S5 patients.
- `PriorityQueue<Patient>` orders elements using a binary min-heap with `Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival)`.
- **Arrival counter:** Each `admit()` assigns `counter++`. When two patients have the same severity, the smaller `arrival` value is served first, preserving FIFO order.
- **Why printing `PriorityQueue` directly is not in treatment order:** `PriorityQueue.toString()` iterates the internal heap array (`heap[0..n-1]`), which only satisfies the heap property (parent <= children), not full sorted order. Only repeated `poll()` extracts elements in treatment order.

**Growth Investigation (`Q6/TriageExperiment.java`):**
- **Prediction:** Each `add()` and `poll()` takes `O(log n)` time (sifting up/down a binary heap of height `log2(n)`), so `n` operations take `O(n log n)`.
- **Starvation & Fair Rule:** If S1–S4 patients arrive continuously, an S5 patient never reaches the heap root. **Fairness rule (Aging):** Track waiting ticks/minutes; if a patient waits longer than `maxWait` (e.g., 30 minutes), promote their effective severity by 1 level (or use `effectiveSeverity = max(1, severity - waitTime / 30)`).
- **Deployment Decision (5,000 patients/day):** `5,000` operations at `O(log 5000) ≈ 13` comparisons each takes `< 5 ms` total—easily fast enough for a city hospital, provided aging is added to prevent S5 starvation.

**Alternative Concept and Complexity Analysis:**

| Approach | `admit()` Time | `next()` Time | Extra Space |
|---|---|---|---|
| `PriorityQueue` (binary heap) | `O(log n)` | `O(log n)` | `O(n)` array |
| Sorted `LinkedList` | `O(n)` | `O(1)` | `O(n)` + node pointers |
| Array of 5 FIFO `ArrayDeque`s | `O(1)` | `O(1)` (scan 5 buckets) | `O(n)` + 5 deque headers |

---

## Q7 – Day-4 Design Challenge: Diagnose, Redesign and Validate

**Selected Limitation:** Q6 (`PriorityQueue`-based Emergency Triage).

**A. Diagnose the Bottleneck:**
- **Where time/work is spent:** Every `admit()` and `poll()` performs `O(log n)` heapify comparisons and array swaps, even though severity is bounded to just 5 integers (`1..5`).
- **Functional limitation:** Pure static-severity `PriorityQueue` causes **starvation** for S5 patients under continuous S1–S2 load.

**B. Define the Design Objective:**
1. Reduce `admit()` and `next()` complexity from `O(log n)` to **`O(1)`** per operation.
2. Eliminate indefinite starvation of minor (S5) patients via bounded **aging promotion**.

**C. Design the Revised Solution (`Q7/TriageRedesignDemo.java`):**
- **What changed:** Replace the single `PriorityQueue<Patient>` with an array of 5 FIFO `ArrayDeque<Patient>` buckets (`buckets[1..5]`) plus an `advanceTick()` aging check.
- **Property exploited:** Severity has a tiny fixed domain (`1..5`), so bucket indexing is `O(1)` and each bucket naturally preserves FIFO arrival order without needing an `arrival` tie-breaker in a heap.
- **Pseudocode:**
  ```text
  PROCEDURE admit(patient)
      buckets[patient.severity].addLast(patient)
  END PROCEDURE

  PROCEDURE next()
      FOR s FROM 1 TO 5
          IF NOT buckets[s].isEmpty() THEN
              RETURN buckets[s].pollFirst()
          END IF
      END FOR
      RETURN NULL
  END PROCEDURE
  ```
- **Trade-offs:** Only works when priority levels are small discrete integers (`1..5`); if priorities were arbitrary continuous doubles, a heap (`PriorityQueue`) would still be needed.

**D. Predict Before Implementing:**
- At `n = 100,000`, the 5-bucket `ArrayDeque` will be noticeably faster than `PriorityQueue` (`O(n)` total vs `O(n log n)`), and `Asha(S5)` will be promoted and served rather than starving behind 20 consecutive S1 arrivals.

**E. Implement and Validate (`Q7/TriageRedesignDemo.java`):**
- Run `TriageRedesignDemo` and record both the fairness output (position at which `Asha` is served) and the `n = 100,000` timing comparison.

**F. Defend the Final Design:**
- The 5-bucket `ArrayDeque` achieves `O(1)` per operation, eliminates heap comparisons, and solves starvation with `advanceTick()`. The original `PriorityQueue` remains preferable only when priority values are unbounded or continuous (e.g., floating-point risk scores).
