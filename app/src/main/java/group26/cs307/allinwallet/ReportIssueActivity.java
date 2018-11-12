package group26.cs307.allinwallet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ReportIssueActivity extends AppCompatActivity{
    private Button sendFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);
        sendFeedback = (Button) findViewById(R.id.feedBackbtn);


        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Feedback submitted", Toast.LENGTH_LONG).show();
                //setContentView(R.layout.activity_main);
            }
        });
    }





}
