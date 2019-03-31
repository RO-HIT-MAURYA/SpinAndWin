package rm.spinandwin;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import rm.spinandwin.helper.Api;
import rm.spinandwin.helper.H;
import rm.spinandwin.helper.Json;
import rm.spinandwin.helper.LoadingDialog;
import rm.spinandwin.helper.Session;
import rm.spinandwin.helper.Static;

public class LogInActivity extends AppCompatActivity {
    private LoadingDialog loadingDialog;
    private Dialog dialog;
    private Json mainJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_log_in);

        loadingDialog = new LoadingDialog(this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpJson();
            }
        });
    }

    private void setUpJson() {
        mainJson = new Json();
        String string = ((EditText) findViewById(R.id.userName)).getText().toString().trim();
        if (string.isEmpty()) {
            Toast.makeText(LogInActivity.this, "Username is mandatory.", Toast.LENGTH_SHORT).show();
            return;
        } else if (string.length() > 1 && string.length() < 10) {
            Toast.makeText(LogInActivity.this, "Enter valid user name", Toast.LENGTH_SHORT).show();
            return;
        }

        mainJson.addString("login_id", string);

        string = ((EditText) findViewById(R.id.password)).getText().toString().trim();
        if (string.isEmpty()) {
            Toast.makeText(LogInActivity.this, "Password is mandatory.", Toast.LENGTH_SHORT).show();
            return;
        }
        mainJson.addString("login_password", string);

        hitLogInApi(mainJson);
    }

    private void hitLogInApi(Json json)
    {
        if (!H.isInternetAvailable(this))
        {
            H.showMessage(this,"Internet connection required");
            return;
        }
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
                    public void onSuccess(Json json) {
                        if (json.getString(Static.status).equalsIgnoreCase("100")) {
                            Session session = new Session(LogInActivity.this);
                            json = json.getJson(Static.data);
                            if (json.getString(Static.IsFirstTimeLogin).equalsIgnoreCase("1")) {
                                String otp = json.getString(Static.OTP);
                                if (otp != null) {
                                    session.addString(Static.OTP, otp);
                                    otpPopUpDialog();
                                }

                            } else if (json.getString(Static.IsFirstTimeLogin).equalsIgnoreCase("0"))
                            {
                                session.addString(Static.data, json + "");
                                startActivity(new Intent(LogInActivity.this,MainActivity.class));
                                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                            }
                        } else
                            H.showMessage(LogInActivity.this, json.getString(Static.message));

                    }
                })
                .run("logIn");
    }

    private void otpPopUpDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.otp_dialog);
        dialog.show();

        dialog.findViewById(R.id.verifyOtp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = ((EditText) dialog.findViewById(R.id.otp)).getText().toString().trim();
                String sessionOtp = new Session(LogInActivity.this).getString(Static.OTP);
                if (sessionOtp != null)
                {
                    if (sessionOtp.equalsIgnoreCase(otp))
                        hitOtpVerificationApi();
                    else
                        H.showMessage(LogInActivity.this, "Please enter valid OTP");
                }
            }
        });

        //dialog.setCancelable(false);

       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.hide();
            }
        },7000);*/
    }

    private void hitOtpVerificationApi() {
        Api.newApi(this, Static.baseUrl + "VerifyOTP").addJson(mainJson)
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
                        if (json.getString(Static.status).equalsIgnoreCase("100"))
                        {
                            hitLogInApi(mainJson);
                        } else
                            H.showMessage(LogInActivity.this, json.getString(Static.message));

                    }
                })
                .run("otpVerification");
    }

}
