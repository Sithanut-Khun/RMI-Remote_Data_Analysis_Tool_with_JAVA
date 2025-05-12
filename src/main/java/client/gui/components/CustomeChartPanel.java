package client.gui.components;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class CustomeChartPanel extends JPanel {
    private JFreeChart chart;
    private JComboBox<String> chartTypeComboBox;

    public CustomeChartPanel(JFreeChart chart) {
        this.chart = chart;
        initComponents();
    }

    public CustomeChartPanel() {
        initComponents();
    }

    private void initComponents() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        chart = ChartFactory.createBarChart(
            "Data Analysis Results",
            "Statistics",
            "Value",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 300));

        chartTypeComboBox = new JComboBox<>(new String[]{"Bar Chart", "Line Chart", "Pie Chart"});
        chartTypeComboBox.addActionListener(e -> updateChartType());

        setLayout(new BorderLayout());
        add(chartTypeComboBox, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    private void updateChartType() {
        String selectedType = (String) chartTypeComboBox.getSelectedItem();
        DefaultCategoryDataset dataset = (DefaultCategoryDataset) chart.getCategoryPlot().getDataset();

        switch (selectedType) {
            case "Bar Chart":
                chart = ChartFactory.createBarChart(
                    "Data Analysis Results",
                    "Statistics",
                    "Value",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
                );
                break;
            case "Line Chart":
                chart = ChartFactory.createLineChart(
                    "Data Analysis Results",
                    "Statistics",
                    "Value",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
                );
                break;
            case "Pie Chart":
                // Pie chart requires a different dataset, so this is a placeholder
                JOptionPane.showMessageDialog(this, "Pie Chart is not yet implemented.");
                break;
        }

        removeAll();
        initComponents();
        revalidate();
        repaint();
    }

    public void updateChart(List<Double> data, Map<String, Double> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (results != null) {
            results.forEach((key, value) ->
                dataset.addValue(value, "Statistics", key));
        }

        chart.getCategoryPlot().setDataset(dataset);
    }

    // private DefaultCategoryDataset createDatasetFromInput() {
    //     DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    //     if (dataInputPanel != null) {
    //         Map<String, Double> inputData = dataInputPanel.; // Assuming DataInputPanel has a method getInputData()
    //         inputData.forEach((key, value) -> dataset.addValue(value, "Statistics", key));
    //     }

    //     return dataset;
    // }
}