package test.myprojects.com.callproject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.Hashtable;

import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.tabFragments.ContactsFragment;
import test.myprojects.com.callproject.tabFragments.FavoritFragment;
import test.myprojects.com.callproject.tabFragments.KeypadFragment;
import test.myprojects.com.callproject.tabFragments.RecentFragment;
import test.myprojects.com.callproject.tabFragments.SettingsFragment;
import test.myprojects.com.callproject.task.SendMessageTask;

public class MainActivity extends FragmentActivity implements MessageInterface {

    public static final String BROADCAST_STATUS_APDATE_ACTION = "status_update_action";
    private String TAG = "MainActivity";

    private FragmentTabHost mTabHost;
    private Handler mPollHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        setTabs();

        Prefs.setLastCallTime(this, 0);

//        String[] projection = { CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE };
//        String where = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE;
//        Cursor c = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,where, null, null);
//
//        while (c.moveToNext())
//        {
//            String name=c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));
//            String phoneNumber = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
//            Log.i(TAG, "Rname " + name + "  Rnumber " + phoneNumber);
//        }
//        c.close();

//        SendMessageTask mtask = new SendMessageTask(null, getCheckPhoneParams());
//        mtask.execute();

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mPollHandler.postDelayed(mPollRunnable, 500);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mPollHandler.removeCallbacks(mPollRunnable);
    }


    private void setTabs() {
        addTab(getString(R.string.favorites), R.drawable.tab_favorites, FavoritFragment.class);
        addTab(getString(R.string.recents), R.drawable.tab_recents, RecentFragment.class);

        addTab(getString(R.string.contacts), R.drawable.tab_contacts, ContactsFragment.class);
        addTab(getString(R.string.keypad), R.drawable.tab_keypad, KeypadFragment.class);

        addTab(getString(R.string.settings), R.drawable.tab_settings, SettingsFragment.class);


    }
    private void addTab(String labelId, int drawableId, Class<?> c) {

        TabHost.TabSpec spec = mTabHost.newTabSpec("tab" + labelId);

        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator,
                mTabHost.getTabWidget(), false);
        TextView title = (TextView) tabIndicator.findViewById(R.id.title);
        title.setText(labelId);
        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
        icon.setImageResource(drawableId);

        spec.setIndicator(tabIndicator);
        mTabHost.addTab(spec, c, null);
    }


    private Runnable mPollRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                Log.i(TAG, "fdf");
                SendMessageTask sTask = new SendMessageTask(MainActivity.this, getRequestInfoParams());
                sTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
                // TODO Auto-generated catch block
            }
            mPollHandler.postDelayed(mPollRunnable, 1000 * 60 * 4);
        }
    };

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        Log.i(TAG, "responseToSendMessage methodName " + methodName);

        if (result == null) {
            Toast.makeText(this, getString(R.string.status_update_error), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {

                SoapObject userStatusSoapObject = (SoapObject) result.getProperty("UserStatus");

                for (int i = 0; i < userStatusSoapObject.getPropertyCount(); i++) {
                    SoapObject csUserStatusSoapObject = (SoapObject) userStatusSoapObject.getProperty(i);
                    Log.i(TAG, "text " + csUserStatusSoapObject.getProperty(i));
                }


//                SoapObject textSoapObject = (SoapObject) userStatusSoapObject.getProperty("csUserStatus");
//
//                for (int i = 0; i < textSoapObject.getPropertyCount(); i++) {
//                    Log.i(TAG, "text " + textSoapObject.getProperty(i));
//                }
//
//                SoapObject accountSetupSoapObject = (SoapObject) result.getProperty("AccountSetup");
//
//                User user = User.getInstance(this);
//                user.setPhoneNumber(etPhoneNumber.getText().toString());
//                user.setPassword(etPassword.getText().toString());
//                user.setName(accountSetupSoapObject.getProperty("Name").toString());
//                user.setEmail(accountSetupSoapObject.getProperty("Email").toString());
//                user.setLanguage(accountSetupSoapObject.getProperty("Language").toString());
//                user.setLogedIn(true);

                Intent returnIntent = new Intent(BROADCAST_STATUS_APDATE_ACTION);
                sendBroadcast(returnIntent);

            }else {
                Toast.makeText(this, getString(R.string.status_update_error), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.status_update_error), Toast.LENGTH_SHORT).show();
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

        long time = Prefs.getLastCallTime(this);
        String endTime;
        if (time == 0) {
            endTime = "2000-01-01T00:00:00";
            Prefs.setLastCallTime(this, System.currentTimeMillis());
        } else {
            endTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format
                    (new java.util.Date(time));
            Prefs.setLastCallTime(this, time);
        }

        Log.i(TAG, "endTime " + endTime);


        pi = new PropertyInfo();
        pi.setName("LastCall");
        pi.setValue(endTime);
        pi.setType(String.class);
        request.addProperty(pi);


        return request;
    }


    public void refreshStatuses() {
        mPollHandler.removeCallbacks(mPollRunnable);
        mPollHandler.postDelayed(mPollRunnable, 50);
    }


    private SoapObject getCheckPhoneParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CHECK_PHONE_NUMBERS);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue("38594000333");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue("123456");
        pi.setType(String.class);
        request.addProperty(pi);

        SoapObject so = new SoapObject(SendMessageTask.NAMESPACE, "PhoneNumbers");
        PropertyInfo pi2 = new PropertyInfo();
        pi2.setName("string");
        pi2.setValue("38594000111");
        pi2.setType(String.class);
        so.addProperty(pi2);

        request.addProperty("PhoneNumbers", so);

        return request;
    }


}
