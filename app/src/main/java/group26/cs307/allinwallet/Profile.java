package group26.cs307.allinwallet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.Map;

public class Profile extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button logout;
    private TextView userinfo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();
        logout = (Button) findViewById(R.id.btn_logout);
        userinfo = (TextView) findViewById(R.id.user_info);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(Profile.this, LoginActivity.class));
            }
        });
        adduserInfo();
        addNumUser();
    }

    public void addNumUser() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                Log.d(TAG, "" + count);
                            }
                            userinfo.append("Number of active users: " + count + "\n");
                        } else {
                            Log.d(TAG, "Error getting number of user", task.getException());
                        }
                    }
                });
    }

    public void adduserInfo() {
        String uid = auth.getUid();
        DocumentReference document = db.collection("users").document(uid);
        document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        userinfo.append("Email:" + data.get("email") + "\n");
//                        Log.d(TAG, "email: " + data.get("email"));
                    } else {
                        Log.d(TAG, "no such document");
                    }
                } else {
                    Log.d(TAG, "error in add userInfo");
                }
            }
        });

    }

}
