package group26.cs307.allinwallet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Random;


public class GraphVisual extends AppCompatActivity {
    BarChart barChart;

    ArrayList<String> theDates = new ArrayList<>();;
    Random random;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("myTag", "nizaiganma!");
        super.onCreate(savedInstanceState);
        Log.d("myTag", "nizaiganma!");
        setContentView(R.layout.activity_graph_visual);



        barChart = (BarChart) findViewById(R.id.bargraph);


        barChart.setDrawValueAboveBar(true);
        barChart.setDrawBarShadow(false);
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);


        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1,40f));
        barEntries.add(new BarEntry(2,44f));
        barEntries.add(new BarEntry(3,30f));
        barEntries.add(new BarEntry(4,36f));
        ArrayList<BarEntry> barEntries1 = new ArrayList<>();
        barEntries1.add(new BarEntry(1,44f));
        barEntries1.add(new BarEntry(2,54f));
        barEntries1.add(new BarEntry(3,60f));
        barEntries1.add(new BarEntry(4,31f));



        BarDataSet barDataSet = new BarDataSet(barEntries, "Date Set1");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarDataSet barDataSet1 = new BarDataSet(barEntries1, "Date Set2");
        barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);




        BarData data = new BarData(barDataSet,barDataSet1);

        float groupSpace = 0.1f;
        float barSpace = 0.02f;
        float barWidth = 0.43f;

        barChart.setData(data);
        data.setBarWidth(barWidth);

        barChart.groupBars(1,groupSpace,barSpace);


        String[] months = new String[] {"Jan", "Feb", "Mar", "April", "May", "Jun"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new MyXAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setGranularity(1);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(1);
    }
    public class MyXAxisValueFormatter implements IAxisValueFormatter{
        private String[] mValues;
        public MyXAxisValueFormatter(String[] values){
            this.mValues = values;
        }
        @Override
        public String getFormattedValue(float value, AxisBase axisBase) {
            return mValues[(int)value];
        }
    }



}
