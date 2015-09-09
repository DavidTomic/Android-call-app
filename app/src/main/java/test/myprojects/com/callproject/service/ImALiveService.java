package test.myprojects.com.callproject.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Calendar;

import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.task.SendMessageTask;

/**
 * Created by dtomic on 09/09/15.
 */
public class ImALiveService extends Service {
    private static final String TAG = "ImALiveService";
    Calendar cur_cal = Calendar.getInstance();
    private int Iamliveseconds = 240;

    @Override
    public void onCreate() {
        Log.i(TAG, "onStartCommand ");
        // TODO Auto-generated method stub
        super.onCreate();

        setAlarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Log.i(TAG, "onStartCommand ");

        User user = User.getInstance(this);
        if (user != null && user.getiAmLiveSeconds() > 29 && user.getiAmLiveSeconds() != Iamliveseconds){
            Iamliveseconds = user.getiAmLiveSeconds();
            setAlarm();
        }


        new IamLiveTask().execute();


        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class IamLiveTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            SoapObject request = getIamLivearams();

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


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    private SoapObject getIamLivearams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.I_AM_LIVE);


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

        String versionCode = "";
        try {
            versionCode = ""+(getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        pi = new PropertyInfo();
        pi.setName("VersionNumber");
        pi.setValue(versionCode);
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("AppType");
        pi.setValue(2);
        pi.setType(Integer.class);
        request.addProperty(pi);


        return request;
    }

    private void setAlarm(){
        Intent intent = new Intent(this, ImALiveService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        cur_cal.setTimeInMillis(System.currentTimeMillis());

        User user = User.getInstance(this);
        if (user != null && user.getiAmLiveSeconds() > 29 && user.getiAmLiveSeconds() != Iamliveseconds){
            Iamliveseconds = user.getiAmLiveSeconds();
        }

        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                Iamliveseconds * 1000, pintent);

        Log.i(TAG, "seconds " + Iamliveseconds);
    }
}
