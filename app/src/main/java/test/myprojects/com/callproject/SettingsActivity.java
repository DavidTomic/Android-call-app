package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.model.User;

public class SettingsActivity extends Activity {


    @OnClick(R.id.bBack)
    public void backClicked(){
        finish();
    }

    @Bind(R.id.tvLanguage)
    TextView tvLanguage;
    @Bind(R.id.tvPhone)
    TextView tvPhone;
    @Bind(R.id.tvName)
    TextView tvName;
    @Bind(R.id.tvEmail)
    TextView tvEmail;
    @Bind(R.id.tvDefaultText)
    TextView tvDefaultText;


    @OnClick(R.id.rlLanguage)
    public void languageClicked() {
        Intent intent = new Intent(this, SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_LANGUAGE);
        startActivity(intent);

    }

    @OnClick(R.id.rlPhone)
    public void phoneClicked() {
        Intent intent = new Intent(this, SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_PHONE);
        startActivity(intent);

    }

    @OnClick(R.id.rlPassword)
    public void passwordClicked() {
        Intent intent = new Intent(this, SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_PASSWORD);
        startActivity(intent);
    }

    @OnClick(R.id.rlName)
    public void nameClicked() {
        Intent intent = new Intent(this, SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_NAME);
        startActivity(intent);
    }

    @OnClick(R.id.rlEmail)
    public void emailClicked() {
        Intent intent = new Intent(this, SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_EMAIL);
        startActivity(intent);
    }

    @OnClick(R.id.rlDefaultText)
    public void defaultTextClicked() {
        startActivity(new Intent(this, DefaultTextActivity.class));
    }

    @OnClick(R.id.rlSetStatus)
    public void setStatusClicked() {
        startActivity(new Intent(this, SetStatusActivity.class));
    }

    @OnClick(R.id.rlManageContacts)
    public void manageContactClicked() {
        startActivity(new Intent(this, AddContactsActivity.class));
    }

    @OnClick(R.id.rlNotification)
    public void notificationClicked(){
        startActivity(new Intent(this, EditNotificationsActivity.class));
    }


    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        User user = User.getInstance(this);

        switch (user.getLanguage()) {
            case ENGLISH:
                tvLanguage.setText(getString(R.string.english));
                break;
            case DANISH:
                tvLanguage.setText(getString(R.string.danish));
                break;
            default:
                tvLanguage.setText("");
                break;
        }

        tvPhone.setText(user.getPhoneNumber());
        tvName.setText(user.getName());
        tvEmail.setText(user.getEmail());
        tvDefaultText.setText(user.getStatusText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }

}
