package group26.cs307.allinwallet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Report extends AppCompatActivity {
    private FirebaseAuth auth;
    private String uid;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "Report";

    private TextView spendingNum, budgetText, budgetNum, remainingBudgetText, remainingBudgetNum;
    private Spinner sortPicker;
    private RadioGroup startDateGroup;
    private RadioButton dateButton;

    private RecyclerView purchaseList;
    private RecyclerView.Adapter purchaseListAdapter;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    public static List<PurchaseItem> purchases;

    private Calendar startOfWeek, startOfMonth, startOfYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        final String color = globalVariable.getThemeSelection();
        if (color != null && color.equals("dark")) {
            LinearLayout li = (LinearLayout) findViewById(R.id.reportLY);
            li.setBackgroundResource(R.color.cardview_dark_background);
            ActionBar ac;
            ac = getSupportActionBar();
            ac.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        }
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();

        spendingNum = (TextView) findViewById(R.id.spending_num);
        budgetText = (TextView) findViewById(R.id.budget_text);
        budgetNum = (TextView) findViewById(R.id.budget_num);
        remainingBudgetText = (TextView) findViewById(R.id.remaining_budget_text);
        remainingBudgetNum = (TextView) findViewById(R.id.remaining_budget_num);

        sortPicker = (Spinner) findViewById(R.id.sort_by_picker);
        ArrayAdapter<CharSequence> sortAA = ArrayAdapter.createFromResource(Report.this,
                R.array.sort_array, android.R.layout.simple_spinner_item);

        sortAA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortPicker.setAdapter(sortAA);

        sortPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                sortPurchases(pos);
                purchaseListAdapter.notifyDataSetChanged();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        purchaseList = (RecyclerView) findViewById(R.id.report_result);
        purchaseList.setHasFixedSize(true);
        purchaseListLayoutManager = new LinearLayoutManager(Report.this);
        purchaseList.setLayoutManager(purchaseListLayoutManager);
        purchases = new ArrayList<>();

        purchaseListAdapter = new PurchaseAdapter(purchases, new PurchaseClickListener() {
            @Override
            public void purchaseListClicked(View v, int position) {
            }
        });

        purchaseList.setAdapter(purchaseListAdapter);

        startDateGroup = (RadioGroup) findViewById(R.id.start_date_group);
        startDateGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                dateButton = (RadioButton) group.findViewById(checkedId);
                if (dateButton != null) {
                    updateReport(checkedId);
                }
            }
        });

        initializeDateReport();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.summary_menu) {
            startActivity(new Intent(Report.this, CategoriesActivity.class));
        } else

            if (item.getItemId() == R.id.share_menu) {
                String message = "The content I wish to share.";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);

                startActivity(Intent.createChooser(share, "How would you like to share this?"));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void updateReport(final int whichButton) {
        final DocumentReference dRef = db.collection("users").document(uid);
        String text;
        Query result = dRef.collection("purchase");
        final int sortMethodPos = sortPicker.getSelectedItemPosition();

        switch (whichButton) {
            case R.id.btn_rpt_week:
                result = result.whereGreaterThanOrEqualTo("date", startOfWeek.getTime());
                text = "weekly";
                break;
            case R.id.btn_rpt_month:
                result = result.whereGreaterThanOrEqualTo("date", startOfMonth.getTime());
                text = "monthly";
                break;
            case R.id.btn_rpt_annul:
                result = result.whereGreaterThanOrEqualTo("date", startOfYear.getTime());
                text = "annual";
                break;
            default:
                text = "";
                break;
        }

        final String budgetType = text + " budget";

        result.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                purchases.clear();
                double sum = 0.0, amount;

                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Log.d(TAG, document.getId() + "-->" + document.getData());
                    amount = document.getDouble("price");
                    sum += amount;
                    purchases.add(new PurchaseItem(document.getString("category"),
                            document.getString("name"), amount,
                            document.getDate("date"), document.getString("location"), document.getId()));
                }

                final double firstSum = sum;

                dRef.collection("recurring expense")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        double tempSum = firstSum;

                        if (task.isSuccessful()) {
                            double tempAmount;
                            Calendar calendar, lowerBound;
                            Calendar upperBound = Calendar.getInstance();

                            switch (whichButton) {
                                case R.id.btn_rpt_week:
                                    calendar = (Calendar) startOfMonth.clone();
                                    lowerBound = (Calendar) startOfWeek.clone();
                                    break;
                                case R.id.btn_rpt_annul:
                                    calendar = (Calendar) startOfYear.clone();
                                    lowerBound = (Calendar) startOfYear.clone();
                                    break;
                                default:
                                    calendar = (Calendar) startOfMonth.clone();
                                    lowerBound = (Calendar) startOfMonth.clone();
                                    break;
                            }

                            int originalMonth = calendar.get(Calendar.MONTH);

                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.getDate("date").after(lowerBound.getTime())) {
                                    lowerBound.setTime(document.getDate("date"));
                                    lowerBound.set(Calendar.HOUR_OF_DAY, 0);
                                    lowerBound.set(Calendar.MINUTE, 0);
                                    lowerBound.set(Calendar.SECOND, 0);
                                    lowerBound.set(Calendar.MILLISECOND, 0);
                                }

                                calendar.set(Calendar.MONTH, originalMonth);
                                calendar.set(Calendar.DAY_OF_MONTH,
                                        document.getLong("recurring").intValue());

                                while (calendar.before(upperBound)) {
                                    if (calendar.compareTo(lowerBound) >= 0) {
                                        tempAmount = document.getDouble("price");
                                        tempSum += tempAmount;

                                        purchases.add(new PurchaseItem(document.getString("category"),
                                                document.getString("name"), tempAmount,
                                                calendar.getTime(),
                                                document.getString("location"), document.getId()));
                                    }

                                    calendar.add(Calendar.MONTH, 1);
                                }
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }

                        spendingNum.setText(String.format(Locale.getDefault(), "%.2f", tempSum));
                        final double finalSum = tempSum;

                        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot uidDoc = task.getResult();
                                    if (uidDoc.exists()) {
                                        Log.d(TAG, uidDoc.getId() + "-->" + uidDoc.getData());

                                        if (uidDoc.contains(budgetType)) {
                                            double budget = uidDoc.getDouble(budgetType);
                                            String remainingBudgetNumText;

                                            if (Double.compare(finalSum, budget) < 0) {
                                                remainingBudgetNumText = String.format(Locale
                                                        .getDefault(), "%.2f", budget - finalSum);
                                            } else {
                                                remainingBudgetNumText = "0.00";
                                            }

                                            budgetText.setText("Your " + budgetType + ":");
                                            budgetNum.setText(String.format(Locale.getDefault(), "%.2f", budget));
                                            remainingBudgetText.setText(R.string.report_remaining_budget_found);
                                            remainingBudgetNum.setText(remainingBudgetNumText);
                                        } else {
                                            budgetText.setText(R.string.report_default_budget_text);
                                            budgetNum.setText(null);
                                            remainingBudgetText.setText(R.string.report_default_remaining_budget_text);
                                            remainingBudgetNum.setText(null);
                                        }
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });

                        sortPurchases(sortMethodPos);
                        purchaseListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void sortPurchases(int sortMethodPos) {
        if (purchases.isEmpty()) {
            return;
        }

        switch (sortMethodPos) {
            case 0:
                Collections.sort(purchases, new Comparator<PurchaseItem>() {
                    @Override
                    public int compare(PurchaseItem o1, PurchaseItem o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });
                break;
            case 1:
                Collections.sort(purchases, new Comparator<PurchaseItem>() {
                    @Override
                    public int compare(PurchaseItem o1, PurchaseItem o2) {
                        return o1.getCategory().compareTo(o2.getCategory());
                    }
                });
                break;
            case 2:
                Collections.sort(purchases, new Comparator<PurchaseItem>() {
                    @Override
                    public int compare(PurchaseItem o1, PurchaseItem o2) {
                        return o1.getTitle().compareTo(o2.getTitle());
                    }
                });
                break;
            case 3:
                Collections.sort(purchases, new Comparator<PurchaseItem>() {
                    @Override
                    public int compare(PurchaseItem o1, PurchaseItem o2) {
                        return Double.compare(o2.getAmount(), o1.getAmount());
                    }
                });
                break;
            default:
                break;
        }
    }

    public void initializeDateReport() {
        startOfWeek = Calendar.getInstance();
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
        startOfWeek.set(Calendar.MINUTE, 0);
        startOfWeek.set(Calendar.SECOND, 0);
        startOfWeek.set(Calendar.MILLISECOND, 0);

        startOfMonth = (Calendar) startOfWeek.clone();
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());

        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        startOfYear = (Calendar) startOfMonth.clone();
        startOfYear.set(Calendar.MONTH, Calendar.JANUARY);
    }
}
