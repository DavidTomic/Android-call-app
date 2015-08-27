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

import org.ksoap2.serialization.SoapObject;

import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.tabFragments.ContactsFragment;
import test.myprojects.com.callproject.tabFragments.FavoritFragment;
import test.myprojects.com.callproject.tabFragments.KeypadFragment;
import test.myprojects.com.callproject.tabFragments.RecentFragment;
import test.myprojects.com.callproject.tabFragments.SettingsFragment;

public class MainActivity extends FragmentActivity implements MessageInterface {

    private String TAG = "MainActivity";

    private FragmentTabHost mTabHost;
//    private final String NAMESPACE = "http://tempuri.org/";
//    private final String URL = "http://call.celox.dk/wsCall.asmx";
//    private final String SOAP_ACTION = "http://tempuri.org/HelloWorld";
//    private final String METHOD_NAME = "HelloWorld";

//    String method = "CreateAccount";
//
//    private final String NAMESPACE = "http://tempuri.org/";
//    private final String URL = "http://call.celox.dk/wsCall.asmx";
//    private final String SOAP_ACTION = "http://tempuri.org/"+method;
//    private final String METHOD_NAME = method;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        setTabs();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Log.i(TAG, "name " + name + "  number " + phoneNumber);
        }
        phones.close();


        String[] projection = { CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE };
        String where = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE;
        Cursor c = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,where, null, null);

        while (c.moveToNext())
        {
            String name=c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String phoneNumber = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
            Log.i(TAG, "Rname " + name + "  Rnumber " + phoneNumber);
        }
        c.close();

//        Button start = (Button) findViewById(R.id.start);
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AsyncCallTest task = new AsyncCallTest();
//                task.execute();
//            }
//        });
    }

    private void setTabs()
    {
        addTab(getString(R.string.favortes), R.drawable.tab_favorites, FavoritFragment.class);
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


//    private class AsyncCallTest extends AsyncTask<String, Void, String> {
//        private static final String TAG = "AsyncCallWS";
//
//        @Override
//        protected String doInBackground(String... params) {
//            Log.i(TAG, "doInBackground");
//            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
////            //Property which holds input parameters
////            PropertyInfo celsiusPI = new PropertyInfo();
////            //Set Name
////            celsiusPI.setName("Celsius");
////            //Set Value
////            celsiusPI.setValue(celsius);
////            //Set dataType
////            celsiusPI.setType(double.class);
////            //Add the property to request object
////            request.addProperty(celsiusPI);
//            //Create envelope
//            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
//                    SoapEnvelope.VER11);
//            envelope.dotNet = true;
//            //Set output SOAP object
//            envelope.setOutputSoapObject(request);
//            //Create HTTP call object
//            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
//
//            try {
//                //Invole web service
//                androidHttpTransport.call(SOAP_ACTION, envelope);
//                //Get the response
//                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//                //Assign it to fahren static variable
//                return response.toString();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            Log.i(TAG, "result " + result);
//        }
//
//    }



//
//    public void getFahrenheit(String celsius) {
//        //Create request
//        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
//        //Property which holds input parameters
//        PropertyInfo celsiusPI = new PropertyInfo();
//        //Set Name
//        celsiusPI.setName("Celsius");
//        //Set Value
//        celsiusPI.setValue(celsius);
//        //Set dataType
//        celsiusPI.setType(double.class);
//        //Add the property to request object
//        request.addProperty(celsiusPI);
//        //Create envelope
//        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
//                SoapEnvelope.VER11);
//        envelope.dotNet = true;
//        //Set output SOAP object
//        envelope.setOutputSoapObject(request);
//        //Create HTTP call object
//        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
//
//        try {
//            //Invole web service
//            androidHttpTransport.call(SOAP_ACTION, envelope);
//            //Get the response
//            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//            //Assign it to fahren static variable
//            fahren = response.toString();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
