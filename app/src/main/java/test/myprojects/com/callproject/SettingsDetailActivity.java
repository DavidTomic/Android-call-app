package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.Language;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;

public class SettingsDetailActivity extends Activity implements MessageInterface {

    private static final String TAG = "SettingsDetailActivity";

    public static final int EDIT_PHONE = 2;
    public static final int EDIT_PASSWORD = 3;
    public static final int EDIT_NAME = 4;
    public static final int EDIT_EMAIL = 5;
    public static final int EDIT_VOICEMAIL = 6;

    private int CURRENT_EDITING = 0;

    private String newPhoneNumber = User.getInstance(this).getPhoneNumber();
    private String newPassword = User.getInstance(this).getPassword();
    private String newName = User.getInstance(this).getName();
    private String newEmail = User.getInstance(this).getEmail();

    @Bind(R.id.tvLabel)
    TextView tvLabel;
    @Bind(R.id.etValue)
    EditText etValue;

    @Bind(R.id.bSave)
    Button bSave;

    @Bind(R.id.rlProgress)
    RelativeLayout rlProgress;

    @OnClick(R.id.bSave)
    public void save() {

        if (etValue.getText().toString().length() < 4) {
            Toast.makeText(this, getString(R.string.enter_correct_data), Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etValue.getWindowToken(), 0);

        rlProgress.setVisibility(View.VISIBLE);

        switch (CURRENT_EDITING) {
            case EDIT_PHONE:
                newPhoneNumber = etValue.getText().toString();
                SendMessageTask task1 = new SendMessageTask(this, getUpdateAccountParams());
                task1.execute();

                break;
            case EDIT_PASSWORD:
                newPassword = etValue.getText().toString();
                SendMessageTask task2 = new SendMessageTask(this, getUpdateAccountParams());
                task2.execute();

                break;
            case EDIT_NAME:
                newName = etValue.getText().toString();
                SendMessageTask task3 = new SendMessageTask(this, getUpdateAccountParams());
                task3.execute();

                break;
            case EDIT_EMAIL:
                newEmail = etValue.getText().toString();
                SendMessageTask task4 = new SendMessageTask(this, getUpdateAccountParams());
                task4.execute();

                break;
            case EDIT_VOICEMAIL:
                String number = etValue.getText().toString();

                if (number.length()>0){
                    Prefs.setVoiceMailNumber(this, number);
                }

                finish();
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_detail);
        ButterKnife.bind(this);

        Bundle extra = getIntent().getExtras();

        if (extra != null && extra.getInt("key", 0) != 0) {

            CURRENT_EDITING = extra.getInt("key");
            Log.i(TAG, "CURRENT_EDITING " + CURRENT_EDITING);

            switch (CURRENT_EDITING) {
                case EDIT_PHONE:
                    tvLabel.setText(getString(R.string.phone_number));

                    break;
                case EDIT_PASSWORD:
                    tvLabel.setText(getString(R.string.password));

                    break;
                case EDIT_NAME:
                    tvLabel.setText(getString(R.string.name));

                    break;
                case EDIT_EMAIL:
                    tvLabel.setText(getString(R.string.email));

                    break;
                case EDIT_VOICEMAIL:
                    tvLabel.setText(getString(R.string.voice_mail));

                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String voiceMailNumber = Prefs.getVoiceMailNumber(this);

                    if (!voiceMailNumber.contentEquals("")){
                        etValue.setText(voiceMailNumber);

                    }else {
                        voiceMailNumber = tm.getVoiceMailNumber();

                        if (voiceMailNumber != null && voiceMailNumber.length()>0){
                            etValue.setText(voiceMailNumber);
                        }
                    }

                    break;
            }
        }
    }



    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {
        if (result == null) {
            showErrorTryAgain();
            return;
        }

        try {

            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {

                if (CURRENT_EDITING == EDIT_PHONE)
                    User.getInstance(this).setPhoneNumber(newPhoneNumber);
                else if (CURRENT_EDITING == EDIT_PASSWORD)
                    User.getInstance(this).setPassword(newPassword);
                else if (CURRENT_EDITING == EDIT_NAME)
                    User.getInstance(this).setName(newName);
                else if (CURRENT_EDITING == EDIT_EMAIL)
                    User.getInstance(this).setEmail(newEmail);

                rlProgress.setVisibility(View.GONE);
                Prefs.setUserData(this, User.getInstance(this));

                finish();

            } else {
                showErrorTryAgain();
            }

        } catch (NullPointerException ne) {
            ne.printStackTrace();
            showErrorTryAgain();
        }
    }

    private SoapObject getUpdateAccountParams() {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.UPDATE_ACCOUNT);

        User user = User.getInstance(this);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("OldPhonenumber");
        pi.setValue(user.getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Oldpassword");
        pi.setValue(user.getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("PhoneNumber");
        pi.setValue(newPhoneNumber);
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(newPassword);
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Name");
        pi.setValue(newName);
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Email");
        pi.setValue(newEmail);
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Language");
        pi.setValue(user.getLanguage().getValue());
        pi.setType(Integer.class);
        request.addProperty(pi);

        return request;
    }

    private void showErrorTryAgain() {

        Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
        rlProgress.setVisibility(View.GONE);
    }
}
