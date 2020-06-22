//package com.alibaba.csp.sentinel.qlearning;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartUtilities;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
//import org.jfree.chart.plot.CategoryPlot;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.renderer.category.LineAndShapeRenderer;
//import org.jfree.data.category.CategoryDataset;
//import org.jfree.data.general.DatasetUtilities;
//
//public class QLearningEvaluation {
//
//
//    public void addRTArray(double avgRT) {
//        this.avgRTArray.add(avgRT);
//    }
//
//    private ArrayList<Double> avgRTArray = new ArrayList<Double>();
//
//
//    /**
//     * 创建JFreeChart Line Chart（折线图）
//     */
//    public static void main(String[] args) {
//        // 步骤1：创建CategoryDataset对象（准备数据）
//        CategoryDataset dataset = createDataset();
//        // 步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置
//        JFreeChart freeChart = createChart(dataset);
//        // 步骤3：将JFreeChart对象输出到文件，Servlet输出流等
//        saveAsFile(freeChart, "E:\\line.jpg", 600, 400);
//    }
//
//    // 保存为文件
//    public static void saveAsFile(JFreeChart chart, String outputPath,
//                                  int weight, int height) {
//        FileOutputStream out = null;
//        try {
//            File outFile = new File(outputPath);
//            if (!outFile.getParentFile().exists()) {
//                outFile.getParentFile().mkdirs();
//            }
//            out = new FileOutputStream(outputPath);
//            // 保存为PNG
//            // ChartUtilities.writeChartAsPNG(out, chart, 600, 400);
//            // 保存为JPEG
//            ChartUtilities.writeChartAsJPEG(out, chart, 600, 400);
//            out.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    // do nothing
//                }
//            }
//        }
//    }
//
//    // 根据CategoryDataset创建JFreeChart对象
//    public static JFreeChart createChart(CategoryDataset categoryDataset) {
//        // 创建JFreeChart对象：ChartFactory.createLineChart
//        JFreeChart jfreechart = ChartFactory.createLineChart("不同类别按小时计算拆线图", // 标题
//                "年分", // categoryAxisLabel （category轴，横轴，X轴标签）
//                "数量", // valueAxisLabel（value轴，纵轴，Y轴的标签）
//                categoryDataset, // dataset
//                PlotOrientation.VERTICAL, true, // legend
//                false, // tooltips
//                false); // URLs
//        // 使用CategoryPlot设置各种参数。以下设置可以省略。
//        CategoryPlot plot = (CategoryPlot) jfreechart.getPlot();
//        // 背景色 透明度
//        plot.setBackgroundAlpha(0.5f);
//        // 前景色 透明度
//        plot.setForegroundAlpha(0.5f);
//        // 其他设置 参考 CategoryPlot类
//        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
//        renderer.setBaseShapesVisible(true); // series 点（即数据点）可见
//        renderer.setBaseLinesVisible(true); // series 点（即数据点）间有连线可见
//        renderer.setUseSeriesOffset(true); // 设置偏移量
//        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
//        renderer.setBaseItemLabelsVisible(true);
//        return jfreechart;
//    }
//
//    /**
//     * 创建CategoryDataset对象
//     *
//     */
//    public static CategoryDataset createDataset() {
//        String[] rowKeys = {"Average RT"};
////        String[] colKeys = {"0:00", "1:00", "2:00", "7:00", "8:00", "9:00",
////                "10:00", "11:00", "12:00", "13:00", "16:00", "20:00", "21:00",
////                "23:00"};
//
//        //时间轴！
//
//        //获取所有存储的RT值。
//
//        double[][] data = {{4, 3, 1, 1, 1, 1, 2, 2, 2, 1, 8, 2, 1, 1},};
//        // 或者使用类似以下代码
//        // DefaultCategoryDataset categoryDataset = new
//        // DefaultCategoryDataset();
//        // categoryDataset.addValue(10, "rowKey", "colKey");
//
//        return DatasetUtilities.createCategoryDataset(rowKeys, data);
//    }
//
//    private static class QLearningEvaluationContainer {
//        private static QLearningEvaluation instance = new QLearningEvaluation();
//    }
//
//    public static QLearningEvaluation getInstance() {
//        return QLearningEvaluation.QLearningEvaluationContainer.instance;
//    }
//}
