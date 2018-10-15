package group26.cs307.allinwallet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.InputType;
import android.content.res.Configuration;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AllinWallet";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAnalytics mFirebaseAnalytics;
    private Button signup, login, main, addpurchase, reset, profile, report, budget, search_by_name;
    private EditText authtext;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the FirebaseAnalytics instance.
        auth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        signup = (Button) findViewById(R.id.btn_signup);
        search_by_name = (Button) findViewById(R.id.btn_search_by_name);
        profile = (Button) findViewById(R.id.btn_profile);
        login = (Button) findViewById(R.id.btn_login);
        main = (Button) findViewById(R.id.btn_dashboard);
        report = (Button) findViewById(R.id.btn_report);
        addpurchase = (Button) findViewById(R.id.add_purchase);
        reset = (Button) findViewById(R.id.btn_reset);
        authtext = (EditText) findViewById(R.id.auth_text);
        budget = (Button) findViewById(R.id.btn_budget);

        if (auth.getCurrentUser() == null) {
            authtext.setText("no user log in");
        } else {
            String email = auth.getCurrentUser().getEmail();
            authtext.setText(email);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            }
        });

        search_by_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Search_by_name.class));
            }
        });

        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MainPage.class));
            }
        });

        addpurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddPurchase.class));
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Report.class));
            }
        });

        budget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter a budget");
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setRawInputType(Configuration.KEYBOARD_12KEY);
                builder.setView(input);

                builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String uid = auth.getUid();

                        if (!TextUtils.isEmpty(uid)) {
                            String budget_text = input.getText().toString();

                            if (TextUtils.isEmpty(budget_text)) {
                                Toast.makeText(getApplicationContext(), "Budget field is empty!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Map<String, Object> budget_info = new HashMap<>();
                                budget_info.put("budget", Double.parseDouble(budget_text));
                                CollectionReference users = db.collection("users");
                                users.document(uid).update(budget_info);
                            }
                        }

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


    }

    public void getInfor() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }
}