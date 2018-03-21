package aseto.dialer;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by rados on 20.03.2018.
 */

public class Threads {

    private Handler handler = null;
    private static Runnable runnable = null;

    protected void mainThread(final int interval) {
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, interval * 1000);
            }
        };

        handler.postDelayed(runnable, interval * 1000);
    }
}
