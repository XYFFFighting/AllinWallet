package group26.cs307.allinwallet;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import android.widget.Toast;

//jenny

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Profile extends AppCompatActivity {
    private ImageView profileImage;
    private Button changeImage;
    private FirebaseAuth auth;
    private Button logout, btn_dlt_act, budgetBotton, incomeButton;
    //private Button refreshbutton;
    private TextView userinfo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";

    private Button btnChoose;

    private final int PICK_IMAGE_REQUEST = 71;

    FirebaseStorage storage;
    StorageReference storageReference;

    private Uri filePath;
    private List<String> incomeTimeSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //jenny
        profileImage = (ImageView) findViewById(R.id.profile_img);
        changeImage = (Button) findViewById(R.id.btn_change);
        budgetBotton = (Button) findViewById(R.id.budgetButton);
        incomeButton = (Button) findViewById(R.id.incomeButton);
        btnChoose = (Button) findViewById(R.id.btn_choose);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        auth = FirebaseAuth.getInstance();
        logout = (Button) findViewById(R.id.btn_logout);
        btn_dlt_act = (Button) findViewById(R.id.btn_dlt_account);
        userinfo = (TextView) findViewById(R.id.user_info);
        adduserInfo();
        try {
            updateImage(profileImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        addNumUser();

        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

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
                                final String uid = user.getUid();
                                deleteData(uid);
                                user.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "user account deleted.");
                                                    startActivity(new Intent(Profile.this, LoginActivity.class));
                                                } else {
                                                    Log.d(TAG, "delete failed");
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

        budgetBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Enter your budget");
                View mView = getLayoutInflater().inflate(R.layout.activity_budget, null);
                final EditText input = (EditText) mView.findViewById(R.id.budgetText);
                final RadioGroup budgetTypeGroup = (RadioGroup) mView.findViewById(R.id
                        .budget_type_group);

                budgetTypeGroup.check(R.id.monthly_budget_type);
                builder.setView(mView);

                builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String uid = auth.getUid();

                        if (!TextUtils.isEmpty(uid)) {
                            String budget_text = input.getText().toString();

                            if (TextUtils.isEmpty(budget_text)) {
                                Toast.makeText(getApplicationContext(), "Budget field is empty!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                String text;
                                switch (budgetTypeGroup.getCheckedRadioButtonId()) {
                                    case R.id.weekly_budget_type:
                                        text = "weekly budget";
                                        break;
                                    case R.id.monthly_budget_type:
                                        text = "monthly budget";
                                        break;
                                    case R.id.annual_budget_type:
                                        text = "annual budget";
                                        break;
                                    default:
                                        text = "Error";
                                        break;
                                }

                                Map<String, Object> budget_info = new HashMap<>();
                                budget_info.put(text, Double.parseDouble(budget_text));
                                CollectionReference users = db.collection("users");
                                users.document(uid).update(budget_info);
                                Toast.makeText(getApplicationContext(), "You have successfully " +
                                                "added " + text,
                                        Toast.LENGTH_SHORT).show();
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

        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Enter your monthly income");
                View mView = getLayoutInflater().inflate(R.layout.activity_income, null);
                final EditText input = (EditText) mView.findViewById(R.id.incomeText);

                builder.setView(mView);
                builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String uid = auth.getUid();

                        if (!TextUtils.isEmpty(uid)) {
                            String income_text = input.getText().toString();

                            if (TextUtils.isEmpty(income_text)) {
                                Toast.makeText(getApplicationContext(), "Income field is empty!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Map<String, Object> income_info = new HashMap<>();
                                income_info.put("income", Double.parseDouble(income_text));
                                CollectionReference users = db.collection("users");
                                users.document(uid).update(income_info);
                                Toast.makeText(getApplicationContext(), "Income added successfully",
                                        Toast.LENGTH_SHORT).show();
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

        getImage();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void updateImage(final ImageView imageView) throws IOException {
        String uid = auth.getUid();
        Log.d(TAG, "uid is:" + uid);
        StorageReference ref = storageReference.child("images/" + uid + "/" + "profile");
        final File localFile = File.createTempFile("images", "jpg");

        ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Log.d(TAG, "download successful");
                if (localFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
        //Log.d(TAG,"download successful" + localFile.getPath());

    }

    private void uploadImage() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String uid = auth.getUid();
            StorageReference ref = storageReference.child("images/" + uid + "/" + "profile");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(Profile.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Profile.this, "Upload Failed Please try again" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            //progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void getImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Getting...");
        progressDialog.show();
        String uid = auth.getUid();
        StorageReference ref = storageReference.child("images/" + uid + "/" + "profile");
        File localFile = null;

        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final File local2 = localFile;
        ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                //Toast.makeText(Profile.this, "success ", Toast.LENGTH_SHORT).show();
                Bitmap bitmap = BitmapFactory.decodeFile(local2.getAbsolutePath());
                profileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //Toast.makeText(Profile.this, "Get Image Failed please check your network", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                        .getTotalByteCount());
                //progressDialog.setMessage("Uploaded "+(int)progress+"%");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
//                                Log.d(TAG, "" + count);
                            }
                            userinfo.append("Number of active users: " + count + "\n");
                        } else {
                            Log.d(TAG, "Error getting number of user", task.getException());
                        }
                    }
                });
    }

    public void deleteData(final String uid) {
        DocumentReference dRef = db.collection("users").document(uid);
        dRef.collection("purchase").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
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
