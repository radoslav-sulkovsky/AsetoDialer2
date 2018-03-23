package aseto.dialer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by rados on 22.03.2018.
 */

public class Events {
    public static void newCall(String type, String state, String msisdn, Date dateStart, Date dateEnd) {
        try {
            int length = (int) (dateEnd.getTime()-dateStart.getTime())/1000;
            JSONObject JO = new JSONObject();
            JO.put("deviceId", MainActivity.androidId);
            JO.put("type", type);
            JO.put("state", state);
            JO.put("msisdn", msisdn);
            JO.put("length", length);
            JO.put("start", dateStart.getTime());
            JO.put("end", dateEnd.getTime());

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

    public static void newSms(String msisdn, String message, Date receivedDate) {
        try {
            JSONObject JO = new JSONObject();
            JO.put("deviceId", MainActivity.androidId);
            JO.put("type", "sms");
            JO.put("msisdn", msisdn);
            JO.put("message", message);
            JO.put("receivedDate", receivedDate.getTime());

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
