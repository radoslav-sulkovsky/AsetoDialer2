package aseto.dialer;

import android.os.Handler;

/**
 * Created by rados on 20.03.2018.
 */

public class Threads {
    private Handler handler = null;
    private static Runnable runnable = null;

    private void mainThread() {
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, 5000);
            }
        };

        handler.postDelayed(runnable, 5000);
    }
}
