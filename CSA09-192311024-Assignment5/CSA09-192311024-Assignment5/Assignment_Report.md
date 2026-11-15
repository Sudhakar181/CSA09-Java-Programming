# CO5 Java Assignment 5
## Data Persistence Engineering – Files and Databases

**Student Name:** Sudhakar R  
**Register Number:** 192311024  
**GitHub Link:** `https://github.com/<your-username>/CSA09-192311024-Assignment5`  
**Java Version:** JDK 17 (or JDK 8+)

> Part A uses algorithmic pseudocode and design diagrams (not Java code). Part B contains working Java implementations, SQL scripts, and experimental instructions.

---

## PART A – PERSISTENCE DESIGN & PSEUDOCODE

### Task A1 – Design a File Format (Bus-Pass Records)

#### A1.1 – Represent the Record
- **(a) Delimited Text File (`passes.csv`):**
  - **Field order:** `passId,regNo,name,routeNo,boardingStop,validityDate,amountPaid`
  - **Delimiter:** Comma (`,`), one record per line (`\n`).
  - **Handling a name containing the delimiter:** Wrap fields containing commas or quotes in double quotes (RFC 4180 CSV escaping, e.g., `"Arun, K."`) and escape internal quotes as `""`, or reject/strip commas during input validation.
- **(b) Binary File (`DataOutputStream`):**
  - **Field order & fixed/typed sizes:** `passId` (UTF/char[10] = 20 B), `regNo` (UTF/char[12] = 24 B), `name` (UTF/char[40] = 80 B), `routeNo` (`int` = 4 B), `boardingStop` (UTF/char[30] = 60 B), `validityDate` (`long` epoch day = 8 B), `amountPaid` (`double` = 8 B). Using `writeUTF()` stores a 2-byte length prefix automatically; using fixed-length `writeChars()` gives constant record size (204 B) for random seeking.

#### A1.2 – Design the Operations (Text-File Design)
- **Streams chosen:** `BufferedReader` wrapping `FileReader` (or `Files.newBufferedReader`) for reading, and `BufferedWriter` wrapping `FileWriter(..., true)` (or `Files.newBufferedWriter`) for appending. Character streams handle UTF-8 text and line boundaries (`readLine()`, `newLine()`), while buffering minimizes OS read/write syscalls.
```text
PROCEDURE addPass(file, passRecord)
    OPEN BufferedWriter(file, APPEND)
    WRITE passRecord.fields JOINED BY ","
    WRITE NEWLINE
    CLOSE writer
END PROCEDURE

PROCEDURE findPassById(file, targetPassId)
    IF file DOES NOT EXIST THEN RETURN NULL
    OPEN BufferedReader(file)
    WHILE (line <- READ_LINE) IS NOT NULL DO
        fields <- SPLIT(line, ",")
        IF LENGTH(fields) != 7 THEN CONTINUE   // skip damaged line
        IF fields[0] EQUALS targetPassId THEN
            CLOSE reader
            RETURN fields
        END IF
    END WHILE
    CLOSE reader
    RETURN NULL
END PROCEDURE

PROCEDURE listByRoute(file, targetRoute)
    results <- EMPTY LIST
    OPEN BufferedReader(file)
    WHILE (line <- READ_LINE) IS NOT NULL DO
        fields <- SPLIT(line, ",")
        IF LENGTH(fields) == 7 AND fields[3] EQUALS targetRoute THEN
            ADD fields TO results
        END IF
    END WHILE
    CLOSE reader
    RETURN results
END PROCEDURE
```

#### A1.3 – Design for Damaged Data
- **Damaged line:** Check `fields.length` and parse numeric fields (`routeNo`, `amountPaid`) inside a `try-catch`; log and skip invalid lines instead of crashing the search.
- **Missing file:** Check `Files.exists(file)` before reading (return `null` / empty list) and open with `CREATE` when writing.
- **Incomplete write (power failure):** Write updates to a temporary file (`passes.tmp`), flush, and atomically rename over `passes.csv`, or append a trailing newline/checksum and ignore partial last lines lacking a valid field count.

#### Alternative Concept & Complexity Analysis (`RandomAccessFile` Fixed-Length)
- Record `i` is located directly by `raf.seek(i * recordSize)`.
- **Find by ID:** If ID maps to index `i` (or via an in-memory ID->index table), lookup is **`O(1)`** seek vs **`O(n)`** full-file scan in delimited text.
- **Space wasted:** Fixed-length padding (e.g., 40-char name = 80 B even for `"Arun"`) wastes space proportional to `(maxFieldLen - actualFieldLen) * n`, whereas CSV stores only actual characters plus 1-byte delimiters.

---

### Task A2 – Don't Read What You Don't Need (50 MB Attendance Log)

