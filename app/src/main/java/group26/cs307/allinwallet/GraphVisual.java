package group26.cs307.allinwallet;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Random;


public class GraphVisual extends AppCompatActivity {
    BarChart barChart;
    private FirebaseAuth auth;
    private static final String TAG = "AllinWallet";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> theDates = new ArrayList<>();;
    Random random;
    String[] monthss = new String[] {"Jan", "Feb", "Mar", "April", "May", "Jun","July","Aug","Sep","Oct","Nov","Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_visual);



        barChart = (BarChart) findViewById(R.id.bargraph);


        barChart.setDrawValueAboveBar(true);
        barChart.setDrawBarShadow(false);
        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);




        String uid = auth.getUid();
        final DocumentReference dRef = db.collection("users").document(uid);
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, document.getId() + "-->" + document.getData());


                        if (document.contains("monthly budget")){

                        }


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


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
