import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;

public class CurrencyConverter extends JFrame {

    private JComboBox<String> fromCurrency;
    private JComboBox<String> toCurrency;
    private JTextField amountField;
    private JButton convertButton;
    private JButton historicalButton; // Button for fetching historical rates
    private JTextField dateField; // Field for entering the date for historical rates
    private JLabel resultLabel;
    private Map<String, Double> rates = new HashMap<>();
    private String apiKey = "db931107aa23594912a7e9dc"; // Your actual API key

    public CurrencyConverter() {
        super("Currency Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);

        // Create the main panel with a vertical layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // From currency selection
        JPanel fromPanel = new JPanel();
        fromCurrency = new JComboBox<>();
        fromPanel.add(new JLabel("From:"));
        fromPanel.add(fromCurrency);

        // To currency selection
        JPanel toPanel = new JPanel();
        toCurrency = new JComboBox<>();
        toPanel.add(new JLabel("To:"));
        toPanel.add(toCurrency);

        // Amount input
        JPanel amountPanel = new JPanel();
        amountField = new JTextField(10);
        amountPanel.add(new JLabel("Amount:"));
        amountPanel.add(amountField);

        // Convert button
        JPanel buttonPanel = new JPanel();
        convertButton = new JButton("Convert");
        buttonPanel.add(convertButton);

        // Historical rates input
        JPanel datePanel = new JPanel();
        dateField = new JTextField(10); // Date field for historical rates
        datePanel.add(new JLabel("Date (YYYY-MM-DD):"));
        datePanel.add(dateField);

        // Historical rates button
        JPanel historicalButtonPanel = new JPanel();
        historicalButton = new JButton("Get Historical Rates");
        historicalButtonPanel.add(historicalButton);

        // Result label
        resultLabel = new JLabel("");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add action listener for the Convert button
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String from = fromCurrency.getSelectedItem().toString();
                    String to = toCurrency.getSelectedItem().toString();
                    double rateFrom = rates.getOrDefault(from, 1.0);
                    double rateTo = rates.getOrDefault(to, 1.0);
                    double result = amount * rateTo / rateFrom;
                    resultLabel.setText(amount + " " + from + " = " + result + " " + to);
                } catch (NumberFormatException ex) {
                    resultLabel.setText("Invalid input. Please enter a valid number.");
                }
            }
        });

        // Add action listener for the Historical button
        historicalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String date = dateField.getText();
                if (!date.isEmpty()) {
                    fetchHistoricalRates(date);
                } else {
                    JOptionPane.showMessageDialog(CurrencyConverter.this, "Please enter a valid date.");
                }
            }
        });

        // Add components to the panel
        panel.add(fromPanel);
        panel.add(toPanel);
        panel.add(amountPanel);
        panel.add(buttonPanel);
        panel.add(datePanel);
        panel.add(historicalButtonPanel);
        panel.add(resultLabel);

        // Add the main panel to the frame
        add(panel);

        // Fetch exchange rates and populate dropdowns
        fetchExchangeRates();
    }

    private void fetchExchangeRates() {
        try {
            String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/USD"; // Real-time exchange rates endpoint

            // Create an HTTP connection
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject exchangeRates = jsonResponse.getJSONObject("conversion_rates");

            // Populate rates map and currency dropdowns
            for (String currency : exchangeRates.keySet()) {
                rates.put(currency, exchangeRates.getDouble(currency));
                fromCurrency.addItem(currency);
                toCurrency.addItem(currency);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch exchange rates: " + e.getMessage());
        }
    }

    private void fetchHistoricalRates(String date) {
        try {
            String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/history?start_date=" + date + "&end_date=" + date; // Correct URL format for historical data

            // Create an HTTP connection
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());

            // Check for valid response
            if (jsonResponse.has("conversion_rates")) {
                JSONObject historicalRates = jsonResponse.getJSONObject("conversion_rates");

                // Display comparison of current vs historical rates for the selected currencies
                String from = fromCurrency.getSelectedItem().toString();
                String to = toCurrency.getSelectedItem().toString();
                double currentRateFrom = rates.getOrDefault(from, 1.0);
                double currentRateTo = rates.getOrDefault(to, 1.0);
                double historicalRateFrom = historicalRates.getDouble(from);
                double historicalRateTo = historicalRates.getDouble(to);

                double currentRate = currentRateTo / currentRateFrom;
                double historicalRate = historicalRateTo / historicalRateFrom;

                resultLabel.setText(String.format("Current Rate: %.4f\nHistorical Rate: %.4f", currentRate, historicalRate));
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch historical data. Please check the date format or the API status.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch historical rates: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CurrencyConverter().setVisible(true);
        });
    }
}
