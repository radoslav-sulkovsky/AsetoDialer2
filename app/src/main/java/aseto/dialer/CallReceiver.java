package aseto.dialer;

import android.content.Context;
import android.widget.Toast;

import java.util.Date;

public class CallReceiver extends CallDetector {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        Toast.makeText(ctx, "Połączenie przychodzące z numeru " + number, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        //
    }

}