package rm.spinandwin.helper;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;

public class Session {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public Session(Context context) {
        preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public Session(Context context, String text) {
        preferences = context.getSharedPreferences(text, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }


    public static Session newInstance(Context context) {
        return new Session(context);
    }

    public static Session newInstance(Context context, String text) {
        return new Session(context, text);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

    public void addInt(String name, int val) {
        editor.putInt(name, val);
        editor.commit();
    }

    public void addBool(String name, boolean val) {
        editor.putBoolean(name, val);
        editor.commit();
    }

    public void addString(String name, String val) {
        editor.putString(name, val);
        editor.commit();
    }

    public boolean getBool(String name) {
        return preferences.getBoolean(name, false);
    }

    public int getInt(String name) {
        return preferences.getInt(name, 0);
    }

    public String getString(String name) {
        return preferences.getString(name, "");
    }

    public void addLong(String name, long val) {
        editor.putLong(name, val);
        editor.commit();
    }

    public long getLong(String name) {
        return preferences.getLong(name, 0);
    }

    public Json getJson(String name) {

        try {
            return new Json(getString(name));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Json();
    }

    public void addJson(String name, Json json) {
        addString(name, json.toString());
    }

}
