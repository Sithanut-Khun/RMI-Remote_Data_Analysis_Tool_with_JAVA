package client.gui.components;

import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ResultsPanel extends JPanel {
    private JTable resultsTable;
    private JScrollPane scrollPane;
    private static final Logger logger = Logger.getLogger(ResultsPanel.class.getName());

    public ResultsPanel() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        // Initialize table with empty model
        resultsTable = new JTable(new DefaultTableModel());
        configureTableAppearance();
        
        scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    private void configureTableAppearance() {
        resultsTable.setAutoCreateRowSorter(true);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setRowHeight(25);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Summary Statistics"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void displayResults(Map<String, Map<String, String>> results, 
                             Map<String, String> columnTypes) {

        logger.info("Recieved results from server. Displaying results in ResultsPanel");                        
        if (results == null || results.isEmpty()) {
            showEmptyMessage();
            return;
        }

        // Get all unique statistic keys (row names)
        Set<String> statKeys = results.values().stream()
            .flatMap(stats -> stats.keySet().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // Prepare column names
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Statistic");
        columnNames.addAll(results.keySet());

        // Prepare data model
        DefaultTableModel model = createTableModel(statKeys, columnNames, results, columnTypes);
        resultsTable.setModel(model);
        configureColumnRenderers();
    }

    private DefaultTableModel createTableModel(Set<String> statKeys, 
                                            List<String> columnNames,
                                            Map<String, Map<String, String>> results,
                                            Map<String, String> columnTypes) {
        Object[][] data = new Object[statKeys.size()][columnNames.size()];
        
        int row = 0;
        for (String stat : statKeys) {
            data[row][0] = stat;
            
            for (int col = 1; col < columnNames.size(); col++) {
                String resultCol = columnNames.get(col);
                String colType = columnTypes.getOrDefault(resultCol, "number");
                String value = results.get(resultCol).getOrDefault(stat, "");
                
                if (!"count".equalsIgnoreCase(stat) && "string".equalsIgnoreCase(colType)) {
                    value = "NaN";
                }
                data[row][col] = value;
            }
            row++;
        }

        return new DefaultTableModel(data, columnNames.toArray()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? String.class : Object.class;
            }
        };
    }

    private void configureColumnRenderers() {
        // Left-align first column
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        // Center-align other columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 1; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void showEmptyMessage() {
        resultsTable.setModel(new DefaultTableModel(
            new Object[][]{{"No results to display"}}, 
            new String[]{""}
        ));
        
        // Center-align the message
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
    }

    // Optional: Add method to access the table if needed
    public JTable getResultsTable() {
        return resultsTable;
    }
}