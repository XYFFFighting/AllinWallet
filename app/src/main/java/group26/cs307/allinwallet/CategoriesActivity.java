package group26.cs307.allinwallet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CategoriesActivity extends AppCompatActivity {
    private FloatingActionButton purchaseButton;
    private TextView welcomeMessage, groceryText, clothesText, housingText, personalText, generalText, transportText, funText;
    private RecyclerView purchaseList;
    private RecyclerView.Adapter purchaseListAdapter;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    public static List<PurchaseItem> purchases;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    public static List<String> defaultCategories = new ArrayList<>(Arrays.asList("Grocery",
            "Clothes", "Housing", "Personal", "General", "Transport", "Fun"));
    private PieChart pieChart;
    private ArrayList<Float> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        groceryText = (TextView) findViewById(R.id.GroceryText);
        clothesText = (TextView) findViewById(R.id.ClothesText);
        housingText = (TextView) findViewById(R.id.HousingText);
        personalText = (TextView) findViewById(R.id.PersonalText);
        generalText = (TextView) findViewById(R.id.GeneralText);
        transportText = (TextView) findViewById(R.id.TransportText);
        funText = (TextView) findViewById(R.id.FunText);
        pieChart = (PieChart)findViewById(R.id.Piechart);
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText("Categories");
        pieChart.setCenterTextSize(10);
        //purchaseList = (RecyclerView) findViewById(R.id.purchase_list);
        //purchaseList.setHasFixedSize(true);
        //purchaseListLayoutManager = new LinearLayoutManager(CategoriesActivity.this);
        //purchaseList.setLayoutManager(purchaseListLayoutManager);
        purchases = new ArrayList<>();
//        String uid = auth.getUid();
//        updateCategoryPage(uid);
        String uid = auth.getUid();
        updateCategoryPage(uid);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValue: " + e.toString());
                Log.d(TAG, "onValue: " + h.toString());
                int pos1 = e.toString().indexOf("y:");
                String purchase = e.toString().substring(pos1 + 2);

                for (int i = 0; i < data.size(); i++ ){
                    if(data.get(i) == Float.parseFloat(purchase)){
                        pos1 = i;
                        break;
                    }
                }
                String ca = defaultCategories.get(pos1+1);
                Toast.makeText(CategoriesActivity.this, "category" + ca + "\n" + "amount: " + purchase, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    public void updateCategoryPage(String uid) {
        final DocumentReference dRef = db.collection("users").document(uid);

        dRef.collection("purchase").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    purchases.clear();

                    groceryText.setText("Groceries Total Amount: ");
                    clothesText.setText("Clothes Total Amount: ");
                    housingText.setText("Housing Total Amount: ");
                    personalText.setText("Personal Total Amount: ");
                    generalText.setText("General Total Amount: ");
                    transportText.setText("Transport Total Amount: ");
                    funText.setText("Fun Total Amount: ");


                    double amount;
                    double groceryAmount = 0.0;
                    double clothesAmount = 0.0;
                    double housingAmount = 0.0;
                    double personalAmount = 0.0;
                    double generalAmount = 0.0;
                    double transportAmount = 0.0;
                    double funAmount = 0.0;

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + "-->" + document.getData());
                        String category = document.getString("category");
                        amount = document.getDouble("price");
                        assert category != null;
                        switch (category) {
                            case "Grocery":
                                groceryAmount += amount;
                                break;
                            case "Clothes":
                                clothesAmount += amount;
                                break;
                            case "Housing":
                                housingAmount += amount;
                                break;
                            case "Personal":
                                personalAmount += amount;
                                break;
                            case "General":
                                generalAmount += amount;
                                break;
                            case "Transport":
                                transportAmount += amount;
                                break;
                            case "Fun":
                                funAmount += amount;
                                break;
                        }
                    }

                    //purchaseListAdapter.notifyDataSetChanged();
                    groceryText.append(Double.toString(groceryAmount));
                    clothesText.append(Double.toString(clothesAmount));
                    housingText.append(Double.toString(housingAmount));
                    personalText.append(Double.toString(personalAmount));
                    generalText.append(Double.toString(generalAmount));
                    transportText.append(Double.toString(transportAmount));
                    funText.append(Double.toString(funAmount));
                    data.clear();
                    data.add((float) groceryAmount);
                    data.add((float) clothesAmount);
                    data.add((float) housingAmount);
                    data.add((float) personalAmount);
                    data.add((float) generalAmount);
                    data.add((float) transportAmount);
                    data.add((float) funAmount);
                    addData();



//                    dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if (task.isSuccessful()) {
//                                DocumentSnapshot document = task.getResult();
//                                if (document.exists()) {
//                                    Log.d(TAG, document.getId() + "-->" + document.getData());
//
//                                    if (document.contains("budget")) {
//                                        //budgetText.append(" / " + document.getDouble("budget"));
//                                    }
//                                } else {
//                                    Log.d(TAG, "No such document");
//                                }
//                            } else {
//                                Log.d(TAG, "get failed with ", task.getException());
//                            }
//                        }
//                    });
                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }


        });
    }

    protected void addData(){
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.YELLOW);
        colors.add(Color.GRAY);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.MAGENTA);
//        float groceryAmount = Float.parseFloat((groceryText.getText().toString()));
//        float clothesAmount = Float.parseFloat((clothesText.getText().toString()));
//        float housingAmount = Float.parseFloat((housingText.getText().toString()));
//        float personalAmount = Float.parseFloat((personalText.getText().toString()));
//        float generalAmount = Float.parseFloat((generalText.getText().toString()));
//        float transportAmount = Float.parseFloat((transportText.getText().toString()));
//        float funAmount = Float.parseFloat((funText.getText().toString()));
//
//        data.add((float) groceryAmount);
//        data.add((float) clothesAmount);
//        data.add((float) housingAmount);
//        data.add((float) personalAmount);
//        data.add((float) generalAmount);
//        data.add((float) transportAmount);
//        data.add((float) funAmount);
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            //Log.d(TAG, "123");
            yEntrys.add(new PieEntry(data.get(i), i));
        }
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Summary");
        pieDataSet.setSliceSpace(1);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(colors);

        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = auth.getUid();
        updateCategoryPage(uid);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // TBA
    }
}