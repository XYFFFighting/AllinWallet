package group26.cs307.allinwallet;

import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Report extends AppCompatActivity {
    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";

    private Date startofWeek, startofMonth, startofYear;

    private Button week, month, annual;
    private String uid;

    private RecyclerView purchaseList;
    private RecyclerView.Adapter purchaseListAdapter;
    private RecyclerView.LayoutManager purchaseListLayoutManager;
    public static List<PurchaseItem> purchases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();

        purchaseList = (RecyclerView) findViewById(R.id.rst_search);
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

        week = (Button) findViewById(R.id.btn_rpt_week);
        month = (Button) findViewById(R.id.btn_rpt_month);
        annual = (Button) findViewById(R.id.btn_rpt_annul);

        initializeDateReport();

        week.setOnClickListener(new View.OnClickListener() {
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
        annual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPurchases(uid, view.getId());
            }
        });
    }

    public void getPurchases(String uid, int whichButton) {
        purchases.clear();
        Query result = db.collection("users").document(uid).collection("purchase");

        switch (whichButton) {
            case R.id.btn_rpt_week:
                result = result.whereGreaterThanOrEqualTo("date", startofWeek);
                break;
            case R.id.btn_rpt_month:
                result = result.whereGreaterThanOrEqualTo("date", startofMonth);
                break;
            case R.id.btn_rpt_annul:
                result = result.whereGreaterThanOrEqualTo("date", startofYear);
                break;

            default:
                break;
        }

        result.orderBy("date", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Log.d(TAG, document.getId() + "-->" + document.getData());
                            purchases.add(new PurchaseItem(document.getString("category"),
                                    document.getString("name"), document.getDouble("price"),
                                    document.getDate("date"), document.getString("location"), document.getId()));
                        }

                        purchaseListAdapter.notifyDataSetChanged();
                    }
                });
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
