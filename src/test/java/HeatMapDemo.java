import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.ui.RectangleAnchor;

import javax.swing.*;
import java.awt.*;

public class HeatMapDemo extends JFrame {

    public HeatMapDemo() {
        setTitle("Heat Map Example");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JFreeChart heatmapChart = createHeatMapChart();
        ChartPanel chartPanel = new ChartPanel(heatmapChart);
        setContentPane(chartPanel);
    }

    private JFreeChart createHeatMapChart() {
        DefaultXYZDataset dataset = new DefaultXYZDataset();

        // Sample data (x, y, z)
        double[] xValues = new double[] {0, 1, 2, 0, 1, 2, 0, 1, 2};
        double[] yValues = new double[] {0, 0, 0, 1, 1, 1, 2, 2, 2};
        double[] zValues = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};

        double[][] data = new double[][] { xValues, yValues, zValues };
        dataset.addSeries("Heat Map", data);

        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockWidth(1.0);
        renderer.setBlockHeight(1.0);
        // Define a gradient color scale using LookupPaintScale
        LookupPaintScale scale = new LookupPaintScale(1, 9, Color.BLUE);
        scale.add(1, Color.BLUE);
        scale.add(5, Color.GREEN);
        scale.add(9, Color.RED);
        renderer.setPaintScale(scale);
        renderer.setPaintScale(scale);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

        JFreeChart chart = new JFreeChart("Heat Map Example", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeatMapDemo demo = new HeatMapDemo();
            demo.setVisible(true);
        });
    }
}
