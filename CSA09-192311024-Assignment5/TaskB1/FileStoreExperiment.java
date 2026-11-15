import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStoreExperiment {
    static long countLinesUnbuffered(Path p) throws IOException {
        long lines = 0;
        try (InputStream in = new FileInputStream(p.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                if (b == '\n') lines++;
            }
        }
        return lines;
    }

    static long countLinesBuffered(Path p) throws IOException {
        long lines = 0;
        try (BufferedReader br = Files.newBufferedReader(p)) {
            while (br.readLine() != null) lines++;
        }
        return lines;
    }

    public static void main(String[] args) throws IOException {
        // 1. Buffering experiment (1 MB and 10 MB; increase to 50 MB if desired)
        for (int mb : new int[]{1, 10}) {
            Path log = Files.createTempFile("log_" + mb + "mb", ".txt");
            String row = "2026-11-15 08:30:00 INFO Student 192311024 checked in\n"; // ~55 bytes
            long targetBytes = mb * 1024L * 1024L;
            try (BufferedWriter bw = Files.newBufferedWriter(log)) {
                for (long written = 0; written < targetBytes; written += row.length()) {
                    bw.write(row);
                }
            }
            long t0 = System.nanoTime();
            long c1 = countLinesUnbuffered(log);
            long unbufMs = (System.nanoTime() - t0) / 1_000_000;

            t0 = System.nanoTime();
            long c2 = countLinesBuffered(log);
            long bufMs = (System.nanoTime() - t0) / 1_000_000;

            System.out.printf("Size=%2d MB | Lines=%d/%d | Unbuffered=%d ms | Buffered=%d ms%n",
                    mb, c1, c2, unbufMs, bufMs);
            Files.deleteIfExists(log);
        }

        // 2. Growth experiment for PassStore.find() (1,000 vs 100,000 records)
        for (int n : new int[]{1000, 10000, 100000}) {
            Path f = Files.createTempFile("passes_" + n, ".csv");
            try (BufferedWriter bw = Files.newBufferedWriter(f)) {
                for (int i = 1; i <= n; i++) {
                    bw.write("P" + i + ",192311" + i + ",Student" + i + ",5,3500.0\n");
                }
            }
            PassStore store = new PassStore(f);
            long t0 = System.nanoTime();
            String[] found = store.find("P" + n); // worst-case last record
            long findMs = (System.nanoTime() - t0) / 1_000_000;
            System.out.printf("Records=%6d | Worst-case find('P%d')=%d ms | Found=%b%n",
                    n, n, findMs, found != null);
            Files.deleteIfExists(f);
        }
    }
}
