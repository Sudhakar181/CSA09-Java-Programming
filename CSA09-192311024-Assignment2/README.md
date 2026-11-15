# CO2 Java Assignment 2 – Strings, Collections & Generics

**Student Name:** Sudhakar R  
**Register Number:** 192311024  
**Java Version:** JDK 17 (or JDK 8+)  
**Repository name:** `CSA09-192311024-Assignment2`

## Project contents
- `Assignment_Report.md` – complete design reasoning, predictions, complexity analyses, and experimental guides for Q1–Q7.
- `Q1/TicketBenchmark.java` – String vs StringBuilder vs StringBuffer benchmark across 3 workloads.
- `Q2/PlaylistBenchmark.java` – ArrayList vs LinkedList vs ArrayDeque benchmark at 3 sizes.
- `Q3/Attendance.java` & `Q3/AttendanceInvestigation.java` – completed `hashCode()`–`equals()` contract and 3 diagnostic tests.
- `Q4/PharmacyInventoryBenchmark.java` – HashMap vs TreeMap vs LinkedHashMap lookup and report generation benchmark.
- `Q5/LeaderboardDemo.java` – completed generic `Leaderboard<T>` and multi-field `Comparator` chain.
- `Q6/Triage.java` & `Q6/TriageExperiment.java` – completed `PriorityQueue` emergency triage and growth experiment.
- `Q7/TriageRedesignDemo.java` – Day-4 Design Challenge: 5-bucket `ArrayDeque` with aging vs `PriorityQueue`.

## How to compile and run

```bash
cd Q1 && javac TicketBenchmark.java && java TicketBenchmark
cd ../Q2 && javac PlaylistBenchmark.java && java PlaylistBenchmark
cd ../Q3 && javac Attendance.java AttendanceInvestigation.java && java Attendance && java AttendanceInvestigation
cd ../Q4 && javac PharmacyInventoryBenchmark.java && java PharmacyInventoryBenchmark
cd ../Q5 && javac LeaderboardDemo.java && java LeaderboardDemo
cd ../Q6 && javac Triage.java TriageExperiment.java && java Triage && java TriageExperiment
cd ../Q7 && javac TriageRedesignDemo.java && java TriageRedesignDemo
```

## Before submitting
1. Run all Java programs on your computer and record your actual measured timings in `Assignment_Report.md`.
2. Save output screenshots in the corresponding `Q1`–`Q7` folders.
3. Push progressive commits (at least one per task) to `CSA09-192311024-Assignment2`.
