import java.util.Arrays;

public class TicketBenchmark {
    private static String buildWithString(String[] fields) {
        String ticket = "";
        for (int i = 0; i < fields.length; i++) {
            ticket = ticket + fields[i] + (i < fields.length - 1 ? " | " : "");
        }
        return ticket;
    }

    private static String buildWithStringBuilder(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]);
            if (i < fields.length - 1) sb.append(" | ");
        }
        return sb.toString();
    }

    private static String buildWithStringBuffer(String[] fields) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]);
            if (i < fields.length - 1) sb.append(" | ");
        }
        return sb.toString();
    }

    private static String[] makeFields(int count) {
        String[] fields = new String[count];
        for (int i = 0; i < count; i++) {
            fields[i] = "Field" + i + ":PNR842910";
        }
        return fields;
    }

    private static long usedMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        System.gc();
        return rt.totalMemory() - rt.freeMemory();
    }

    private static void runWorkload(String label, int tickets, int fieldCount, int repeats) {
        String[] fields = makeFields(fieldCount);
        System.out.println("=== " + label + " (tickets=" + tickets + ", fields=" + fieldCount + ") ===");

        // Warm-up
        for (int i = 0; i < 5; i++) {
            buildWithString(fields);
            buildWithStringBuilder(fields);
            buildWithStringBuffer(fields);
        }

        long tString = 0, tBuilder = 0, tBuffer = 0;
        long memBefore, memAfter;

        memBefore = usedMemoryBytes();
        for (int r = 0; r < repeats; r++) {
            long start = System.nanoTime();
            int sink = 0;
            for (int t = 0; t < tickets; t++) {
                sink += buildWithString(fields).length();
            }
            tString += System.nanoTime() - start;
            if (sink < 0) System.out.print("");
        }
        memAfter = usedMemoryBytes();
        long estStringObjs = (long) tickets * (2L * fieldCount);
        System.out.printf("String (+)    : avg %.3f ms | est. intermediate String objs: %d | delta mem: %d KB%n",
                (tString / (double) repeats) / 1e6, estStringObjs, (memAfter - memBefore) / 1024);

        memBefore = usedMemoryBytes();
        for (int r = 0; r < repeats; r++) {
            long start = System.nanoTime();
            int sink = 0;
            for (int t = 0; t < tickets; t++) {
                sink += buildWithStringBuilder(fields).length();
            }
            tBuilder += System.nanoTime() - start;
            if (sink < 0) System.out.print("");
        }
        memAfter = usedMemoryBytes();
        System.out.printf("StringBuilder : avg %.3f ms | est. intermediate String objs: %d | delta mem: %d KB%n",
                (tBuilder / (double) repeats) / 1e6, tickets, (memAfter - memBefore) / 1024);

        memBefore = usedMemoryBytes();
        for (int r = 0; r < repeats; r++) {
            long start = System.nanoTime();
            int sink = 0;
            for (int t = 0; t < tickets; t++) {
                sink += buildWithStringBuffer(fields).length();
            }
            tBuffer += System.nanoTime() - start;
            if (sink < 0) System.out.print("");
        }
        memAfter = usedMemoryBytes();
        System.out.printf("StringBuffer  : avg %.3f ms | est. intermediate String objs: %d | delta mem: %d KB%n%n",
                (tBuffer / (double) repeats) / 1e6, tickets, (memAfter - memBefore) / 1024);
    }

    public static void main(String[] args) {
        runWorkload("Workload 1: Single Short Ticket", 1, 8, 5);
        runWorkload("Workload 2: Bulk Standard Tickets", 10000, 50, 3);
        runWorkload("Workload 3: Heavy Detailed Tickets", 5000, 120, 3);
    }
}
