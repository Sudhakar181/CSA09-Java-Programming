# CO3 Java Assignment 3 – Exception Handling & Multithreading

**Student A:** Sudhakar R (`192311024`)  
**Student B:** [Partner Name] (`[Partner Register Number]`)  
**Java Version:** JDK 17 (or JDK 8+)  
**Repository name:** `CSA09-192311024-Assignment3`

## Project contents
- `Assignment_Report.md` – complete answers for Q1–Q7 with Student A, Student B, Joint, and Alternative Concept sections.
- `Q1/AtmWithdrawalDemo.java` – ATM exception handling with multi-catch and `finally`.
- `Q2/StorageDemo.java` – completed exception chaining (`SubmissionStorageException`).
- `Q3/SalesReportGenerator.java` – manual `try-catch-finally` vs `try-with-resources`.
- `Q4/ScannerStationsDemo.java` – `Thread` vs `Runnable`, `start()` vs `run()`, `join()`, `isAlive()`, daemon threads.
- `Q5/SeatBooking.java` – completed synchronized cinema seat booking.
- `Q6/TokenCounter.java` & `Q6/WalletDeadlockDemo.java` – completed producer–consumer (`wait`/`notifyAll`) and deadlock-free lock ordering.
- `Q7/ResultProcessor.java` – concurrent fault-tolerant file processor using `ExecutorService`, `Callable`, `Future`, and `ConcurrentHashMap`.

## How to compile and run

```bash
cd Q1 && javac AtmWithdrawalDemo.java && java AtmWithdrawalDemo
cd ../Q2 && javac StorageDemo.java && java StorageDemo
cd ../Q3 && javac SalesReportGenerator.java && java SalesReportGenerator
cd ../Q4 && javac ScannerStationsDemo.java && java ScannerStationsDemo
cd ../Q5 && javac SeatBooking.java && java SeatBooking
cd ../Q6 && javac TokenCounter.java WalletDeadlockDemo.java && java TokenCounter && java WalletDeadlockDemo
cd ../Q7 && javac ResultProcessor.java && java ResultProcessor
```
