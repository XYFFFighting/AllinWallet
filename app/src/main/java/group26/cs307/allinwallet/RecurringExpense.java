
package group26.cs307.allinwallet;

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
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecurringExpense extends AppCompatActivity implements View.OnClickListener {
    private Button addButton, clearButton;
    private Spinner categoryPicker;
    private EditText inputName, inputPrice, inputDay;
    private int recurringDay;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private static final String TAG = "Recurring Expense";

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

        addButton = (Button) findViewById(R.id.add_button);
        clearButton = (Button) findViewById(R.id.clear_button);
        inputName = (EditText) findViewById(R.id.item_name);
        inputPrice = (EditText) findViewById(R.id.item_price);
        inputDay = (EditText) findViewById(R.id.item_day);
        recurringDay = -1;

        categoryPicker = (Spinner) findViewById(R.id.category_picker);
        ArrayAdapter categoryAA = new ArrayAdapter(RecurringExpense.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(categoryAA);

        addButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        inputDay.setOnClickListener(this);

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
                        recurringExpenses.add(new RecurringExpenseItem(document.getString("category"),
                                document.getString("name"), document.getDouble("price"),
                                document.getDate("date"), document.getLong("recurring").intValue(),
                                document.getId()));
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
        String dUID = date.toString();
        int position = recurringExpenses.size();
        recurringExpenses.add(new RecurringExpenseItem(category,
                name, price, date, recurringDay, dUID));
        recurringListAdapter.notifyItemInserted(position);

        Map<String, Object> newRecurringExpense = new HashMap<>();
        newRecurringExpense.put("name", name);
        newRecurringExpense.put("price", price);
        newRecurringExpense.put("category", category);
        newRecurringExpense.put("date", date);
        newRecurringExpense.put("recurring", recurringDay);

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
            case R.id.add_button: {
                String name = inputName.getText().toString();
                String price = inputPrice.getText().toString();
                String category = categoryPicker.getSelectedItem().toString();
                Date date = Calendar.getInstance().getTime();

                if (recurringDay == -1) {
                    inputDay.setError("Recurring day cannot be empty");
                    return;
                }

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
                inputName.setText(null);
                inputPrice.setText(null);
                inputDay.setText(null);
                recurringDay = -1;
                categoryPicker.setSelection(0);
            }
            break;
            case R.id.item_day: {
                AlertDialog.Builder builder = new AlertDialog.Builder(RecurringExpense.this);
                builder.setTitle("Choose a recurring day");
                View mView = getLayoutInflater().inflate(R.layout.recurring_expense_number_picker, null);
                final NumberPicker dayPicker = (NumberPicker) mView.findViewById(R.id.day_picker);
                dayPicker.setMinValue(1);
                dayPicker.setMaxValue(28);
                builder.setView(mView);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        recurringDay = dayPicker.getValue();
                        inputDay.setText(RecurringExpenseItem.toRecurringDayString(recurringDay));
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
            break;
            default:
                break;
        }
    }
}
