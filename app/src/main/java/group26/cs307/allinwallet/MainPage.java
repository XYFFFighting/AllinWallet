package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;

public class MainPage extends AppCompatActivity {
    private Button purchaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        purchaseButton = (Button) findViewById(R.id.addPurchase);

        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainPage.this, AddPurchase.class));
            }
        });
    }

}
