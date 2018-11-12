package group26.cs307.allinwallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPurchase extends AppCompatActivity {
    String mCurrentPhotoPath;
    private Button save, cancel, getlocation, btn_take_picture;
    private ImageView img_reci;
    private Bitmap recip;
    private Spinner categoryPicker;
    private EditText inputName, inputPrice, inputDate;
    private TextView txt_location;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "AllinWallet";

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar calendar;
    private SimpleDateFormat formatter;
    private int passedPurchaseIndex;
    private PurchaseItem item;
    public static List<String> defaultCategories = new ArrayList<>(Arrays.asList("Grocery",
            "Clothes", "Housing", "Personal", "General", "Transport", "Fun"));
    private List<String> categories;
    private Intent takePictureIntent;
    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        passedPurchaseIndex = getIntent().getIntExtra("item_key", -1);
        if (passedPurchaseIndex != -1) {
            item = MainPage.purchases.get(passedPurchaseIndex);
        }

        save = (Button) findViewById(R.id.save_button);
        cancel = (Button) findViewById(R.id.cancel_button);
        getlocation = (Button) findViewById(R.id.btn_get_location);
        btn_take_picture = (Button) findViewById(R.id.btn_take_pic);
        categoryPicker = (Spinner) findViewById(R.id.category_picker);
        inputName = (EditText) findViewById(R.id.item_name);
        inputPrice = (EditText) findViewById(R.id.item_price);
        inputDate = (EditText) findViewById(R.id.item_date);
        txt_location = (TextView) findViewById(R.id.txt_location);
        img_reci = (ImageView) findViewById(R.id.img_reci);
        categories = new ArrayList<>();
        categories.addAll(defaultCategories);
        // TO-DO: get categories from firebase
        ArrayAdapter spinnerAA = new ArrayAdapter(AddPurchase.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(spinnerAA);
        formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        calendar = Calendar.getInstance();

        btn_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TakePictureIntent();
                handleSmallCameraPhoto(takePictureIntent);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String price = inputPrice.getText().toString();
                String category = categoryPicker.getSelectedItem().toString();
                String location = txt_location.getText().toString();
                if(location.equals("Location")){
                    location = "";
                }
                Date date = calendar.getTime();

                if (TextUtils.isEmpty(name)) {
                    inputName.setError("Title cannot be empty");
                    return;
                }
                if (TextUtils.isEmpty(price)) {
                    inputPrice.setError("Amount cannot be empty");
                    return;
                }
                Log.d(TAG, "Item Title: " + name);
                Log.d(TAG, "Item Amount: " + price);
                Log.d(TAG, "Item Category: " + category);
                Log.d(TAG, "Item Date: " + date);
                Log.d(TAG, "location: " + location);

                if (passedPurchaseIndex == -1) {
                    addPurchase(name, Double.parseDouble(price), category, date, location);
                } else {
                    updatePurchase(name, Double.parseDouble(price), category, date, location, item.getDocumentUID());
                }
                onBackPressed();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                String date = formatter.format(calendar.getTime());
                inputDate.setText(date);
                Log.d(TAG, "onDateSet: mm/dd/yyy:" + date);
            }
        };

        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(AddPurchase.this,
                        dateSetListener, year, month, day).show();
            }
        });

        getlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                } else {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try {
                        String city = getCityName(location.getLatitude(), location.getLongitude());
                        txt_location.setText(city);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddPurchase.this, "Not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        if (passedPurchaseIndex != -1) {
            setTitle(R.string.title_activity_edit_purchase);
            inputName.setText(item.getTitle());
            inputPrice.setText(item.getAmountString());
            inputDate.setText(item.getDateString());
            calendar.setTime(item.getDate());
            if(!item.getLocation().equals("")){
                txt_location.setText(item.getLocation());
            } else {
                txt_location.setText("");
            }
            categoryPicker.setSelection(categories.indexOf(item.getCategory()));
            getlocation.setVisibility(View.INVISIBLE);
        } else {
            inputDate.setText(formatter.format(calendar.getTime()));
        }
    }

    public void addPurchase(String name, double price, String category, Date date, String location) {
        String time = Calendar.getInstance().getTime().toString();

        Log.d(TAG, "purchase sending time is: " + time);
        String uid = auth.getUid();

        Map<String, Object> purchaselist = new HashMap<>();
        purchaselist.put("name", name);
        purchaselist.put("price", price);
        purchaselist.put("category", category);
        purchaselist.put("date", date);
        purchaselist.put("location", location);

        db.collection("users").document(uid)
                .collection("purchase").document(time).set(purchaselist);
        Log.d(TAG, uid + " send purchase data");
    }

    public void updatePurchase(String name, double price, String category, Date date, String location, String
            documentUID) {
        String uid = auth.getUid();

        Map<String, Object> purchaselist = new HashMap<>();
        purchaselist.put("name", name);
        purchaselist.put("price", price);
        purchaselist.put("category", category);
        purchaselist.put("date", date);
        purchaselist.put("location", location);

        db.collection("users").document(uid)
                .collection("purchase").document(documentUID).update(purchaselist);
        Log.d(TAG, uid + " update purchase data");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1000:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try {
                        String city = getCityName(location.getLatitude(), location.getLongitude());
                        txt_location.setText(city);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddPurchase.this, "Not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }

    private String getCityName(double latitude, double lontitude) {
        Log.d(TAG, "latitude:" + latitude + " lontitude" + lontitude);
        String cityName = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, lontitude, 10);
            if (addresses.size() > 0) {
                for(Address address : addresses) {
                    if (address.getLocality() != null && address.getLocality().length() > 0) {
                        cityName = address.getLocality();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private void TakePictureIntent(){
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "group26.cs307.allinwallet",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }


    }


    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
        recip = (Bitmap) extras.get("data");
        img_reci.setImageBitmap(recip);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "add picture resume");
        if(takePictureIntent!= null)
            handleSmallCameraPhoto(takePictureIntent);
    }
}


