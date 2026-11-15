import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalesReportGenerator {
    private static final Logger LOG = Logger.getLogger(SalesReportGenerator.class.getName());

    // Manual try-catch-finally version (demonstrates how an exception in close() can leak the writer)
    public static double generateManual(List<Path> storeFiles, Path summaryFile) throws IOException {
        double total = 0;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(summaryFile.toFile()));
            for (Path p : storeFiles) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(p.toFile()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        total += Double.parseDouble(line.trim());
                    }
                } catch (NumberFormatException | IOException e) {
                    LOG.log(Level.WARNING, "Manual version failed on file: " + p, e);
                    throw e;
                } finally {
                    if (reader != null) reader.close();
                }
            }
            writer.write("Total Sales: " + total);
        } finally {
            if (writer != null) writer.close();
        }
        return total;
    }

    // Robust try-with-resources version (closes resources automatically in reverse order)
    public static double generateWithTwr(List<Path> storeFiles, Path summaryFile) throws IOException {
        double total = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(summaryFile)) {
            for (Path p : storeFiles) {
                try (BufferedReader reader = Files.newBufferedReader(p)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        total += Double.parseDouble(line.trim());
                    }
                } catch (NumberFormatException | IOException e) {
                    LOG.log(Level.WARNING, "TWR version failed on file: " + p, e);
                    throw e;
                }
            }
            writer.write("Total Sales: " + total);
        }
        return total;
    }

    public static void main(String[] args) throws IOException {
        Path f1 = Files.createTempFile("store1", ".txt");
        Path f2 = Files.createTempFile("store2", ".txt");
        Path bad = Files.createTempFile("store_bad", ".txt");
        Path out1 = Files.createTempFile("summary_manual", ".txt");
        Path out2 = Files.createTempFile("summary_twr", ".txt");

        Files.writeString(f1, "1200.50\n800.00\n");
        Files.writeString(f2, "450.25\n");
        Files.writeString(bad, "CORRUPT_DATA\n");

        System.out.println("Valid manual total: " + generateManual(List.of(f1, f2), out1));
        System.out.println("Valid TWR total:    " + generateWithTwr(List.of(f1, f2), out2));

        try {
            generateWithTwr(List.of(f1, bad, f2), out2);
        } catch (NumberFormatException e) {
            System.out.println("Caught expected failure in failing input: " + e.getMessage());
        }

        Files.deleteIfExists(f1);
        Files.deleteIfExists(f2);
        Files.deleteIfExists(bad);
        Files.deleteIfExists(out1);
        Files.deleteIfExists(out2);
    }
}
