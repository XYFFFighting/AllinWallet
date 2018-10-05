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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "AllinWallet";
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;

    ImageView head;
    Button conti, logbut,reset_password,signupbut;
    EditText emaill, pw;
    TextView tips;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //       if (auth.getCurrentUser() != null) {
//            startActivity(new Intent(LoginActivity.this, MainPage.class));
//            finish();
//        }

        // set the view now
        setContentView(R.layout.activity_login);

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
                                    Toast.makeText(LoginActivity.this, "Succ", Toast.LENGTH_LONG).show();
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
                                    tips.setVisibility(View.VISIBLE);
//                                    finish();
                                }
                            }
                        });
            }
        });
    }

    public void addEmail(String email) {
        String uid = auth.getUid();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", email);
        CollectionReference users = db.collection("users");
        users.document(uid).update(userInfo);
    }
}