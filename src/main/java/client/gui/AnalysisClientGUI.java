package client.gui;

import java.util.logging.Logger;

import javax.swing.*;
import shared.DataService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;

import client.gui.components.DataInputPanel;
import client.gui.components.ResultsPanel;
import client.gui.components.CustomeChartPanel;

public class AnalysisClientGUI extends JFrame {
    private DataService dataService;
    private DataInputPanel dataInputPanel;
    private ResultsPanel resultsPanel;
    private CustomeChartPanel chartPanel;
    private static final Logger logger = Logger.getLogger(AnalysisClientGUI.class.getName());

    public AnalysisClientGUI() {
        initializeRMI(); // Make sure to call this to establish the connection
        setupUI();
    }

    private void initializeRMI() {
    final String host = "127.0.0.1"; // Force loopback
    final int port = 1099;
    final int retryDelay = 1000;
    final int maxAttempts = 3;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            System.out.println("Attempting connection to port " + port + " (attempt " + attempt + ")");

            Registry registry = LocateRegistry.getRegistry(host, port);
            dataService = (DataService) registry.lookup("DataService");

            // String response = dataService.testConnection();
            System.out.println("Connected to RMI server on port " + port);
            return;

        } catch (Exception e) {
            System.err.println("Attempt " + attempt + " failed: " + e.getMessage());

            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to connect to RMI server after " + maxAttempts + " attempts.\n" +
                    "Make sure the server and rmiregistry are running on localhost:1099.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }
}

private void setupUI() {
    setTitle("Remote Data Analysis Tool");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000, 800);  // Increased height for better default visibility
    setLocationRelativeTo(null);

    // Main container with BoxLayout to allow vertical stacking
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Create components
    dataInputPanel = new DataInputPanel(dataService);
    resultsPanel = new ResultsPanel();
    chartPanel = new CustomeChartPanel();  // Ensure this is correctly named and implemented

    // Optional: Titled borders for visual grouping
    dataInputPanel.setBorder(BorderFactory.createTitledBorder("Data Input"));
    resultsPanel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));
    chartPanel.setBorder(BorderFactory.createTitledBorder("Visualization Chart"));
    // Set background colors for better visibility
    dataInputPanel.setBackground(Color.LIGHT_GRAY);
    resultsPanel.setBackground(Color.WHITE);
    chartPanel.setBackground(Color.WHITE);

    // Add components to main panel
    mainPanel.add(dataInputPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(resultsPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(chartPanel);

    // Add Exit button at the bottom
    JButton exitButton = new JButton("Exit");
    exitButton.addActionListener(e -> {
        logger.info("Exiting the application.");
        JOptionPane.showMessageDialog(this, "Exiting the application.");
        System.exit(0);
    });
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(exitButton);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(buttonPanel);

    // Add scroll pane to mainPanel
    JScrollPane scrollPane = new JScrollPane(mainPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling

    // Add scrollPane to frame
    add(scrollPane);

    // Listener for processing results and charts
    dataInputPanel.setAnalysisListener((results, parsedData) -> {
        Map<String, String> columnTypes = new HashMap<>();
        resultsPanel.displayResults(results, columnTypes);
        chartPanel.updateChart(parsedData, results);
    });

}



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnalysisClientGUI client = new AnalysisClientGUI();
            client.setVisible(true);
        });
    }
}
