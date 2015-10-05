package test.myprojects.com.callproject.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import test.myprojects.com.callproject.MainActivity;
import test.myprojects.com.callproject.model.User;

/**
 * Created by davidtomic on 04/10/15.
 */
public class TimerBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "TimerBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");

        Intent returnIntent = new Intent(MainActivity.BROADCAST_STATUS_UPDATE_ACTION);
        context.sendBroadcast(returnIntent);

        if (intent.getLongExtra("time", 0) == User.getInstance(context).getStatusStartTime()){
            SetAlarm(context, User.getInstance(context).getStatusEndTime());
        }
    }

    public static void SetAlarm(Context context, long time)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TimerBroadcastReceiver.class);
        i.putExtra("time", time);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, time, pi);
    }

    public static void CancelAlarmIfNeed(Context context)
    {

        long currentMillies = System.currentTimeMillis();

        if (currentMillies > User.getInstance(context).getStatusStartTime()
                && currentMillies < User.getInstance(context).getStatusEndTime()){

            Intent intent = new Intent(context, TimerBroadcastReceiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }



    }
}
