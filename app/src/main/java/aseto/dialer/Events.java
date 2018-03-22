package aseto.dialer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by rados on 22.03.2018.
 */

public class Events {
    protected static void newCall(String type, String phoneNumber, Date dateStart, Date dateEnd) {
        try {
            int length = (int) (dateEnd.getTime()-dateStart.getTime())/1000;
            JSONObject JO = new JSONObject();
            JO.put("deviceId", MainActivity.androidId);
            JO.put("type", type);
            JO.put("phoneNumber", phoneNumber);
            JO.put("length", length);

            String jsonString = JO.toString();

            HttpAPI HttpAPI = new HttpAPI(new AsyncResponse() {
                @Override
                public void processFinish(String response) {
                    // nothing to do
                }
            });

            HttpAPI.execute("https://aseto.ecallcenter.pl/android/status.php", jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
