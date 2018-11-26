package group26.cs307.allinwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ShareActivity extends AppCompatActivity{
    private Button sendFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        //sendFeedback = (Button) findViewById(R.id.feedBackbtn);

        //Will use this template across other activities to share certain content
        //Look into other ways to share things

        String message = "The content I wish to share.";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, "How would you like to share this?"));
    }





}