- **A2.1 – Predict the Cost:** `FileInputStream.read()` makes **~52,428,800** (`50 * 1024 * 1024`) individual read calls—one per byte. Each unbuffered `read()` triggers a user-to-kernel-space OS system call, making it tens of seconds slow.
- **A2.2 – Redesign Using Buffering:** Wrap in `BufferedInputStream` (default 8 KB buffer) or read into a `byte[65536]` (64 KB) buffer and count `'\n'` bytes. This reduces OS `read` syscalls from ~52.4 million to **~800–6,400** syscalls (a >1,000x speedup).
- **A2.3 – When Buffering Does Not Help:** Reading small files (< 1 KB) in a single bulk `read(byte[])`, or performing random-access seeks (`RandomAccessFile.seek()`) to scattered offsets where each read jumps to a new disk page and invalidates the buffer.

#### Alternative Concept & Complexity Analysis (`Files.readAllLines()` vs Buffered Stream)
- `Files.readAllLines()` loads every line into a `List<String>` in heap memory: Time `O(file_size)`, Memory **`O(file_size)`** (~50 MB+ plus `String` object overhead, risking `OutOfMemoryError` on huge logs).
- **Buffered stream:** Time `O(file_size)`, Memory **`O(buffer_size)`** (constant 8–64 KB). Use `readAllLines()` only for small config files; use streaming for logs.

---

### Task A3 – Discover the Data-Access Design Pattern

- **A3.1 – Common Pattern:** Repeated in all three (`StudentDB`, `CourseDB`, `MarksDB`): opening `Connection`, creating `PreparedStatement`, executing SQL, closing resources. Differing parts: table name, SQL queries, and `ResultSet` -> object row mapping.
- **A3.2 – Generic DAO Interface:**
```text
INTERFACE Dao<T, K>
    PROCEDURE insert(entity : T)
    FUNCTION findById(id : K) : T
    FUNCTION findAll() : LIST<T>
    PROCEDURE update(entity : T)
    PROCEDURE delete(id : K)
END INTERFACE

CLASS StudentDao IMPLEMENTS Dao<Student, String>
    // Implements insert, findById, findAll, update, delete using student table SQL
END CLASS
```
- **A3.3 – Object–Table Mapping:** `Student.regNo` (String) -> `student.reg_no` (`VARCHAR(20) PRIMARY KEY`); `Student.name` -> `student.name` (`VARCHAR(100)`); `Student.dept` -> `student.dept` (`VARCHAR(50)`).

#### Alternative Concept & Complexity Analysis (SQL in Service/GUI vs `Dao<T, K>`)
When a column is renamed, SQL scattered in GUI/service classes forces edits across **every screen/method** that touches that table (`O(screens)` places), whereas `Dao<T, K>` isolates the change to **1 DAO class** (`O(1)`). Runtime time and space per DB operation are identical (`O(1)` per row).

---

### Task A4 – What Should Be Serialized?

- **A4.1 – Transient Design & Object Graph:**
  - Mark `password` (`transient` for security—never persist plaintext credentials to disk) and `Connection` (`transient` because live DB sockets/streams are not `Serializable` and cannot survive JVM restarts).
  - Serializing `Hostel` traverses the entire reachable **object graph**: `Hostel` -> `List<Room>` -> `List<Resident>`, writing each non-transient field and preserving shared object references.
- **A4.2 – Versioning (`serialVersionUID`):**
  - Adding `phoneNumber` to `Resident` changes the class structure. If `serialVersionUID` was **not** explicitly declared, the JVM computes a new hash and throws `InvalidClassException` when reading old files.
  - If an explicit `private static final long serialVersionUID = 1L;` is kept identical, Java deserializes old `Resident` objects successfully and sets the new `phoneNumber` field to its default (`null`).

#### Alternative Concept & Complexity Analysis (CSV/JSON vs Java Serialization)
- **File size:** Binary serialization includes class metadata/descriptors; CSV is smallest (raw text values), JSON is slightly larger (field names repeated).
- **Time for `n` residents:** Both are `O(n)`. Java serialization uses reflection; CSV/JSON parsers are language-independent and human-readable.
- **Schema evolution (new field):** CSV requires positional/header handling; JSON and `serialVersionUID`-pinned serialization both default missing fields cleanly.

---

### Task A5 – Design for Correctness and Safety

- **A5.1 – Transaction Pseudocode (₹500 Transfer):**
```text
PROCEDURE transferWallet(con, hostelId, canteenId, amount)
    con.setAutoCommit(FALSE)
    TRY
        UPDATE wallet SET balance = balance - amount WHERE id = hostelId
        UPDATE wallet SET balance = balance + amount WHERE id = canteenId
        con.commit()
    CATCH SQLException
        con.rollback()
        RETHROW error
    FINALLY
        con.setAutoCommit(TRUE)
    END TRY
END PROCEDURE
```
- **A5.2 – SQL Injection Analysis & Redesign:**
  - If `id` is `admin' OR '1'='1` and `pwd` is anything (or `' OR '1'='1`), the concatenated SQL becomes `... WHERE id='admin' OR '1'='1' AND pwd='...'`, which evaluates to `TRUE` and logs in without a valid password.
  - **Safe Redesign:** Use `PreparedStatement`:
    `SELECT * FROM users WHERE id = ? AND pwd = ?` and bind `ps.setString(1, id); ps.setString(2, pwd);`. The DB treats parameters strictly as data literals, never executable SQL.

