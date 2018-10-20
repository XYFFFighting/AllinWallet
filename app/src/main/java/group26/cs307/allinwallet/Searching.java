package group26.cs307.allinwallet;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Searching extends Activity {
    private Spinner categoryPicker;
    private List<String> categories;
    private EditText inputName;
    private Button btn_search;
    private TextView result_text;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;
    public static List<String> defaultCategories = new ArrayList<>(Arrays.asList("any","Grocery",
            "Clothes", "House", "Personal", "General", "Transport", "Fun"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);
        auth = FirebaseAuth.getInstance();

        btn_search = (Button)findViewById(R.id.btn_search);
        categoryPicker = (Spinner)findViewById(R.id.category_picker);
        inputName = (EditText)findViewById(R.id.item_name2);
        result_text = (TextView)findViewById(R.id.rst_search);

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
    }

    public void search_by_all(String category, String name){
        result_text.setText(null);
        String uid = auth.getUid();
        CollectionReference col_purchase = db.collection("users").document(uid).collection("purchase");
        //Query result = col_purchase.whereEqualTo("name", name).whereEqualTo("category", category);
        Query result = col_purchase;
        if(!category.equals("any")){
            result = result.whereEqualTo("category", category);
        }
        if(!name.equals("")){
            result = result.whereEqualTo("name", name);
        }
        result.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Log.d(TAG, document.getId() + "-->" + document.getData());
                            String text = document.getId() + " " + document.getData().toString() + '\n';
                            result_text.append(text);
                        }

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
