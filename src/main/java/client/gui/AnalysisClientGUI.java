package client.gui;

import javax.swing.*;
import java.awt.*;
import shared.DataService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import client.gui.components.DataInputPanel;
import client.gui.components.ResultsPanel;
import client.gui.components.CustomeChartPanel;

public class AnalysisClientGUI extends JFrame {
    private DataService dataService;
    private DataInputPanel dataInputPanel;
    private ResultsPanel resultsPanel;
    private CustomeChartPanel chartPanel;

    public AnalysisClientGUI() {
        initializeRMI();
        setupUI();
    }

    private void initializeRMI() {

        int[] portsToTry = {1099, 2099, 3099, 4099};
        boolean connected = false;

        for (int port : portsToTry) {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", port);
                dataService = (DataService) registry.lookup("DataService");
                System.out.println("Connected to RMI server on port " + port + ": " + dataService.testConnection());
                connected = true;
                break;
            } catch (Exception e) {
                System.out.println("Failed to connect on port " + port + ": " + e.getMessage());
            }
        }

        if (!connected) {
            JOptionPane.showMessageDialog(this, "Error connecting to server on all ports.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
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
        dataInputPanel.setAnalysisListener((results, data) -> {
            resultsPanel.displayResults(results);
            chartPanel.updateChart(data, results);
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