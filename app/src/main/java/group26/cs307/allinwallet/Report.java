package group26.cs307.allinwallet;

import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Calendar;
import java.util.Locale;

public class Report extends AppCompatActivity {
    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";

    private Date startofWeek, startofMonth, startofYear;

    private Button week, month, annual;
    private String uid;
    private String[] Month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();

        week = (Button) findViewById(R.id.btn_rpt_week);
        month = (Button) findViewById(R.id.btn_rpt_month);
        annual = (Button) findViewById(R.id.btn_rpt_annul);

        initializeDateReport();

        annual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPurchases(uid, view.getId());
            }
        });
        month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPurchases(uid, view.getId());
            }
        });
    }

    public void getPurchases(String uid, final int type) {
        /*report.setText("");
        String currenttime = Calendar.getInstance().getTime().toString();
        final String year = getYear(currenttime);
        //Log.d(TAG, "year: " + year);
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
                                if (type == 3) {
                                    Log.d(TAG, "year2: " + getYear(time));
                                    if (year.equals(getYear(time))) {
                                        report.append(time + " price: " + price + " name: " + name + "\n");
                                    }
                                }
                                if (type == 2) {
                                    if (month.equals(getMonth(time))) {
                                        report.append(time + " price: " + price + " name: " + name + "\n");
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }); */
    }

    public String getMonth(String ctime) {
        String[] timelist = ctime.split("\\s+");
        return timelist[1];
    }

    public String getDate(String ctime) {
        String[] timelist = ctime.split("\\s+");
        return timelist[2];
    }

    public String getYear(String ctime) {
        String[] timelist = ctime.split("\\s+");
        return timelist[5];
    }

    public void initializeDateReport() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Calendar temp = (Calendar) calendar.clone();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        startofWeek = calendar.getTime();

        temp.set(Calendar.DAY_OF_MONTH, 1);
        startofMonth = temp.getTime();

        temp.set(Calendar.MONTH, 1);
        startofYear = temp.getTime();
    }
}
