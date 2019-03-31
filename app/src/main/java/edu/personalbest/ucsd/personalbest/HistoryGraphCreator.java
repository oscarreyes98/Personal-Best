package edu.personalbest.ucsd.personalbest;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

import edu.personalbest.ucsd.personalbest.Calendar.ICalendar;

public class HistoryGraphCreator {

    final String[] monthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    final private int[] chartColors = new int[]{Color.rgb(60,100,255), Color.rgb(255, 100, 55), Color.rgb(191, 255, 53)};
    static final int USER = 0;
    static final int FRIEND = 1;
    final String TAG = "HistoryGraphCreator";

    private int code;
    private CombinedChart graph;

    public HistoryGraphCreator(CombinedChart graph, int code){
        this.graph = graph;
        this.code = code;
    }

    public HistoryGraphCreator create(CombinedChart graph, int code){
        this.graph = graph;
        this.code = code;
        return this;
    }

    // takes in calendar with date one month ago
    public HistoryGraphCreator axisSetup(ICalendar calendar){
        List<String> axisDates;
        axisDates = new ArrayList<>();

        for(int i = 0; i < 28; i ++){
            calendar.incrementDay();
            axisDates.add(monthNames[calendar.getDateFields()[1]] + " " + calendar.getDateFields()[0]);
        }

        XAxis xAxis = this.graph.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(axisDates));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(28);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(25f);


        YAxis yAxis = this.graph.getAxisLeft();
        yAxis.setAxisMinimum(-10);

        YAxis yAxis2 = this.graph.getAxisRight();
        yAxis2.setAxisMinimum(-10);
        yAxis2.setDrawGridLines(false);

        return this;
    }


    public HistoryGraphCreator graphDataSetup(float[][] stepData, int[] goals){
        // Retrieve week step data for bar graph
        BarDataSet weekSet = getMonthData(stepData);
        BarData weekData = new BarData(weekSet);

        // Create combined data for step data and goal lines
        CombinedData combWeekData = new CombinedData();

        // Sets step data
        combWeekData.setData(weekData);

        // Sets Goal Lines
        LineData goalLines = getGoalData(goals);
        combWeekData.setData(goalLines);

        // Creates Graph with goal and step data
        this.graph.setData(combWeekData);
        return this;
    }

    public HistoryGraphCreator legendSetup(){
        LegendEntry[] legend = new LegendEntry[3];
        legend[0] = new LegendEntry("Incidental Steps", Legend.LegendForm.SQUARE, Float.NaN, Float.NaN, null, chartColors[0] );
        legend[1] = new LegendEntry("Intentional Steps", Legend.LegendForm.SQUARE, Float.NaN, Float.NaN, null, chartColors[1] );
        legend[2] = new LegendEntry("Daily Step Goal", Legend.LegendForm.LINE, Float.NaN, Float.NaN, null, chartColors[2]);

        Legend chartLegend = this.graph.getLegend();
        chartLegend.setEnabled(false);


        chartLegend.setCustom(legend);
        if(code == FRIEND) {
            Log.d(TAG, "Friend's Graph");
            chartLegend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
            chartLegend.setWordWrapEnabled(true);
            chartLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            chartLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            chartLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        }

        chartLegend.setEnabled(true);
        return this;
    }

    public HistoryGraphCreator interactionSetup(){
        // Disables vertical drag and zoom
        this.graph.setScaleEnabled(true);
        this.graph.setPinchZoom(true);
        this.graph.setDoubleTapToZoomEnabled(true);
        this.graph.setScaleXEnabled(true);
        this.graph.setScaleYEnabled(false);
        // Limits view to between 7 days and 28 days
        this.graph.setVisibleXRangeMinimum(7);
        this.graph.setVisibleXRangeMaximum(28);

        if(code == USER) {
            Log.d(TAG, "My Graph");
            this.graph.zoom(4f, 1f, 0, 0);
            this.graph.moveViewToX(21);
        }
        // Removes description
        this.graph.getDescription().setEnabled(false);

        return this;
    }

    public CombinedChart getGraph(){
        return this.graph;
    }


    private BarDataSet getMonthData(float[][] stepData){
        ArrayList<BarEntry> week = new ArrayList<>();
        BarDataSet weekSet;

        // Load week step data from User object
        float[][] monthStepData = stepData;

        for (int i = 0; i < 28; i++){
            week.add(new BarEntry( ((float)i+0.5f), monthStepData[i]));
        }

        weekSet = new BarDataSet(week, "This Week's Step History");
        int[] barColors = new int[]{chartColors[0], chartColors[1]};
        weekSet.setColors(barColors);
        return weekSet;
    }

    private LineData getGoalData(int[] goals) {

        // Load Goal data
        int[] stepGoal = goals;

        LineDataSet[] goalDataSet = new LineDataSet[28];
        LineData goalLines = new LineData();

        for(int i = 0; i < 28; i++){
            ArrayList<Entry> dayGoal = new ArrayList<>();
            dayGoal.add(new Entry(i, stepGoal[i]));
            dayGoal.add(new Entry((i+1), stepGoal[i]));
            goalDataSet[i] = new LineDataSet(dayGoal, " ");
            goalDataSet[i].setDrawCircles(false);
            goalDataSet[i].setColor(chartColors[2]);
            goalDataSet[i].setLineWidth(2.5f);
            goalLines.addDataSet(goalDataSet[i]);
        }

        goalLines.setDrawValues(false);
        goalLines.setHighlightEnabled(false);

        return goalLines;
    }

}
