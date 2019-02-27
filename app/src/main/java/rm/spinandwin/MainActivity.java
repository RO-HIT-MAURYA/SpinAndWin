package rm.spinandwin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rm.spinandwin.helper.Api;
import rm.spinandwin.helper.H;
import rm.spinandwin.helper.Json;
import rm.spinandwin.helper.LoadingDialog;
import rm.spinandwin.helper.Session;
import rm.spinandwin.helper.Static;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Dialog dialog;
    private long coins;
    private String userId;
    private String assignedUserId;
    private LoadingDialog loadingDialog;
    private JsonObjectRequest jsonObjectRequest;
    private long time;
    private String betCoin,betNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new LoadingDialog(this);

        String data = new Session(this).getString(Static.data);
        try {
            Json json = new Json(data);
            String string = json.getString(Static.TotalCoins);
            coins = Long.parseLong(string);
            ((TextView)findViewById(R.id.coinCount)).setText(string);
            userId = json.getString(Static.UserId);
            assignedUserId = json.getString(Static.AssignedUserId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        getRecentNumbers();
        //setUpGrid(2);
    }

    private void getRecentNumbers()
    {
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Static.baseUrl + "GetRecentNumbers", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    H.log("getRecentNumbers", response.toString());
                    if (response.getString(Static.status).equalsIgnoreCase("100"))
                        setBlueLayoutData(response);
                    else
                        H.showMessage(MainActivity.this,Static.message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                H.log("errorIs", error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(jsonObjectRequest);
    }

    private void setBlueLayoutData(JSONObject jsonObject)
    {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(Static.data);
            LinearLayout linearLayout = findViewById(R.id.blueLayout);
            for (int i=0; i<5;i++)
            {
                jsonObject = jsonArray.getJSONObject(i);
                String string = jsonObject.getString(Static.number);
                if (string!=null)
                    ((TextView)linearLayout.getChildAt(i)).setText(string);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int count;

    public void onRowOneClick(View view)
    {
        if (view.getId() == R.id.transfer) {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.transfer_layout);

            dialog.findViewById(R.id.close).setOnClickListener(this);
            dialog.findViewById(R.id.transfer).setOnClickListener(this);
            dialog.show();
        }
        else if (view.getId() == R.id.logout)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setMessage("Do you really want to exit?");
            adb.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Session(MainActivity.this).clear();
                    finish();
                }
            });
            adb.setNegativeButton("NO",null);
            adb.show();
        }
        else
        {
            H.showMessage(this, ((TextView) view).getText().toString());
            LinearLayout linearLayout = findViewById(R.id.topMostRow);
            TextView textView;
            for (int i=1; i<linearLayout.getChildCount()-3;i++)
            {
                textView = (TextView)linearLayout.getChildAt(i);
                textView.setBackground(getResources().getDrawable(R.drawable.tv_sbg));
                textView.setTextColor(getResources().getColor(R.color.black));
            }
            view.setBackground(getResources().getDrawable(R.drawable.tv_bg));
            ((TextView)view).setTextColor(getResources().getColor(R.color.white));

            betCoin = ((TextView)view).getText().toString();
            H.log("betCoinIs",betCoin);
            H.log("betNumIs",betNumber);
            if (betCoin!=null && betNumber!=null)
            {
                if (!betCoin.isEmpty() && !betNumber.isEmpty())
                    hitBettingApi(betNumber,betCoin);
            }
        }
    }

    public void on1To36Click(View view) {
        H.log("clickIs", ((TextView) view).getText().toString());
        H.showMessage(this,((TextView) view).getText().toString());

        betNumber = ((TextView) view).getText().toString();
        if (betCoin!=null && betNumber!=null)
        {
            if (!betCoin.isEmpty() && !betNumber.isEmpty())
                hitBettingApi(betNumber,betCoin);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close)
            dialog.hide();
    }

    public void onBorderClick(View view) {
        H.log("borderClickIs", view.getTag().toString());
        H.showMessage(this, view.getTag().toString());

        betNumber = view.getTag().toString();
        if (betCoin!=null && betNumber!=null)
        {
            if (!betCoin.isEmpty() && !betNumber.isEmpty())
                hitBettingApi(betNumber,betCoin);
        }
    }

    public void onTransferClick(View view) {
        String string = ((EditText) dialog.findViewById(R.id.transferAmount)).getText().toString();
        try {
            long l = Long.parseLong(string);
            if (l > coins) {
                H.showMessage(this, "You don't have sufficient coins");
                return;
            } else
                hitTransferCoinApi(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hitTransferCoinApi(long l) {
        Json json = new Json();
        if (userId != null)
            json.addString(Static.user_id, userId);
        if (assignedUserId != null)
            json.addString(Static.assigned_d_userid, assignedUserId);
        json.addString(Static.coins, "" + l);

        Api.newApi(this, Static.baseUrl + "TransferCoin").addJson(json)
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
                        H.showMessage(MainActivity.this, "Something went wrong.");
                    }
                })
                .onSuccess(new Api.OnSuccessListener() {
                    @Override
                    public void onSuccess(Json json) {
                        if (json.getString(Static.status).equalsIgnoreCase("100")) {
                            H.showMessage(MainActivity.this, json.getString(Static.message));
                            json = json.getJson(Static.data);
                            try {
                                long l = json.getLong(Static.TotalCoins);
                                coins = l;
                                ((TextView)findViewById(R.id.coinCount)).setText(""+coins);
                                dialog.hide();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else
                            H.showMessage(MainActivity.this, json.getString(Static.message));

                    }
                })
                .run("transferCoin");
    }

    private void hitBettingApi(String bN,String bC)
    {
        Json json = new Json();
        json.addString(Static.user_id,userId);
        json.addString(Static.bet_number,bN);
        json.addString(Static.bet_coin,bC);

        Api.newApi(this, Static.baseUrl + "Betting").addJson(json)
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
                        H.showMessage(MainActivity.this, "Something went wrong.");
                    }
                })
                .onSuccess(new Api.OnSuccessListener() {
                    @Override
                    public void onSuccess(Json json) {
                        if (json.getString(Static.status).equalsIgnoreCase("100"))
                        {
                            betCoin = "";
                            betNumber = "";
                            json = json.getJson(Static.data);
                            String string = json.getString(Static.TotalCoins);
                            coins = Long.parseLong(string);
                            ((TextView)findViewById(R.id.coinCount)).setText(string);
                        } else
                            H.showMessage(MainActivity.this, json.getString(Static.message));

                    }
                })
                .run("betting");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed()
    {
        if (System.currentTimeMillis() - time < 800)
            super.onBackPressed();
        else {
            time = System.currentTimeMillis();
            H.showMessage(this, "Press again to exit.");
        }
    }
}
