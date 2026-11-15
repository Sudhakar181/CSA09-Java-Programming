import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdversarialDemo {
    private static final String URL = "jdbc:mysql://localhost:3306/transport_db";
    private static final String USER = "root";
    private static final String PWD = "password";

    static boolean unsafeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }

    static boolean safeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, pwd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static void testDamagedBusPassFile() throws IOException {
        Path badFile = Files.createTempFile("damaged_passes", ".csv");
        Files.writeString(badFile,
                "P2001,192311001,Arun,5,3500.0\n"
                + "P2002,ONLY_TWO_FIELDS\n"
                + "\n"
                + "P2003,192311003,Divya,7,NOT_A_NUMBER\n");

        System.out.println("=== Damaged Bus-Pass File Test ===");
        try (BufferedReader br = Files.newBufferedReader(badFile)) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    System.out.println("Line " + lineNo + " skipped: empty line");
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    System.out.println("Line " + lineNo + " skipped: expected 5 fields, got " + parts.length);
                    continue;
                }
                try {
                    int route = Integer.parseInt(parts[3].trim());
                    double amount = Double.parseDouble(parts[4].trim());
                    System.out.println("Line " + lineNo + " valid: " + parts[0] + " route=" + route + " amt=" + amount);
                } catch (NumberFormatException e) {
                    System.out.println("Line " + lineNo + " skipped: non-numeric route/amount (" + e.getMessage() + ")");
                }
            }
        }
        Files.deleteIfExists(badFile);
    }

    public static void main(String[] args) throws IOException {
        testDamagedBusPassFile();

        // Adversarial SQL injection input
        String evilId = "admin' OR '1'='1";
        String anyPwd = "wrong_password";
        System.out.println("\n=== SQL Injection Adversarial Input ===");
        System.out.println("Evil ID: " + evilId + " | Password: " + anyPwd);
        try (Connection con = DriverManager.getConnection(URL, USER, PWD)) {
            System.out.println("Unsafe login accepted? " + unsafeLogin(con, evilId, anyPwd));
            System.out.println("Safe PreparedStatement accepted? " + safeLogin(con, evilId, anyPwd));
        } catch (SQLException e) {
            System.out.println("DB not connected (run schema.sql first): " + e.getMessage());
        }
    }
}
