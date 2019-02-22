package rm.spinandwin;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class LogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               setUpJson();
            }
        });
    }

    private void setUpJson()
    {
        JSONObject jsonObject = new JSONObject();
        String userName= ((EditText)findViewById(R.id.userName)).getText().toString().trim();
        if (userName.isEmpty()) {
            Toast.makeText(LogInActivity.this, "Username is mandatory.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (userName.length()>1 && userName.length()<10) {
            Toast.makeText(LogInActivity.this, "Enter valid user name", Toast.LENGTH_SHORT).show();
            return;
        }

        String pass = ((EditText)findViewById(R.id.password)).getText().toString().trim();
        if (pass.isEmpty()) {
            Toast.makeText(LogInActivity.this, "Password is mandatory.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            jsonObject.put("login_id",userName);
            jsonObject.put("login_password",pass);
            hitApi(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitApi(JSONObject jsonObject)
    {
        Log.e("requestBodyIs",jsonObject+"");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "http://apispinwheel.spinwheels.info/GamerLogin"
                , jsonObject, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                Log.e("responseIs",""+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("errorIs",""+error);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
//                headers.put("Accept", "application/json");
                return headers;
            }//get headers
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                2000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(LogInActivity.this);
        requestQueue.add(jsonObjectRequest);
    }
}
