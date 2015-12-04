package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import test.myprojects.com.callproject.model.User;

public class StartActivity extends Activity {

    private static final String TAG = "StartActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


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
