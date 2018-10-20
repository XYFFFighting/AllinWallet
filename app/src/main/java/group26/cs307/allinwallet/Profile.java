package group26.cs307.allinwallet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//jenny
import java.io.File;
import android.app.Activity;
//import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

//jenny

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class Profile extends AppCompatActivity {
    private FirebaseAuth auth;
    private Button logout, btn_dlt_act;
    private TextView userinfo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();
        logout = (Button) findViewById(R.id.btn_logout);
        btn_dlt_act = (Button) findViewById(R.id.btn_dlt_account);
        userinfo = (TextView) findViewById(R.id.user_info);
        adduserInfo();
        addNumUser();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
                alertDialog.setTitle("Log Out");
                alertDialog.setMessage("Are you sure to log out?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                startActivity(new Intent(Profile.this, LoginActivity.class));
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
        btn_dlt_act.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
                alertDialog.setTitle("Delete Account");
                alertDialog.setMessage("Are you sure to delete your account?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                String uid = user.getUid();
                                deleteData(uid);
                                user.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "user account deleted.");
                                                    startActivity(new Intent(Profile.this, LoginActivity.class));
                                                    }
                                            }
                                        });
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
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

    public void deleteData(final String uid){
        DocumentReference dRef = db.collection("users").document(uid);
        dRef.collection("purchase").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document:task.getResult()){
                        String did = document.getId();
                        db.collection("users").document(uid).collection("purchase").document(did)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "document delete successful");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "document delete unsuccessful");
                                    }
                                });
                    }
                }
            }
        });

        db.collection("users").document(uid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "document delete successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "document delete unsuccessful");
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
