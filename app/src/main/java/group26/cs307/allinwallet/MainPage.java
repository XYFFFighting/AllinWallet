package group26.cs307.allinwallet;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainPage extends AppCompatActivity {
    private FloatingActionButton purchaseButton;
    private TextView dateText, budgetNumText, spendingNumText, incomeNumText;
    private Calendar startOfMonth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "MainPage";
    private FirebaseAuth auth;
    public static String currencySign = "$";
    public static boolean isBudgetUpdated;
    public static double budgetNum;
    public static boolean isSpendingUpdated;
    public static double spendingNum;
    public static boolean isIncomeUpdated;
    public static double incomeNum;

    private RecyclerView purchaseList;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    public static RecyclerView.Adapter purchaseListAdapter;
    public static List<PurchaseItem> purchases;

    View view;
    LinearLayout li;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        final String color = globalVariable.getThemeSelection();
        if (color != null && color.equals("dark")) {
            li = (LinearLayout) findViewById(R.id.mainPageLY);
            li.setBackgroundResource(R.color.cardview_dark_background);
        }

        currencyGroup = findViewById(R.id.currency_type_group);
        welcomeMessage = (TextView) findViewById(R.id.welcomeText);
        budgetText = (TextView) findViewById(R.id.budgetText);
        auth = FirebaseAuth.getInstance();

        dateText = (TextView) findViewById(R.id.date_text);
        budgetNumText = (TextView) findViewById(R.id.budget_num);
        spendingNumText = (TextView) findViewById(R.id.spending_num);
        incomeNumText = (TextView) findViewById(R.id.income_num);
        isBudgetUpdated = false;
        budgetNum = 0.0;
        isSpendingUpdated = false;
        spendingNum = 0.0;
        isIncomeUpdated = false;
        incomeNum = 0.0;
        setDate();

        purchaseButton = (FloatingActionButton) findViewById(R.id.fab);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, AddPurchase.class));
            }
        });

        purchaseList = (RecyclerView) findViewById(R.id.purchase_list);
        purchaseList.setHasFixedSize(true);
        purchaseListLayoutManager = new LinearLayoutManager(MainPage.this);
        purchaseList.setLayoutManager(purchaseListLayoutManager);
        purchases = new ArrayList<>();

        purchaseListAdapter = new PurchaseAdapter(purchases, new PurchaseClickListener() {
            @Override
            public void purchaseListClicked(View v, int position) {
                Intent intent = new Intent(MainPage.this, AddPurchase.class);
                intent.putExtra("item_key", position);
                startActivity(intent);
            }
        });

        purchaseList.setAdapter(purchaseListAdapter);

        ItemTouchHelper.SimpleCallback purchaseItemCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                final int position = viewHolder.getLayoutPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
                builder.setTitle("Delete this purchase?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deletePurchaseItem(position);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface
                        .OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        purchaseListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        };

        ItemTouchHelper purchaseItemTouchHelper = new ItemTouchHelper(purchaseItemCallback);
        purchaseItemTouchHelper.attachToRecyclerView(purchaseList);
        initializeMainPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.issue_menu:
                startActivity(new Intent(MainPage.this, Issue.class));
                return true;
            case R.id.profile_menu:
                startActivity(new Intent(MainPage.this, Profile.class));
                return true;
            case R.id.recurring_menu:
                startActivity(new Intent(MainPage.this, RecurringExpense.class));
                return true;
            case R.id.report_menu:
                startActivity(new Intent(MainPage.this, Report.class));
                return true;
            case R.id.search_menu:
                startActivity(new Intent(MainPage.this, Searching.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setDate() {
        startOfMonth = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy",
                Locale.getDefault());
        dateText.append(formatter.format(startOfMonth.getTime()));

        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);
    }

    public void initializeMainPage() {
        String uid = auth.getUid();
        final DocumentReference dRef = db.collection("users").document(uid);

        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        Log.d(TAG, document.getId() + "-->" + document.getData());

                        if (document.contains("Currency")) {
                            currencySign = document.getString("Currency");
                        }

                        if (document.contains("monthly budget")) {
                            budgetNum = document.getDouble("monthly budget");
                            budgetNumText.setText(String.format(Locale.getDefault(),
                                    "%s%.2f", currencySign, budgetNum));
                        }

                        if (document.contains("income")) {
                            incomeNum = document.getDouble("income");
                            incomeNumText.setText(String.format(Locale.getDefault(),
                                    "%s%.2f", currencySign, incomeNum));
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }

                dRef.collection("purchase").whereGreaterThanOrEqualTo("date", startOfMonth.getTime())
                        .orderBy("date", Query.Direction.DESCENDING)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double amount;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + "-->" + document.getData());
                                amount = document.getDouble("price");

                                spendingNum += amount;
                                purchases.add(new PurchaseItem(document.getString("category"),
                                        document.getString("name"), amount,
                                        document.getDate("date"), document.getString("location"), document.getId()));
                            }

                            purchaseListAdapter.notifyDataSetChanged();
                            spendingNumText.setText(String.format(Locale.getDefault(),
                                    "%s%.2f", currencySign, spendingNum));
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });
    }

    public void updateMainPage() {
        if (isBudgetUpdated) {
            budgetNumText.setText(String.format(Locale.getDefault(),
                    "%s%.2f", currencySign, budgetNum));
            isBudgetUpdated = false;
        }

        if (isSpendingUpdated) {
            spendingNumText.setText(String.format(Locale.getDefault(),
                    "%s%.2f", currencySign, spendingNum));
            isSpendingUpdated = false;
        }

        if (isIncomeUpdated) {
            incomeNumText.setText(String.format(Locale.getDefault(),
                    "%s%.2f", currencySign, incomeNum));
            isIncomeUpdated = false;
        }
    }

    public void deletePurchaseItem(int position) {
        String uid = auth.getUid();
        PurchaseItem item = purchases.get(position);
        String documentUID = item.getDocumentUID();
        final double amount = item.getAmount();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(item.getDate());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        String result = String.format(Locale.getDefault(), "%d-%d", year, month);

        spendingNum -= amount;
        spendingNumText.setText(String.format(Locale.getDefault(),
                "%s%.2f", currencySign, spendingNum));
        purchases.remove(position);
        purchaseListAdapter.notifyItemRemoved(position);

        db.collection("users").document(uid).collection("purchase")
                .document(documentUID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "purchase delete successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "purchase delete unsuccessful");
                    }
                });

        Log.d(TAG, "summary date: " + result);

        final DocumentReference dRef = db.collection("users").document(uid)
                .collection("summary").document(result);

        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        double sum = document.getDouble("amount");
                        sum -= amount;
                        Map<String, Object> amount = new HashMap<>();
                        amount.put("amount", sum);
                        dRef.update(amount);
                    }
                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMainPage();
    }
}
