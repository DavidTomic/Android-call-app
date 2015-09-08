package test.myprojects.com.callproject.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.service.UpdateStatusIntentService;

/**
 * Created by davidtomic on 08/09/15.
 */
public class CallReceiver extends PhonecallReceiver {

    private static final String TAG = "CallReceiver";

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Log.i(TAG, "onIncomingCallStarted");
        Intent msgIntent = new Intent(ctx, UpdateStatusIntentService.class);
        msgIntent.putExtra("status", 3);
        ctx.startService(msgIntent);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.i(TAG, "onOutgoingCallStarted");
        Intent msgIntent = new Intent(ctx, UpdateStatusIntentService.class);
        msgIntent.putExtra("status", 3);
        ctx.startService(msgIntent);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.i(TAG, "onIncomingCallEnded");
        Intent msgIntent = new Intent(ctx, UpdateStatusIntentService.class);
        msgIntent.putExtra("status", User.getInstance(ctx).getStatus().getValue());
        ctx.startService(msgIntent);
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.i(TAG, "onOutgoingCallEnded");
        Intent msgIntent = new Intent(ctx, UpdateStatusIntentService.class);
        msgIntent.putExtra("status", User.getInstance(ctx).getStatus().getValue());
        ctx.startService(msgIntent);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.i(TAG, "onMissedCall");
        Intent msgIntent = new Intent(ctx, UpdateStatusIntentService.class);
        msgIntent.putExtra("status", User.getInstance(ctx).getStatus().getValue());
        ctx.startService(msgIntent);
    }




}
