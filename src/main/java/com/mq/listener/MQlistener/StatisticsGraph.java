package com.mq.listener.MQlistener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JFrame;

public class StatisticsGraph extends JFrame {

    public StatisticsGraph(String title) {
        super(title);
        JFreeChart barChart = createChart(createDataset());
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Assuming you're graphing the PUTS metric for each queue
        for (Map.Entry<String, Map<String, Integer>> entry : queueStatsMap.entrySet()) {
            String queueName = entry.getKey();
            int puts = entry.getValue().getOrDefault("PUTS", 0);
            dataset.addValue(puts, "PUTS", queueName);
        }

        return dataset;
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset) {
        return ChartFactory.createBarChart(
                "Queue Statistics",
                "Queue Name",
                "PUTS",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            StatisticsGraph graph = new StatisticsGraph("Queue Statistics");
            graph.pack();
            graph.setVisible(true);
        });
    }
}