package rm.spinandwin.helper;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class Api {

    private static final String TAG = "Api";
    private static final int SOCKET_TIMEOUT_MS = 20000;

    public static int GET = 0;
    public static int POST = 1;

    private int method = POST;

    private static int JSON_REQUEST = 1;
    private int requestType = 0;

    private OnResultListener onResultListener = null;
    private OnSuccessListener onSuccessListener = null;
    private OnErrorListener onErrorListener = null;
    private OnLoadingListener onLoadingListener = null;
    private OnHeaderRequestListener onHeaderRequestListener = null;

    private Context context;
    private String url;
    private JSONObject json;
    private JsonObjectRequest jsonObjectRequest;


    public Api(Context context, String url) {
        this.context = context;
        this.url = url;
        json = new JSONObject();
    }

    public static Api newApi(Context context, String url) {
        return new Api(context, url);
    }

    public Api setMethod(int method) {
        return this;
    }

    public Api addJson(JSONObject json) {
        this.json = json;
        this.requestType = JSON_REQUEST;
        return this;
    }

    public Api onResult(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
        return this;
    }

    public Api onSuccess(OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        return this;
    }

    public Api onHeaderRequest(OnHeaderRequestListener onHeaderRequestListener) {
        this.onHeaderRequestListener = onHeaderRequestListener;
        return this;
    }

    public Api onError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public Api onLoading(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
        return this;
    }

    public Api run(final String code) {

        H.log("Api Call", "______________________________________________");
        H.log("Api Url", code + ": " + url);
        H.log("Api Data", code + ": " + json.toString());

        if (onLoadingListener != null) onLoadingListener.onLoading(true);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(getMethod(), url, json, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                if (onLoadingListener != null) onLoadingListener.onLoading(false);
                H.log("Api Response", code + ": " + response.toString());

                if (onSuccessListener != null) {
                    try {
                        onSuccessListener.onSuccess(new Json(response.toString()));
                    } catch (JSONException e) {
                        H.log(TAG, e.toString());
                    }
                }

                if (onResultListener != null) {
                    try {
                        onResultListener.onResult(new Json(response.toString()));
                    } catch (JSONException e) {
                        H.log(TAG, e.toString());
                    }
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (onLoadingListener != null) onLoadingListener.onLoading(false);

                H.log("Api Error", code + ": " + error.toString());

                if (onErrorListener != null)
                    onErrorListener.onError();

                if (onResultListener != null)
                    onResultListener.onResult(null);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                if (onHeaderRequestListener != null) {
                    return onHeaderRequestListener.getHeaders();
                }//if

                return super.getHeaders();
            }//get headers
        };

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonRequest);

        this.jsonObjectRequest = jsonRequest;

        return this;
    }

    public void cancel() {
        if (jsonObjectRequest != null)
            jsonObjectRequest.cancel();
    }

    private int getMethod() {

        if (method == GET)
            return Request.Method.GET;

        return Request.Method.POST;
    }

    public static int getInt(JSONObject json, String param) {
        try {
            return json.getInt(param);
        } catch (JSONException e) {
            H.log(TAG, e.toString());
        }
        return 0;
    }

    public static boolean getBoolean(JSONObject json, String param) {
        try {
            return json.getBoolean(param);
        } catch (JSONException e) {
            H.log(TAG, e.toString());
        }
        return false;
    }

    public static String getString(JSONObject json, String param) {
        try {
            return json.getString(param);
        } catch (JSONException e) {
            H.log(TAG, e.toString());
        }
        return "";
    }

    public static JSONObject getJsonObject(JSONObject json, String param) {
        try {
            return json.getJSONObject(param);
        } catch (JSONException e) {
            H.log(TAG, e.toString());
        }
        return new JSONObject();
    }

    public static JSONArray getJsonArray(JSONObject json, String param) {
        try {
            return json.getJSONArray(param);
        } catch (JSONException e) {
            H.log(TAG, e.toString());
        }
        return new JSONArray();
    }

    public static JSONArray getJsonArray(String json) {
        JSONArray array;
        try {
            array = new JSONArray(json);
        } catch (JSONException e) {
            H.log(TAG, e.toString());
            array = new JSONArray();
        }
        return array;
    }

    public interface OnSuccessListener {
        void onSuccess(Json json);
    }

    public interface OnResultListener {
        void onResult(Json json);
    }

    public interface OnErrorListener {
        void onError();
    }

    public interface OnLoadingListener {
        void onLoading(boolean isLoading);
    }

    public interface OnHeaderRequestListener {
        Map<String, String> getHeaders();
    }

}//class