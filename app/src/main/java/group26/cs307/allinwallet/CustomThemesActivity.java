package group26.cs307.allinwallet;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
            ActionBar ac;
            ac = getSupportActionBar();
            ac.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
            TextView tv;
            tv = findViewById(R.id.themesTitle);
            tv.setTextColor(Color.parseColor("#ffffff"));
        }

    }

    public void goDark(View v) {
        view.setBackgroundResource(R.color.cardview_dark_background);
        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        globalVariable.setThemeSelection("dark");
        ActionBar ac;
        ac = getSupportActionBar();
        ac.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        TextView tv;
        tv = findViewById(R.id.themesTitle);
        tv.setTextColor(Color.parseColor("#ffffff"));

    }
}
