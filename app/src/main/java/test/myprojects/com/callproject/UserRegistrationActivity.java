package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;

public class UserRegistrationActivity extends Activity implements MessageInterface {

    private static final String TAG = "UserRegistration";

    @Bind(R.id.bSignLog)
    Button bTitle;
    @Bind(R.id.bConfirm)
    Button bConfirm;
    @Bind(R.id.etPhoneNumber)
    EditText etPhoneNumber;
    @Bind(R.id.etPassword)
    EditText etPassword;
    @Bind(R.id.etEmail)
    EditText etEmail;
    @Bind(R.id.etName)
    EditText etName;

    @OnClick(R.id.bSignLog)
    public void titleClicked() {
        if (isLogIn) {
            isLogIn = false;
        } else {
            isLogIn = true;
        }

        refreshUI();
    }

    @OnClick(R.id.bConfirm)
    public void confirmClicked() {

        if (isLogIn) {

            if (!(etPhoneNumber.getText().toString().length() > 5 && etPassword.getText()
                    .toString().length() > 3)) {
                showErrorMessage();
                return;
            }
            SendMessageTask task = new SendMessageTask(this, getLogInParams());
            task.execute();

        } else {

            if (!(etPhoneNumber.getText().toString().length() > 5 && etPassword.getText()
                    .toString().length() > 3 && etName.getText().toString().length() > 3
                    && etEmail.getText().toString().length() > 5)) {
                showErrorMessage();
                return;
            }

            SendMessageTask task = new SendMessageTask(this, getCreateAccountParams());
            task.execute();
        }


    }

    private boolean isLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        ButterKnife.bind(this);

        Bundle basket = getIntent().getExtras();

        if (basket != null && basket.getBoolean("isLogIn")) {
            isLogIn = true;
        }

        refreshUI();

    }

    private void refreshUI() {

        String text = bTitle.getText().toString();
        SpannableString spannablecontent = new SpannableString(text);

        if (isLogIn) {
            spannablecontent.setSpan(new ForegroundColorSpan(Color.GRAY), 0, text.indexOf("/"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannablecontent.setSpan(new ForegroundColorSpan(getResources().getColor
                            (R.color.blue_default)), text.indexOf("/"), text.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            etName.setVisibility(View.INVISIBLE);
            etEmail.setVisibility(View.INVISIBLE);
            bConfirm.setText(getString(R.string.log_in));
        } else {
            spannablecontent.setSpan(new ForegroundColorSpan(getResources().getColor
                            (R.color.blue_default)), 0, text.indexOf("/"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannablecontent.setSpan(new ForegroundColorSpan(Color.GRAY),
                    text.indexOf("/"), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            etName.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            bConfirm.setText(getString(R.string.sign_up));
        }

        bTitle.setText(spannablecontent);
    }

    private void showErrorMessage() {

        Toast.makeText(this, getString(R.string.fill_all_data), Toast.LENGTH_SHORT).show();
    }

    private SoapObject getCreateAccountParams() {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CREATE_ACCOUNT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(etPhoneNumber.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(etPassword.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Name");
        pi.setValue(etName.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Email");
        pi.setValue(etEmail.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Language");
        pi.setValue(1);
        pi.setType(Integer.class);
        request.addProperty(pi);

        return request;
    }

    private SoapObject getLogInParams() {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.LOGIN);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(etPhoneNumber.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Password");
        pi.setValue(etPassword.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

    private SoapObject getCheckPhoneParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CHECK_PHONE_NUMBERS);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(etPhoneNumber.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Password");
        pi.setValue(etPassword.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        SoapObject so = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CHECK_PHONE_NUMBERS);
        PropertyInfo pi2 = new PropertyInfo();
        pi2.setName("string");
        pi2.setValue("38593000222");
        pi2.setType(String.class);
        so.addProperty(pi2);

        request.addProperty("PhoneNumbers", so);

        return request;
    }

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        if (result == null)
            return;


        if (methodName == SendMessageTask.LOGIN) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    SoapObject defaultTextSoapObject = (SoapObject)result.getProperty("DefaultText");
                    SoapObject textSoapObject = (SoapObject)defaultTextSoapObject.getProperty("Text");

                    for (int i =0; i<textSoapObject.getPropertyCount(); i++){
                        Log.i(TAG, "text " + textSoapObject.getProperty(i));
                    }


                    SoapObject accountSetupSoapObject = (SoapObject)result.getProperty("AccountSetup");


                    User user = User.getInstance(this);
                    user.setPhoneNumber(etPhoneNumber.getText().toString());
                    user.setPassword(etPassword.getText().toString());
                    user.setName(accountSetupSoapObject.getProperty("Name").toString());
                    user.setEmail(accountSetupSoapObject.getProperty("Email").toString());
                    user.setLanguage(accountSetupSoapObject.getProperty("Language").toString());
                    user.setLogedIn(true);

                    Prefs.setUserData(this, user);

//                    SendMessageTask mtask = new SendMessageTask(null, getCheckPhoneParams());
//                    mtask.execute();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();

                }else {
                    showErrorMessage();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
            }


        } else {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    User user = User.getInstance(this);
                    user.setPhoneNumber(etPhoneNumber.getText().toString());
                    user.setPassword(etPassword.getText().toString());
                    user.setName(etName.getText().toString());
                    user.setEmail(etEmail.getText().toString());
                    user.setLanguage("1");
                    user.setLogedIn(true);

                    Prefs.setUserData(this, user);

                    startActivity(new Intent(this, MainActivity.class));
                    finish();

                } else if (resultStatus == 0) {
                    Toast.makeText(this, getString(R.string.user_already_exists),
                            Toast.LENGTH_SHORT).show();
                } else {
                    showErrorMessage();
                }


            }catch (NullPointerException ne) {
                ne.printStackTrace();
            }

        }

    }
}
