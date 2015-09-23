package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.User;

public class StartActivity extends Activity {

    private static final String TAG = "StartActivity";


    //OLD CODE
//    @OnClick(R.id.bSignUp)
//    public void signClicked(){
//        Intent i = new Intent(this, UserRegistrationActivity.class);
//        i.putExtra("isLogIn", false);
//        startActivity(i);
//        finish();
//    }
//    @OnClick(R.id.bLogIn)
//    public void logClicked(){
//        Intent i = new Intent(this, UserRegistrationActivity.class);
//        i.putExtra("isLogIn", true);
//        startActivity(i);
//        finish();
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_start);
//        ButterKnife.bind(this);
//
//        if (User.getInstance(this).isLogedIn()){
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }
//
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);

        Log.i(TAG, "localTime " + sdf.format(new Date()));

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());

        Log.i(TAG, "utcTime " + utcTime);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (User.getInstance(StartActivity.this).isLogedIn()) {
                            startActivity(new Intent(StartActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Intent i = new Intent(StartActivity.this, UserRegistrationActivity.class);
                            i.putExtra("isLogIn", false);
                            startActivity(i);
                            finish();
                        }
                    }
                });

            }
        }, 1500);

    }
}
