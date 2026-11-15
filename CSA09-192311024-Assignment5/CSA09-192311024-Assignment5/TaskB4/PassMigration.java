import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PassMigration {
    private static final String URL = "jdbc:mysql://localhost:3306/transport_db";
    private static final String USER = "root";
    private static final String PWD = "password";

    public static void main(String[] args) throws IOException, SQLException {
        Path csv = Files.createTempFile("legacy_passes", ".csv");
        Files.writeString(csv,
                "M101,192311001,Arun,4,3200.0\n"
                + "M102,BAD_ROW\n"
                + "M103,192311003,Divya,7,INVALID\n"
                + "M104,192311004,Karthik,2,2900.0\n");

        List<String[]> validRows = new ArrayList<>();
        List<String> failedReport = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csv)) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    failedReport.add("Line " + lineNo + ": wrong field count -> " + line);
                    continue;
                }
                try {
                    Integer.parseInt(parts[3].trim());
                    Double.parseDouble(parts[4].trim());
                    validRows.add(parts);
                } catch (NumberFormatException e) {
                    failedReport.add("Line " + lineNo + ": invalid number -> " + line);
                }
            }
        }

        System.out.println("Valid rows ready for migration: " + validRows.size());
        System.out.println("Failed validation report:");
        for (String err : failedReport) System.out.println("  " + err);

        try (Connection con = DriverManager.getConnection(URL, USER, PWD)) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("\n=== DatabaseMetaData ===");
            System.out.println("Product: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("Driver:  " + meta.getDriverName() + " " + meta.getDriverVersion());
            try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                System.out.print("Tables:  ");
                while (tables.next()) System.out.print(tables.getString("TABLE_NAME") + " ");
                System.out.println();
            }

            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "REPLACE INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)")) {
                for (String[] r : validRows) {
                    ps.setString(1, r[0].trim());
                    ps.setString(2, r[1].trim());
                    ps.setString(3, r[2].trim());
                    ps.setInt(4, Integer.parseInt(r[3].trim()));
                    ps.setDouble(5, Double.parseDouble(r[4].trim()));
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
                System.out.println("Migrated " + validRows.size() + " valid rows in one transaction.");
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        Files.deleteIfExists(csv);
    }
}
