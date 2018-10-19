package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainPage extends AppCompatActivity {
    private Button purchaseButton;
    private TextView welcomeMessage, budgetText;
    private RecyclerView purchaseList;
    private RecyclerView.Adapter purchaseListAdapter;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    private List<PurchaseItem> purchases;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        purchaseButton = (Button) findViewById(R.id.addPurchase);
        welcomeMessage = (TextView) findViewById(R.id.welcomeText);
        budgetText = (TextView) findViewById(R.id.budgetText);
        setDate();
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainPage.this, AddPurchase.class));
            }
        });

        purchaseList = (RecyclerView) findViewById(R.id.purchase_list);
        purchaseList.setHasFixedSize(true);
        purchaseListLayoutManager = new LinearLayoutManager(MainPage.this);
        purchaseList.setLayoutManager(purchaseListLayoutManager);
        purchases = new ArrayList<>();
        purchaseListAdapter = new PurchaseAdapter(purchases);
        purchaseList.setAdapter(purchaseListAdapter);
    }

    public void setDate() {
        Date today = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.getDefault());
        String date = formatter.format(today);
        welcomeMessage.append(date);
    }

    public void updateMainPage(String uid) {
        final DocumentReference dRef = db.collection("users").document(uid);

        dRef.collection("purchase").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                document.getString("name"), amount, document.getId()));
                    }

                    purchaseListAdapter.notifyDataSetChanged();
                    budgetText.append(Double.toString(sum));

                    dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, document.getId() + "-->" + document.getData());

                                    if (document.contains("budget")) {
                                        budgetText.append(" / " + document.getDouble("budget"));
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
