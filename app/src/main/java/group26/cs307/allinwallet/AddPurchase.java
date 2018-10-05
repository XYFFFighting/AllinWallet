package group26.cs307.allinwallet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPurchase extends AppCompatActivity {
    private Button add;
    private Button cancel;
    private Spinner categoryPicker;
    private EditText inputName, inputPrice;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "AllinWallet";

    public static List<String> defaultCategories = new ArrayList<>(Arrays.asList("Grocery",
            "Clothes", "House","Personal", "General" , "Transport" , "Fun"  ));
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        add = (Button) findViewById(R.id.add_item_button);
        cancel = (Button) findViewById(R.id.cancel_button);
        categoryPicker = (Spinner) findViewById(R.id.category_picker);
        inputName = (EditText) findViewById(R.id.item_name);
        inputPrice = (EditText) findViewById(R.id.item_price);
        categories = new ArrayList<>();
        categories.addAll(defaultCategories);
        // get categories from firebase
        ArrayAdapter spinnerAA = new ArrayAdapter(AddPurchase.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(spinnerAA);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String price = inputPrice.getText().toString();
                String category = categoryPicker.getSelectedItem().toString();

                if (TextUtils.isEmpty(name)) {
                    inputName.setError("name cannot be empty");
                    return;
                }
                if (TextUtils.isEmpty(price)) {
                    inputPrice.setError("price cannot be empty");
                    return;
                }
                Log.d(TAG, "Item Name: " + name);
                Log.d(TAG, "Item Price: " + price);
                Log.d(TAG, "Item Category: " + category);
                addPurchase(name, price, category);

                onBackPressed();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void addPurchase(String name, String price, String category) {
        Date currentTime = Calendar.getInstance().getTime();
        String time = currentTime.toString();
        Log.d(TAG, "purchase sending time is: " + time);
        String uid = auth.getUid();
        Map<String, Object> purchaselist = new HashMap<>();
        //purchaselist.put("uid", uid);
        purchaselist.put("name", name);
        purchaselist.put("price", price);
        purchaselist.put("category", category);
        //purchaselist.put("time", time);
        CollectionReference purchase = db.collection("users");
        purchase.document(uid).collection("purchase").document(time).set(purchaselist);
        Log.d(TAG, uid + " send purchase data");
    }
}
