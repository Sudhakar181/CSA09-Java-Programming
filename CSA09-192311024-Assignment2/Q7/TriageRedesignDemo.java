import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Random;

public class TriageRedesignDemo {
    static class Patient {
        final String name;
        int severity;
        final long arrivalTick;
        Patient(String name, int severity, long arrivalTick) {
            this.name = name;
            this.severity = severity;
            this.arrivalTick = arrivalTick;
        }
        @Override public String toString() { return name + "(S" + severity + ")"; }
    }

    // Original PriorityQueue-based design: O(log n) admit/poll, no aging
    static class OriginalPriorityTriage {
        private final PriorityQueue<Patient> pq = new PriorityQueue<>(
                Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrivalTick));
        void admit(Patient p) { pq.add(p); }
        Patient next() { return pq.poll(); }
    }

    // Redesigned 5-Bucket ArrayDeque Triage: O(1) admit/poll + bounded aging threshold
    static class BucketTriageWithAging {
        @SuppressWarnings("unchecked")
        private final Deque<Patient>[] buckets = new ArrayDeque[6]; // indices 1..5
        private final long maxWaitTicks;
        private long currentTick = 0;

        BucketTriageWithAging(long maxWaitTicks) {
            this.maxWaitTicks = maxWaitTicks;
            for (int s = 1; s <= 5; s++) buckets[s] = new ArrayDeque<>();
        }

        void admit(Patient p) {
            buckets[p.severity].addLast(p);
        }

        void advanceTick() {
            currentTick++;
            // Promote aged patients by 1 severity level if they waited >= maxWaitTicks
            for (int s = 2; s <= 5; s++) {
                while (!buckets[s].isEmpty() && (currentTick - buckets[s].peekFirst().arrivalTick) >= maxWaitTicks) {
                    Patient promoted = buckets[s].pollFirst();
                    promoted.severity = s - 1;
                    buckets[s - 1].addLast(promoted);
                }
            }
        }

        Patient next() {
            for (int s = 1; s <= 5; s++) {
                if (!buckets[s].isEmpty()) return buckets[s].pollFirst();
            }
            return null;
        }
    }

    public static void main(String[] args) {
        // 1. Fairness validation: minor S5 patient does not starve under continuous S1-S2 arrivals
        BucketTriageWithAging fairTriage = new BucketTriageWithAging(5);
        fairTriage.admit(new Patient("Asha", 5, 0));
        for (int t = 1; t <= 20; t++) {
            fairTriage.admit(new Patient("Urgent" + t, 1, t));
            fairTriage.advanceTick();
        }
        int pos = 0;
        Patient p;
        while ((p = fairTriage.next()) != null) {
            pos++;
            if (p.name.equals("Asha")) {
                System.out.println("Fairness check: Asha (initially S5) served at position " + pos + " as " + p);
                break;
            }
        }

        // 2. Performance benchmark at n = 100,000
        int n = 100_000;
        Random rnd = new Random(42);
        int[] severities = new int[n];
        for (int i = 0; i < n; i++) severities[i] = 1 + rnd.nextInt(5);

        OriginalPriorityTriage orig = new OriginalPriorityTriage();
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) orig.admit(new Patient("P" + i, severities[i], i));
        while (orig.next() != null) {}
        long origTime = System.nanoTime() - t0;

        BucketTriageWithAging redes = new BucketTriageWithAging(1000);
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) redes.admit(new Patient("P" + i, severities[i], i));
        while (redes.next() != null) {}
        long redesTime = System.nanoTime() - t0;

        System.out.printf("n=%d | Original PriorityQueue: %.3f ms | Redesigned 5-Bucket ArrayDeque: %.3f ms%n",
                n, origTime / 1e6, redesTime / 1e6);
    }
}
