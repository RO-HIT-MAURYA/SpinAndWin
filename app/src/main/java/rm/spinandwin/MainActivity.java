package rm.spinandwin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private long time;
    private String betCoin = "1";
    private int betAmount = 1;
    private String betNumber;
    private RelativeLayout relativeLayout;
    private TextView textView;
    private MediaPlayer mediaPlayer;
    private String color = "";
    private int totalWinAmt;//used to count win amt from all 6 probability,
    private boolean isInFront = true;
    private String wheelId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new LoadingDialog(MainActivity.this);

        mediaPlayer = MediaPlayer.create(this, R.raw.sound);

        adjustSeconds();

        String data = new Session(this).getString(Static.data);
        try {
            Json json = new Json(data);
            String string = json.getString(Static.TotalCoins);
            totalCoins = Integer.parseInt(string);
            ((TextView) findViewById(R.id.coinCount)).setText(string);
            userId = json.getString(Static.UserId);
            assignedUserId = json.getString(Static.AssignedUserId);
            wheelId = new Session(this).getString("wheelId");

        } catch (Exception e) {
            e.printStackTrace();
        }
        //setUpGrid(2);

        checkForPendingTask();
        getRecentNumbers();
    }

    private void checkForPendingTask() {
        Session session = new Session(this);
        if (session.getBool("minimizedB4WinNum")) {
            hitForWinningNumber();
            hitTotalCoinsById();
        } else if (session.getBool("minimizedB4WinAmt")) {
            totalWinAmt = extractInt(session.getString("winAmt"));
            hitTotalCoinsById();
        }

    }


    Calendar calendar;
    Date date;

    private void adjustSeconds() {
        //min = calendar.get(Calendar.MINUTE);
        //final int m = min;

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
            }
        }, 60000 - ((sec * 1000) + ms));

    }

    int i;

    private void showTimerOnScreen() {
        loadingDialog = new LoadingDialog(this, false);
        loadingDialog.findViewById(R.id.loadingDialog).setVisibility(View.INVISIBLE);
        loadingDialog.findViewById(R.id.msg).setVisibility(View.INVISIBLE);
        final TextView textView = findViewById(R.id.spineTime);
        CountDownTimer countDownTimer = new CountDownTimer(59000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                i = (int) (millisUntilFinished / 1000) - 10;

                if (i == 11)
                    loadingDialog.show();
                if (i == 0) {
                    loadingDialog.hide();
                    disableBetAmtRow();
                    hitForWinningNumber();
                }
                if (i <= 10)
                    textView.setTextColor(getResources().getColor(R.color.red));
                if (i >= 0)
                    textView.setText(i + "");
            }

            @Override
            public void onFinish() {
                finish();
                startActivity(getIntent());
               /* if (isInFront) {
                    finish();
                    startActivity(getIntent());
                }*/
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
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            childLayout = (LinearLayout) linearLayout.getChildAt(i);
            for (int j = 0; j < childLayout.getChildCount(); j++) {
                relativeLayout = (RelativeLayout) childLayout.getChildAt(j);
                relativeLayout.getChildAt(1).setEnabled(false);
            }
        }
    }

    private void hitForWinningNumber() {
        Json json = new Json();
        json.addString(Static.user_id, userId);
        json.addString(Static.spin_id, wheelId);

        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }

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
                            new Session(MainActivity.this).addBool("minimizedB4WinNum", false);
                            new Session(MainActivity.this).addBool("minimizedB4WinAmt", true);

                            json = json.getJson(Static.data);
                            String winNumber = json.getInt(Static.WinNumber) + "";
                            //String winAmount = json.getInt(Static.WinAmount) + "";
                            color = json.getString(Static.WinNumberColour);
                            totalCoins = json.getInt(Static.TotalCoins);
                            new ApiTask(winNumber);
                            new Session(MainActivity.this).addString("winAmt", totalWinAmt + "");
                            spinCircle(winNumber, color, totalCoins);

                        } /*else
                            H.showMessage(MainActivity.this, json.getString(Static.message));*/
                    }
                })
                .run("winningNumberApi");
    }

    final float factor = 4.72f;// (360/38)/2;

    private void spinCircle(final String winNumber, final String color, final int totalCoins) {
        ImageView imageView = findViewById(R.id.circle);
        //Random random = new Random();
        int degree = 0, oldDegree;
        final TextView textView = findViewById(R.id.winNum);

        oldDegree = degree * 360;
        /*degree = random.nextInt(3600);

        Log.e("degreeIs", degree + "");*/
        //degree = degree + 720;
        degree = getDegree(winNumber) + 720;
        Log.e("degreesIs", degree + "");
        RotateAnimation rotateAnimation = new RotateAnimation(oldDegree, degree, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        final int finalDegree = degree;
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mediaPlayer.start();
                textView.setText("");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText(currentNumber(360 - (finalDegree % 360)));
                if (color.equalsIgnoreCase("b"))
                    textView.setBackgroundColor(getResources().getColor(R.color.black));
                else if (color.equalsIgnoreCase("r"))
                    textView.setBackgroundColor(getResources().getColor(R.color.red));
                else
                    textView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                //((TextView) findViewById(R.id.winningAmt)).setText(winAmt);todo win num from api
                ((TextView) findViewById(R.id.winningAmt)).setText(totalWinAmt + "");//from all calculation
                hitTotalCoinsById();
                findViewById(R.id.winNumLayout).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.coinCount)).setText(totalCoins + "");
                updateSessionCoins(totalCoins + "");
                getRecentNumbers();
                //if (color.equalsIgnoreCase());
                Log.e("selectedIs", currentNumber(360 - (finalDegree % 360)));
                //H.log("totalWinAmtIs",totalWinAmt+"");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(rotateAnimation);
    }

    private void hitTotalCoinsById() {
        Json json = new Json();
        json.addString(Static.user_id, userId);
        json.addString(Static.spin_id, wheelId);
        json.addString(Static.winning_amount, totalWinAmt + "");

        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }

        Api.newApi(this, Static.baseUrl + "GetTotalCoinsByID").addJson(json)
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
                            json = json.getJson(Static.data);
                            int l = json.getInt(Static.TotalCoins);
                            updateSessionCoins(l + "");
                            totalCoins = l;
                            ((TextView) findViewById(R.id.coinCount)).setText("" + totalCoins);
                            new Session(MainActivity.this).addBool("minimizedB4WinAmt", false);

                        } /*else
                            H.showMessage(MainActivity.this, json.getString(Static.message));*/

                    }
                })
                .run("getTotalCoinsByID");
    }

    private int getDegree(String winNumber) {
        int i = 0;

        if (winNumber.equalsIgnoreCase("1"))
            i = 2290;
        else if (winNumber.equalsIgnoreCase("2"))
            i = 1382;
        else if (winNumber.equalsIgnoreCase("3"))
            i = 2536;
        else if (winNumber.equalsIgnoreCase("4"))
            i = 2483;
        else if (winNumber.equalsIgnoreCase("5"))
            i = 169;
        else if (winNumber.equalsIgnoreCase("6"))
            i = 3134;
        else if (winNumber.equalsIgnoreCase("7"))
            i = 2215;
        else if (winNumber.equalsIgnoreCase("8"))
            i = 3435;
        else if (winNumber.equalsIgnoreCase("9"))
            i = 2978;
        else if (winNumber.equalsIgnoreCase("10"))
            i = 3062;
        else if (winNumber.equalsIgnoreCase("11"))
            i = 220;
        else if (winNumber.equalsIgnoreCase("12"))
            i = 1482;
        else if (winNumber.equalsIgnoreCase("13"))
            i = 3115;
        else if (winNumber.equalsIgnoreCase("14"))
            i = 1914;
        else if (winNumber.equalsIgnoreCase("15"))
            i = 3221;
        else if (winNumber.equalsIgnoreCase("16"))
            i = 150;
        else if (winNumber.equalsIgnoreCase("17"))
            i = 2800;
        else if (winNumber.equalsIgnoreCase("18"))
            i = 1154;
        else if (winNumber.equalsIgnoreCase("19"))
            i = 690;
        else if (winNumber.equalsIgnoreCase("20"))
            i = 1204;
        else if (winNumber.equalsIgnoreCase("21"))
            i = 2473;
        else if (winNumber.equalsIgnoreCase("22"))
            i = 2964;
        else if (winNumber.equalsIgnoreCase("23"))
            i = 189;
        else if (winNumber.equalsIgnoreCase("24"))
            i = 2678;
        else if (winNumber.equalsIgnoreCase("25"))
            i = 293;
        else if (winNumber.equalsIgnoreCase("26"))
            i = 2169;
        else if (winNumber.equalsIgnoreCase("27"))
            i = 1687;
        else if (winNumber.equalsIgnoreCase("28"))
            i = 765;
        else if (winNumber.equalsIgnoreCase("29"))
            i = 67;
        else if (winNumber.equalsIgnoreCase("30"))
            i = 3452;
        else if (winNumber.equalsIgnoreCase("31"))
            i = 3344;
        else if (winNumber.equalsIgnoreCase("32"))
            i = 2151;
        else if (winNumber.equalsIgnoreCase("33"))
            i = 1581;
        else if (winNumber.equalsIgnoreCase("34"))
            i = 1715;
        else if (winNumber.equalsIgnoreCase("35"))
            i = 1827;
        else if (winNumber.equalsIgnoreCase("36"))
            i = 3465;
        else if (winNumber.equalsIgnoreCase("0"))
            i = 2156;
        else
            i = 2423;

        return i;
    }

    private String currentNumber(int degrees) {
        String text = "";
        if (degrees >= (factor * 1) && degrees < (factor * 3)) {
            text = "32";
        }
        if (degrees >= (factor * 3) && degrees < (factor * 5)) {
            text = "15";

        }
        if (degrees >= (factor * 5) && degrees < (factor * 7)) {
            text = "19";

        }
        if (degrees >= (factor * 7) && degrees < (factor * 9)) {
            text = "4";

        }
        if (degrees >= (factor * 9) && degrees < (factor * 11)) {
            text = "21";
        }
        if (degrees >= (factor * 11) && degrees < (factor * 13)) {
            text = "2";
        }
        if (degrees >= (factor * 13) && degrees < (factor * 15)) {
            text = "25";
        }
        if (degrees >= (factor * 15) && degrees < (factor * 17)) {
            text = "17";
        }
        if (degrees >= (factor * 17) && degrees < (factor * 19)) {
            text = "34";
        }
        if (degrees >= (factor * 19) && degrees < (factor * 21)) {
            text = "00";
        }
        if (degrees >= (factor * 21) && degrees < (factor * 23)) {
            text = "6";
        }
        if (degrees >= (factor * 23) && degrees < (factor * 25)) {
            text = "27";
        }
        if (degrees >= (factor * 25) && degrees < (factor * 27)) {
            text = "13";
        }
        if (degrees >= (factor * 27) && degrees < (factor * 29)) {
            text = "36";
        }
        if (degrees >= (factor * 29) && degrees < (factor * 31)) {
            text = "11";
        }
        if (degrees >= (factor * 31) && degrees < (factor * 33)) {
            text = "30";
        }
        if (degrees >= (factor * 33) && degrees < (factor * 35)) {
            text = "8";
        }
        if (degrees >= (factor * 35) && degrees < (factor * 37)) {
            text = "23";
        }
        if (degrees >= (factor * 37) && degrees < (factor * 39)) {
            text = "10";
        }
        if (degrees >= (factor * 39) && degrees < (factor * 41)) {
            text = "5";
        }
        if (degrees >= (factor * 41) && degrees < (factor * 43)) {
            text = "24";
        }
        if (degrees >= (factor * 43) && degrees < (factor * 45)) {
            text = "16";
        }
        if (degrees >= (factor * 45) && degrees < (factor * 47)) {
            text = "33";
        }
        if (degrees >= (factor * 47) && degrees < (factor * 49)) {
            text = "1";
        }
        if (degrees >= (factor * 49) && degrees < (factor * 51)) {
            text = "20";
        }
        if (degrees >= (factor * 51) && degrees < (factor * 53)) {
            text = "14";
        }
        if (degrees >= (factor * 53) && degrees < (factor * 55)) {
            text = "31";
        }
        if (degrees >= (factor * 55) && degrees < (factor * 57)) {
            text = "9";
        }
        if (degrees >= (factor * 57) && degrees < (factor * 59)) {
            text = "22";
        }
        if (degrees >= (factor * 59) && degrees < (factor * 61)) {
            text = "18";
        }
        if (degrees >= (factor * 61) && degrees < (factor * 63)) {
            text = "29";
        }
        if (degrees >= (factor * 63) && degrees < (factor * 65)) {
            text = "7";
        }
        if (degrees >= (factor * 65) && degrees < (factor * 67)) {
            text = "28";
        }
        if (degrees >= (factor * 67) && degrees < (factor * 69)) {
            text = "12";
        }
        if (degrees >= (factor * 69) && degrees < (factor * 71)) {
            text = "35";
        }
        if (degrees >= (factor * 71) && degrees < (factor * 73)) {
            text = "3";
        }
        if (degrees >= (factor * 73) && degrees < (factor * 75)) {
            text = "26";
        }
        if ((degrees >= (factor * 75) && degrees < 360) || (degrees > 0 && degrees < (factor * 1))) {
            text = "0";
        }
        /*if ((degrees >= (factor *77) && degrees < 360)|| (degrees>0 && degrees<(factor*1)))
        {
            text = "0 green";
        }*/

        return text;
    }

    private void getRecentNumbers() {

        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Static.baseUrl + "GetRecentNumbers", new JSONObject(), new Response.Listener<JSONObject>() {
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
        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }

        try {
            JSONArray jsonArray = jsonObject.getJSONArray(Static.data);
            LinearLayout linearLayout = findViewById(R.id.blueLayout);
            for (int i = 0; i < 5; i++) {
                jsonObject = jsonArray.getJSONObject(i + 1);
                String string = jsonObject.getString(Static.number);
                if (string != null)
                    ((TextView) linearLayout.getChildAt(i)).setText(string);
            }
            jsonObject = jsonArray.getJSONObject(0);
            wheelId = jsonObject.getString(Static.number);
            new Session(MainActivity.this).addString("wheelId", wheelId);
            H.log("wheelIdIs", wheelId + "");

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
            adb.setMessage("Do you really want to logout?");
            adb.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Session(MainActivity.this).clear();
                    Intent intent = new Intent(MainActivity.this, LogInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            adb.setNegativeButton("NO", null);
            adb.show();
        } else {
            //MediaPlayer.create(this, R.raw.coin).start();
            //H.showMessage(this, ((TextView) view).getText().toString());
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
        // MediaPlayer.create(this, R.raw.keyclick).start();
        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }
        H.log("clickIs", ((TextView) view).getText().toString());
        //H.showMessage(this, ((TextView) view).getText().toString());

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

    public void onBorderClick(View view) {
        // MediaPlayer.create(this, R.raw.keyclick).start();
        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }
        H.log("borderClickIs", view.getTag().toString());
        //H.showMessage(this, view.getTag().toString());

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
        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }
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

        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }

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

        if (!H.isInternetAvailable(this)) {
            H.showMessage(this, "Network not available");
            return;
        }

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
                            new Session(MainActivity.this).addBool("minimizedB4WinNum", true);
                        } /*else
                            H.showMessage(MainActivity.this, json.getString(Static.message));*/

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
    public void onClick(View v) {
        if (v.getId() == R.id.close)
            dialog.hide();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time < 800) {
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            time = System.currentTimeMillis();
            H.showMessage(this, "Press again to exit.");
        }
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        Session session = new Session(this);
        session.addString("winAmt", totalWinAmt + "");
        session.addBool("sessionNeeded", true);
    }*/

    /*@Override
    protected void onStart() {
        super.onStart();

        if (!isInFront)
            startActivity(getIntent());
        isInFront = true;
        H.log("cycleIsStart",isInFront+"");
    }*/

    public class ApiTask {
        int winNum;

        ApiTask(String winNumber) {
            try {
                if (winNumber.equals("00"))
                    winNum = 37;
                else
                    winNum = Integer.parseInt(winNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }

            LinearLayout linearLayout = findViewById(R.id.superLayout);
            int k = findId(winNum);
            if (k != -1) {
                TextView textView = linearLayout.findViewById(k);
                if (textView.getVisibility() == View.VISIBLE) {
                    int l = extractInt(textView.getText().toString());
                    H.log("I am", "Executed");
                    totalWinAmt = l * 36;
                    H.log("apiWinIsNumberSpecific", totalWinAmt + "");
                }
                calculateByColor();
                calculateByEvenOdd();
                calculateBy0And00();
                calculateBy1To2();
                calculateByTopRow();
                calculateByBottomRow();
            }
        }

        private void calculateByBottomRow() {
            if (winNum > 0 && winNum < 13 && findViewById(R.id._oneTo12).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._oneTo12)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 3);
                H.log("apiWinIs1-12Amt", totalWinAmt + "");
            } else if (winNum > 12 && winNum < 25 && findViewById(R.id._thirteenTo24).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._thirteenTo24)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 3);
                H.log("apiWinIs13-24Amt", totalWinAmt + "");
            } else if (winNum > 24 && winNum < 37 && findViewById(R.id._twentyFiveTo36).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._twentyFiveTo36)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 3);
                H.log("apiWinIs25-36Amt", totalWinAmt + "");
            }
        }

        private void calculateByTopRow() {
            if (winNum > 0 && winNum < 19 && findViewById(R.id._oneTo18).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._oneTo18)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 2);
                H.log("apiWinIs1-18Amt", totalWinAmt + "");
            } else if (winNum > 18 && winNum < 37 && findViewById(R.id._nineteenTo36).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._nineteenTo36)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 2);
                H.log("apiWinIs19-36Amt", totalWinAmt + "");
            }
        }

        private void calculateBy1To2() {
            if (winNum % 3 == 0 && findViewById(R.id._thirtyNine).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._thirtyNine)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 3);
                H.log("apiWinIs1-2Amt", totalWinAmt + "");
                return;
            }
            List<String> list = Arrays.asList(getResources().getStringArray(R.array.row_two));
            if (list.contains(winNum + "") && findViewById(R.id._thirtyEight).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._thirtyEight)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 3);
                H.log("apiWinIs2-3Amt", totalWinAmt + "");
                return;
            }
            list = Arrays.asList(getResources().getStringArray(R.array.row_three));
            if (list.contains(winNum + "") && findViewById(R.id._thirtySeven).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._thirtySeven)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 3);
                H.log("apiWinIs3-4Amt", totalWinAmt + "");
            }
        }

        private void calculateBy0And00() {
            if (winNum == 0 && findViewById(R.id._zero).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._zero)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 36);
                H.log("apiWinIs0Amt", totalWinAmt + "");
                return;
            } else if (winNum == 37 && findViewById(R.id._doubleZero).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._doubleZero)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 36);
                H.log("apiWinIs00Amt", totalWinAmt + "");
            }
        }

        private void calculateByEvenOdd() {
            if (winNum % 2 == 0 && winNum != 0 && findViewById(R.id._even).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._even)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 2);
                H.log("apiWinIsEvenAmt", totalWinAmt + "");
            } else if (winNum % 2 != 0 && winNum != 37 && findViewById(R.id._odd).getVisibility() == View.VISIBLE)//37 means 00 so it will be considered as odd
            {
                int l = extractInt(((TextView) findViewById(R.id._odd)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 2);
                H.log("apiWinIsOddAmt", totalWinAmt + "");
            }

        }

        private void calculateByColor() {
            if (color.equals("R") && findViewById(R.id._red).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._red)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 2);
                H.log("apiWinIsRedAmt", totalWinAmt + "");
            } else if (color.equals("B") && findViewById(R.id._black).getVisibility() == View.VISIBLE) {
                int l = extractInt(((TextView) findViewById(R.id._black)).getText().toString());
                totalWinAmt = totalWinAmt + (l * 2);
                H.log("apiWinIsBlackAmt", totalWinAmt + "");
            }

        }

        private int extractInt(String numString) {
            int i = 0;
            try {
                i = Integer.parseInt(numString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return i;
        }

        private int findId(int i) {
            switch (i) {
                case 1:
                    return R.id._1;

                case 2:
                    return R.id._2;

                case 3:
                    return R.id._3;

                case 4:
                    return R.id._4;

                case 5:
                    return R.id._5;

                case 6:
                    return R.id._6;

                case 7:
                    return R.id._7;

                case 8:
                    return R.id._8;

                case 9:
                    return R.id._9;

                case 10:
                    return R.id._10;

                case 11:
                    return R.id._11;

                case 12:
                    return R.id._12;

                case 13:
                    return R.id._13;

                case 14:
                    return R.id._14;

                case 15:
                    return R.id._15;

                case 16:
                    return R.id._16;

                case 17:
                    return R.id._17;

                case 18:
                    return R.id._18;

                case 19:
                    return R.id._19;

                case 20:
                    return R.id._20;

                case 21:
                    return R.id._21;

                case 22:
                    return R.id._22;

                case 23:
                    return R.id._23;

                case 24:
                    return R.id._24;

                case 25:
                    return R.id._25;

                case 26:
                    return R.id._26;

                case 27:
                    return R.id._27;

                case 28:
                    return R.id._28;

                case 29:
                    return R.id._29;

                case 30:
                    return R.id._30;

                case 31:
                    return R.id._31;

                case 32:
                    return R.id._32;

                case 33:
                    return R.id._33;

                case 34:
                    return R.id._34;

                case 35:
                    return R.id._35;

                case 36:
                    return R.id._36;

                case 0:
                    return R.id._zero;

                case 37:
                    return R.id._doubleZero;

                default:
                    return -1;

            }
        }
    }
}
