package test.myprojects.com.callproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.IntentCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import test.myprojects.com.callproject.Util.DataBase;
import test.myprojects.com.callproject.Util.InternetStatus;
import test.myprojects.com.callproject.Util.NotificationUtil;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Notification;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.service.ImALiveService;
import test.myprojects.com.callproject.service.NotificationService;
import test.myprojects.com.callproject.tabFragments.AnswerMachineFragment;
import test.myprojects.com.callproject.tabFragments.ContactsFragment;
import test.myprojects.com.callproject.tabFragments.FavoritFragment;
import test.myprojects.com.callproject.tabFragments.KeypadFragment;
import test.myprojects.com.callproject.tabFragments.RecentFragment;
import test.myprojects.com.callproject.task.CheckAndUpdateAllContactsTask;
import test.myprojects.com.callproject.task.SendMessageTask;
import test.myprojects.com.callproject.view.ReclickableTabHost;

public class MainActivity extends FragmentActivity implements MessageInterface {

    public static final String BROADCAST_STATUS_UPDATE_ACTION = "status_update_action";
    private String TAG = "MainActivity";

    private ReclickableTabHost mTabHost;
    private Handler mPollHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        mTabHost = (ReclickableTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        setTabs();

        mTabHost.setOnVoicemailClickListener(new ReclickableTabHost.VoicemailClickListener() {
            @Override
            public void onVoicemailClicked() {
             //   mTabHost.getTabWidget().getChildAt(4).setSelected(true);

                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String voiceMailNumber = Prefs.getVoiceMailNumber(MainActivity.this);

                if (!voiceMailNumber.contentEquals("")){
                        startActivity(new Intent(Intent.ACTION_CALL,
                                Uri.parse("tel:" + voiceMailNumber)));

                }else {
                    voiceMailNumber = tm.getVoiceMailNumber();

                    if (voiceMailNumber != null && voiceMailNumber.length()>0){
                        startActivity(new Intent(Intent.ACTION_CALL,
                                Uri.parse("tel:" + voiceMailNumber)));
                    }else {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.please_enter_your_voicemail), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Prefs.setLastCallTime(this, "2000-01-01T00:00:00");

        new SendMessageTask(this, getDefaultTextParams()).execute();

        Intent pushIntent = new Intent(this, ImALiveService.class);
        startService(pushIntent);

  //      new SendMessageTask(null, getGetContactParams()).execute();

    }

//        private SoapObject getGetContactParams() {
//
//        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.GET_CONTACT);
//
//        PropertyInfo pi = new PropertyInfo();
//        pi.setName("Phonenumber");
//        pi.setValue(User.getInstance(this).getPhoneNumber());
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        pi = new PropertyInfo();
//        pi.setName("password");
//        pi.setValue(User.getInstance(this).getPassword());
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        return request;
//    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");


        if (InternetStatus.isOnline(this)){
            new SendMessageTask(this, getLogInParams()).execute();
        }else {
            Toast.makeText(this, getString(R.string.please_enable_internet), Toast.LENGTH_LONG).show();
        }


        Intent pushIntent = new Intent(this, NotificationService.class);
        stopService(pushIntent);
        mPollHandler.postDelayed(mPollRunnable, 50);

        checkAndUpdateAllContact();

    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        Intent pushIntent = new Intent(this, NotificationService.class);
        startService(pushIntent);
        mPollHandler.removeCallbacks(mPollRunnable);


    }

    private void setTabs() {
        addTab(getString(R.string.favorites), R.drawable.tab_favorites, FavoritFragment.class);
        addTab(getString(R.string.recents), R.drawable.tab_recents, RecentFragment.class);

        addTab(getString(R.string.contacts), R.drawable.tab_contacts, ContactsFragment.class);
        addTab(getString(R.string.keypad), R.drawable.tab_keypad, KeypadFragment.class);

        addTab(getString(R.string.voice_mail), R.drawable.answer_machine_icon, AnswerMachineFragment.class);

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
                SendMessageTask sTask = new SendMessageTask(MainActivity.this, getRequestInfoParams());
                sTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
                // TODO Auto-generated catch block
            }
            mPollHandler.postDelayed(mPollRunnable, 1000 * User.getInstance(MainActivity.this).getRequestStatusInfoSeconds());
        }
    };



    public void refreshStatuses() {
        mPollHandler.removeCallbacks(mPollRunnable);
        mPollHandler.postDelayed(mPollRunnable, 50);
    }

    public void checkAndUpdateAllContact(){
        new CheckAndUpdateAllContactsTask(this).execute();
    }

    private SoapObject getDefaultTextParams() {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.GET_DEFAULT_TEXT);

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


        return request;
    }

