package aseto.dialer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isNetworkAvailable()) {
            Popup("Brak połączenia z internetem, zamykam aplikację.");
            finish();
            return;
        }

        HttpAPI HttpAPI = new HttpAPI(new AsyncResponse() {
            @Override
            public void processFinish(String response) {
                JSONObject jsonObject;

                try {
                    jsonObject = new JSONObject(response);
                    String strStatus = jsonObject.getString("status");

                    if(strStatus.equals("OK")) {
                        int strInterval = jsonObject.getInt("interval");
                        Threads Threads = new Threads();
                        Threads.mainThread(strInterval);

                        Popup("Urządzenie zalogowane do systemu, uruchomino usługę i oczekuję na instrukcje!");
                    } else {
                        Popup("Wykryto nowe urządzenie!");
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        String androidId = getAndroidId();

        HttpAPI.execute("https://sulkowski.it/aseto/api.php", "{logme: '" + androidId + "'}");
    }

    private void makeCall(final String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Popup("Brak uprawnień! Sprawdź uprawnienia aplikacji (CALL_PHONE)");
            return;
        }

        startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
    }

    public void Popup(String toastMessage) {
        Context context = getApplicationContext();
        CharSequence text = toastMessage;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public String getAndroidId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
