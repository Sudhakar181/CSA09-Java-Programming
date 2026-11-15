public class WalletDeadlockDemo {
    static class Wallet {
        final int id;
        double balance;
        Wallet(int id, double balance) { this.id = id; this.balance = balance; }
    }

    // Deadlock-free transfer using consistent lock ordering by wallet ID
    static void transferSafe(Wallet from, Wallet to, double amount) {
        Wallet first = (from.id < to.id) ? from : to;
        Wallet second = (from.id < to.id) ? to : from;
        synchronized (first) {
            synchronized (second) {
                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Wallet w1 = new Wallet(1, 5000);
        Wallet w2 = new Wallet(2, 5000);

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) transferSafe(w1, w2, 10);
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) transferSafe(w2, w1, 10);
        });

        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("Completed without deadlock. w1=" + w1.balance + ", w2=" + w2.balance
                + ", total=" + (w1.balance + w2.balance));
    }
}
