package rm.spinandwin;

import android.app.Dialog;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

import rm.spinandwin.helper.Api;
import rm.spinandwin.helper.H;
import rm.spinandwin.helper.Json;
import rm.spinandwin.helper.LoadingDialog;
import rm.spinandwin.helper.Static;

import static android.os.Build.VERSION_CODES.P;

public class LogInActivity extends AppCompatActivity
{
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        loadingDialog = new LoadingDialog(this);

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
        Json json = new Json();
        String string= ((EditText)findViewById(R.id.userName)).getText().toString().trim();
        if (string.isEmpty()) {
            Toast.makeText(LogInActivity.this, "Username is mandatory.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (string.length()>1 && string.length()<10) {
            Toast.makeText(LogInActivity.this, "Enter valid user name", Toast.LENGTH_SHORT).show();
            return;
        }

        json.addString("login_id",string);

        string = ((EditText)findViewById(R.id.password)).getText().toString().trim();
        if (string.isEmpty()) {
            Toast.makeText(LogInActivity.this, "Password is mandatory.", Toast.LENGTH_SHORT).show();
            return;
        }

        json.addString("login_password",string);

        //hitLogInApi(json);
        otpPopUp();
    }

    private void otpPopUp() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.otp_dialog);
        dialog.show();
        dialog.setCancelable(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.hide();
            }
        },7000);
    }

    private void hitLogInApi(Json json)
    {
        Api.newApi(this, Static.baseUrl + "GamerLogin").addJson(json)
                .setMethod(Api.POST)
                .onLoading(new Api.OnLoadingListener() {
                    @Override
                    public void onLoading(boolean isLoading) {
                        if (isLoading)
                            loadingDialog.show("loading...");
                        else
                            loadingDialog.dismiss();
                    }
                })
                .onError(new Api.OnErrorListener() {
                    @Override
                    public void onError() {
                        H.showMessage(LogInActivity.this, "Something went wrong.");
                    }
                })
                .onSuccess(new Api.OnSuccessListener() {
                    @Override
                    public void onSuccess(Json json)
                    {
                        H.log("jsonIs",""+json);
                    }
                })
                .run("logIn");
    }
}
