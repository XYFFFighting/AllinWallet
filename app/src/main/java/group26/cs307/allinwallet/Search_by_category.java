package group26.cs307.allinwallet;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Search_by_category extends Activity {
    private Spinner categoryPicker;
    private List<String> categories;
    private Button btn_search;
    private TextView search_rst;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    public static List<String> defaultCategories = new ArrayList<>(Arrays.asList("Grocery",
            "Clothes", "House", "Personal", "General", "Transport", "Fun"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_category);

        categoryPicker = (Spinner)findViewById(R.id.category_picker);
        btn_search = (Button)findViewById(R.id.btn_search);
        search_rst = (TextView)findViewById(R.id.rst_search_by_type);


        categories = new ArrayList<>();
        categories.addAll(defaultCategories);
        ArrayAdapter spinnerAA = new ArrayAdapter(Search_by_category.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(spinnerAA);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String category = categoryPicker.getSelectedItem().toString();
                Log.d(TAG, "Item Category" + category);
                search_by_type(category);
            }
        });


    }

    public void search_by_type(String type){
        search_rst.setText(null);
        String uid = auth.getUid();
        CollectionReference col_purchase = db.collection("users").document(uid).collection("purchase");
        Query result = col_purchase.whereEqualTo("category", type);
        result.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                       for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                           Log.d(TAG, document.getId() + "-->" + document.getData());
                           String text = document.getId() + " " + document.getData().toString() + '\n';
                           search_rst.append(text);
                       }

                    }
                });
    }

}