    private SoapObject getLogInParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.LOG_IN);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(this).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Password");
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

        String lastCallTime = Prefs.getLastCallTime(this);
        if (lastCallTime.contentEquals("2000-01-01T00:00:00")) {

            List<Contact> contactList = User.getInstance(this).getContactList();

            for (Contact c : contactList){
                c.setStatus(null);
                c.setStatusText(null);
            }

        }


        pi = new PropertyInfo();
        pi.setName("LastCall");
        pi.setValue(lastCallTime);
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        //   Log.i(TAG, "responseToSendMessage methodName " + methodName);

        if (result == null) {
            return;
        }

        if (methodName.contentEquals(SendMessageTask.REQUEST_STATUS_INFO)) {
            try {
                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    String executionTime = result.getProperty("ExecutionTime").toString();
                    Prefs.setLastCallTime(this, executionTime);

                    List<Contact> contactList = User.getInstance(this).getContactList();

                    //   Log.i(TAG, "C LIST " + contactList.size());

                    SoapObject userStatusSoapObject = (SoapObject) result.getProperty("UserStatus");

                    for (int i = 0; i < userStatusSoapObject.getPropertyCount(); i++) {
                        SoapObject csUserStatusSoapObject = (SoapObject) userStatusSoapObject.getProperty(i);

                        String phoneNumber = ""+csUserStatusSoapObject.getProperty("PhoneNumber");
                        //      Log.i(TAG, "pohoneNumber " + pohoneNumber);


                        for (Contact c : contactList){
                            if (c.getPhoneNumber().contentEquals(phoneNumber)){

                                //      Log.i(TAG, "nasao " + c.getPhoneNumber());
                                int status = Integer.valueOf(csUserStatusSoapObject.getProperty("Status").toString());

                                c.setStatus(Status.values()[status]);

                                String statusText = "" + csUserStatusSoapObject.getProperty("StatusText");
                                if (statusText.contentEquals("anyType{}") || statusText.contentEquals("(null)")){
                                    statusText = "";
                                }
                                c.setStatusText(statusText);
                                //  c.setEndTime(""+csUserStatusSoapObject.getProperty("EndTimeStatus"));


                                List<test.myprojects.com.callproject.model.Notification> nList = DataBase.getNotificationListFromDb(DataBase.
                                        getInstance(MainActivity.this).getWritableDatabase());

                                if (nList.size() > 0){
                                    List<test.myprojects.com.callproject.model.Notification> matchList = new ArrayList<>();
                                    for (Notification notification : nList) {
                                        if (notification.getPhoneNumber().contentEquals(phoneNumber) && notification.getStatus().getValue() != status) {
                                            notification.setStatus(test.myprojects.com.callproject.model.Status.values()[status]);
                                            matchList.add(notification);
                                            DataBase.removeNotificationFromDb(DataBase.getInstance
                                                    (MainActivity.this).getWritableDatabase(), notification);
                                            break;
                                        }
                                    }

                                    if (matchList.size() > 0) {
                                        Log.i(TAG, "MAKE NOTIFICATION " + matchList.size());
                                        NotificationUtil.showNotification(MainActivity.this, matchList);
                                    }
                                }

                                break;
                            }
                        }

                    }

                    Intent returnIntent = new Intent(BROADCAST_STATUS_UPDATE_ACTION);
                    sendBroadcast(returnIntent);

                } else {
                    // Toast.makeText(this, getString(R.string.status_update_error), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                //  Toast.makeText(this, getString(R.string.status_update_error), Toast.LENGTH_SHORT).show();
            }
        } else if (methodName.contentEquals(SendMessageTask.GET_DEFAULT_TEXT)) {
            try {
                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {
                    SoapObject textSoapObject = (SoapObject) result.getProperty("DefaultText");

                    List<String> list = new ArrayList<String>();
                    for (int i = 0; i < textSoapObject.getPropertyCount(); i++) {
                        list.add("" + textSoapObject.getProperty(i));
                        //  Log.i(TAG, "text " + textSoapObject.getProperty(i));
                    }

                    Prefs.saveDefaultTexts(this, list);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if (methodName.contentEquals(SendMessageTask.LOG_IN)) {
            try {
                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 0 || resultStatus == 1) {

                    Intent pushIntent = new Intent(this, ImALiveService.class);
                    stopService(pushIntent);
                    User.empty();
                    Prefs.deleteUserSettings(this);
                    Intent i = new Intent(this, StartActivity.class);
                    i.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }else if (resultStatus == 2){

                    User user = User.getInstance(this);
                    user.setiAmLiveSeconds(Integer.valueOf(result.getProperty("ImALive").toString()));

                    int updateStatusOnList = Integer.valueOf(result.getProperty("UpdateStatusOnList").toString());

                    if (updateStatusOnList < 10)
                        updateStatusOnList = 10;

                    user.setRequestStatusInfoSeconds(updateStatusOnList);

                    String statusText = "" + result.getProperty("StatusText");
                    if (statusText.contentEquals("anyType{}")){
                        statusText = "";
                    }
                    user.setStatusText(statusText);

                    long currentMillies = System.currentTimeMillis();

                    Log.i(TAG, "currentMillies " + currentMillies);
                    Log.i(TAG, "getStatusStartTime " + User.getInstance(this).getStatusStartTime());
                    Log.i(TAG, "getStatusEndTime " + User.getInstance(this).getStatusEndTime());

                    if (currentMillies < User.getInstance(this).getStatusEndTime()){

                    }else{
                        Log.i(TAG, "HERE");
                        user.setStatus(Status.values()[Integer.valueOf(result.getProperty("Status").toString())]);
                    }


                    //    Log.i(TAG, "lang user " + user.getLanguage().getValue());

                    SoapObject inviteSMSSoapObject = (SoapObject) result.getProperty("InviteSMS");
                    for (int i = 0; i < inviteSMSSoapObject.getPropertyCount(); i++) {
                        SoapObject csInviteSMSSoapObject = (SoapObject) inviteSMSSoapObject.getProperty(i);

                        int lang = Integer.parseInt(csInviteSMSSoapObject.getProperty("Language").toString());

                        //  Log.i(TAG, "lang login " + lang);

                        if (lang == user.getLanguage().getValue()){
                            user.setSmsInviteText(csInviteSMSSoapObject.getProperty("SMSText").toString());
                            //        Log.i(TAG, "getSmsInviteText " + user.getSmsInviteText());
                            break;
                        }

                    }


                    Prefs.setUserData(this, user);

                }
            } catch (NullPointerException ne) {
                ne.printStackTrace();
            }
        }
    }

}
