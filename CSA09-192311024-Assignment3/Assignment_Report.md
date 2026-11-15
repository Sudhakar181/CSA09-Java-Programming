# CO3 Java Assignment 3
## Exception Handling & Multithreading

**Student A:** Sudhakar R (`192311024`)  
**Student B:** [Partner Name] (`[Partner Register Number]`)  
**GitHub Link (Student A):** `https://github.com/<your-username>/CSA09-192311024-Assignment3`  
**Java Version:** JDK 17 (or JDK 8+)

> Both students must execute every program in `Q1`–`Q7` in their own repository and be ready to explain all sections during the viva.

---

## Individual Contribution Record

| Question | Student A – Primary Responsibility | Student B – Primary Responsibility | Joint |
|---|---|---|---|
| Q1 | Exception Design | Testing & Tracing | Single `catch (Exception e)` analysis |
| Q2 | Exception Hierarchy & Propagation Design | Failure Scenarios & Chaining Evidence | Meaningful `SubmissionStorageException` defence |
| Q3 | Debugging & Experimental Analysis | Design & Correctness | Limitations of `try-with-resources` |
| Q4 | Thread Design | Behavioural Experiments | Non-determinism & thread-order independence |
| Q5 | Producing & Explaining the Race | Comparing Synchronization Strategies | 20-screen deployment strategy |
| Q6 | Producer–Consumer Design | Deadlock Analysis & Prevention | Shared progress guarantees |
| Q7 | Stage 2 – ExecutorService & Collection Design | Stage 3 – Implementation & Benchmarking | Stage 1 & Stage 4 |

---

## Q1 – Handling Failure in a Bank ATM Simulation

### Student A – Exception Design
**Relevant Throwable Hierarchy:**
```text
Throwable
 ├── Error (e.g., OutOfMemoryError - not caught)
 └── Exception
      ├── IOException (checked) -> FileNotFoundException (missing card-reader file)
      └── RuntimeException (unchecked)
           ├── InputMismatchException (letters entered instead of numbers)
           ├── IllegalArgumentException (negative/zero withdrawal)
           └── InsufficientBalanceException (custom unchecked: amount > balance)
```

| Failure Point | Exception Type | Checked / Unchecked | Where Caught |
|---|---|---|---|
| Card-reader file missing | `FileNotFoundException` | Checked | `catch (FileNotFoundException e)` in `main` |
| Non-numeric input | `InputMismatchException` | Unchecked | Multi-catch `InputMismatchException \| IllegalArgumentException` |
| Negative/zero amount | `IllegalArgumentException` | Unchecked | Multi-catch `InputMismatchException \| IllegalArgumentException` |
| Amount > balance | `InsufficientBalanceException` | Unchecked | `catch (InsufficientBalanceException e)` |

- **Order of catch blocks:** Specific/subclass exceptions (`FileNotFoundException`, `InputMismatchException | IllegalArgumentException`) must precede broader ones (`Exception`), otherwise the compiler rejects the narrower catch as unreachable.
- **What `finally` guarantees:** `sc.close()` and session cleanup always execute whether the withdrawal succeeds, throws a handled exception, or returns early.

### Student B – Testing and Tracing (`Q1/AtmWithdrawalDemo.java`)
- **Test inputs:** `500` (valid), `-100` (`IllegalArgumentException`), `5000` (`InsufficientBalanceException`), `"abc"` (`InputMismatchException`), missing card-reader path (`FileNotFoundException`). In all 5 cases, `finally` executes.
- **Broader catch before narrower:** Placing `catch (Exception e)` before `catch (FileNotFoundException e)` causes a compile-time error: `exception FileNotFoundException has already been caught`.

### Joint Design Question
Catching `Exception` in a single block is acceptable only at a top-level crash boundary (e.g., logging an unexpected failure before exiting). Doing it inside business logic loses error specificity, makes targeted recovery impossible, and accidentally swallows programming bugs like `NullPointerException`.

### Alternative Concept & Complexity Analysis
Using `Scanner.hasNextInt()` and returning `-1` avoids exception-object creation (`O(1)` return vs `O(depth)` stack-trace capture for exceptions), which saves time and memory when invalid inputs are frequent. However, it clutters normal logic with nested `if` checks and forces callers to remember magic error codes (`-1`, `-2`) that can be silently ignored.

---

## Q2 – User-Defined Exceptions and Propagation in an Online Examination Portal

### Student A – Exception Hierarchy & Propagation Design
```text
Exception (checked)
 └── ExamException
      ├── ExamWindowClosedException (checked: business rule)
      ├── StudentNotRegisteredException (checked: business rule)
      └── SubmissionStorageException (checked: wraps IOException)
```
- **Why checked:** All three represent recoverable domain/I/O failures that the controller must handle and translate into clear messages for the student.
- **Propagation path:** `Storage.save()` catches `IOException` and throws `SubmissionStorageException` -> `SubmissionService.submit()` declares `throws ExamException` -> `ExamController` catches `ExamException` and displays a friendly error to the student while logging `getCause()`.

