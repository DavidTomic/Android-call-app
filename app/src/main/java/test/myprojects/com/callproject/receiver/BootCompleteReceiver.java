package test.myprojects.com.callproject.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import test.myprojects.com.callproject.service.ImALiveService;

/**
 * Created by dtomic on 09/09/15.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, ImALiveService.class);
            context.startService(pushIntent);
        }
    }
}