package group26.cs307.allinwallet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;


public class GraphVisual extends AppCompatActivity {
    BarChart barChart;
    private FirebaseAuth auth;
    private static final String TAG = "AllinWallet";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> theDates = new ArrayList<>();;
    Random random;
    Vector<String> strV = new Vector<String>();
    Vector<Integer> intV = new Vector<>();
    //added for themes
    View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_visual);

        //added for themes
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        final boolean isDark = globalVariable.getThemeSelection();
        if (isDark) {
            view = this.getWindow().getDecorView();
            view.setBackgroundResource(R.color.cardview_dark_background);
        }


        barChart = (BarChart) findViewById(R.id.bargraph);


        barChart.setDrawValueAboveBar(true);
        barChart.setDrawBarShadow(false);
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);




        String uid = auth.getUid();

        final DocumentReference dRef = db.collection("users").document(uid);

        dRef.collection("summary").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        strV.add(document.getId());
                        double sum = document.getDouble("amount");
                        intV.add((int)sum);




                    }
                    ArrayList<BarEntry> barEntries = new ArrayList<>();
                    for(int i = 0; i < intV.size();i++){
                        barEntries.add(new BarEntry(i+1,intV.get(i)));

                    }
                    int size = strV.size();

                    String[] temp = new String[size+1];
                    temp[0] = "past";
                    for(int i = 1; i < temp.length; i++){
                        temp[i] = strV.get(i-1);
                    }


                    Log.d(TAG, "monthsize:"+temp.length);

//                    barEntries.add(new BarEntry(1,40f));
//                    barEntries.add(new BarEntry(2,44f));
//                    barEntries.add(new BarEntry(3,30f));
//                    barEntries.add(new BarEntry(4,36f));
//                    ArrayList<BarEntry> barEntries1 = new ArrayList<>();
//                    barEntries1.add(new BarEntry(1,44f));
//                    barEntries1.add(new BarEntry(2,54f));
//                    barEntries1.add(new BarEntry(3,60f));
//                    barEntries1.add(new BarEntry(4,31f));



                    BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly consumption");
                    barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
//                    BarDataSet barDataSet1 = new BarDataSet(barEntries1, "Date Set2");
//                    barDataSet1.setColors(ColorTemplate.COLORFUL_COLORS);




                    BarData data = new BarData(barDataSet);

//                    float groupSpace = 0.1f;
//                    float barSpace = 0.02f;
                    float barWidth = 0.8f;

                    barChart.setData(data);
                    data.setBarWidth(barWidth);

//                    barChart.groupBars(1,groupSpace,barSpace);


                    String[] months = new String[] {"Jan22", "Feb", "Mar", "April"};


                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setValueFormatter(new MyXAxisValueFormatter(temp));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
                    xAxis.setGranularity(1);
                    xAxis.setCenterAxisLabels(false);
                    xAxis.setAxisMinimum(1);

                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }
        });



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
