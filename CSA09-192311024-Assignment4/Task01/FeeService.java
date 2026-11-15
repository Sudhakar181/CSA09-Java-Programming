import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FeeService {
    private static final String URL = "jdbc:mysql://localhost:3306/campus_db";
    private static final String USER = "root";
    private static final String PWD = "password";

    public String validateAndPay(String regNo, String rawAmount) {
        if (regNo == null || regNo.trim().isEmpty()) {
            return "Error: Register number cannot be empty.";
        }
        double amount;
        try {
            amount = Double.parseDouble(rawAmount.trim());
        } catch (NumberFormatException e) {
            return "Error: Enter a valid numeric amount.";
        }
        if (amount <= 0) {
            return "Error: Amount must be greater than zero.";
        }

        String sql = "INSERT INTO fees(reg_no, amount) VALUES (?, ?)";
        try (Connection con = DriverManager.getConnection(URL, USER, PWD);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            ps.setDouble(2, amount);
            ps.executeUpdate();
            return String.format("Payment of Rs %.2f recorded for %s", amount, regNo.trim());
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    public String generateReceipt(String regNo, String rawAmount) {
        if (regNo == null || regNo.trim().isEmpty()) {
            return "No register number entered for receipt.";
        }
        return "RECEIPT | RegNo: " + regNo.trim() + " | Amount: Rs " + rawAmount.trim();
    }
}
