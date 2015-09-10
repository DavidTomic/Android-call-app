package test.myprojects.com.callproject.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.service.UpdateStatusIntentService;

/**
 * Created by developer dtomic on 09/09/15.
 */
public class CallStateReceiver extends BroadcastReceiver {

    private static final String TAG = "CallStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "onReceive");

        String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

        if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            Log.i(TAG, "EXTRA_STATE_IDLE");
            Intent msgIntent = new Intent(context, UpdateStatusIntentService.class);
            msgIntent.putExtra("status", User.getInstance(context).getStatus().getValue());
            context.startService(msgIntent);
        }
        else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            Log.i(TAG, "EXTRA_STATE_OFFHOOK");
            Intent msgIntent = new Intent(context, UpdateStatusIntentService.class);
            msgIntent.putExtra("status", Status.ON_PHONE.getValue());
            context.startService(msgIntent);
        }
        else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
            Log.i(TAG, "EXTRA_STATE_RINGING");
            Intent msgIntent = new Intent(context, UpdateStatusIntentService.class);
            msgIntent.putExtra("status", Status.ON_PHONE.getValue());
            context.startService(msgIntent);
        }

    }
}
