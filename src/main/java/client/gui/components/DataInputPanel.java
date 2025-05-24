package client.gui.components;

import java.util.logging.Logger;

import shared.DataService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;


public class DataInputPanel extends JPanel {
    private JTable csvTable;
    private JButton analyzeButton;
    private JButton loadButton;
    private DataService dataService;
    private AnalysisListener analysisListener;
    private static final Logger logger = Logger.getLogger(DataInputPanel.class.getName());

    

    public interface AnalysisListener {
    void onAnalysisPerformed(
        Map<String, Map<String, String>> results,
        List<Map<String, String>> parsedCSVRows
    );
}



    public DataInputPanel(DataService dataService) {
        this.dataService = dataService;
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        csvTable = new JTable(new DefaultTableModel());
        csvTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
        csvTable.setFillsViewportHeight(true);

        loadButton = new JButton("Load CSV");
        loadButton.addActionListener(this::loadFileData);

        analyzeButton = new JButton("Analyze");
        analyzeButton.setEnabled(false);
        analyzeButton.addActionListener(this::performAnalysis);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(loadButton);
        buttonPanel.add(analyzeButton);

        add(new JScrollPane(csvTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setAnalysisListener(AnalysisListener listener) {
        this.analysisListener = listener;
    }

    private void performAnalysis(ActionEvent e) {
        DefaultTableModel model = (DefaultTableModel) csvTable.getModel();
        logger.info("Sending data to server for analysis");
        if (model.getRowCount() == 0) {
            showError("No data to analyze");
            return;
        }

        // Show column selection dialog with original column names
        ColumnSelectionDialog selectionDialog = new ColumnSelectionDialog(
            (JFrame)SwingUtilities.getWindowAncestor(this),
            getColumnNames(model)
        );
        
        if (!selectionDialog.showDialog()) {
            return; // User cancelled
        }

        List<String> selectedColumnNames = selectionDialog.getSelectedColumns();
        List<Integer> selectedColumnIndices = selectionDialog.getSelectedColumnIndices();

        // Prepare data for selected columns
        List<List<String>> csvData = prepareDataForAnalysis(model, selectedColumnIndices);
        List<Map<String, String>> parsedCSVRows = convertToRowMaps(selectedColumnNames, csvData);
        try {
            // Send to server with original column names
            Map<String, Map<String, String>> results = dataService.analyzeCSV(selectedColumnNames, csvData);

            // Trigger listener
            if (analysisListener != null) {
             analysisListener.onAnalysisPerformed(results, parsedCSVRows);
        }
        } catch (RemoteException ex) {
            showError("Server error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Analysis error: " + ex.getMessage());
        }
    }

    private List<String> getColumnNames(DefaultTableModel model) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            names.add(model.getColumnName(i));
        }
        return names;
    }

    private List<List<String>> prepareDataForAnalysis(DefaultTableModel model, List<Integer> columnIndices) {
        List<List<String>> data = new ArrayList<>();
        
        // Add all rows
        for (int row = 0; row < model.getRowCount(); row++) {
            List<String> rowData = new ArrayList<>();
            for (int colIndex : columnIndices) {
                Object value = model.getValueAt(row, colIndex);
                rowData.add(value != null ? value.toString() : "");
            }
            data.add(rowData);
        }
        
        return data;
    }


    private void loadFileData(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV Files", "csv", "txt"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadCSVFile(fileChooser.getSelectedFile());
        }
    }

    private void loadCSVFile(File file) {
        DefaultTableModel model = new DefaultTableModel();
        logger.info("Loading CSV file: " + file.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Read headers
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Empty CSV file");
            }

            // Process headers
            String[] headers = cleanHeaders(headerLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
            model.setColumnIdentifiers(headers);

            // Read data rows
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] row = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                model.addRow(cleanRowData(row, headers.length));
            }

            csvTable.setModel(model);
            analyzeButton.setEnabled(model.getRowCount() > 0);
        } catch (Exception ex) {
            showError("Error loading file: " + ex.getMessage());
            logger.severe("Error loading CSV file: " + ex.getMessage());
        }
    }

    private String[] cleanHeaders(String[] rawHeaders) {
        String[] cleaned = new String[rawHeaders.length];
        for (int i = 0; i < rawHeaders.length; i++) {
            cleaned[i] = rawHeaders[i].trim()
                .replaceAll("^\"|\"$", "")
                .replaceAll("[^\\w\\s]", "")
                .trim();
            if (cleaned[i].isEmpty()) {
                cleaned[i] = "Column " + (i + 1);
            }
        }
        return cleaned;
    }

    private String[] cleanRowData(String[] rawRow, int expectedLength) {
        String[] cleaned = new String[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            if (i < rawRow.length) {
                cleaned[i] = rawRow[i].trim().replaceAll("^\"|\"$", "");
            } else {
                cleaned[i] = "";
            }
        }
        return cleaned;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Helper dialog for column selection
    private static class ColumnSelectionDialog {
        private final JDialog dialog;
        private final List<JCheckBox> checkBoxes = new ArrayList<>();
        private final List<String> columnNames;

        public ColumnSelectionDialog(JFrame parent, List<String> columnNames) {
            this.columnNames = columnNames;
            dialog = new JDialog(parent, "Select Columns", true);
            initComponents();
        }

        private void initComponents() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            for (String name : columnNames) {
                JCheckBox cb = new JCheckBox(name);
                checkBoxes.add(cb);
                checkBoxPanel.add(cb);
            }

            JButton selectAll = new JButton("Select All");
            selectAll.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(true)));

            JButton selectNone = new JButton("Select None");
            selectNone.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(false)));

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            buttonPanel.add(selectAll);
            buttonPanel.add(selectNone);
            buttonPanel.add(okButton);

            panel.add(new JScrollPane(checkBoxPanel), BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(dialog.getParent());
        }

        public boolean showDialog() {
            dialog.setVisible(true);
            return checkBoxes.stream().anyMatch(JCheckBox::isSelected);
        }

        public List<String> getSelectedColumns() {
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isSelected()) {
                    selected.add(columnNames.get(i));
                }
            }
            return selected;
        }

        public List<Integer> getSelectedColumnIndices() {
            List<Integer> selected = new ArrayList<>();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isSelected()) {
                    selected.add(i);
                }
            }
            return selected;
        }
    }


    private List<Map<String, String>> convertToRowMaps(List<String> headers, List<List<String>> rows) {
        List<Map<String, String>> parsedRows = new ArrayList<>();
        for (List<String> row : rows) {
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                rowMap.put(headers.get(i), i < row.size() ? row.get(i) : "");
            }
            parsedRows.add(rowMap);
        }
        return parsedRows;
    }


}