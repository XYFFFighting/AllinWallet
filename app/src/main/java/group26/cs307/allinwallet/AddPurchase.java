
package group26.cs307.allinwallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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

public class AddPurchase extends AppCompatActivity implements View.OnClickListener {
    private Button save, cancel;
    private CheckBox isRecurringExpense;
    private ImageView img_reci;
    private Spinner categoryPicker;
    private EditText inputName, inputPrice, inputDate;
    private String locationString;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private static final String TAG = "AllinWallet";

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar calendar;
    private SimpleDateFormat formatter;
    private int passedPurchaseIndex;
    FirebaseStorage storage;
    StorageReference storageReference;
    private PurchaseItem item;
    private static List<String> categories = new ArrayList<>(Arrays.asList("Grocery",
            "Clothes", "Housing", "Personal", "General", "Transport", "Fun"));
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);

        passedPurchaseIndex = getIntent().getIntExtra("item_key", -1);
        if (passedPurchaseIndex != -1) {
            item = MainPage.purchases.get(passedPurchaseIndex);
        }

        locationString = "No Location";
        isRecurringExpense = (CheckBox) findViewById(R.id.recurring_check_box);
        save = (Button) findViewById(R.id.save_button);
        cancel = (Button) findViewById(R.id.cancel_button);
        inputName = (EditText) findViewById(R.id.item_name);
        inputPrice = (EditText) findViewById(R.id.item_price);
        inputDate = (EditText) findViewById(R.id.item_date);
        img_reci = (ImageView) findViewById(R.id.img_reci);

        categoryPicker = (Spinner) findViewById(R.id.category_picker);
        ArrayAdapter categoryAA = new ArrayAdapter(AddPurchase.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        categoryPicker.setAdapter(categoryAA);

        formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        calendar = Calendar.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        save.setOnClickListener(this);
        cancel.setOnClickListener(this);
        inputDate.setOnClickListener(this);

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

        if (passedPurchaseIndex != -1) {
            setTitle(R.string.title_activity_edit_purchase);
            inputName.setText(item.getTitle());
            inputPrice.setText(item.getAmountString());
            inputDate.setText(item.getDateString());
            calendar.setTime(item.getDate());
            updateReci(item.getDocumentUID());
            categoryPicker.setSelection(categories.indexOf(item.getCategory()));
            isRecurringExpense.setVisibility(View.GONE);
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

        if (img_reci.getDrawable() != null) {
            uploadrecipe(time);
        }
    }

    public void uploadrecipe(String time) {
        //upload picture
        String uid = auth.getUid();
        StorageReference ref = storageReference.child("images/" + uid + "/" + "purchase" + "/" + time + "/" + "recipe");
        img_reci.setDrawingCacheEnabled(true);
        img_reci.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) img_reci.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "upload recipe failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
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

        if (img_reci.getDrawable() != null) {
            uploadrecipe(documentUID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try {
                        String city = getCityName(location.getLatitude(), location.getLongitude());
                        Toast.makeText(AddPurchase.this, "Located: " + city, Toast.LENGTH_SHORT)
                                .show();
                        locationString = city;
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
                for (Address address : addresses) {
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

    private void updateReci(String time) {
        String uid = auth.getUid();
        StorageReference ref = storageReference.child("images/" + uid + "/" + "purchase" + "/" + time + "/" + "recipe");
        Log.d(TAG, "update recipe: " + "images/" + uid + "/" + "purchase" + "/" + time + "/" + "recipe");
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
                Bitmap bitmap = BitmapFactory.decodeFile(local2.getAbsolutePath());
                img_reci.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img_reci.setImageBitmap(imageBitmap);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_purchase_menu, menu);

        if (passedPurchaseIndex == -1) {
            MenuItem check = menu.findItem(R.id.action_check_location);
            check.setVisible(false);
        } else {
            MenuItem get = menu.findItem(R.id.action_get_location);
            get.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_get_location:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                } else {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try {
                        String city = getCityName(location.getLatitude(), location.getLongitude());
                        Toast.makeText(AddPurchase.this, "Located: " + city, Toast.LENGTH_SHORT)
                                .show();
                        locationString = city;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddPurchase.this, "Not found", Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            case R.id.action_check_location:
                if (TextUtils.equals(item.getLocation(), "No Location")) {
                    Toast.makeText(AddPurchase.this, "No Location Found", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(AddPurchase.this, "Place of Purchase: " + item.getLocation(), Toast.LENGTH_SHORT)
                            .show();
                }

                return true;
            case R.id.menu_add_receipt:
                dispatchTakePictureIntent();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                String name = inputName.getText().toString();
                String price = inputPrice.getText().toString();
                String category = categoryPicker.getSelectedItem().toString();
                String location = locationString;
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
                Log.d(TAG, "location: " + locationString);

                if (passedPurchaseIndex == -1) {
                    addPurchase(name, Double.parseDouble(price), category, date, location);
                } else {
                    updatePurchase(name, Double.parseDouble(price), category, date, location, item.getDocumentUID());
                }

                onBackPressed();
                break;
            case R.id.cancel_button:
                onBackPressed();
                break;
            case R.id.item_date:
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(AddPurchase.this,
                        dateSetListener, year, month, day).show();
                break;
            default:
                break;
        }
    }
}
