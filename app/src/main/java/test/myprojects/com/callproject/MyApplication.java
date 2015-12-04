package test.myprojects.com.callproject;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.splunk.mint.Mint;

import java.util.Locale;

import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.Util.WindowSize;

/**
 * Created by dtomic on 08/09/15.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        //Mint.enableDebug();
        Mint.initAndStartSession(this, "fb54e21b");

        WindowSize.initialize(this);


    //    Log.i(TAG, "locale " + Locale.getDefault().getCountry());

        String prefscountryCode = Prefs.getLanguageCountryCode(this);

     //   Log.i(TAG, "prefscountryCode " + prefscountryCode);

        if (!prefscountryCode.contentEquals("")){

            Locale myLocale = new Locale(prefscountryCode);
            Locale.setDefault(myLocale);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
        }

    }
}

