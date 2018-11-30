package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ShareActivity extends AppCompatActivity{
    private Button sendFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        //sendFeedback = (Button) findViewById(R.id.feedBackbtn);

        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Our application name");
            String sAux = "\nLet us recommend you this application\n\n";
            sAux = sAux + "https://github.com/raynaran/AllinWallet \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose one"));
        } catch(Exception e) {
            //e.toString();
        }
    }





}
