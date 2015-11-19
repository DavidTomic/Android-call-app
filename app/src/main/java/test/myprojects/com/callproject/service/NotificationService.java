package test.myprojects.com.callproject.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.StartActivity;
import test.myprojects.com.callproject.Util.DataBase;
import test.myprojects.com.callproject.Util.NotificationUtil;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Notification;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.task.SendMessageTask;

/**
 * Created by dtomic on 09/09/15.
 */
public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private String lastCallTime;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate ");
        // TODO Auto-generated method stub
        super.onCreate();

        lastCallTime = Prefs.getLastCallTime(this);
        startNotificationService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Log.i(TAG, "onStartCommand ");

        new getRequestInfo().execute();
        return Service.START_NOT_STICKY;
    }

    private class getRequestInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {


            List<test.myprojects.com.callproject.model.Notification> nList = DataBase.getNotificationListFromDb(DataBase.
                    getInstance(NotificationService.this).getWritableDatabase());

            if (nList.size() == 0) {
                stopNotificationService();
                Log.i(TAG, "stopping nList.size");
                return null;
            }


            SoapObject request = getRequestInfoParams();

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapEnvelope.dotNet = true;
            soapEnvelope.setOutputSoapObject(request);


            HttpTransportSE aht = new HttpTransportSE(SendMessageTask.URL);
            aht.debug = true;

            try {
                aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                aht.call(SendMessageTask.NAMESPACE + request.getName(), soapEnvelope);

                SoapObject result = (SoapObject) soapEnvelope.getResponse();
                Log.i(TAG, "result " + result);

                try {
                    int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                    if (resultStatus == 2) {
                        lastCallTime = result.getProperty("ExecutionTime").toString();

                        SoapObject userStatusSoapObject = (SoapObject) result.getProperty("UserStatus");

                        if (userStatusSoapObject.getPropertyCount() == 0)
                            return null;


                        List<test.myprojects.com.callproject.model.Notification> matchList = new ArrayList<>();

                        for (int i = 0; i < userStatusSoapObject.getPropertyCount(); i++) {
                            SoapObject csUserStatusSoapObject = (SoapObject) userStatusSoapObject.getProperty(i);

                            String phoneNumber = "" + csUserStatusSoapObject.getProperty("PhoneNumber");
                            int status = Integer.valueOf(csUserStatusSoapObject.getProperty("Status").toString());
                            Log.i(TAG, "phoneNumber " + phoneNumber);
                            Log.i(TAG, "status " + status);


                            for (Notification notification : nList) {
                                if (notification.getPhoneNumber().contentEquals(phoneNumber) && notification.getStatus().getValue() != status) {
                                    notification.setStatus(test.myprojects.com.callproject.model.Status.values()[status]);
                                    matchList.add(notification);
                                    DataBase.removeNotificationFromDb(DataBase.getInstance
                                            (NotificationService.this).getWritableDatabase(), notification);
                                    break;
                                }
                            }
                        }


                        if (matchList.size() > 0) {
                            Log.i(TAG, "MAKE NOTIFICATION " + matchList.size());
                           // showNotification(matchList);
                            NotificationUtil.showNotification(NotificationService.this, matchList);
                        }

                        if (nList.size() == matchList.size()) {
                            Log.i(TAG, "stopping");
                            stopNotificationService();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }
    private SoapObject getRequestInfoParams() {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.REQUEST_STATUS_INFO);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(this).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(this).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("LastCall");
        pi.setValue(lastCallTime);
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }


//    private void showNotification(List<Notification> notificationList) {
//
////        Intent intent = new Intent(this, StartActivity.class);
////        // use System.currentTimeMillis() to have a unique ID for the pending intent
////        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
//
//        String contentTitle = getString(R.string.user_changed_statuses) + " ";
//        String contentText = "";
//
//        if (notificationList.size() == 1) {
//            contentTitle = notificationList.get(0).getName();
//            contentText = getString(R.string.changed_status_to) + " " + getStatusText(notificationList.get(0).getStatus());
//        } else {
//            for (int i = 0; i < notificationList.size() && i < 3; i++) {
//                contentText += notificationList.get(i).getName() +
//                        getString(R.string.changed_status_to) +
//                        getStatusText(notificationList.get(i).getStatus());
//            }
//        }
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                this).
//                setSmallIcon(R.mipmap.ic_launcher).
//                setAutoCancel(true)
//                .setContentTitle(contentTitle)
//                .setContentText(contentText)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//
//
//        Intent myIntent = new Intent(this, StartActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.from(this);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(StartActivity.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(myIntent);
//
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
//                PendingIntent.FLAG_CANCEL_CURRENT);
//        mBuilder.setContentIntent(resultPendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0, mBuilder.build());
//    }
//    private String getStatusText(Status status) {
//
//        String statusText = "";
//
//        switch (status) {
//            case RED_STATUS:
//                statusText = getString(R.string.busy);
//                break;
//            case GREEN_STATUS:
//                statusText = getString(R.string.online);
//                break;
//            case YELLOW_STATUS:
//                statusText = getString(R.string.not_available);
//                break;
//            case ON_PHONE:
//                statusText = getString(R.string.speaking);
//                break;
//        }
//
//        return statusText;
//    }


    private void stopNotificationService() {
        Log.i(TAG, "stopNotificationService");
        this.stopSelf();

    }
    private void startNotificationService() {
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        int interval = 20;
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                interval * 1000, pintent);

        Log.i(TAG, "interval " + interval);
    }
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pintent);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
