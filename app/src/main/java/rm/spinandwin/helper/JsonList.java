package rm.spinandwin.helper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class JsonList extends ArrayList<Json> {

    public JsonList() {
    }

    public JsonList(String array) {

        try {

            JSONArray arr = new JSONArray(array);

            for (int i = 0; i < arr.length(); i++) {
                try {
                    add(new Json(arr.getJSONObject(i).toString()));
                } catch (JSONException e) {
                    H.log("JsonList", e.toString());
                }
            }

        } catch (JSONException e) {
            H.log("JsonList", e.toString());
        }

    }

}
