package group26.cs307.allinwallet;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPurchase extends AppCompatActivity {
    private Button save, delete;
    private Spinner categoryPicker;
    private EditText inputName, inputPrice, inputDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "AllinWallet";

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar calendar;
    private SimpleDateFormat formatter;
    private int passedPurchaseIndex;
    private PurchaseItem item;

    public static List<String> defaultCategories = new ArrayList<>(Arrays.asList("Grocery",
            "Clothes", "Housing", "Personal", "General", "Transport", "Fun"));
    private List<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        passedPurchaseIndex = getIntent().getIntExtra("item_key", -1);
        if (passedPurchaseIndex != -1) {
            item = MainPage.purchases.get(passedPurchaseIndex);
        }

        save = (Button) findViewById(R.id.save_button);
        delete = (Button) findViewById(R.id.delete_button);
        categoryPicker = (Spinner) findViewById(R.id.category_picker);
        inputName = (EditText) findViewById(R.id.item_name);
        inputPrice = (EditText) findViewById(R.id.item_price);
        inputDate = (EditText) findViewById(R.id.item_date);
        categories = new ArrayList<>();
        categories.addAll(defaultCategories);
        // TO-DO: get categories from firebase
        ArrayAdapter spinnerAA = new ArrayAdapter(AddPurchase.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(spinnerAA);
        formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        calendar = Calendar.getInstance();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String price = inputPrice.getText().toString();
                String category = categoryPicker.getSelectedItem().toString();
                Date date = calendar.getTime();

                if (TextUtils.isEmpty(name)) {
                    inputName.setError("Title cannot be empty");
                    return;
                }
                if (TextUtils.isEmpty(price)) {
                    inputPrice.setError("Amount cannot be empty");
                    return;
                }
                Log.d(TAG, "Item Title: " + name);
                Log.d(TAG, "Item Amount: " + price);
                Log.d(TAG, "Item Category: " + category);
                Log.d(TAG, "Item Date: " + date);

                if (passedPurchaseIndex == -1) {
                    addPurchase(name, Double.parseDouble(price), category, date);
                } else {
                    updatePurchase(name, Double.parseDouble(price), category, date, item.getDocumentUID());
                }
                onBackPressed();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TO-DO
                onBackPressed();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                String date = formatter.format(calendar.getTime());
                inputDate.setText(date);
                Log.d(TAG, "onDateSet: mm/dd/yyy:" + date);
            }
        };

        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(AddPurchase.this,
                        dateSetListener, year, month, day).show();
            }
        });

        if (passedPurchaseIndex != -1) {
            setTitle(R.string.title_activity_edit_purchase);
            inputName.setText(item.getTitle());
            inputPrice.setText(item.getAmountString());
            inputDate.setText(item.getDateString());
            calendar.setTime(item.getDate());
            categoryPicker.setSelection(categories.indexOf(item.getCategory()));

            delete.setVisibility(View.VISIBLE);
            delete.setClickable(true);
        } else {
            inputDate.setText(formatter.format(calendar.getTime()));
        }
    }

    public void addPurchase(String name, double price, String category, Date date) {
        String time = Calendar.getInstance().getTime().toString();

        Log.d(TAG, "purchase sending time is: " + time);
        String uid = auth.getUid();

        Map<String, Object> purchaselist = new HashMap<>();
        purchaselist.put("name", name);
        purchaselist.put("price", price);
        purchaselist.put("category", category);
        purchaselist.put("date", date);

        db.collection("users").document(uid)
                .collection("purchase").document(time).set(purchaselist);
        Log.d(TAG, uid + " send purchase data");
    }

    public void updatePurchase(String name, double price, String category, Date date, String
            documentUID) {
        String uid = auth.getUid();

        Map<String, Object> purchaselist = new HashMap<>();
        purchaselist.put("name", name);
        purchaselist.put("price", price);
        purchaselist.put("category", category);
        purchaselist.put("date", date);

        db.collection("users").document(uid)
                .collection("purchase").document(documentUID).update(purchaselist);
        Log.d(TAG, uid + " update purchase data");
    }
}
