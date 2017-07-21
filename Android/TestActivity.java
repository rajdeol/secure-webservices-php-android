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

    class saveComment extends AsyncTask<String, String, JSONObject> {

        ApiAccess apiAccess = new ApiAccess();
        JSONObject json;

        private ProgressDialog pDialog;

        private static final String URL = "Url-to-your-server";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(TestActivity.this);
            pDialog.setMessage("Saving Comment...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("comment", args[0]);

                Log.d("Request", "Starting");

                try {
                    json = apiAccess.makeHttpRequest(
                            URL, "POST", params);
                } catch(Exception e) {
                    e.printStackTrace();
                }

                if (json != null) {
                    Log.d("Response: ", "> " + json.toString());
                } else {
                    Log.e("JSON Data", "Didnt receive any data from server");
                    Toast.makeText(TestActivity.this, "Check Your Connection and Try Again", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
                pDialog = null;
            }

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                        Toast.makeText(TestActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TestActivity.this, "Test Successful", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
