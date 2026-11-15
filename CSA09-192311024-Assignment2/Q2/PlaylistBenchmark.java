import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PlaylistBenchmark {
    static class Song {
        final int id;
        final String title;
        Song(int id) {
            this.id = id;
            this.title = "Track-" + id;
        }
    }

    private static void populate(List<Song> list, int n) {
        list.clear();
        for (int i = 0; i < n; i++) list.add(new Song(i));
    }

    private static void benchmarkSize(int n) {
        System.out.println("=== Playlist Size n = " + n + " ===");
        List<Song> arrayList = new ArrayList<>();
        List<Song> linkedList = new LinkedList<>();

        // 1. Add at end
        long t0 = System.nanoTime();
        populate(arrayList, n);
        long alAddEnd = System.nanoTime() - t0;

        t0 = System.nanoTime();
        populate(linkedList, n);
        long llAddEnd = System.nanoTime() - t0;

        // 2. Insert/remove at front (500 ops)
        int ops = Math.min(500, n / 2);
        t0 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            arrayList.add(0, new Song(i));
            arrayList.remove(0);
        }
        long alFront = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            linkedList.add(0, new Song(i));
            linkedList.remove(0);
        }
        long llFront = System.nanoTime() - t0;

        // 3. Insert/remove at middle (500 ops)
        t0 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            int mid = arrayList.size() / 2;
            arrayList.add(mid, new Song(i));
            arrayList.remove(mid);
        }
        long alMid = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            int mid = linkedList.size() / 2;
            linkedList.add(mid, new Song(i));
            linkedList.remove(mid);
        }
        long llMid = System.nanoTime() - t0;

        // 4. Random access get(k) (1000 ops)
        Random rnd = new Random(42);
        int[] indices = new int[1000];
        for (int i = 0; i < indices.length; i++) indices[i] = rnd.nextInt(n);

        long sink = 0;
        t0 = System.nanoTime();
        for (int idx : indices) sink += arrayList.get(idx).id;
        long alGet = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (int idx : indices) sink += linkedList.get(idx).id;
        long llGet = System.nanoTime() - t0;

        // 5. Iterate full list
        t0 = System.nanoTime();
        for (Song s : arrayList) sink += s.id;
        long alIter = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (Song s : linkedList) sink += s.id;
        long llIter = System.nanoTime() - t0;

        if (sink < 0) System.out.print("");

        System.out.printf("Add at end      | ArrayList: %7.3f ms | LinkedList: %7.3f ms%n", alAddEnd / 1e6, llAddEnd / 1e6);
        System.out.printf("Front ins/rem   | ArrayList: %7.3f ms | LinkedList: %7.3f ms%n", alFront / 1e6, llFront / 1e6);
        System.out.printf("Middle ins/rem  | ArrayList: %7.3f ms | LinkedList: %7.3f ms%n", alMid / 1e6, llMid / 1e6);
        System.out.printf("Random get(k)   | ArrayList: %7.3f ms | LinkedList: %7.3f ms%n", alGet / 1e6, llGet / 1e6);
        System.out.printf("Full iteration  | ArrayList: %7.3f ms | LinkedList: %7.3f ms%n%n", alIter / 1e6, llIter / 1e6);
    }

    private static void benchmarkUpNextQueue(int ops) {
        System.out.println("=== Up Next Queue (addFirst/addLast/removeFirst, ops=" + ops + ") ===");
        Deque<Song> arrayDeque = new ArrayDeque<>();
        Deque<Song> linkedList = new LinkedList<>();

        long t0 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            if (i % 2 == 0) arrayDeque.addLast(new Song(i));
            else arrayDeque.addFirst(new Song(i));
            if (arrayDeque.size() > 100) arrayDeque.removeFirst();
        }
        long adTime = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            if (i % 2 == 0) linkedList.addLast(new Song(i));
            else linkedList.addFirst(new Song(i));
            if (linkedList.size() > 100) linkedList.removeFirst();
        }
        long llTime = System.nanoTime() - t0;

        System.out.printf("ArrayDeque: %.3f ms | LinkedList: %.3f ms%n", adTime / 1e6, llTime / 1e6);
    }

    public static void main(String[] args) {
        benchmarkSize(1000);
        benchmarkSize(10000);
        benchmarkSize(50000);
        benchmarkUpNextQueue(50000);
    }
}
