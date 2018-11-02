package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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

public class CategoriesActivity extends AppCompatActivity {
    private FloatingActionButton purchaseButton;
    private TextView welcomeMessage, groceryText, clothesText, housingText, personalText, generalText, transportText, funText;
    private RecyclerView purchaseList;
    private RecyclerView.Adapter purchaseListAdapter;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    public static List<PurchaseItem> purchases;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        groceryText = (TextView) findViewById(R.id.GroceryText);
        clothesText = (TextView) findViewById(R.id.ClothesText);
        housingText = (TextView) findViewById(R.id.HousingText);
        personalText = (TextView) findViewById(R.id.PersonalText);
        generalText = (TextView) findViewById(R.id.GeneralText);
        transportText = (TextView) findViewById(R.id.TransportText);
        funText = (TextView) findViewById(R.id.FunText);


        //purchaseList = (RecyclerView) findViewById(R.id.purchase_list);
        //purchaseList.setHasFixedSize(true);
        //purchaseListLayoutManager = new LinearLayoutManager(CategoriesActivity.this);
        //purchaseList.setLayoutManager(purchaseListLayoutManager);
        purchases = new ArrayList<>();
    }

    public void updateCategoryPage(String uid) {
        final DocumentReference dRef = db.collection("users").document(uid);

        dRef.collection("purchase").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    purchases.clear();

                    groceryText.setText("Groceries Total Amount: ");
                    clothesText.setText("Clothes Total Amount: ");
                    housingText.setText("Housing Total Amount: ");
                    personalText.setText("Personal Total Amount: ");
                    generalText.setText("General Total Amount: ");
                    transportText.setText("Transport Total Amount: ");
                    funText.setText("Fun Total Amount: ");


                    double amount;
                    double groceryAmount = 0.0;
                    double clothesAmount = 0.0;
                    double housingAmount = 0.0;
                    double personalAmount = 0.0;
                    double generalAmount = 0.0;
                    double transportAmount = 0.0;
                    double funAmount = 0.0;

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + "-->" + document.getData());
                        String category = document.getString("category");
                        amount = document.getDouble("price");
                        assert category != null;
                        switch (category) {
                            case "Grocery":
                                groceryAmount += amount;
                                break;
                            case "Clothes":
                                clothesAmount += amount;
                                break;
                            case "Housing":
                                housingAmount += amount;
                                break;
                            case "Personal":
                                personalAmount += amount;
                                break;
                            case "General":
                                generalAmount += amount;
                                break;
                            case "Transport":
                                transportAmount += amount;
                                break;
                            case "Fun":
                                funAmount += amount;
                                break;
                        }
                    }

                    //purchaseListAdapter.notifyDataSetChanged();
                    groceryText.append(Double.toString(groceryAmount));
                    clothesText.append(Double.toString(clothesAmount));
                    housingText.append(Double.toString(housingAmount));
                    personalText.append(Double.toString(personalAmount));
                    generalText.append(Double.toString(generalAmount));
                    transportText.append(Double.toString(transportAmount));
                    funText.append(Double.toString(funAmount));


                    dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, document.getId() + "-->" + document.getData());

                                    if (document.contains("budget")) {
                                        //budgetText.append(" / " + document.getDouble("budget"));
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
        updateCategoryPage(uid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TBA
    }
}