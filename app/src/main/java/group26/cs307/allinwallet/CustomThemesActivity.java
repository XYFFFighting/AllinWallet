package group26.cs307.allinwallet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CustomThemesActivity extends AppCompatActivity {
    //private Button sendFeedback;
    View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cus_themes);
        view = this.getWindow().getDecorView();

        final GlobalClass gv = (GlobalClass) getApplicationContext();
        final String color = gv.getThemeSelection();
        //Toast.makeText(getApplicationContext(), color, Toast.LENGTH_SHORT).show();
        if (color != null && color.equals("dark") ) {
           view.setBackgroundResource(R.color.cardview_dark_background);
        }

    }

    public void goDark(View v) {
        view.setBackgroundResource(R.color.cardview_dark_background);
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        globalVariable.setThemeSelection("dark");


    }
}