#### Alternative Concept & Complexity Analysis (Two Auto-Commit Updates vs Transaction)
With two separate auto-commit statements, a crash between them debits ₹500 from the hostel wallet without crediting the canteen wallet (money lost!). A single transaction executes both updates under one commit/log flush (often faster or comparable in time) while guaranteeing **atomicity** (all-or-nothing).

---

## PART B – IMPLEMENTATION & EXPERIMENTAL INVESTIGATION

### Task B1 – Implement and Instrument the File Store (`TaskB1/PassStore.java`, `TaskB1/FileStoreExperiment.java`)
- **Completed TODO 1 (`PassStore.find`):** Reads line by line with `BufferedReader` in `try-with-resources`, skips empty/damaged lines (`parts.length != 5`), and returns the matching 5-field array or `null`.
- **Why `P1002,DAMAGED LINE` does not stop the search:** `parts.length` is `2` (`!= 5`), so `continue` skips the malformed line and proceeds to `P1003`.
- **Alternative Concept (`HashMap<String, String[]>` at startup vs file scan):**
  - File scan per `find()`: Time for `q` searches on `n` passes = **`O(q * n)`**, Memory = **`O(1)`**.
  - Startup `HashMap`: Time = **`O(n + q)`** (`O(n)` load + `O(1)` per query), Memory = **`O(n)`** to hold all `n` records in RAM.

### Task B2 – Engineer an Adversarial Input (`TaskB2/AdversarialDemo.java`)
- **SQL Injection:** `id = "admin' OR '1'='1"` makes `unsafeLogin()` return `true`, whereas `safeLogin()` (`PreparedStatement`) treats the string literally and returns `false`.
- **Damaged Bus-Pass File:** Empty lines, wrong field counts, and non-numeric amounts are caught and skipped without crashing.
- **Alternative Concept (Quote escaping / Regex validation vs `PreparedStatement`):** Escaping quotes or regex filtering is fragile (misses encoding tricks/edge cases) and adds `O(len)` regex overhead per query, whereas `PreparedStatement` separates code from data at the protocol level and allows query-plan caching.

### Task B3 – Statement vs PreparedStatement vs Batch (`TaskB3/schema.sql`, `TaskB3/PassLoader.java`, `TaskB3/JdbcBenchmark.java`)
- **Completed TODO 1–3 (`PassLoader.loadAll`):** Sets 5 typed parameters, batches every 1,000 rows, calls `con.commit()` on success, and `con.rollback()` in `catch`.
- **Mid-batch failure:** Inserting `"INVALID_AMOUNT"` half-way throws `NumberFormatException`, triggers `con.rollback()`, and leaves **`0` rows** in `bus_pass` (vs partial rows committed under auto-commit).
- **Engineering Trade-Off:**
  - **(a) Nightly import of 100,000 passes:** `PreparedStatement` with `addBatch()`/`executeBatch()` + single transaction (minimizes network round-trips and disk flushes).
  - **(b) Single pass at counter:** Single `PreparedStatement` with auto-commit (or single transaction).
- **Alternative Concept (Multi-row `INSERT` / MySQL `LOAD DATA`):** Multi-row `INSERT` builds one huge SQL packet (`O(b)` buffer memory for batch `b`); `LOAD DATA` streams directly inside the MySQL server and is the fastest bulk loader (`O(n)`), using `O(1)` client batch memory.

### Task B4 – Final Engineering Decision (`TaskB4/PassMigration.java`)
- **Migration & `DatabaseMetaData`:** Validates CSV rows, logs rejected lines, batch-inserts valid rows in one transaction, and prints `DatabaseMetaData` (`Product`, `Driver`, `Tables`)—essential when migrating servers to verify driver compatibility and schema presence automatically.
- **Production Decision (12 campuses, 100x scale, concurrent counters):** Use a **central relational database (MySQL via JDBC)** with connection pooling and `PreparedStatement`. Text files and Java serialization cannot safely handle concurrent multi-counter writes, lack indexed `O(log n)`/`O(1)` search across 800,000+ passes, and lack ACID transactions.
- **Alternative Concept (12 Separate Campus Serialized Files):** Searching across all 12 campuses requires loading and scanning 12 files (`O(N)` total across campuses) and wastes `O(N)` RAM; concurrent counters would corrupt files without distributed file locks.
