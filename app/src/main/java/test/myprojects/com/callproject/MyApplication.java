package test.myprojects.com.callproject;

import android.app.Application;

import com.splunk.mint.Mint;

/**
 * Created by dtomic on 08/09/15.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Mint.enableDebug();
        Mint.initAndStartSession(this, "fb54e21b");
    }
}

