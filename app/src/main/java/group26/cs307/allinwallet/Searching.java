package group26.cs307.allinwallet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Searching extends AppCompatActivity {
    private Spinner categoryPicker;
    private List<String> categories;
    private EditText inputName, inputStart, inputEnd;
    private Button btn_search;
    private RecyclerView purchaseList;
    private RecyclerView.Adapter purchaseListAdapter;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    public static List<PurchaseItem> purchases;
    private DatePickerDialog.OnDateSetListener startDateSetListener, endDateSetListener;
    private Calendar calendar;
    private SimpleDateFormat formatter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    public static List<String> defaultCategories = new ArrayList<>(Arrays.asList("Any", "Grocery",
            "Clothes", "House", "Personal", "General", "Transport", "Fun"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);
        auth = FirebaseAuth.getInstance();

        btn_search = (Button) findViewById(R.id.btn_search);
        categoryPicker = (Spinner) findViewById(R.id.category_picker);
        inputName = (EditText) findViewById(R.id.item_name2);
        inputStart = (EditText) findViewById(R.id.start_date);
        inputEnd = (EditText) findViewById(R.id.end_date);
        calendar = Calendar.getInstance();
        formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        purchaseList = (RecyclerView) findViewById(R.id.rst_search);
        purchaseList.setHasFixedSize(true);
        purchaseListLayoutManager = new LinearLayoutManager(Searching.this);
        purchaseList.setLayoutManager(purchaseListLayoutManager);
        purchases = new ArrayList<>();

        purchaseListAdapter = new PurchaseAdapter(purchases, new PurchaseClickListener() {
            @Override
            public void purchaseListClicked(View v, int position) {
            }
        });

        purchaseList.setAdapter(purchaseListAdapter);

        categories = new ArrayList<>();
        categories.addAll(defaultCategories);
        ArrayAdapter spinnerAA = new ArrayAdapter(Searching.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(spinnerAA);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String category = categoryPicker.getSelectedItem().toString();
                Log.d(TAG, "Item Category" + category);
//                if(!name.equals("") && !category.equals("any")){
//                    search_by_both(category, name);
//                }
//                else if(name.equals("") && !category.equals("any")){
//                    search_by_type(category);
//                }
//                else if(!name.equals("") && category.equals("any")){
//                    search_by_name(name);
//                }
                search_by_all(category, name);

            }
        });

        startDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                String date = formatter.format(calendar.getTime());
                inputStart.setText(date);
                Log.d(TAG, "onDateSet: mm/dd/yyy:" + date);
            }
        };

        endDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                String date = formatter.format(calendar.getTime());
                inputEnd.setText(date);
                Log.d(TAG, "onDateSet: mm/dd/yyy:" + date);
            }
        };

        inputStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(Searching.this,
                        startDateSetListener, year, month, day).show();
            }
        });

        inputEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(Searching.this,
                        endDateSetListener, year, month, day).show();
            }
        });
    }

    public void search_by_all(String category, String name) {
        purchases.clear();
        String uid = auth.getUid();
        CollectionReference col_purchase = db.collection("users").document(uid).collection("purchase");
        //Query result = col_purchase.whereEqualTo("name", name).whereEqualTo("category", category);
        Query result = col_purchase;
        if (!category.equals("Any")) {
            result = result.whereEqualTo("category", category);
        }
        if (!name.equals("")) {
            result = result.whereEqualTo("name", name);
        }
        result.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Log.d(TAG, document.getId() + "-->" + document.getData());
                            String text = document.getId() + " " + document.getData().toString() + '\n';
                            purchases.add(new PurchaseItem(document.getString("category"),
                                    document.getString("name"), document.getDouble("price"),
                                    document.getString("date"), document.getId()));
                        }

                        purchaseListAdapter.notifyDataSetChanged();
                    }
                });
    }

//    public void search_by_type(String type){
//        result_text.setText(null);
//        String uid = auth.getUid();
//        CollectionReference col_purchase = db.collection("users").document(uid).collection("purchase");
//        Query result = col_purchase.whereEqualTo("category", type);
//        result.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                            Log.d(TAG, document.getId() + "-->" + document.getData());
//                            String text = document.getId() + " " + document.getData().toString() + '\n';
//                            result_text.append(text);
//                        }
//
//                    }
//                });
//    }
//
//    public void search_by_name(String name){
//        result_text.setText(null);
//        String uid = auth.getUid();
//        CollectionReference col_purchase = db.collection("users").document(uid).collection("purchase");
//        Query result = col_purchase.whereEqualTo("name", name);
//        result.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                            Log.d(TAG, document.getId() + "-->" + document.getData());
//                            String text = document.getId() + " " + document.getData().toString() + '\n';
//                            result_text.append(text);
//                        }
//
//                    }
//                });
//    }


}
