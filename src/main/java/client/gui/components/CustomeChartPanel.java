package client.gui.components;

import java.util.logging.Logger;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.chart.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

public class CustomeChartPanel extends JPanel {
    private JComboBox<String> chartTypeCombo;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> valueCombo;
    private JComboBox<String> aggregationCombo;
    private JPanel chartContainer;
    private JComboBox<String> xAxisCombo;
    private JComboBox<String> yAxisCombo;
    private JComboBox<String> legendCombo;
    private static final Logger logger = Logger.getLogger(CustomeChartPanel.class.getName());


    private List<Map<String, String>> parsedCSVRows;

    public CustomeChartPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel controlPanel = new JPanel(new GridLayout(4, 4, 10, 10));

        chartTypeCombo = new JComboBox<>(new String[]{"Pie Chart", "Bar Chart", "Stacked Bar Chart", "Scatter Plot"});
        categoryCombo = new JComboBox<>();
        valueCombo = new JComboBox<>();
        aggregationCombo = new JComboBox<>(new String[]{"Count", "Sum"});
        xAxisCombo = new JComboBox<>();
        yAxisCombo = new JComboBox<>();
        legendCombo = new JComboBox<>();

        JButton generateButton = new JButton("Generate Chart");
        generateButton.addActionListener(this::generateChart);

        chartTypeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateControlVisibility((String) chartTypeCombo.getSelectedItem());
            }
        });

        controlPanel.add(new JLabel("Chart Type:"));
        controlPanel.add(chartTypeCombo);
        controlPanel.add(new JLabel("Category Column:"));
        controlPanel.add(categoryCombo);
        controlPanel.add(new JLabel("Value Column:"));
        controlPanel.add(valueCombo);
        controlPanel.add(new JLabel("Aggregation Type:"));
        controlPanel.add(aggregationCombo);
        controlPanel.add(new JLabel("X-Axis Column:"));
        controlPanel.add(xAxisCombo);
        controlPanel.add(new JLabel("Y-Axis Column:"));
        controlPanel.add(yAxisCombo);
        controlPanel.add(new JLabel("Legend Column:"));
        controlPanel.add(legendCombo);

        add(controlPanel, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(generateButton);
        add(buttonPanel, BorderLayout.CENTER);

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Chart Display"));
        add(chartContainer, BorderLayout.SOUTH);

        updateControlVisibility((String) chartTypeCombo.getSelectedItem());
    }

    private void updateControlVisibility(String chartType) {
        boolean isPie = chartType.equals("Pie Chart");
        boolean isBar = chartType.equals("Bar Chart");
        boolean isStackedBar = chartType.equals("Stacked Bar Chart");
        boolean isScatter = chartType.equals("Scatter Plot");

        categoryCombo.setEnabled(isPie || isBar || isStackedBar);
        valueCombo.setEnabled(isPie || isBar || isStackedBar);
        aggregationCombo.setEnabled(isPie || isBar || isStackedBar);
        xAxisCombo.setEnabled(isScatter);
        yAxisCombo.setEnabled(isScatter);
        legendCombo.setEnabled(isScatter || isStackedBar || isBar);
    }

    public void populateColumnSelectors(List<Map<String, String>> parsedCSVRows) {
        this.parsedCSVRows = parsedCSVRows;

        Set<String> columns = new LinkedHashSet<>();
        for (Map<String, String> row : parsedCSVRows) {
            columns.addAll(row.keySet());
        }

        categoryCombo.removeAllItems();
        valueCombo.removeAllItems();
        xAxisCombo.removeAllItems();
        yAxisCombo.removeAllItems();
        legendCombo.removeAllItems();
        legendCombo.addItem("None");

        for (String col : columns) {
            categoryCombo.addItem(col);
            valueCombo.addItem(col);
            xAxisCombo.addItem(col);
            yAxisCombo.addItem(col);
            legendCombo.addItem(col);
        }
    }

    private void generateChart(ActionEvent e) {
        String chartType = (String) chartTypeCombo.getSelectedItem();
        logger.info("User selected chart type: " + chartType);

        if (chartType == null) {
            JOptionPane.showMessageDialog(this, "Please select a chart type.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (chartType) {
            case "Pie Chart":
                renderPieChart((String) categoryCombo.getSelectedItem(), (String) valueCombo.getSelectedItem(), (String) aggregationCombo.getSelectedItem());
                break;
            case "Bar Chart":
                renderBarChart((String) categoryCombo.getSelectedItem(), (String) valueCombo.getSelectedItem(), (String) aggregationCombo.getSelectedItem());
                break;
            case "Scatter Plot":
                renderScatterPlot((String) xAxisCombo.getSelectedItem(), (String) yAxisCombo.getSelectedItem(), (String) legendCombo.getSelectedItem());
                break;
            case "Stacked Bar Chart":
                renderStackedBarChart((String) categoryCombo.getSelectedItem(), (String) valueCombo.getSelectedItem(), (String) legendCombo.getSelectedItem(), (String) aggregationCombo.getSelectedItem());
                break;
        }
    }

    private void renderPieChart(String categoryCol, String valueCol, String aggregation) {
        if (categoryCol == null || valueCol == null || aggregation == null) return;

        Map<String, Double> aggregatedData = aggregateData(categoryCol, valueCol, aggregation);
        DefaultPieDataset dataset = new DefaultPieDataset();
        double total = aggregatedData.values().stream().mapToDouble(Double::doubleValue).sum();

        for (Map.Entry<String, Double> entry : aggregatedData.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart("Pie Chart", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new PieSectionLabelGenerator() {
            private final DecimalFormat df = new DecimalFormat("0.00");

            @Override
            public String generateSectionLabel(org.jfree.data.general.PieDataset dataset1, Comparable key) {
                Number value = dataset1.getValue(key);
                double percent = value.doubleValue() / total * 100;
                return String.format("%s: %.0f (%.2f%%)", key, value.doubleValue(), percent);
            }

            @Override
            public AttributedString generateAttributedSectionLabel(org.jfree.data.general.PieDataset dataset, Comparable key) {
                return null;
            }
        });

        updateChartDisplay(chart);
    }



    private void renderBarChart(String categoryCol, String valueCol, String aggregation) {
        String legendCol = (String) legendCombo.getSelectedItem(); // <-- get legend column
        
        if ("None".equals(legendCol)) {
            legendCol = null;
        }
        if (categoryCol == null || valueCol == null || aggregation == null) return;

        boolean showLegend = (legendCol != null && !legendCol.isEmpty());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (showLegend) {
            // Group by category and legend
            Map<String, Map<String, Double>> grouped = new LinkedHashMap<>();
            for (Map<String, String> row : parsedCSVRows) {
                String category = row.get(categoryCol);
                String legend = row.get(legendCol);
                String valueStr = row.get(valueCol);
                if (category == null || legend == null || valueStr == null) continue;
                grouped.putIfAbsent(category, new LinkedHashMap<>());
                Map<String, Double> legendMap = grouped.get(category);
                legendMap.putIfAbsent(legend, 0.0);
                try {
                    if (aggregation.equals("Sum")) {
                        double value = Double.parseDouble(valueStr);
                        legendMap.put(legend, legendMap.get(legend) + value);
                    } else if (aggregation.equals("Count")) {
                        legendMap.put(legend, legendMap.get(legend) + 1);
                    }
                } catch (NumberFormatException ignored) {}
            }
            for (Map.Entry<String, Map<String, Double>> entry : grouped.entrySet()) {
                String category = entry.getKey();
                for (Map.Entry<String, Double> legendEntry : entry.getValue().entrySet()) {
                    dataset.addValue(legendEntry.getValue(), legendEntry.getKey(), category);
                }
            }
        } else {
            // No legend, group by category only
            Map<String, Double> aggregatedData = aggregateData(categoryCol, valueCol, aggregation);
            for (Map.Entry<String, Double> entry : aggregatedData.entrySet()) {
                dataset.addValue(entry.getValue(), valueCol, entry.getKey());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Bar Chart",
                categoryCol,
                aggregation + " of " + valueCol,
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                showLegend,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setInsets(new RectangleInsets(10.0, 5.0, 5.0, 5.0));

        updateChartDisplay(chart);
    }

    private void renderScatterPlot(String xCol, String yCol, String legendCol) {
        if (xCol == null || yCol == null) {
            JOptionPane.showMessageDialog(this, "Please select both X and Y axis columns.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, List<double[]>> categorizedData = new LinkedHashMap<>();

        for (Map<String, String> row : parsedCSVRows) {
            try {
                double x = Double.parseDouble(row.get(xCol));
                double y = Double.parseDouble(row.get(yCol));
                String category = (legendCol != null && row.containsKey(legendCol)) ? row.get(legendCol) : "Data Points";

                categorizedData.putIfAbsent(category, new ArrayList<>());
                categorizedData.get(category).add(new double[]{x, y});
            } catch (Exception ignored) {
            }
        }

        if (categorizedData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selected columns must be numeric and contain valid data.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultXYDataset dataset = new DefaultXYDataset();
        for (Map.Entry<String, List<double[]>> entry : categorizedData.entrySet()) {
            List<double[]> points = entry.getValue();
            double[][] data = new double[2][points.size()];
            for (int i = 0; i < points.size(); i++) {
                data[0][i] = points.get(i)[0];
                data[1][i] = points.get(i)[1];
            }
            dataset.addSeries(entry.getKey(), data);
        }

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Scatter Plot",
                xCol,
                yCol,
                dataset
        );

        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setDefaultToolTipGenerator((xyDataset, series, item) ->
                String.format("(%s, %s)", xyDataset.getX(series, item), xyDataset.getY(series, item)));

        updateChartDisplay(chart);
    }



    private void renderStackedBarChart(String categoryCol, String valueCol, String legendCol, String aggregation) {
        if (categoryCol == null || valueCol == null || legendCol == null || aggregation == null) return;

        // Map<category, Map<legend, aggregateValue>>
        Map<String, Map<String, Double>> stackedData = new LinkedHashMap<>();

        for (Map<String, String> row : parsedCSVRows) {
            String category = row.get(categoryCol);
            String valueStr = row.get(valueCol);
            String legend = row.get(legendCol);

            if (category == null || valueStr == null || legend == null) continue;

            stackedData.putIfAbsent(category, new LinkedHashMap<>());
            Map<String, Double> legendMap = stackedData.get(category);
            legendMap.putIfAbsent(legend, 0.0);

            try {
                if (aggregation.equals("Sum")) {
                    double value = Double.parseDouble(valueStr);
                    legendMap.put(legend, legendMap.get(legend) + value);
                } else if (aggregation.equals("Count")) {
                    legendMap.put(legend, legendMap.get(legend) + 1);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Map<String, Double>> entry : stackedData.entrySet()) {
            String category = entry.getKey();
            for (Map.Entry<String, Double> legendEntry : entry.getValue().entrySet()) {
                dataset.addValue(legendEntry.getValue(), legendEntry.getKey(), category);
            }
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
                "Stacked Bar Chart",
                categoryCol,
                aggregation + " of " + valueCol,
                dataset
        );

        updateChartDisplay(chart);
    }




    private Map<String, Double> aggregateData(String categoryCol, String valueCol, String aggregation) {
        Map<String, Double> aggregationMap = new LinkedHashMap<>();

        for (Map<String, String> row : parsedCSVRows) {
            String category = row.get(categoryCol);
            String valueStr = row.get(valueCol);

            if (category == null || valueStr == null) continue;

            aggregationMap.putIfAbsent(category, 0.0);

            try {
                if (aggregation.equals("Sum")) {
                    double value = Double.parseDouble(valueStr);
                    aggregationMap.put(category, aggregationMap.get(category) + value);
                } else if (aggregation.equals("Count")) {
                    aggregationMap.put(category, aggregationMap.get(category) + 1);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return aggregationMap;
    }

    private void updateChartDisplay(JFreeChart chart) {
        chartContainer.removeAll();
        chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    public void updateChart(List<Map<String, String>> parsedCSVRows, Map<String, Map<String, String>> results) {
        populateColumnSelectors(parsedCSVRows);
    }
}


