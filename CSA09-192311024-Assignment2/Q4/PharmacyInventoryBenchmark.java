import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class PharmacyInventoryBenchmark {
    private static void testSize(int n) {
        System.out.println("=== Pharmacy Inventory Size n = " + n + " ===");
        List<String> codes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            codes.add(String.format("MED-%06d", i));
        }
        // Shuffle insertion order so insertion order != code order
        List<String> insertOrder = new ArrayList<>(codes);
        Collections.shuffle(insertOrder, new Random(123));

        Map<String, Integer> hashMap = new HashMap<>();
        Map<String, Integer> treeMap = new TreeMap<>();
        Map<String, Integer> linkedMap = new LinkedHashMap<>();

        for (int i = 0; i < n; i++) {
            String code = insertOrder.get(i);
            hashMap.put(code, 50 + (i % 100));
            treeMap.put(code, 50 + (i % 100));
            linkedMap.put(code, 50 + (i % 100));
        }

        // 100,000 lookups
        Random rnd = new Random(42);
        String[] queries = new String[100_000];
        for (int i = 0; i < queries.length; i++) {
            queries[i] = codes.get(rnd.nextInt(n));
        }

        long sink = 0;
        long t0 = System.nanoTime();
        for (String q : queries) sink += hashMap.get(q);
        long hmLookup = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (String q : queries) sink += treeMap.get(q);
        long tmLookup = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (String q : queries) sink += linkedMap.get(q);
        long lmLookup = System.nanoTime() - t0;

        // Report 1: Code-order report
        t0 = System.nanoTime();
        List<String> hmSortedKeys = new ArrayList<>(hashMap.keySet());
        Collections.sort(hmSortedKeys);
        for (String k : hmSortedKeys) sink += hashMap.get(k);
        long hmCodeReport = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (Map.Entry<String, Integer> e : treeMap.entrySet()) sink += e.getValue();
        long tmCodeReport = System.nanoTime() - t0;

        // Report 2: Insertion-order report
        t0 = System.nanoTime();
        for (Map.Entry<String, Integer> e : linkedMap.entrySet()) sink += e.getValue();
        long lmInsertReport = System.nanoTime() - t0;

        if (sink < 0) System.out.print("");

        System.out.printf("100k lookups       | HashMap: %6.3f ms | TreeMap: %6.3f ms | LinkedHashMap: %6.3f ms%n",
                hmLookup / 1e6, tmLookup / 1e6, lmLookup / 1e6);
        System.out.printf("Code-order report  | HashMap (+sort): %6.3f ms | TreeMap (natural): %6.3f ms%n",
                hmCodeReport / 1e6, tmCodeReport / 1e6);
        System.out.printf("Insert-order report| LinkedHashMap (natural): %6.3f ms%n%n",
                lmInsertReport / 1e6);
    }

    public static void main(String[] args) {
        testSize(500);
        testSize(5000);
        testSize(50000);
    }
}
