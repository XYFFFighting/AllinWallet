package group26.cs307.allinwallet;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
public class UploadActivity extends AppCompatActivity {
    private ImageView image;
    private EditText imageName;
    private Button btnUpload,btnNext,btnBack;
    private ProgressDialog mProgressDialog;

    private final static int mWidth = 512;
    private final static int mLength = 512;

    private ArrayList<String> pathArray;
    private int array_position;

    private StorageReference mStorageRef;
    private FirebaseAuth auth;

    public UploadActivity() {
    }
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_layout);
        image = (ImageView) findViewById(R.id.uploadImage);
        btnBack = (Button) findViewById(R.id.btnBackImage);
        btnNext = (Button) findViewById(R.id.btnNextImage);
        btnUpload = (Button) findViewById(R.id.btnUploadImage);
        imageName = (EditText) findViewById(R.id.imageName);
        pathArray = new ArrayList<>();
        mProgressDialog = new ProgressDialog(UploadActivity.this);
        auth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();
    }
}
