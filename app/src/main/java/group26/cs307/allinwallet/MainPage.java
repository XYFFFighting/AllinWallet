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
import android.widget.RadioGroup;
import android.widget.TextView;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainPage extends AppCompatActivity {
    private FloatingActionButton purchaseButton;
    private TextView welcomeMessage, budgetText;
    private Date startofMonth;
    private RecyclerView purchaseList;
    private RecyclerView.Adapter purchaseListAdapter;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    public static List<PurchaseItem> purchases;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    private RadioGroup currencyGroup;
    private int CurrencyselectedRadioButtonID;
    public static String currencySign = "$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        currencyGroup = findViewById(R.id.currency_type_group);
        welcomeMessage = (TextView) findViewById(R.id.welcomeText);
        budgetText = (TextView) findViewById(R.id.budgetText);
        setDate();

        purchaseButton = (FloatingActionButton) findViewById(R.id.fab);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, AddPurchase.class));
            }
        });


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


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
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

//        CurrencyselectedRadioButtonID = currencyGroup.getCheckedRadioButtonId();
//        if (CurrencyselectedRadioButtonID != -1) {
////
////            RadioButton selectedRadioButton = (RadioButton) findViewById(CurrencyselectedRadioButtonID);
////            currencySign = selectedRadioButton.getText().toString();
////        }
////        else{
////            currencySign = "";
////        }
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
            case R.id.share_menu:
                String message = "The content I wish to share.";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "How would you like to share this?"));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault());
        String date = formatter.format(calendar.getTime());
        welcomeMessage.append(date);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startofMonth = calendar.getTime();
    }

    public void updateMainPage(String uid) {
        final DocumentReference dRef = db.collection("users").document(uid);

        dRef.collection("purchase").whereGreaterThanOrEqualTo("date", startofMonth)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    purchases.clear();
                    budgetText.setText("Current spending: ");
                    double sum = 0.0, amount;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + "-->" + document.getData());
                        amount = document.getDouble("price");

                        sum += amount;
                        purchases.add(new PurchaseItem(document.getString("category"),
                                document.getString("name"), amount,
                                document.getDate("date"), document.getString("location"), document.getId()));
                    }


                    purchaseListAdapter.notifyDataSetChanged();
                    budgetText.append(String.format(Locale.getDefault(),
                            "%.2f", sum));
                    // 29 $

                    dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, document.getId() + "-->" + document.getData());

                                    if (document.contains("monthly budget")) {
                                        budgetText.append(String.format(Locale.getDefault(),
                                                " / %.2f", document.getDouble("monthly budget")));
                                        //300 $
                                    }
                                    if (document.contains("Currency")) {
                                        currencySign = document.getString("Currency");
                                    }

                                    if (document.contains("income")) {
                                        String income = String.format(Locale.getDefault(),
                                                "\nYour monthly income: %.2f",
                                                document.getDouble("income"));
                                        budgetText.append(income);
                                    }
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void deletePurchaseItem(int position) {
        String uid = auth.getUid();
        String documentUID = purchases.get(position).getDocumentUID();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = auth.getUid();
        updateMainPage(uid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TBA
    }
}
