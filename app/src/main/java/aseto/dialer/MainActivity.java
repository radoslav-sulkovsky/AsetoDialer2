package aseto.dialer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Context context;
    public static String androidId;

    boolean doubleBackToExitPressedOnce = false;

    private Handler handler = null;
    private static Runnable runnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        androidId = getAndroidId();

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
                        mainThread(strInterval);
                        showNotification();
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

        HttpAPI.execute("https://aseto.ecallcenter.pl/android/api.php", "{logme: '" + androidId + "'}");
    }

    @Override
    protected void onDestroy() {
        cancelNotification(context, 0);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Popup("Wciśnij ponownie aby wyjść z aplikacji");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void makeCall(final String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Popup("Brak uprawnień! Sprawdź uprawnienia aplikacji (CALL_PHONE)");
            return;
        }

        startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
    }

    private void Popup(String toastMessage) {
        Context context = getApplicationContext();
        CharSequence text = toastMessage;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private String getAndroidId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // notifications
    private void showNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "aseto001");

        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Aseto Dialer");
        mBuilder.setContentText("Aplikacja jest uruchomiona");
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.setOngoing(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("aseto001","Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notifyId);
    }

    protected void mainThread(final int interval) {
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if(!CallReceiver.outgoingCallInProcess) {
                    HttpAPI HttpAPI = new HttpAPI(new AsyncResponse() {

                        @Override
                        public void processFinish(String response) {
                            JSONObject jsonObject;

                            try {
                                jsonObject = new JSONObject(response);
                                String strAction = jsonObject.getString("action");

                                if (strAction.equals("makecall")) {
                                    String strMsisdn = jsonObject.getString("msisdn");
                                    Popup("Nowe zadanie! Wykonuję połączenie do " + strMsisdn);

                                    makeCall(strMsisdn);
                                } else {
                                    Popup("Brak nowych zadań");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    HttpAPI.execute("https://aseto.ecallcenter.pl/android/action.php", "{}");
                }

                handler.postDelayed(runnable, interval * 1000);
            }
        };

        handler.postDelayed(runnable, interval * 1000);
    }
}
