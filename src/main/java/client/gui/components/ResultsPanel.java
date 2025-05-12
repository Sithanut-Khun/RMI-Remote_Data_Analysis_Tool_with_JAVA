package client.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ResultsPanel extends JPanel {
    private JTextArea resultsArea;

    public ResultsPanel() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        resultsArea = new JTextArea(10, 40);
        resultsArea.setEditable(false);
        resultsArea.setBorder(BorderFactory.createTitledBorder("Analysis Results"));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void displayResults(Map<String, Double> results) {
        StringBuilder sb = new StringBuilder();
        
        if (results == null || results.isEmpty()) {
            sb.append("No results to display");
        } else {
            results.forEach((key, value) -> 
                sb.append(String.format("%-15s: %.4f%n", key, value)));
        }
        
        resultsArea.setText(sb.toString());
    }
}