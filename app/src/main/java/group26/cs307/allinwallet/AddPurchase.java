package group26.cs307.allinwallet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddPurchase extends AppCompatActivity {
    private Button add;
    private EditText inputName, inputPrice;
    private static final String TAG = "AllinWallet";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_purchase);
        add = (Button) findViewById(R.id.add_item_button);
        inputName = (EditText) findViewById(R.id.item_name);
        inputPrice = (EditText) findViewById(R.id.item_price);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String price = inputPrice.getText().toString();
                Log.d(TAG, "Item Name: " + name);
                Log.d(TAG, "Item Price: " + price);
            }
        });

    }


}
