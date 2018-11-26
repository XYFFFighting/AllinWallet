
package group26.cs307.allinwallet;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecurringExpense extends AppCompatActivity implements View.OnClickListener {
    private Button save, clear;
    private Spinner categoryPicker;
    private EditText inputName, inputPrice, inputDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private static final String TAG = "Recurring Expense";

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar calendar;
    private SimpleDateFormat formatter;

    private RecyclerView recurringList;
    private RecyclerView.Adapter recurringListAdapter;
    private RecyclerView.LayoutManager recurringListLayoutManager;
    public static List<PurchaseItem> recurringExpenses;

    private static List<String> categories = new ArrayList<>(Arrays.asList("Grocery",
            "Clothes", "Housing", "Personal", "General", "Transport", "Fun"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        save = (Button) findViewById(R.id.save_button);
        clear = (Button) findViewById(R.id.clear_button);
        inputName = (EditText) findViewById(R.id.item_name);
        inputPrice = (EditText) findViewById(R.id.item_price);
        inputDate = (EditText) findViewById(R.id.item_date);

        categoryPicker = (Spinner) findViewById(R.id.category_picker);
        ArrayAdapter categoryAA = new ArrayAdapter(RecurringExpense.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(categoryAA);

        formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        calendar = Calendar.getInstance();
        inputDate.setText(formatter.format(calendar.getTime()));

        save.setOnClickListener(this);
        clear.setOnClickListener(this);
        inputDate.setOnClickListener(this);

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

        recurringList = (RecyclerView) findViewById(R.id.report_result);
        recurringList.setHasFixedSize(true);
        recurringListLayoutManager = new LinearLayoutManager(RecurringExpense.this);
        recurringList.setLayoutManager(recurringListLayoutManager);
        recurringExpenses = new ArrayList<>();

        recurringListAdapter = new PurchaseAdapter(recurringExpenses, new PurchaseClickListener() {
            @Override
            public void purchaseListClicked(View v, int position) {
                final int mPosition = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(RecurringExpense.this);
                builder.setTitle("Delete this recurring expense?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteRecurringExpense(mPosition);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface
                        .OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });

        recurringList.setAdapter(recurringListAdapter);
        initializeRecurringList();
    }

    public void initializeRecurringList() {
        db.collection("users").document(auth.getUid())
                .collection("recurring expense").orderBy("date")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    recurringExpenses.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        recurringExpenses.add(new PurchaseItem(document.getString("category"),
                                document.getString("name"), document.getDouble("price"),
                                document.getDate("date"), document.getId()));
                    }

                    recurringListAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void addRecurringExpense(String name, double price, String category, Date date) {
        String UID = auth.getUid();
        String dUID = Calendar.getInstance().getTime().toString();
        int position = recurringExpenses.size();
        recurringExpenses.add(new PurchaseItem(category,
                name, price, date, UID));
        recurringListAdapter.notifyItemInserted(position);

        Map<String, Object> newRecurringExpense = new HashMap<>();
        newRecurringExpense.put("name", name);
        newRecurringExpense.put("price", price);
        newRecurringExpense.put("category", category);
        newRecurringExpense.put("date", date);

        db.collection("users").document(UID)
                .collection("recurring expense").document(dUID).set(newRecurringExpense);
    }

    public void deleteRecurringExpense(int position) {
        String UID = auth.getUid();
        String dUID = recurringExpenses.get(position).getDocumentUID();
        recurringExpenses.remove(position);
        recurringListAdapter.notifyItemRemoved(position);

        db.collection("users").document(UID).collection("recurring expense")
                .document(dUID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "recurring expense delete successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "recurring expense delete unsuccessful");
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button: {
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

                addRecurringExpense(name, Double.parseDouble(price), category, date);
            }
            break;
            case R.id.clear_button: {
                inputDate.setText(null);
                inputPrice.setText(null);
                inputDate.setText(null);
                categoryPicker.setSelection(0);
            }
            break;
            case R.id.item_date: {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(RecurringExpense.this,
                        dateSetListener, year, month, day).show();
            }
            break;
            default:
                break;
        }
    }
}