### Student B – Failure Scenarios & Chaining Evidence (`Q2/StorageDemo.java`)
- **Completed TODO 1:** `throw new SubmissionStorageException("Could not store " + fileName, e);`
- **Output:** `Saved answer01.pdf` / `Error: Could not store answer02.pdf` / `Cause: Disk quota exceeded`.
- **If `e` is not passed to constructor:** `e.getCause()` returns `null`, causing `e.getCause().getMessage()` to throw a `NullPointerException`, and the root cause (`Disk quota exceeded`) is lost.

### Joint Conclusion
Letting raw `IOException` reach the UI leaks internal storage details (paths, disk quotas) and couples the UI to `java.io`. Wrapping it in `SubmissionStorageException` preserves abstraction while keeping the root cause for server logs.

### Alternative Concept & Complexity Analysis
If `ExamException` extended `RuntimeException` (unchecked), `throws` clauses would disappear from method signatures and callers would not be forced by the compiler to handle them. Stack-trace capture costs `O(stack_depth)` time and memory per thrown exception; chaining adds one extra object reference (`cause`) and preserves both traces.

---

## Q3 – Resource Safety, Debugging and Robust Code

### Student B – Design and Correctness (`Q3/SalesReportGenerator.java`)
- **Resources opened:** One `BufferedWriter` (summary file) and `f` `BufferedReader`s (store files).
- **Why manual `finally` leaks:** If `reader.close()` or `writer.close()` throws an `IOException`, or if multiple resources are opened in one block without nested `try-finally`, later `close()` calls are skipped and file descriptors leak until GC.
- **`try-with-resources` guarantee:** Resources declared in `try (...)` are closed automatically in **reverse order of creation**. If both the `try` body and `close()` throw, the `close()` exception is attached as a **suppressed exception** (`getSuppressed()`) rather than masking the primary exception.

### Student A – Debugging & Experimental Analysis
- Using `store_bad.txt` containing `"CORRUPT_DATA"`, `generateWithTwr` logs the exact failing file and closes both `BufferedReader` and `BufferedWriter` cleanly (0 leaked handles).

### Joint Engineering Decision
`try-with-resources` alone does not guarantee **atomic output**: if a failure occurs half-way through writing `summary.txt`, a partial/incomplete summary file remains on disk. Write to a temporary file and atomically rename/move it only after all store files succeed.

### Alternative Concept & Complexity Analysis
For `f` store files: processing time is `O(total_lines)` in all versions. Peak open file handles: `O(1)` (1 writer + 1 reader at a time) in `try-with-resources`; up to `O(f)` leaked handles if files are opened without closing on failure.

---

## Q4 – Thread Creation and the Thread Life Cycle

### Student A – Thread Design (`Q4/ScannerStationsDemo.java`)
- **`extends Thread` vs `implements Runnable`:** Extending `Thread` uses up Java's single class inheritance; implementing `Runnable` separates the task from the thread runner, allows extending another class, and works with `ExecutorService`.
- **Thread Life Cycle:** `NEW` -> (`.start()`) -> `RUNNABLE` <-> `TIMED_WAITING` (`sleep()`) / `WAITING` (`join()`) -> `TERMINATED`.
- **`run()` vs `start()`:** Calling `s3.run()` executes on the `main` thread (`s3.isAlive()` remains `false`); `start()` allocates a new OS/JVM thread stack and schedules `run()` concurrently.
- **`join()`:** Blocks `main` until all four station threads reach `TERMINATED`, ensuring the final total (`100`) is printed only after all batches finish.

### Student B – Behavioural Experiments
- Output order varies across runs because the OS thread scheduler preempts and interleaves threads non-deterministically. Priorities (`MAX_PRIORITY`) are hints, not guarantees. Setting `s4.setDaemon(true)` means the JVM will not wait for `s4` if all user threads finish (unless `join()` is called).

### Alternative Concept & Complexity Analysis
Using `ExecutorService` with `p` fixed threads for `n` tasks completes in `O(n / p)` batches and bounds stack memory to `O(p * stack_size)` instead of `O(n * stack_size)` when spawning raw threads per batch.

---

## Q5 – Race Conditions in a Cinema Seat-Booking System

### Student A – Producing & Explaining the Race (`Q5/SeatBooking.java`)
- **Critical section:** Check-then-act on `booked[seat]` and `bookingsMade++`.
- **Interleaving:** Thread A reads `booked[0] == false`, gets preempted; Thread B reads `booked[0] == false`, sets `booked[0] = true` and `bookingsMade++`; Thread A resumes and also sets `booked[0] = true` and `bookingsMade++` -> double booking!
- **Before `synchronized`:** Over 10 runs, `bookingsMade` can exceed `10` (e.g., 11–14). With `public synchronized boolean book(int seat)`, output is always `Seats sold: 10`.

