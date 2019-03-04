package rm.spinandwin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.util.Calendar;
import java.util.Date;

import rm.spinandwin.helper.Api;
import rm.spinandwin.helper.H;
import rm.spinandwin.helper.Json;
import rm.spinandwin.helper.LoadingDialog;
import rm.spinandwin.helper.Session;
import rm.spinandwin.helper.Static;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Dialog dialog;
    private int totalCoins;
    private String userId;
    private String assignedUserId;
    private LoadingDialog loadingDialog;
    private int totalBatedAmt;
    private JsonObjectRequest jsonObjectRequest;
    private long time;
    private String betCoin = "1";
    private int betAmount = 1;
    private String betNumber;
    private RelativeLayout relativeLayout;
    private TextView textView;
    private CountDownTimer countDownTimer;
    private int totalBetAmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adjustSeconds();

        String data = new Session(this).getString(Static.data);
        try {
            Json json = new Json(data);
            String string = json.getString(Static.TotalCoins);
            totalCoins = Integer.parseInt(string);
            ((TextView) findViewById(R.id.coinCount)).setText(string);
            userId = json.getString(Static.UserId);
            assignedUserId = json.getString(Static.AssignedUserId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //setUpGrid(2);
    }


    Calendar calendar;
    Date date;
    int min;
    /*Thread thread;
    Handler handler;*/

    private void adjustSeconds() {
        //min = calendar.get(Calendar.MINUTE);
        //final int m = min;
        loadingDialog = new LoadingDialog(MainActivity.this);
        loadingDialog.show("loading...");
        calendar = Calendar.getInstance();
        date = new Date(System.currentTimeMillis());
        calendar.setTime(date);
        int ms = calendar.get(Calendar.MILLISECOND);
        int sec = calendar.get(Calendar.SECOND);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.hide();
                showTimerOnScreen();
                getRecentNumbers();
            }
        }, 60000 - ((sec * 1000) + ms));

    }

    int i;

    private void showTimerOnScreen() {
        loadingDialog = new LoadingDialog(this, false);
        loadingDialog.findViewById(R.id.loadingDialog).setVisibility(View.INVISIBLE);
        loadingDialog.findViewById(R.id.msg).setVisibility(View.INVISIBLE);
        final TextView textView = findViewById(R.id.spineTime);
        countDownTimer = new CountDownTimer(59000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                i = (int) (millisUntilFinished / 1000) - 10;

                if (i == 10)
                    loadingDialog.show();
                if (i == 0) {
                    loadingDialog.hide();
                    disableBetAmtRow();
                }
                if (i <= 10)
                    textView.setTextColor(getResources().getColor(R.color.red));
                if (i >= 0)
                    textView.setText(i + "");
               /* if (i==3)
                    hitForWinningNumber();*/

            }

            @Override
            public void onFinish() {
                startActivity(getIntent());
            }
        }.start();
    }

    private void disableBetAmtRow() {
        LinearLayout linearLayout = findViewById(R.id.topMostRow);
        RelativeLayout relativeLayout;
        for (int i = 0; i < linearLayout.getChildCount(); i++)//for bet amt row
        {
            linearLayout.getChildAt(i).setEnabled(false);
        }

        linearLayout = findViewById(R.id.lL1);
        for (int i = 1; i < linearLayout.getChildCount(); i++) // for 1-18 & 19-36 row
        {
            relativeLayout = (RelativeLayout) linearLayout.getChildAt(i);
            relativeLayout.getChildAt(1).setEnabled(false);
        }

        linearLayout = findViewById(R.id.lL2);
        for (int i = 0; i < linearLayout.getChildCount(); i++) //for  3: 1 to 3
        {
            relativeLayout = (RelativeLayout) linearLayout.getChildAt(i);
            relativeLayout.getChildAt(1).setEnabled(false);
        }

        linearLayout = findViewById(R.id.lL3);
        for (int i = 0; i < linearLayout.getChildCount(); i++) // for 0 & 00
        {
            relativeLayout = (RelativeLayout) linearLayout.getChildAt(i);
            relativeLayout.getChildAt(1).setEnabled(false);
        }

        linearLayout = findViewById(R.id.lL4);
        for (int i = 1; i < linearLayout.getChildCount(); i++) // for 1-12 row
        {
            relativeLayout = (RelativeLayout) linearLayout.getChildAt(i);
            relativeLayout.getChildAt(1).setEnabled(false);
        }

        linearLayout = findViewById(R.id.lL5);
        for (int i = 1; i < linearLayout.getChildCount(); i++) // for even odd row
        {
            relativeLayout = (RelativeLayout) linearLayout.getChildAt(i);
            relativeLayout.getChildAt(1).setEnabled(false);
        }

        linearLayout = findViewById(R.id.parentLayout);
        LinearLayout childLayout;
        for (int i=0; i<linearLayout.getChildCount(); i++)
        {
            childLayout = (LinearLayout)linearLayout.getChildAt(i);
            for (int j=0; j<childLayout.getChildCount(); j++)
            {
                relativeLayout = (RelativeLayout)childLayout.getChildAt(j);
                relativeLayout.getChildAt(1).setEnabled(false);
            }
        }

    }

    private void hitForWinningNumber() {
        Json json = new Json();
        json.addString(Static.user_id, userId);

        Api.newApi(this, Static.baseUrl + "GetWinningNumberByID").addJson(json)
                .setMethod(Api.POST)
                .onLoading(new Api.OnLoadingListener() {
                    @Override
                    public void onLoading(boolean isLoading) {
                        if (isLoading)
                            H.log("loading...", "winning number Api");
                        else
                            H.log("loading... ", "completed");
                            /*loadingDialog.show("loading...");
                        else
                            loadingDialog.dismiss();*/
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
                            json = json.getJson(Static.data);
                            String winNumber = json.getString(Static.WinNumber);
                            LinearLayout linearLayout = findViewById(R.id.superLayout);
                            //Drawable drawable = linearLayout.findViewsWithText(textView,);
                        } else
                            H.showMessage(MainActivity.this, json.getString(Static.message));

                    }
                })
                .run("winningNumberApi");
    }

    private void getRecentNumbers() {
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Static.baseUrl + "GetRecentNumbers", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    H.log("getRecentNumbers", response.toString());
                    if (response.getString(Static.status).equalsIgnoreCase("100"))
                        setBlueLayoutData(response);
                    else
                        H.showMessage(MainActivity.this, Static.message);
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

    private void setBlueLayoutData(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(Static.data);
            LinearLayout linearLayout = findViewById(R.id.blueLayout);
            for (int i = 0; i < 5; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String string = jsonObject.getString(Static.number);
                if (string != null)
                    ((TextView) linearLayout.getChildAt(i)).setText(string);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onRowOneClick(View view) {
        if (view.getId() == R.id.transfer) {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.transfer_layout);

            dialog.findViewById(R.id.close).setOnClickListener(this);
            dialog.findViewById(R.id.transfer).setOnClickListener(this);
            dialog.show();
        } else if (view.getId() == R.id.logout) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setMessage("Do you really want to exit?");
            adb.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Session(MainActivity.this).clear();
                    finish();
                }
            });
            adb.setNegativeButton("NO", null);
            adb.show();
        } else {
            H.showMessage(this, ((TextView) view).getText().toString());
            LinearLayout linearLayout = findViewById(R.id.topMostRow);
            TextView textView;
            for (int i = 1; i < linearLayout.getChildCount() - 3; i++) {
                textView = (TextView) linearLayout.getChildAt(i);
                textView.setBackground(getResources().getDrawable(R.drawable.tv_sbg));
                textView.setTextColor(getResources().getColor(R.color.black));
            }
            view.setBackground(getResources().getDrawable(R.drawable.tv_bg));
            ((TextView) view).setTextColor(getResources().getColor(R.color.white));

            betCoin = ((TextView) view).getText().toString();
            betAmount = extractInt(betCoin);

            H.log("betCoinIs", betCoin);
            H.log("betNumIs", betNumber);
        }
    }

    public void on1To36Click(View view) {
        H.log("clickIs", ((TextView) view).getText().toString());
        H.showMessage(this, ((TextView) view).getText().toString());

        betNumber = ((TextView) view).getText().toString();

        if (!betCoin.isEmpty() && !betNumber.isEmpty()) {
            if (betAmount > totalCoins)
                H.showMessage(this, "You don't have sufficient coins");
            else
                hitBettingApi(betNumber, betCoin);
        }

        relativeLayout = (RelativeLayout) view.getParent();
        textView = (TextView) relativeLayout.getChildAt(0);
        textView.setVisibility(View.VISIBLE);
        Object tag = textView.getTag();
        H.log("tagIs", tag + "");
        if (tag == null) {
            textView.setTag(betAmount);
            textView.setText(betAmount + "");
        } else {
            int i = (int) textView.getTag() + betAmount;
            textView.setTag(i);
            textView.setText(i + "");
        }

        //int i =0;

        H.log("betCoinIs", betCoin);
        H.log("betNumIs", betNumber);

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

        if (!betCoin.isEmpty() && !betNumber.isEmpty()) {
            if (betAmount > totalCoins)
                H.showMessage(this, "You don't have sufficient coins");
            else
                hitBettingApi(betNumber, betCoin);
        }

        relativeLayout = (RelativeLayout) view.getParent();
        textView = (TextView) relativeLayout.getChildAt(0);
        textView.setVisibility(View.VISIBLE);
        Object tag = textView.getTag();
        H.log("tagIs", tag + "");
        if (tag == null) {
            textView.setTag(betAmount);
            textView.setText(betAmount + "");
        } else {
            int i = (int) textView.getTag() + betAmount;
            textView.setTag(i);
            textView.setText(i + "");
        }

        H.log("betCoinIs", betCoin);
        H.log("betNumIs", betNumber);
    }

    public void onTransferClick(View view) {
        String string = ((EditText) dialog.findViewById(R.id.transferAmount)).getText().toString();
        try {
            long l = Long.parseLong(string);
            if (l > totalCoins) {
                H.showMessage(this, "You don't have sufficient totalCoins");
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

                            int l = json.getInt(Static.TotalCoins);
                            updateSessionCoins(l + "");
                            totalCoins = l;
                            ((TextView) findViewById(R.id.coinCount)).setText("" + totalCoins);
                            dialog.hide();

                        } else
                            H.showMessage(MainActivity.this, json.getString(Static.message));

                    }
                })
                .run("transferCoin");
    }


    int bc;//to avoid private of bC

    private void hitBettingApi(String bN, String bC) {
        Json json = new Json();
        json.addString(Static.user_id, userId);
        json.addString(Static.bet_number, bN);
        json.addString(Static.bet_coin, bC);
        bc = extractInt(bC);

        Api.newApi(this, Static.baseUrl + "Betting").addJson(json)
                .setMethod(Api.POST)
                .onLoading(new Api.OnLoadingListener() {
                    @Override
                    public void onLoading(boolean isLoading) {
                        /*if (isLoading)
                            loadingDialog.show("loading...");
                        else
                            loadingDialog.dismiss();*/
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
                            //betCoin = "";
                            betNumber = "";
                            json = json.getJson(Static.data);
                            String string = json.getString(Static.TotalCoins);
                            totalCoins = extractInt(string);
                            ((TextView) findViewById(R.id.coinCount)).setText(string);
                            updateSessionCoins(string);
                            totalBatedAmt = totalBatedAmt + bc;
                            ((TextView) findViewById(R.id.betAmt)).setText(totalBatedAmt + "");
                        } else
                            H.showMessage(MainActivity.this, json.getString(Static.message));

                    }
                })
                .run("betting");
    }

    private void updateSessionCoins(String str) {
        Session session = new Session(this);
        String data = session.getString(Static.data);
        try {
            Json json = new Json(data);
            //String string = json.getString(Static.TotalCoins);
            json.addString(Static.TotalCoins, str);

            session.addString(Static.data, json + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int extractInt(String string) {
        int i = 0;
        try {
            i = Integer.parseInt(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time < 800)
            super.onBackPressed();
        else {
            time = System.currentTimeMillis();
            H.showMessage(this, "Press again to exit.");
        }
    }
}
