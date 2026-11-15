# CO5 Java Assignment 5 – Data Persistence Engineering (Files & Databases)

**Student Name:** Sudhakar R  
**Register Number:** 192311024  
**Java Version:** JDK 17 (or JDK 8+)  
**Repository name:** `CSA09-192311024-Assignment5`

## Project contents
- `Assignment_Report.md` – complete Part A (Tasks A1–A5: record layouts, pseudocode, DAO design, serialization, transactions/injection) and Part B (Tasks B1–B4: experimental reasoning & production decision).
- `TaskA1`–`TaskA5/` – Part A task notes (pseudocode and diagrams in `Assignment_Report.md`).
- `TaskB1/PassStore.java` & `TaskB1/FileStoreExperiment.java` – completed `PassStore` (`TODO 1`) + buffering & growth benchmarks.
- `TaskB2/AdversarialDemo.java` – SQL injection vs `PreparedStatement` + damaged bus-pass file test.
- `TaskB3/schema.sql`, `TaskB3/PassLoader.java`, `TaskB3/JdbcBenchmark.java` – completed `PassLoader` (`TODO 1–3`) + batch/rollback benchmark.
- `TaskB4/PassMigration.java` – CSV migration with failure report & `DatabaseMetaData` inspection.

## How to compile and run

```bash
cd TaskB1 && javac PassStore.java FileStoreExperiment.java && java PassStore && java FileStoreExperiment
cd ../TaskB2 && javac AdversarialDemo.java && java -cp .:mysql-connector-j.jar AdversarialDemo
cd ../TaskB3 && javac PassLoader.java JdbcBenchmark.java && java -cp .:mysql-connector-j.jar JdbcBenchmark
cd ../TaskB4 && javac PassMigration.java && java -cp .:mysql-connector-j.jar PassMigration
```
