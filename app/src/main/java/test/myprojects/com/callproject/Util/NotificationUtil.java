package test.myprojects.com.callproject.Util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.List;

import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.StartActivity;
import test.myprojects.com.callproject.model.Notification;
import test.myprojects.com.callproject.model.Status;

/**
 * Created by developer dtomic on 17/11/15.
 */
public class NotificationUtil {

    public static void showNotification(Context context, List<Notification> notificationList) {

        String contentTitle = context.getString(R.string.user_changed_statuses) + " ";
        String contentText = "";

        if (notificationList.size() == 1) {
            contentTitle = notificationList.get(0).getName();
            contentText = context.getString(R.string.changed_status_to) + " " + getStatusText(context, notificationList.get(0).getStatus());
        } else {
            for (int i = 0; i < notificationList.size() && i < 3; i++) {
                contentText += notificationList.get(i).getName() +
                        context.getString(R.string.changed_status_to) +
                        getStatusText(context, notificationList.get(i).getStatus());
            }
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).
                setSmallIcon(R.mipmap.ic_launcher).
                setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));


        Intent myIntent = new Intent(context, StartActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.from(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(StartActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(myIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mBuilder.build());
    }

    private static String getStatusText(Context context, Status status) {

        String statusText = "";

        switch (status) {
            case RED_STATUS:
                statusText = context.getString(R.string.busy);
                break;
            case GREEN_STATUS:
                statusText = context.getString(R.string.online);
                break;
            case YELLOW_STATUS:
                statusText = context.getString(R.string.not_available);
                break;
            case ON_PHONE:
                statusText = context.getString(R.string.speaking);
                break;
        }

        return statusText;
    }
}