### Student B & Joint Engineering Decision
- **Method vs Block:** `synchronized` method locks `this` (`SeatBooking` screen) for the entire method; `synchronized (lock)` narrows the lock to just the critical section. `static synchronized` locks `SeatBooking.class` (across all screens) and would only be needed if screens shared static mutable state.
- **20-Screen Deployment:** Lock **per-screen** (`SeatBooking` instance lock) so bookings on Screen 1 never block bookings on Screen 20.

### Alternative Concept & Complexity Analysis
- `AtomicBoolean[]` with `compareAndSet(false, true)`: lock-free `O(1)` per uncontended booking; space `O(s)` `AtomicBoolean` objects.
- `ReentrantLock`: `O(1)` uncontended, supports `tryLock()` timeouts; `O(1)` lock object per screen.
- `ConcurrentHashMap<Integer, Boolean>` with `putIfAbsent`: `O(1)` average; higher memory (`O(s)` map nodes).

---

## Q6 – Deadlock and Inter-Thread Communication

### Student A – Producer–Consumer Design (`Q6/TokenCounter.java`)
- **Why `while` inside `synchronized`:** `wait()` releases the monitor and sleeps; when woken, another thread might have filled/emptied the buffer before this thread re-acquires the lock, or a spurious wakeup may occur—so the condition must be re-checked in a `while` loop.
- **`notify()` vs `notifyAll()`:** With multiple consumers and a producer sharing one monitor, `notify()` wakes only one arbitrary thread (e.g., a consumer waking another consumer when the buffer is empty), risking missed signals/deadlock. `notifyAll()` wakes all waiting threads safely.
- **`sleep()` vs `wait()`:** `Thread.sleep()` keeps all held locks; `Object.wait()` releases the object's monitor lock.

### Student B – Deadlock Analysis & Prevention (`Q6/WalletDeadlockDemo.java`)
- **Four deadlock conditions:** Mutual exclusion (wallet locks), Hold and wait (holding `from` while waiting for `to`), No preemption, Circular wait (`T1` holds `W1` -> wants `W2`; `T2` holds `W2` -> wants `W1`).
- **Prevention:** Acquire locks in a **consistent global order** (`from.id < to.id ? from : to`), breaking circular wait.

### Alternative Concept & Complexity Analysis
Replacing `wait()/notifyAll()` with `ArrayBlockingQueue<Integer>(5)` gives `O(1)` `put`/`take` with internal `ReentrantLock` + two `Condition`s (`notFull`, `notEmpty`), eliminating manual `while`/`notifyAll` bugs. Replacing lock ordering with `tryLock(timeout)` avoids deadlock via backoff but risks livelock under heavy contention; lock ordering is simpler and deadlock-free.

---

## Q7 – Design a Concurrent, Fault-Tolerant Processor from First Principles

### Stage 1 – Interpret the Problem (Joint)
- **Parallel tasks:** Reading, parsing, and validating each of the 10,000 score files are independent.
- **Shared data:** Valid scores map and failure log/counts.
- **Failures:** Missing file (`IOException`), non-numeric/corrupt content (`NumberFormatException`), out-of-range score. Each must be caught inside its task so one bad file never aborts the pool.

### Stage 2 – Design (Student A)
- Use `ExecutorService` (`Executors.newFixedThreadPool(k)`), `Callable<FileOutcome>`, and `Future<FileOutcome>`.
- Store results in `ConcurrentHashMap<String, Integer>` and `AtomicInteger` counters (lock-free/striped thread safety).

### Stage 3 & Stage 4 – Implement, Measure & Defend (`Q7/ResultProcessor.java`)
- Tested with pool sizes `1, 2, 4, 8`.
- **Recommended pool size:** Number of CPU cores (or `cores * 2` for I/O-bound file reads). Beyond that, context-switching and disk-I/O contention prevent further speedup.
- **Correctness guarantee:** `pool.invokeAll(tasks)` waits for every `Future`, and `processOne()` catches `IOException` and `NumberFormatException` per file, so `successCount + failCount == totalFiles` always holds.
- **Remaining limitation:** Storing all 10,000 `Future` objects and results in memory simultaneously; for millions of files, use `CompletionService` or stream results to disk.

### Alternative Concept & Complexity Analysis
- `files.parallelStream()`: Uses the common `ForkJoinPool` (`cores - 1` threads); `O(n / p)` time, `O(p * stack_size)` memory, but shares the global pool with the rest of the JVM.
- **One thread per file (10,000 threads):** `10,000 * ~1 MB` stack = `~10 GB` virtual memory -> `OutOfMemoryError` and severe OS thrashing. `ExecutorService` bounds threads to `p` (`O(p * stack_size)`).
