package group26.cs307.allinwallet;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Date;

import java.util.Calendar;

public class Report extends AppCompatActivity {
    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private Button week, month, annual;
    private EditText report;
    private String uid;
    private String[] Month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        auth = FirebaseAuth.getInstance();
        week = (Button) findViewById(R.id.btn_rpt_week);
        month = (Button) findViewById(R.id.btn_rpt_month);
        annual = (Button) findViewById(R.id.btn_rpt_annul);
        report = (EditText) findViewById(R.id.text_report);
        uid = auth.getUid();

        annual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    getPurchase(uid, 3);
            }
        });
        month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPurchase(uid, 2);
            }
        });
    }

    public void getPurchase(String uid, final int type) {
        report.setText("");
        String currenttime = Calendar.getInstance().getTime().toString();
        final String year = getYear(currenttime);
        Log.d(TAG, "year: " + year);
        final String month = getMonth(currenttime);
        final String date = getDate(currenttime);
        //1 for week, 2 for month, 3 for year
        db.collection("users").document(uid).collection("purchase")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + "-->" + document.getData());
                                String time = document.getId();
                                Object price = document.getData().get("price");
                                Object name = document.getData().get("name");
                                if(type == 3){
//                                    Log.d(TAG, "year2: " + getYear(time));
                                    if(year.equals(getYear(time))){
                                        report.append(time + " price: " + price + " name: " + name + "\n");
                                    }
                                }
                                if(type == 2 ){
                                    if(month.equals(getMonth(time))){
                                        report.append(time + " price: " + price + " name: " + name + "\n");
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public String getMonth(String ctime){
        String[] timelist = ctime.split("\\s+");
        return timelist[1];
    }

    public String getDate(String ctime){
        String[] timelist = ctime.split("\\s+");
        return timelist[2];
    }

    public String getYear(String ctime){
        String[] timelist = ctime.split("\\s+");
        return timelist[5];
    }
}
