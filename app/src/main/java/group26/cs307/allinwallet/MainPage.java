package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainPage extends AppCompatActivity {
    private Button purchaseButton;
    private TextView welcomeMessage, purchaseList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        String uid = auth.getUid();
        purchaseButton = (Button) findViewById(R.id.addPurchase);
        welcomeMessage = (TextView) findViewById(R.id.welcomeText);
        purchaseList = (TextView) findViewById(R.id.purchase_list);
        setDate(welcomeMessage);
        getPurchase(uid);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainPage.this, AddPurchase.class));
            }
        });
    }

    public void setDate(TextView view) {
        Date today = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.getDefault());
        String date = formatter.format(today);
        view.append(date);
    }

    public String getPurchase(String uid){
        db.collection("users").document(uid).collection("purchase")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + "-->" + document.getData());
                                String purchase = document.getId() + " " + document.getData().toString() + '\n';
                                purchaseList.append(purchase);
                            }
                        }
                        else {
                            Log.e(TAG,"Error getting documents: ", task.getException() );
                        }
                    }
                });
        return "";
    }

}
