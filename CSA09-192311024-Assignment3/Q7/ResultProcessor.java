import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultProcessor {
    static class FileOutcome {
        final String fileName;
        final boolean success;
        final int score;
        final String error;

        FileOutcome(String fileName, boolean success, int score, String error) {
            this.fileName = fileName;
            this.success = success;
            this.score = score;
            this.error = error;
        }
    }

    static FileOutcome processOne(Path file) {
        try {
            String raw = Files.readString(file).trim();
            int score = Integer.parseInt(raw);
            if (score < 0 || score > 100) {
                return new FileOutcome(file.getFileName().toString(), false, 0, "Out-of-range score: " + score);
            }
            return new FileOutcome(file.getFileName().toString(), true, score, null);
        } catch (IOException e) {
            return new FileOutcome(file.getFileName().toString(), false, 0, "Missing/unreadable file: " + e.getMessage());
        } catch (NumberFormatException e) {
            return new FileOutcome(file.getFileName().toString(), false, 0, "Corrupt score format: " + e.getMessage());
        }
    }

    static void runWithPool(List<Path> files, int threads) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        ConcurrentHashMap<String, Integer> validScores = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> failures = new ConcurrentHashMap<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long t0 = System.nanoTime();
        List<Callable<FileOutcome>> tasks = new ArrayList<>(files.size());
        for (Path p : files) tasks.add(() -> processOne(p));

        List<Future<FileOutcome>> futures = pool.invokeAll(tasks);
        for (Future<FileOutcome> f : futures) {
            try {
                FileOutcome out = f.get();
                if (out.success) {
                    validScores.put(out.fileName, out.score);
                    successCount.incrementAndGet();
                } else {
                    failures.put(out.fileName, out.error);
                    failCount.incrementAndGet();
                }
            } catch (ExecutionException e) {
                failCount.incrementAndGet();
            }
        }
        pool.shutdown();
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("Pool=%d threads | Time=%4d ms | Success=%d | Failed=%d%n",
                threads, elapsedMs, successCount.get(), failCount.get());
    }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("exam_scores");
        List<Path> files = new ArrayList<>();
        int totalFiles = 200; // Scale to 10000 for full benchmark
        for (int i = 0; i < totalFiles; i++) {
            Path p = dir.resolve("student_" + i + ".txt");
            if (i % 25 == 0) {
                Files.writeString(p, "CORRUPT");
            } else if (i % 37 == 0) {
                // Intentionally do not create file to simulate missing file
            } else {
                Files.writeString(p, String.valueOf(50 + (i % 50)));
            }
            files.add(p);
        }

        for (int poolSize : new int[]{1, 2, 4, 8}) {
            runWithPool(files, poolSize);
        }

        for (Path p : files) Files.deleteIfExists(p);
        Files.deleteIfExists(dir);
    }
}
