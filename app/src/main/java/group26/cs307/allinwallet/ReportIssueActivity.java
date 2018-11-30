package group26.cs307.allinwallet;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ReportIssueActivity extends AppCompatActivity{
    private Button sendFeedback;
    View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        final String color = globalVariable.getThemeSelection();
        if (color != null && color.equals("dark")) {
            view = this.getWindow().getDecorView();
            view.setBackgroundResource(R.color.cardview_dark_background);
            ActionBar ac;
            ac = getSupportActionBar();
            ac.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
            TextView issue;
            issue = findViewById(R.id.issueTitle);
            issue.setTextColor(Color.parseColor("#ffffff"));
            EditText edit;
            edit = findViewById(R.id.editText);
            edit.setHintTextColor(Color.parseColor("#ffffff"));
        }

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
