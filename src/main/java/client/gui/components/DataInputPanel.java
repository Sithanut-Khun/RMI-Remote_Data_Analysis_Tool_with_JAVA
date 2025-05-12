package client.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import shared.DataService;
import java.rmi.RemoteException;
import java.util.Map;

public class DataInputPanel extends JPanel {
    // private JTextArea dataInputArea;
    private JButton analyzeButton;
    private JButton loadFileButton;
    private DataService dataService;
    private AnalysisListener analysisListener;

    public DataInputPanel(DataService dataService) {
        this.dataService = dataService;
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        analyzeButton = new JButton("Analyze Data");
        analyzeButton.addActionListener(this::performAnalysis);

        loadFileButton = new JButton("Load CSV File");
        loadFileButton.addActionListener(this::loadFileData);

        // Table to display CSV data
        String[] columnNames = {"Column1", "Column2"}; // Placeholder column names
        String[][] data = {}; // Placeholder empty data
        JTable csvTable = new JTable(data, columnNames);
        csvTable.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(csvTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("CSV Data"));

        // Add the table to the panel
        this.csvTable = csvTable;
        this.tableScrollPane = tableScrollPane;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("<html><h1 style='text-align:center;'>Welcome to Remote Data Analysis</h1><p style='text-align:center;'>Please Input CSV file to Analyse</p></html>", SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        add(tableScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(analyzeButton);
        buttonPanel.add(loadFileButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void performAnalysis(ActionEvent e) {
        try {
            List<Double> data = new ArrayList<>();

            if (csvTable.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Please load a CSV file to analyze",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Combine data from CSV table
            for (int i = 0; i < csvTable.getRowCount(); i++) {
                for (int j = 0; j < csvTable.getColumnCount(); j++) {
                    try {
                        data.add(Double.parseDouble(csvTable.getValueAt(i, j).toString()));
                    } catch (NumberFormatException ex) {
                        // Ignore invalid numbers in the table
                    }
                }
            }

            Map<String, Double> results = dataService.analyzeData(data);

            if (analysisListener != null) {
                analysisListener.onAnalysisPerformed(results, data);
            }
        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Error communicating with server: " + ex.getMessage(),
                    "Communication Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFileData(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && selectedFile.getName().endsWith(".csv")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    String line = reader.readLine();
                    if (line == null) {
                        JOptionPane.showMessageDialog(this, "The file is empty",
                                "File Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Parse the header (column names)
                    String[] columnNames = line.split(",");
                    for (int i = 0; i < columnNames.length; i++) {
                        columnNames[i] = columnNames[i].trim().replaceAll("[^a-zA-Z0-9_ ]", "");
                    }

                    // Parse the data rows
                    List<String[]> dataRows = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        dataRows.add(line.split(","));
                    }

                    // Update the table model with new data
                    String[][] data = dataRows.toArray(new String[0][]);
                    csvTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(),
                            "File Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a valid CSV file",
                        "File Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    

    private JTable csvTable;
    private JScrollPane tableScrollPane;

    public void setAnalysisListener(AnalysisListener listener) {
        this.analysisListener = listener;
    }

    public interface AnalysisListener {
        void onAnalysisPerformed(Map<String, Double> results, List<Double> originalData);
    }
}