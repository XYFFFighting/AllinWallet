package group26.cs307.allinwallet;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

public class Search_by_name extends Activity {
    private EditText inputName;
    private Button btn_search;
    private TextView result_text;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AllinWallet";
    private FirebaseAuth auth;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_name);
        auth = FirebaseAuth.getInstance();

        btn_search = (Button)findViewById(R.id.btn_search);
        inputName = (EditText)findViewById(R.id.item_name);
        result_text = (TextView)findViewById(R.id.rst_text);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString();
                search_by_name(name);
            }
        });

    }

    public void search_by_name(String name){
        result_text.setText(null);
        String uid = auth.getUid();
        CollectionReference col_purchase = db.collection("users").document(uid).collection("purchase");
        Query result = col_purchase.whereEqualTo("name", name);
        result.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Log.d(TAG, document.getId() + "-->" + document.getData());
                            String text = document.getId() + " " + document.getData().toString() + '\n';
                            result_text.append(text);
                        }

                    }
                });
    }

}
