package test.myprojects.com.callproject;

import android.database.Cursor;
import android.os.Bundle;
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

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.Hashtable;

import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.tabFragments.ContactsFragment;
import test.myprojects.com.callproject.tabFragments.FavoritFragment;
import test.myprojects.com.callproject.tabFragments.KeypadFragment;
import test.myprojects.com.callproject.tabFragments.RecentFragment;
import test.myprojects.com.callproject.tabFragments.SettingsFragment;
import test.myprojects.com.callproject.task.SendMessageTask;

public class MainActivity extends FragmentActivity implements MessageInterface {

    private String TAG = "MainActivity";

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        setTabs();



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

        SendMessageTask mtask = new SendMessageTask(null, getCheckPhoneParams());
        mtask.execute();

    }

    private void setTabs()
    {
        addTab(getString(R.string.favorites), R.drawable.tab_favorites, FavoritFragment.class);
        addTab(getString(R.string.recents), R.drawable.tab_recents, RecentFragment.class);

        addTab(getString(R.string.contacts), R.drawable.tab_contacts, ContactsFragment.class);
        addTab(getString(R.string.keypad), R.drawable.tab_keypad, KeypadFragment.class);

        addTab(getString(R.string.settings), R.drawable.tab_settings, SettingsFragment.class);
    }

    private void addTab(String labelId, int drawableId, Class<?> c)
    {

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

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

    }

    private SoapObject getCheckPhoneParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CHECK_PHONE_NUMBERS);

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

        SoapObject so = new SoapObject(SendMessageTask.NAMESPACE, "PhoneNumbers");
        PropertyInfo pi2 = new PropertyInfo();
        pi2.setName("string");
        pi2.setValue("38593000222");
        pi2.setType(String.class);
        so.addProperty(pi2);


        request.addProperty("PhoneNumbers", so);

        return request;
    }

    class PhoneNumbers implements KvmSerializable{

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        private String string;

        @Override
        public Object getProperty(int i) {
            return string;
        }

        @Override
        public int getPropertyCount() {
            return 1;
        }

        @Override
        public void setProperty(int i, Object o) {

        }

        @Override
        public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
            propertyInfo.type = PropertyInfo.STRING_CLASS;
            propertyInfo.name = "string";
        }
    }


}
