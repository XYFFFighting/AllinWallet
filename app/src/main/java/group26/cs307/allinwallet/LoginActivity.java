package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "AllinWallet";
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;

    ImageView head;
    Button conti, logbut, reset_password, signupbut;
    EditText emaill, pw;
    TextView tips;

    //5 TIPS FOR NOW

    List<String> tipList = new ArrayList<>();

    String tip1 = "TIP:\n Buy store-brand products, as they are generally cheaper, yet the same.";
    String tip2 = "TIP:\n Check retailmenot.com, as the website contains many coupons.";
    String tip3 = "TIP:\n Try cooking more at home than eating out, as it is usually more expensive.";
    String tip4 = "TIP:\n You can download music for free on numerous mp3 websites instead of purchasing via iTunes.";
    String tip5 = "TIP:\n Stream TV shows and movies for free using putlocker.com.";

    View view;
    LinearLayout li;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //insert all tips into tipList

        tipList.add(tip1);
        tipList.add(tip2);
        tipList.add(tip3);
        tipList.add(tip4);
        tipList.add(tip5);

        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        //       if (auth.getCurrentUser() != null) {
//            startActivity(new Intent(LoginActivity.this, MainPage.class));
//            finish();
//        }

        // set the view now
        setContentView(R.layout.activity_login);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        final String color = globalVariable.getThemeSelection();
        if (color != null && color.equals("dark")) {
            li = (LinearLayout) findViewById(R.id.loginLY);
            li.setBackgroundResource(R.color.cardview_dark_background);
        }

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);

        //zhang
        conti = (Button) findViewById(R.id.continuebutton);
        tips = (TextView) findViewById(R.id.tipview);

        //zhang modify
        conti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainPage.class);
                startActivity(intent);
                finish();
            }
        });

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.d(TAG, "User ID: " + auth.getUid());
                                    addEmail(email);
//                                    Intent intent = new Intent(LoginActivity.this, MainPage.class);
//                                    startActivity(intent);
                                    Toast.makeText(LoginActivity.this, "Success!", Toast
                                            .LENGTH_LONG).show();
//                                    finish();
                                    head = findViewById(R.id.head);
                                    head.setVisibility(View.INVISIBLE);
                                    reset_password = findViewById(R.id.btn_reset_password);
                                    reset_password.setVisibility(View.INVISIBLE);
                                    signupbut = findViewById(R.id.btn_signup);
                                    signupbut.setVisibility(View.INVISIBLE);
                                    logbut = findViewById(R.id.btn_login);
                                    logbut.setVisibility(View.INVISIBLE);
                                    emaill = (EditText) findViewById(R.id.email);
                                    pw = (EditText) findViewById(R.id.password);
                                    TextInputLayout til = findViewById(R.id.passw);
                                    til.setVisibility(View.INVISIBLE);
                                    emaill.setHint("");
                                    pw.setHint("");
                                    emaill.setVisibility(View.INVISIBLE);
                                    pw.setVisibility(View.INVISIBLE);
                                    TextInputLayout em = findViewById(R.id.em);
                                    em.setVisibility(View.INVISIBLE);
                                    conti.setVisibility(View.VISIBLE);

                                    //RANDOMIZE THE INDEX HERE:

                                    Random indexGenerator = new Random();
                                    int i = indexGenerator.nextInt(tipList.size());

                                    //get string at generated index from tipList

                                    tips.setText(tipList.get(i));
                                    tips.setVisibility(View.VISIBLE);
//                                    finish();
                                }
                            }
                        });
            }
        });
    }

    public void addEmail(final String email) {
        String uid = auth.getUid();
        final DocumentReference dRef = db.collection("users").document(uid);

        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("email", email);

                    if (document.exists()) {
                        Log.d(TAG, document.getId() + "-->" + document.getData());
                        dRef.update(userInfo);
                    } else {
                        Log.d(TAG, "Creating new document");
                        dRef.set(userInfo);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}