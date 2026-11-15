import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class EventRegistrationApp extends JFrame {
    // Data-Access Layer (JDBC)
    static class RegistrationDao {
        private static final String URL = "jdbc:mysql://localhost:3306/campus_events";
        private static final String USER = "root";
        private static final String PWD = "password";

        void insert(String regNo, String name, String eventName) throws SQLException {
            String sql = "INSERT INTO event_registrations(student_reg_no, student_name, event_name) VALUES (?, ?, ?)";
            try (Connection con = DriverManager.getConnection(URL, USER, PWD);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, regNo);
                ps.setString(2, name);
                ps.setString(3, eventName);
                ps.executeUpdate();
            }
        }

        List<Object[]> fetchPage(String eventFilter, int limit, int offset) throws SQLException {
            boolean all = "All".equals(eventFilter);
            String sql = all
                    ? "SELECT reg_id, student_reg_no, student_name, event_name FROM event_registrations ORDER BY reg_id DESC LIMIT ? OFFSET ?"
                    : "SELECT reg_id, student_reg_no, student_name, event_name FROM event_registrations WHERE event_name = ? ORDER BY reg_id DESC LIMIT ? OFFSET ?";
            List<Object[]> rows = new ArrayList<>();
            try (Connection con = DriverManager.getConnection(URL, USER, PWD);
                 PreparedStatement ps = con.prepareStatement(sql)) {
                int idx = 1;
                if (!all) ps.setString(idx++, eventFilter);
                ps.setInt(idx++, limit);
                ps.setInt(idx, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
                    }
                }
            }
            return rows;
        }
    }

    // Service Layer
    static class RegistrationService {
        private final RegistrationDao dao = new RegistrationDao();

        String register(String regNo, String name, String eventName) {
            if (regNo == null || !regNo.trim().matches("[A-Za-z0-9]{5,20}")) {
                return "Validation Error: Register number must be 5-20 alphanumeric characters.";
            }
            if (name == null || name.trim().isEmpty()) {
                return "Validation Error: Student name cannot be empty.";
            }
            try {
                dao.insert(regNo.trim().toUpperCase(), name.trim(), eventName);
                return "OK: Registered " + regNo.trim().toUpperCase() + " for " + eventName;
            } catch (SQLIntegrityConstraintViolationException dup) {
                return "Duplicate Error: Student is already registered for " + eventName;
            } catch (SQLException e) {
                return "Database Error: " + e.getMessage();
            }
        }
    }

    // Presentation Layer (Swing GUI)
    private final JTextField regNoField = new JTextField(10);
    private final JTextField nameField = new JTextField(14);
    private final JComboBox<String> eventBox = new JComboBox<>(new String[]{"Hackathon", "Dance", "Quiz", "Music"});
    private final JComboBox<String> filterBox = new JComboBox<>(new String[]{"All", "Hackathon", "Dance", "Quiz", "Music"});
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Reg No", "Name", "Event"}, 0);
    private final JLabel statusLabel = new JLabel("Ready");
    private final RegistrationService service = new RegistrationService();
    private final RegistrationDao dao = new RegistrationDao();
    private int currentPage = 0;
    private static final int PAGE_SIZE = 50;

    public EventRegistrationApp() {
        super("Campus Event Registration");
        setLayout(new BorderLayout(8, 8));

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export CSV");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(exportItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton registerBtn = new JButton("Register");
        formPanel.add(new JLabel("Reg No:"));
        formPanel.add(regNoField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Event:"));
        formPanel.add(eventBox);
        formPanel.add(registerBtn);
        add(formPanel, BorderLayout.NORTH);

        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton filterBtn = new JButton("Load / Filter");
        JButton prevBtn = new JButton("Prev Page");
        JButton nextBtn = new JButton("Next Page");
        controls.add(new JLabel("Filter Event:"));
        controls.add(filterBox);
        controls.add(filterBtn);
        controls.add(prevBtn);
        controls.add(nextBtn);
        bottomPanel.add(controls, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        registerBtn.addActionListener(e -> onRegister());
        filterBtn.addActionListener(e -> { currentPage = 0; loadTableAsync(); });
        prevBtn.addActionListener(e -> { if (currentPage > 0) { currentPage--; loadTableAsync(); } });
        nextBtn.addActionListener(e -> { currentPage++; loadTableAsync(); });
        exportItem.addActionListener(e -> exportCsv());
        exitItem.addActionListener(e -> dispose());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(760, 460);
        setLocationRelativeTo(null);
    }

    private void onRegister() {
        String r = regNoField.getText();
        String n = nameField.getText();
        String ev = (String) eventBox.getSelectedItem();
        statusLabel.setText("Submitting...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return service.register(r, n, ev);
            }

            @Override
            protected void done() {
                try {
                    String msg = get();
                    statusLabel.setText(msg);
                    if (msg.startsWith("OK:")) {
                        regNoField.setText("");
                        nameField.setText("");
                        loadTableAsync();
                    } else {
                        JOptionPane.showMessageDialog(EventRegistrationApp.this, msg, "Registration", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadTableAsync() {
        String filter = (String) filterBox.getSelectedItem();
        int offset = currentPage * PAGE_SIZE;
        statusLabel.setText("Loading page " + (currentPage + 1) + "...");

        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return dao.fetchPage(filter, PAGE_SIZE, offset);
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) tableModel.addRow(row);
                    statusLabel.setText("Loaded " + rows.size() + " rows (Page " + (currentPage + 1) + ")");
                } catch (Exception ex) {
                    statusLabel.setText("Load failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void exportCsv() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("registrations_export.csv"))) {
            bw.write("ID,RegNo,Name,Event\n");
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                bw.write(tableModel.getValueAt(r, 0) + "," + tableModel.getValueAt(r, 1) + ","
                        + tableModel.getValueAt(r, 2) + "," + tableModel.getValueAt(r, 3) + "\n");
            }
            statusLabel.setText("Exported to registrations_export.csv");
        } catch (IOException ex) {
            statusLabel.setText("Export failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventRegistrationApp().setVisible(true));
    }
}
