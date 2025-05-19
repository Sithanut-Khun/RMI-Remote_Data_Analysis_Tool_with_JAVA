package client.gui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import shared.DataService;
import java.awt.event.ActionListener;


public class DataInputPanel extends JPanel {
    private JButton analyzeButton;
    private JButton loadFileButton;
    private DataService dataService;
    private AnalysisListener analysisListener;
    private JTable csvTable;
    private JScrollPane tableScrollPane;

    public DataInputPanel(DataService dataService) {
        this.dataService = dataService;
        initComponents();
        setupLayout();
    }

    // private void initComponents() {
    //     analyzeButton = new JButton("Analyze Data");
    //     analyzeButton.setEnabled(false); // Disabled until file is loaded
    //     analyzeButton.addActionListener(this::performAnalysis);

    //     loadFileButton = new JButton("Load CSV File");
    //     loadFileButton.addActionListener(this::loadFileData);

    //     csvTable = new JTable(new DefaultTableModel());
    //     csvTable.setFillsViewportHeight(true);
    //     tableScrollPane = new JScrollPane(csvTable);
    //     tableScrollPane.setBorder(BorderFactory.createTitledBorder("CSV Data"));
    // }


    private void initComponents() {
            // Initialize buttons with consistent styling
            loadFileButton = createButton("Load CSV File", true, this::loadFileData);
            analyzeButton = createButton("Analyze Data", false, this::performAnalysis);

            // Configure table with better default settings
            csvTable = new JTable(new DefaultTableModel()) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table non-editable
                }
            };
            
            // Table styling and configuration
            csvTable.setFillsViewportHeight(true);
            csvTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            csvTable.setAutoCreateRowSorter(true); // Enable sorting
            csvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Better for wide tables
            
            // Scroll pane configuration
            tableScrollPane = new JScrollPane(csvTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("CSV Data"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }    

    // Helper method for consistent button creation
    private JButton createButton(String text, boolean enabled, ActionListener listener) {
        JButton button = new JButton(text);
        button.setEnabled(enabled);
        button.addActionListener(listener);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 10, 5, 10));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        return button;
    }



    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("<html><h1 style='text-align:center;'>Welcome to Remote Data Analysis</h1>" +
                "<p style='text-align:center;'>Please load a CSV file to analyze</p></html>",
                SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        add(tableScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadFileButton);
        buttonPanel.add(analyzeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }



    private void performAnalysis(ActionEvent e) {
        DefaultTableModel model = (DefaultTableModel) csvTable.getModel();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to analyze", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show column selection dialog
        JPanel panel = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> checkBoxes = new ArrayList<>();
        
        for (int i = 0; i < model.getColumnCount(); i++) {
            JCheckBox checkBox = new JCheckBox(model.getColumnName(i));
            checkBoxes.add(checkBox);
            panel.add(checkBox);
        }

        JCheckBox selectAll = new JCheckBox("Select All");
        selectAll.addActionListener(evt -> {
            boolean selected = selectAll.isSelected();
            checkBoxes.forEach(cb -> cb.setSelected(selected));
        });
        panel.add(selectAll);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Select Columns to Analyze", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // Get selected column indices
        List<Integer> selectedColumns = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selectedColumns.add(i);
            }
        }

        if (selectedColumns.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No columns selected", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Extract data from selected columns
        List<List<String>> csvData = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            List<String> rowData = new ArrayList<>();
            for (int col : selectedColumns) {
                Object value = model.getValueAt(row, col);
                String strValue = value != null ? value.toString() : "";
                rowData.add(strValue);
            }
            csvData.add(rowData);
        }

        try {
            // Send to server for analysis
            Map<String, Map<String, String>> results = dataService.analyzeCSV(selectedColumns, csvData);

            // Prepare the data for the results panel and chart
            List<Double> numericData = new ArrayList<>();
            results.forEach((column, stats) -> {
                if (stats.containsKey("mean")) {
                    try {
                        numericData.add(Double.parseDouble(stats.get("mean")));
                    } catch (NumberFormatException ex) {
                        // Skip if the value is not numeric
                    }
                }
            });

            // Send results to listener for display
            if (analysisListener != null) {
                analysisListener.onAnalysisPerformed(results, numericData);
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Analysis listener not set", 
                        "Error", JOptionPane.WARNING_MESSAGE);
            }

        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, 
                    "Server error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "An unexpected error occurred: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
}



    private void loadFileData(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV Files", "csv", "txt")); // Allow both .csv and .txt

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.exists()) {
                JOptionPane.showMessageDialog(this,
                    "File does not exist",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadCSVFile(selectedFile);
        }
    }



    // private void loadFileData(ActionEvent e) {
    //     JFileChooser fileChooser = new JFileChooser();
    //     fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    //     fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

    //     int result = fileChooser.showOpenDialog(this);
    //     if (result == JFileChooser.APPROVE_OPTION) {
    //         File selectedFile = fileChooser.getSelectedFile();
    //         loadCSVFile(selectedFile);
    //     }
    // }

    // private void loadCSVFile(File file) {
    //     DefaultTableModel model = new DefaultTableModel();
    //     try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    //         String headerLine = reader.readLine();
    //         if (headerLine == null) throw new IllegalArgumentException("Empty CSV file");

    //         String[] headers = headerLine.split(",");
    //         for (int i = 0; i < headers.length; i++) {
    //             headers[i] = headers[i].replaceAll("[^a-zA-Z0-9 ]", "").trim();
    //         }
    //         model.setColumnIdentifiers(headers);

    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             model.addRow(line.split(","));
    //         }

    //         csvTable.setModel(model);
    //         analyzeButton.setEnabled(true);

    //     } catch (Exception ex) {
    //         JOptionPane.showMessageDialog(this,
    //                 "Error loading file: " + ex.getMessage(),
    //                 "Error", JOptionPane.ERROR_MESSAGE);
    //         ex.printStackTrace();
    //     }
    // }


    private void loadCSVFile(File file) {
        DefaultTableModel model = new DefaultTableModel();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Read and clean headers
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Empty CSV file");
            }

            // Handle quoted headers and trim
            String[] headers = headerLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim()
                            .replaceAll("^\"|\"$", "")  // Remove surrounding quotes
                            .replaceAll("[^a-zA-Z0-9 ]", ""); // Clean special chars
                if (headers[i].isEmpty()) {
                    headers[i] = "Column " + (i + 1); // Default name if empty
                }
            }
            model.setColumnIdentifiers(headers);

            // Read data rows
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines
                
                // Handle quoted values and trim
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                String[] cleanedRow = new String[headers.length];
                
                // Ensure we don't exceed header count
                for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                    cleanedRow[i] = row[i].trim().replaceAll("^\"|\"$", "");
                }
                // Fill any missing columns with empty strings
                for (int i = row.length; i < headers.length; i++) {
                    cleanedRow[i] = "";
                }
                
                model.addRow(cleanedRow);
            }

            csvTable.setModel(model);
            analyzeButton.setEnabled(model.getRowCount() > 0);
            System.out.println("Rows loaded: " + model.getRowCount());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading file: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
   }


    public void setAnalysisListener(AnalysisListener listener) {
        this.analysisListener = listener;
    }

    public interface AnalysisListener {
        void onAnalysisPerformed(Map<String, Map<String, String>> results, List<Double> data);
    }
}
