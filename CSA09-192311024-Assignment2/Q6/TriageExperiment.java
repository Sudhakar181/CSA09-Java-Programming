import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class TriageExperiment {
    static class Patient {
        final String name;
        final int severity;
        final long arrival;
        Patient(String name, int severity, long arrival) {
            this.name = name; this.severity = severity; this.arrival = arrival;
        }
        @Override public String toString() { return name + "(S" + severity + ")"; }
    }

    private static void runGrowthTest(int n) {
        PriorityQueue<Patient> pq = new PriorityQueue<>(
                Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival));
        Random rnd = new Random(42);
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            pq.add(new Patient("P" + i, 1 + rnd.nextInt(5), i));
        }
        long addTime = System.nanoTime() - t0;

        t0 = System.nanoTime();
        while (!pq.isEmpty()) pq.poll();
        long pollTime = System.nanoTime() - t0;

        System.out.printf("n=%6d | addAll: %7.3f ms | pollAll: %7.3f ms%n", n, addTime / 1e6, pollTime / 1e6);
    }

    public static void main(String[] args) {
        runGrowthTest(1000);
        runGrowthTest(10000);
        runGrowthTest(100000);
    }
}
