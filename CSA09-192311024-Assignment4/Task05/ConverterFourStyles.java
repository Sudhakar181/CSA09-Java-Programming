import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;

// Demonstrates styles (a) Frame implements ActionListener, (b) Named inner class,
// (c) Anonymous inner class, and (d) Lambda + WindowAdapter
public class ConverterFourStyles extends Frame implements ActionListener {
    private final TextField amount = new TextField(10);
    private final Choice target = new Choice();
    private final Label result = new Label("Ready           ");

    public ConverterFourStyles() {
        super("Currency Converter (4 Styles)");
        setLayout(new FlowLayout());
        target.add("USD");
        target.add("EUR");

        Button btnA = new Button("Style A (Frame)");
        Button btnB = new Button("Style B (Inner)");
        Button btnC = new Button("Style C (Anon)");
        Button btnD = new Button("Style D (Lambda)");

        add(new Label("INR:"));
        add(amount);
        add(target);
        add(btnA);
        add(btnB);
        add(btnC);
        add(btnD);
        add(result);

        // (a) Frame implements ActionListener
        btnA.addActionListener(this);
        // (b) Named inner class
        btnB.addActionListener(new NamedConvertListener());
        // (c) Anonymous inner class
        btnC.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertAndShow("Anonymous");
            }
        });
        // (d) Lambda expression (also attached to Enter key in amount)
        ActionListener lambda = e -> convertAndShow("Lambda");
        btnD.addActionListener(lambda);
        amount.addActionListener(lambda);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        convertAndShow("Frame");
    }

    private class NamedConvertListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            convertAndShow("NamedInner");
        }
    }

    private void convertAndShow(String style) {
        try {
            double inr = Double.parseDouble(amount.getText().trim());
            String cur = target.getSelectedItem();
            double rate = "USD".equals(cur) ? 0.012 : 0.011;
            String msg = String.format("[%s] %.2f %s", style, inr * rate, cur);
            result.setText(msg);
            JOptionPane.showMessageDialog(this, msg);
        } catch (NumberFormatException ex) {
            result.setText("Invalid amount");
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ConverterFourStyles().setVisible(true);
    }
}
