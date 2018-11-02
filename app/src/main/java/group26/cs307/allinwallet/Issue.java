package group26.cs307.allinwallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Issue extends Activity {
    private Button btn_submit;
    private EditText txt_issue;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        auth = FirebaseAuth.getInstance();
        btn_submit = (Button)findViewById(R.id.btn_submit);
        txt_issue = (EditText)findViewById(R.id.txt_issue);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String issue = txt_issue.getText().toString();
                sendEmail(issue);
            }
        });

    }

    protected void sendEmail(String text){
        String[] TO = {
                "xu881@purdue.edu"
        };
        String uid = auth.getUid();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, TAG + " uid: " + uid);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i(TAG, "email sent");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Issue.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
