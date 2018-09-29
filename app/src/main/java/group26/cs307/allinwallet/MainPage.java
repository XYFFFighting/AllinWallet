package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainPage extends AppCompatActivity {
    private Button purchaseButton;
    private TextView welcomeMessage;

    public void setDate(TextView view) {
        Date today = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.getDefault());
        String date = formatter.format(today);
        view.append(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        purchaseButton = (Button) findViewById(R.id.addPurchase);
        welcomeMessage = (TextView) findViewById(R.id.welcomeText);

        setDate(welcomeMessage);

        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainPage.this, AddPurchase.class));
            }
        });
    }

}
