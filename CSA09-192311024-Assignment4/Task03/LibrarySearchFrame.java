import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LibrarySearchFrame extends JFrame {
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"All", "CS", "Math", "Physics", "Electronics"});
    private final JTextArea resultsArea = new JTextArea(12, 45);
    private final JLabel statusLabel = new JLabel("Status: Ready (0 results)");

    public LibrarySearchFrame() {
        super("Campus Library Search");
        setLayout(new BorderLayout(8, 8));

        // NORTH: Title + Search Row
        JPanel topContainer = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Campus Library Catalog Search", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        topContainer.add(title, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchBtn = new JButton("Search");
        searchRow.add(new JLabel("Query:"));
        searchRow.add(searchField);
        searchRow.add(new JLabel("Category:"));
        searchRow.add(categoryBox);
        searchRow.add(searchBtn);
        topContainer.add(searchRow, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // CENTER: Results area (grows on resize)
        resultsArea.setEditable(false);
        resultsArea.setText("1. Clean Code - Robert C. Martin [CS]\n2. Effective Java - Joshua Bloch [CS]\n3. Operating System Concepts - Silberschatz [CS]");
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        // SOUTH: Navigation buttons + Status bar
        JPanel bottomContainer = new JPanel(new BorderLayout());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevBtn = new JButton("Prev");
        JButton nextBtn = new JButton("Next");
        JButton reserveBtn = new JButton("Reserve");
        buttonRow.add(prevBtn);
        buttonRow.add(nextBtn);
        buttonRow.add(reserveBtn);

        bottomContainer.add(buttonRow, BorderLayout.NORTH);
        bottomContainer.add(statusLabel, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            String cat = (String) categoryBox.getSelectedItem();
            statusLabel.setText("Status: Searched '" + q + "' in category " + cat);
        });
        reserveBtn.addActionListener(e -> statusLabel.setText("Status: Selected book reserved."));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearchFrame().setVisible(true));
    }
}
