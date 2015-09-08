package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.model.User;

public class StartActivity extends Activity {

    private static final String TAG = "StartActivity";

    @OnClick(R.id.bSignUp)
    public void signClicked(){
        Intent i = new Intent(this, UserRegistrationActivity.class);
        i.putExtra("isLogIn", false);
        startActivity(i);
        finish();
    }
    @OnClick(R.id.bLogIn)
    public void logClicked(){
        Intent i = new Intent(this, UserRegistrationActivity.class);
        i.putExtra("isLogIn", true);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        if (User.getInstance(this).isLogedIn()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

}
