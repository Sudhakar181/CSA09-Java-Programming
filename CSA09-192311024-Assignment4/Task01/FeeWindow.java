import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(12);
    private final JTextField amount = new JTextField(10);
    private final JButton pay = new JButton("Pay");
    private final JButton clear = new JButton("Clear");
    private final JButton receipt = new JButton("Receipt");
    private final JLabel status = new JLabel("Ready");
    private final FeeService service = new FeeService();

    public FeeWindow() {
        super("Student Fee Payment");
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new FlowLayout());
        form.add(new JLabel("Reg No:"));
        form.add(regNo);
        form.add(new JLabel("Amount:"));
        form.add(amount);

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(pay);
        buttons.add(clear);
        buttons.add(receipt);

        add(form, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        pay.addActionListener(e -> handlePay());
        clear.addActionListener(e -> {
            regNo.setText("");
            amount.setText("");
            status.setText("Cleared");
        });
        receipt.addActionListener(e ->
                JOptionPane.showMessageDialog(this, service.generateReceipt(regNo.getText(), amount.getText())));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void handlePay() {
        pay.setEnabled(false);
        status.setText("Processing payment...");
        String r = regNo.getText();
        String a = amount.getText();

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return service.validateAndPay(r, a);
            }

            @Override
            protected void done() {
                try {
                    String msg = get();
                    status.setText(msg);
                    JOptionPane.showMessageDialog(FeeWindow.this, msg);
                } catch (Exception ex) {
                    status.setText("Error: " + ex.getMessage());
                } finally {
                    pay.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}
