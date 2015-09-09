package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.InternetStatus;
import test.myprojects.com.callproject.Util.Language;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Contact;
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
    @Bind(R.id.rlProgress)
    RelativeLayout rlProgress;

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
                showErrorCheckData();
                return;
            }

            rlProgress.setVisibility(View.VISIBLE);

            SendMessageTask task = new SendMessageTask(this, getLogInParams());
            task.execute();

        } else {

            if (!(etPhoneNumber.getText().toString().length() > 5 && etPassword.getText()
                    .toString().length() > 3 && etName.getText().toString().length() > 3
                    && etEmail.getText().toString().length() > 5)) {
                showErrorCheckData();
                return;
            }

            rlProgress.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (!InternetStatus.isOnline(this)) {
            Toast.makeText(this, getString(R.string.please_enable_internet), Toast.LENGTH_LONG).show();
        }
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

    private void showErrorCheckData() {

        Toast.makeText(this, getString(R.string.fill_all_data), Toast.LENGTH_SHORT).show();
        rlProgress.setVisibility(View.GONE);
    }

    private void showErrorTryAgain() {

        Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
        rlProgress.setVisibility(View.GONE);
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
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.GET_ACCOUNT_SETUP);

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

//    private SoapObject getCheckPhoneParams() {
//
//        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CHECK_PHONE_NUMBERS);
//
//        PropertyInfo pi = new PropertyInfo();
//        pi.setName("Phonenumber");
//        pi.setValue(User.getInstance(this).getPhoneNumber());
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        pi = new PropertyInfo();
//        pi.setName("password");
//        pi.setValue(User.getInstance(this).getPassword());
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        SoapObject phoneNumbersSoapObject = new SoapObject(SendMessageTask.NAMESPACE, "PhoneNumbers");
//
//        List<Contact> cList = User.getInstance(this).getContactList();
//
//        for (Contact contact : cList) {
//            PropertyInfo piPhoneNumber = new PropertyInfo();
//            piPhoneNumber.setName("string");
//            piPhoneNumber.setValue(contact.getPhoneNumber());
//            piPhoneNumber.setType(String.class);
//            phoneNumbersSoapObject.addProperty(piPhoneNumber);
//        }
//
//        request.addProperty("PhoneNumbers", phoneNumbersSoapObject);
//
//        return request;
//    }

//    private SoapObject getUpdateStatusParams() {
//
//        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.UPDATE_STATUS);
//
//
//        PropertyInfo pi = new PropertyInfo();
//        pi.setName("Phonenumber");
//        pi.setValue(User.getInstance(this).getPhoneNumber());
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        pi = new PropertyInfo();
//        pi.setName("password");
//        pi.setValue(User.getInstance(this).getPassword());
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        pi = new PropertyInfo();
//        pi.setName("Status");
//        pi.setValue(User.getInstance(this).getStatus().getValue());
//        pi.setType(Integer.class);
//        request.addProperty(pi);
//
//        pi = new PropertyInfo();
//        pi.setName("EndTime");
//        pi.setValue("2000-01-01T00:00:00");
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        pi = new PropertyInfo();
//        pi.setName("Text");
//        pi.setValue("");
//        pi.setType(String.class);
//        request.addProperty(pi);
//
//        return request;
//    }

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        if (result == null) {
            User.empty();
            showErrorTryAgain();
            return;
        }


        if (methodName.contentEquals(SendMessageTask.GET_ACCOUNT_SETUP)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    SoapObject defaultTextSoapObject = (SoapObject) result.getProperty("DefaultText");
                    SoapObject textSoapObject = (SoapObject) defaultTextSoapObject.getProperty("Text");

                    List<String> list = new ArrayList<String>();
                    for (int i = 0; i < textSoapObject.getPropertyCount(); i++) {
                        list.add("" + textSoapObject.getProperty(i));
                        Log.i(TAG, "text " + textSoapObject.getProperty(i));
                    }

                    Prefs.saveDefaultTexts(this, list);

                    SoapObject accountSetupSoapObject = (SoapObject) result.getProperty("AccountSetup");

                    User user = User.getInstance(this);
                    user.setPhoneNumber(etPhoneNumber.getText().toString());
                    user.setPassword(etPassword.getText().toString());
                    user.setName(accountSetupSoapObject.getProperty("Name").toString());
                    user.setEmail(accountSetupSoapObject.getProperty("Email").toString());
                    user.setLanguage(Language.ENGLISH);
                    user.setLogedIn(true);

                    Prefs.setUserData(this, User.getInstance(this));
                    startActivity(new Intent(this, MainActivity.class));
                    finish();


                } else {
                    showErrorCheckData();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorTryAgain();
            }


        } else if (methodName.contentEquals(SendMessageTask.CREATE_ACCOUNT)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    User user = User.getInstance(this);
                    user.setPhoneNumber(etPhoneNumber.getText().toString());
                    user.setPassword(etPassword.getText().toString());
                    user.setName(etName.getText().toString());
                    user.setEmail(etEmail.getText().toString());
                    user.setLanguage(Language.ENGLISH);
                    user.setLogedIn(true);

                    Prefs.setUserData(this, User.getInstance(this));

                    Intent intent = new Intent(this, AddContactsActivity.class);
                    intent.putExtra("cameFromCreateAccount", true);
                    startActivity(intent);
                    finish();

                } else if (resultStatus == 0) {
                    Toast.makeText(this, getString(R.string.user_already_exists),
                            Toast.LENGTH_SHORT).show();
                    rlProgress.setVisibility(View.GONE);
                } else {
                    showErrorCheckData();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorTryAgain();
            }

        }

//        else if (methodName.contentEquals(SendMessageTask.UPDATE_STATUS)) {
//            try {
//
//                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());
//
//                if (resultStatus == 2) {
//
//                    Prefs.setUserData(this, User.getInstance(this));
//
//
//                    if (isLogIn){
//                        startActivity(new Intent(this, MainActivity.class));
//                        finish();
//                    }else {
//                        startActivity(new Intent(this, AddContactsActivity.class));
//                        finish();
//                    }
//
//                } else {
//                    User.empty();
//                    showErrorCheckData();
//                }
//
//
//            } catch (NullPointerException ne) {
//                ne.printStackTrace();
//                User.empty();
//                showErrorTryAgain();
//            }
//        }
    }
}
