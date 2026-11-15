import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcBenchmark {
    private static final String URL = "jdbc:mysql://localhost:3306/transport_db";
    private static final String USER = "root";
    private static final String PWD = "password";

    private static List<String[]> generatePasses(int count, String prefix) {
        List<String[]> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            list.add(new String[]{prefix + i, "192311" + i, "Student" + i, String.valueOf((i % 20) + 1), "3500.0"});
        }
        return list;
    }

    private static void verifyMetaData(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT pass_id, reg_no, name, route_no, amount FROM bus_pass LIMIT 5")) {
            ResultSetMetaData md = rs.getMetaData();
            System.out.println("=== ResultSetMetaData ===");
            for (int c = 1; c <= md.getColumnCount(); c++) {
                System.out.println(md.getColumnName(c) + " : " + md.getColumnTypeName(c));
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, USER, PWD)) {
            for (int size : new int[]{1000, 10000, 100000}) {
                List<String[]> data = generatePasses(size, "B" + size + "_");
                try (Statement st = con.createStatement()) { st.executeUpdate("DELETE FROM bus_pass"); }

                long t0 = System.nanoTime();
                int loaded = PassLoader.loadAll(con, data);
                long batchMs = (System.nanoTime() - t0) / 1_000_000;
                System.out.printf("Batch loadAll(%d): %d rows in %d ms%n", size, loaded, batchMs);
            }

            verifyMetaData(con);

            // Transaction rollback test with invalid amount half-way
            try (Statement st = con.createStatement()) { st.executeUpdate("DELETE FROM bus_pass"); }
            List<String[]> badBatch = generatePasses(10, "BAD");
            badBatch.get(5)[4] = "INVALID_AMOUNT";
            try {
                PassLoader.loadAll(con, badBatch);
            } catch (RuntimeException e) {
                System.out.println("Caught expected mid-batch failure: " + e.getMessage());
            }
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
                if (rs.next()) System.out.println("Rows remaining after rollback: " + rs.getInt(1) + " (expected 0)");
            }
        }
    }
}
