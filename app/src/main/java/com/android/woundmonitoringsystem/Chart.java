package com.android.woundmonitoringsystem;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class Chart extends Fragment {
    PieChartView chart;
    String feas;
    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList barEntries;
    Float burning , cut , surgical , scratch;
    public Chart() {
        // Required empty public constructor
    }
    public static Chart newInstance(String param1, String param2) {
        Chart fragment = new Chart();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        feas = getArguments().getString("feas");

        View view =  inflater.inflate(R.layout.fragment_chart, container, false);
        burning = Float.valueOf(feas) - Float.valueOf(feas)/10;
        cut = Float.valueOf(feas) - Float.valueOf(feas)/3;
        surgical = Float.valueOf(feas) - Float.valueOf(feas)/5;
        scratch = Float.valueOf(feas);
//        chart = view.findViewById(R.id.barChart);
        barChart = view.findViewById(R.id.BarChart);
        getEntries();
        barDataSet = new BarDataSet(barEntries, "Feasibility");
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        barDataSet.setColors(Color.rgb(255, 255, 0) , Color.rgb(0, 255, 0)  , Color.rgb(255, 165, 0)  , Color.rgb(255, 0, 0) );
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(18f);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(getBarEntryLabels()));
        XAxis bottomAxis = barChart.getXAxis();
        bottomAxis.setLabelCount(getBarEntryLabels().size());
        bottomAxis.setGranularity(1);
//        bottomAxis
//        embedChart();
        return view;
    }

    private void getEntries() {
        barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, scratch , "Scratch"));
        barEntries.add(new BarEntry(1, cut , "Cut"));
        barEntries.add(new BarEntry(2, surgical , "Surgical"));
        barEntries.add(new BarEntry(3, burning , "Burning"));
    }
    private ArrayList<String> getBarEntryLabels() {
        ArrayList<String> BarEntryLabels = new ArrayList<String>();
        BarEntryLabels.add("Scratch");
        BarEntryLabels.add("Cut");
        BarEntryLabels.add("Surgical Incision");
        BarEntryLabels.add("Burning");
        return BarEntryLabels;
    }
    public void embedChart(){
        List<SliceValue> pieData = new ArrayList<>();
        pieData.add(new SliceValue(scratch, Color.BLUE).setLabel("Scratch"));
        pieData.add(new SliceValue(burning, Color.GRAY).setLabel("Burning"));
        pieData.add(new SliceValue(cut, Color.RED).setLabel("Cut"));
        pieData.add(new SliceValue(surgical, Color.MAGENTA).setLabel("Surgical Incision"));
        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(14);
        pieChartData.setHasCenterCircle(true).setCenterText1("Feasibility").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        chart.setPieChartData(pieChartData);
    }
}