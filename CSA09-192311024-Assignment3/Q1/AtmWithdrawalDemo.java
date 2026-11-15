import java.io.File;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.Scanner;

class InsufficientBalanceException extends RuntimeException {
    InsufficientBalanceException(String msg) { super(msg); }
}

public class AtmWithdrawalDemo {
    static void withdraw(String accountNo, double amount, double balance, String cardReaderPath)
            throws FileNotFoundException {
        File cardFile = new File(cardReaderPath);
        if (!cardFile.exists()) {
            throw new FileNotFoundException("Card-reader device file missing: " + cardReaderPath);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive: " + amount);
        }
        if (amount > balance) {
            throw new InsufficientBalanceException("Requested " + amount + " exceeds balance " + balance);
        }
        System.out.println("Dispensed Rs " + amount + " for account " + accountNo);
    }

    public static void main(String[] args) {
        String[] testInputs = {"500", "-100", "5000", "abc"};
        double balance = 2000.0;

        for (String raw : testInputs) {
            Scanner sc = new Scanner(raw);
            try {
                System.out.println("\n--- Testing input: '" + raw + "' ---");
                double amt = sc.nextDouble();
                // Use "." (existing directory) to simulate present card-reader for first 3 cases
                withdraw("ACC101", amt, balance, ".");
            } catch (InputMismatchException | IllegalArgumentException e) {
                System.out.println("Caught input/validation error (multi-catch): " + e.getClass().getSimpleName() + " - " + e.getMessage());
            } catch (InsufficientBalanceException e) {
                System.out.println("Caught business error: " + e.getMessage());
            } catch (FileNotFoundException e) {
                System.out.println("Caught checked I/O error: " + e.getMessage());
            } finally {
                sc.close();
                System.out.println("finally: Card session closed and scanner released.");
            }
        }
    }
}
