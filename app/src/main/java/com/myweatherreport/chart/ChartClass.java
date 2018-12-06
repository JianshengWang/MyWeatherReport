package com.myweatherreport.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;

/**
 * Created by hp-pc on 2018/11/23.
 */

public class ChartClass {

    public static void initChart(LineChart lineChart, XAxis xAxis, YAxis leftYAxis, YAxis rightYaxis, Legend legend, float yMin) {
        lineChart.setDrawGridBackground(false);//是否展示网格线
        lineChart.setDrawBorders(true);  //是否显示边界
        //lineChart.setBackgroundColor(0x8000);  //背景为透明
        lineChart.setDrawBorders(false);  //是否显示边框
        lineChart.setDragEnabled(false);  //是否可以拖动
        lineChart.setTouchEnabled(false);  //是否有触摸事件
        lineChart.animateX(1500);
        lineChart.animateY(2500);  //xy轴的动画效果

        Description description = new Description();//隐藏X轴描述
        description.setEnabled(false);
        lineChart.setDescription(description);

        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYaxis = lineChart.getAxisRight();  //xy轴的设置
        xAxis.setDrawGridLines(false);
        rightYaxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(true);  //去除XY轴自己的网格线
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);  //设置网格线为虚线
        rightYaxis.setEnabled(false);  //去掉右侧y轴
        leftYAxis.setTextColor(Color.WHITE);
        //x轴设置显示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        //y轴从0开始，不然会上移一点
        leftYAxis.setAxisMinimum(0f);
        rightYaxis.setAxisMinimum(0f);

        /***折线图标签设置***/
        legend = lineChart.getLegend();
        //设置类型
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(18f);
        legend.setTextColor(Color.WHITE);
        //显示位置左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里
        legend.setDrawInside(false);
        //不显示名称
        //legend.setEnabled(false);
        //不允许出边界
        legend.setWordWrapEnabled(true);

        //警戒线
        LimitLine limitLine = new LimitLine(0f, "零度线");
        limitLine.setLineColor(Color.BLACK);
        limitLine.setLineWidth(4f);
        limitLine.setTextSize(12f);
        leftYAxis.addLimitLine(limitLine);
    }

    /*** 初始化一条曲线 lineDataSet   颜色为color***/
    public static void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode){
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(4f);
        lineDataSet.setCircleRadius(6f);
        //设置点是实心还是空心
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setValueTextSize(14f);
        //设置折线图填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(4f);
        lineDataSet.setFormSize(15.f);

        lineDataSet.setValueTextColor(Color.WHITE);

        //设置圆滑曲线，如果没有则为折线
        if (mode == null){
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            lineDataSet.setMode(mode);
        }
    }

}
