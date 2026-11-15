import java.util.concurrent.atomic.AtomicInteger;

public class ScannerStationsDemo {
    static class StationThread extends Thread {
        private final int batchSize;
        private final AtomicInteger total;
        StationThread(String name, int batchSize, AtomicInteger total) {
            super(name);
            this.batchSize = batchSize;
            this.total = total;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(50);
                total.addAndGet(batchSize);
                System.out.println(getName() + " (extends Thread) scanned " + batchSize + " scripts on " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class StationRunnable implements Runnable {
        private final String stationName;
        private final int batchSize;
        private final AtomicInteger total;
        StationRunnable(String stationName, int batchSize, AtomicInteger total) {
            this.stationName = stationName;
            this.batchSize = batchSize;
            this.total = total;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(40);
                total.addAndGet(batchSize);
                System.out.println(stationName + " (Runnable) scanned " + batchSize + " scripts on " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger total = new AtomicInteger(0);

        Thread s1 = new StationThread("Station-1", 25, total);
        Thread s2 = new StationThread("Station-2", 30, total);
        Thread s3 = new Thread(new StationRunnable("Station-3", 20, total), "Worker-3");
        Thread s4 = new Thread(new StationRunnable("Station-4", 25, total), "Worker-4");

        // Evidence: calling run() directly executes on 'main' thread, not a new thread
        s3.run();
        System.out.println("After direct run(), s3.isAlive() = " + s3.isAlive());

        total.set(0);
        s1.setPriority(Thread.MAX_PRIORITY);
        s4.setDaemon(true);

        s1.start(); s2.start(); s3.start(); s4.start();
        System.out.println("During execution, s1.isAlive() = " + s1.isAlive());

        s1.join(); s2.join(); s3.join(); s4.join();
        System.out.println("All stations finished. Total scripts scanned: " + total.get());
    }
}
