package aseto.dialer;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class CallReceiver extends CallDetector {

    public static boolean outgoingCallInProcess, incomingCallInProcess;

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        incomingCallInProcess = true;
        Events.newCall("new", number, start, Calendar.getInstance().getTime());
        Toast.makeText(ctx, "Połączenie przychodzące z numeru " + number, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Events.newCall("answered", number, start, Calendar.getInstance().getTime());
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Events.newCall("incoming", number, start, end);
        incomingCallInProcess = false;
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        outgoingCallInProcess = true;
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Events.newCall("outgoing", number, start, end);
        outgoingCallInProcess = false;
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Events.newCall("missed", number, start, Calendar.getInstance().getTime());
        outgoingCallInProcess = false;
        incomingCallInProcess = false;
    }

}