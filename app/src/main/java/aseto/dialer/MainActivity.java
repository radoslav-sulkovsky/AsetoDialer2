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
import android.telephony.SmsManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Context context;
    public static String androidId;

    boolean doubleBackToExitPressedOnce = false;

    private Handler handler = null;
    private static Runnable runnable = null;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        androidId = getAndroidId();

        if(!isNetworkAvailable()) {
            Popup("Brak połączenia z Internetem, zamykam aplikację");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    finish();
                }

            }, 1000);
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.SEND_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }

            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.RECEIVE_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }

            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }
        }

        HttpAPI HttpAPI = new HttpAPI(new AsyncResponse() {

            @Override
            public void processFinish(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String strStatus = jsonObject.getString("status");

                    if(strStatus.equals("OK")) {
                        int strInterval = jsonObject.getInt("interval");
                        if(strInterval > 0) {
                            mainThread(strInterval);
                            showNotification();
                            Popup("Urządzenie zalogowane do systemu!");
                        } else {
                            Popup("Nieprawidłowy interwał, zamykam aplikację");
                            finish();
                            return;
                        }
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

        try {
            JSONObject JO = new JSONObject();
            JO.put("logme", androidId);

            String jsonString = JO.toString();

            HttpAPI.execute("https://aseto.ecallcenter.pl/android/api.php", jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(doubleBackToExitPressedOnce) {
            super.onBackPressed();
            cancelNotification(context, 0);
            System.exit(0);
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

    //region actions
    private void makeCall(final String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Popup("Brak uprawnień! Sprawdź uprawnienia aplikacji (CALL_PHONE)");
            return;
        }

        startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
    }

    private void sendSms(final String msisdn, final String message) {
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(msisdn, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //endregion

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

    //region notifications
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
    //endregion

    public boolean isJSON(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    protected void mainThread(final int interval) {
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if(!CallReceiver.outgoingCallInProcess && !CallReceiver.incomingCallInProcess) {
                    HttpAPI HttpAPI = new HttpAPI(new AsyncResponse() {

                        @Override
                        public void processFinish(String response) {
                            try {
                                JSONObject jsonObject;

                                if(!isJSON(response)) {
                                    return;
                                }

                                jsonObject = new JSONObject(response);

                                String strAction = jsonObject.getString("action");
                                String strMsisdn;

                                switch(strAction) {
                                    case "makecall":
                                        strMsisdn = jsonObject.getString("msisdn");
                                        Popup("Nowe zadanie! Wykonuję połączenie do " + strMsisdn);

                                        makeCall(strMsisdn);
                                        break;
                                    case "sendsms":
                                        strMsisdn = jsonObject.getString("msisdn");
                                        String strMessage = jsonObject.getString("message");
                                        Popup("Nowe zadanie! Wysłano SMS do " + strMsisdn);

                                        sendSms(strMsisdn, strMessage);
                                        break;
                                    default:
                                        Popup("Brak nowych zadań");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    try {
                        JSONObject JO = new JSONObject();
                        JO.put("deviceId", androidId);

                        String jsonString = JO.toString();

                        HttpAPI.execute("https://aseto.ecallcenter.pl/android/action.php", jsonString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                handler.postDelayed(runnable, interval * 1000);
            }
        };

        handler.postDelayed(runnable, interval * 1000);
    }
}
