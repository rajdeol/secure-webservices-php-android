import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class TestActivity extends AppCompatActivity {

    private Button testBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        testBtn = (Button) findViewById(R.id.testBtn);

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new saveComment().execute(comment.getText().toString());
            }
        });
    }

    public boolean testApiCall() {
        ApiAccess apiAccess = new ApiAccess();
        String URL = "Url-to-your-server";
        HashMap<String, String> params = new HashMap<>();
        params.put("test", "testvalue");

        try {
            json = apiAccess.makeHttpRequest( URL, "POST", params);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
