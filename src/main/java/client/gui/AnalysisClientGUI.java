package client.gui;

import javax.swing.*;
import java.awt.*;
import shared.DataService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import client.gui.components.DataInputPanel;
import client.gui.components.ResultsPanel;
import client.gui.components.CustomeChartPanel;

public class AnalysisClientGUI extends JFrame {
    private DataService dataService;
    private DataInputPanel dataInputPanel;
    private ResultsPanel resultsPanel;
    private CustomeChartPanel chartPanel;

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

            String response = dataService.testConnection();
            System.out.println("✅ Connected to RMI server on port " + port + ": " + response);
            return;

        } catch (Exception e) {
            System.err.println("❌ Attempt " + attempt + " failed: " + e.getMessage());

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
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create components
        dataInputPanel = new DataInputPanel(dataService);
        resultsPanel = new ResultsPanel();
        chartPanel = new CustomeChartPanel();
        
        // Add components to main panel
        mainPanel.add(dataInputPanel, BorderLayout.NORTH);
        mainPanel.add(resultsPanel, BorderLayout.CENTER);
        mainPanel.add(chartPanel, BorderLayout.SOUTH);
        
        // Set up communication between panels
        dataInputPanel.setAnalysisListener((results, numericData) -> {
        resultsPanel.displayResults(results);
        chartPanel.updateChart(numericData, results);
});


           add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnalysisClientGUI client = new AnalysisClientGUI();
            client.setVisible(true);
        });
    }
}
