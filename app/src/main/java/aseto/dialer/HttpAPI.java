package aseto.dialer;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by rados on 20.03.2018.
 */

public class HttpAPI extends AsyncTask<String, String, String> {

    public AsyncResponse delegate = null;

    public HttpAPI(AsyncResponse asyncResponse) {
        delegate = asyncResponse;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected String doInBackground(String... params) {
        HttpsURLConnection con = null;

        String url = params[0];
        String jsonString = params[1];

        String USER_AGENT = "Mozilla/5.0";

        try {
            URL obj = new URL(url);
            con = (HttpsURLConnection) obj.openConnection();

            con.setConnectTimeout(4000);

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Charset", "UTF-8");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes("json=" + jsonString);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "false";
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String aVoid) {
        super.onPostExecute(aVoid);
        delegate.processFinish(aVoid);
    }
}
