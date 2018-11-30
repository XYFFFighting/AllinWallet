package group26.cs307.allinwallet;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity implements View.OnClickListener {
    private ImageView profileImage;
    private FirebaseAuth auth;
    private Button logout, btn_dlt_act, budgetButton, incomeButton,
            currencyButton, btnChoose, themeButton, calendarButton;
    private TextView userinfo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private final int PICK_IMAGE_REQUEST = 71;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        profileImage = (ImageView) findViewById(R.id.profile_img);
        budgetButton = (Button) findViewById(R.id.budgetButton);
        incomeButton = (Button) findViewById(R.id.incomeButton);
        btnChoose = (Button) findViewById(R.id.btn_choose);
        currencyButton = (Button) findViewById(R.id.currency);
        logout = (Button) findViewById(R.id.btn_logout);
        btn_dlt_act = (Button) findViewById(R.id.btn_dlt_account);
        themeButton = (Button) findViewById(R.id.theme_button);
        calendarButton = (Button) findViewById(R.id.calendar_button);
        userinfo = (TextView) findViewById(R.id.user_info);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        final String color = globalVariable.getThemeSelection();
        if (color != null && color.equals("dark")) {
            LinearLayout li = (LinearLayout) findViewById(R.id.profileLY);
            li.setBackgroundResource(R.color.cardview_dark_background);
            LinearLayout top = (LinearLayout) findViewById(R.id.profileTopLY);
            top.setBackgroundResource(R.color.black);
            //buttons
            budgetButton.setTextColor(Color.parseColor("#ffffff"));
            incomeButton.setTextColor(Color.parseColor("#ffffff"));
            btnChoose.setTextColor(Color.parseColor("#ffffff"));
            currencyButton.setTextColor(Color.parseColor("#ffffff"));
            logout.setTextColor(Color.parseColor("#ffffff"));
            btn_dlt_act.setTextColor(Color.parseColor("#ffffff"));
            themeButton.setTextColor(Color.parseColor("#ffffff"));
            calendarButton.setTextColor(Color.parseColor("#ffffff"));
        }

        adduserInfo();
        getImage();

        btnChoose.setOnClickListener(this);
        logout.setOnClickListener(this);
        btn_dlt_act.setOnClickListener(this);
        budgetButton.setOnClickListener(this);
        incomeButton.setOnClickListener(this);
        currencyButton.setOnClickListener(this);
        themeButton.setOnClickListener(this);
        calendarButton.setOnClickListener(this);
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
                            Toast.makeText(Profile.this, "Avatar Uploaded", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(Profile.this, "Upload Failed Please try again" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Bitmap bitmap = BitmapFactory.decodeFile(local2.getAbsolutePath());
                profileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
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

            uploadImage();
        }
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
        final CollectionReference cRef = db.collection("users");

        cRef.document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String str = "Email: " + documentSnapshot.getString("email") + "\n";
                        userinfo.setText(str);

                        cRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    int count = task.getResult().size();
                                    userinfo.append("Number of Users: " + count);
                                } else {
                                    Log.d(TAG, "Error getting number of user", task.getException());
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "no such document");
                    }
                } else {
                    Log.d(TAG, "error in add userInfo");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_logout: {
                AlertDialog alertDialogLogout = new AlertDialog.Builder(Profile.this).create();
                alertDialogLogout.setTitle("Log Out");
                alertDialogLogout.setMessage("Are you sure to log out?");
                alertDialogLogout.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                startActivity(new Intent(Profile.this, LoginActivity.class));
                            }
                        });
                alertDialogLogout.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialogLogout.show();
            }
            break;
            case R.id.btn_dlt_account: {
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
            break;
            case R.id.budgetButton: {
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
                                Double budget = Double.parseDouble(budget_text);
                                String text;

                                switch (budgetTypeGroup.getCheckedRadioButtonId()) {
                                    case R.id.weekly_budget_type:
                                        text = "weekly budget";
                                        break;
                                    case R.id.monthly_budget_type:
                                        text = "monthly budget";
                                        MainPage.budgetNum = budget;
                                        MainPage.isBudgetUpdated = true;
                                        break;
                                    case R.id.annual_budget_type:
                                        text = "annual budget";
                                        break;
                                    default:
                                        text = "Error";
                                        break;
                                }

                                Map<String, Object> budget_info = new HashMap<>();
                                budget_info.put(text, budget);

                                db.collection("users").document(uid).update(budget_info);
                                Toast.makeText(getApplicationContext(),
                                        "You have successfully added " + text,
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
            break;
            case R.id.incomeButton: {
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
                                Double income = Double.parseDouble(income_text);
                                MainPage.incomeNum = income;
                                MainPage.isIncomeUpdated = true;
                                Map<String, Object> income_info = new HashMap<>();
                                income_info.put("income", income);

                                db.collection("users").document(uid).update(income_info);
                                Toast.makeText(getApplicationContext(), "Income updated successfully",
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
            break;
            case R.id.currency: {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Set a currency symbol");
                View mView = getLayoutInflater().inflate(R.layout.activity_currency, null);
                final RadioGroup budgetTypeGroup = (RadioGroup) mView.findViewById(R.id
                        .currency_type_group);

                budgetTypeGroup.check(R.id.USD_type);
                builder.setView(mView);

                builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String uid = auth.getUid();

                        if (!TextUtils.isEmpty(uid)) {
                            String text;

                            switch (budgetTypeGroup.getCheckedRadioButtonId()) {
                                case R.id.USD_type:
                                    text = "$";
                                    break;
                                case R.id.EUR_type:
                                    text = "€";
                                    break;
                                case R.id.CNY_type:
                                    text = "¥";
                                    break;
                                default:
                                    text = "Error";
                                    break;
                            }

                            MainPage.currencySign = text;
                            Map<String, Object> budget_info = new HashMap<>();
                            budget_info.put("Currency", text);

                            db.collection("users").document(uid).update(budget_info);
                            Toast.makeText(getApplicationContext(),
                                    "You have successfully updated your currency symbol, the app " +
                                            "will restart now!",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            finish();
                            startActivity(intent);
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
            break;
            case R.id.btn_choose: {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
            break;
            case R.id.theme_button: {
                startActivity(new Intent(Profile.this, CustomThemesActivity.class));
            }
            break;
            case R.id.calendar_button: {
                long startMillis = Calendar.getInstance().getTimeInMillis();
                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, startMillis);
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(builder.build());
                startActivity(intent);
            }
            break;
            default:
                break;
        }
    }
}
