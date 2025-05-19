package client.gui.components;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class CustomeChartPanel extends JPanel {
    private JFreeChart chart;
    private JComboBox<String> chartTypeComboBox;

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
        }

        removeAll();
        initComponents();
        revalidate();
        repaint();
    }

    

    public void updateChart(List<Double> data, Map<String, Map<String, String>> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (results != null) {
            results.forEach((column, stats) -> {
                if (stats.containsKey("mean")) {
                    try {
                        double mean = Double.parseDouble(stats.get("mean"));
                        dataset.addValue(mean, "Statistics", column);
                    } catch (NumberFormatException e) {
                        // Handle invalid number formats gracefully
                    }
                }
            });
        }

        // Update the chart with the new dataset
        if (chart != null) {
            chart.getCategoryPlot().setDataset(dataset);
        }
    }
}
