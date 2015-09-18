package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import java.util.Locale;

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
    private Language language;

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
        isLogIn = !isLogIn;

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
                    .toString().length() > 3 && etName.getText().toString().length() > 1
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

        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();

        if (mPhoneNumber != null && mPhoneNumber.length()>3){
            etPhoneNumber.setText(mPhoneNumber);
        }
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

        String countryCode = Locale.getDefault().getCountry();

        if (countryCode.contains("en")){
            language = Language.ENGLISH;
        }else if (countryCode.contentEquals("da") || countryCode.contentEquals("en")){
            language = Language.DANISH;
        }else {
            language = Language.DEFAULT;
        }

        pi = new PropertyInfo();
        pi.setName("Language");
        pi.setValue(language.getValue());
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

    private SoapObject getAddMultiContactsParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.ADD_MULTI_CONTACT);

        PropertyInfo pi1 = new PropertyInfo();
        pi1.setName("Phonenumber");
        pi1.setValue(etPhoneNumber.getText().toString());
        pi1.setType(String.class);
        request.addProperty(pi1);

        pi1 = new PropertyInfo();
        pi1.setName("password");
        pi1.setValue(etPassword.getText().toString());
        pi1.setType(String.class);
        request.addProperty(pi1);

        SoapObject contactsSoapObject = new SoapObject(SendMessageTask.NAMESPACE, "Contacts");

        List<Contact> cList = User.getInstance(this).getContactList();


        for (Contact contact : cList) {

            SoapObject csContactsSoapObject = new SoapObject(SendMessageTask.NAMESPACE, "csContacts");

            PropertyInfo pi = new PropertyInfo();
            pi.setName("Phonenumber");
            pi.setValue(contact.getPhoneNumber());
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);


            pi = new PropertyInfo();
            pi.setName("Name");
            pi.setValue(contact.getName());
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);

            pi = new PropertyInfo();
            pi.setName("Noter");
            pi.setValue("");
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);

            pi = new PropertyInfo();
            pi.setName("Number");
            pi.setValue("");
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);

            pi = new PropertyInfo();
            pi.setName("URL");
            pi.setValue("");
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);

            pi = new PropertyInfo();
            pi.setName("Adress");
            pi.setValue("");
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);

            pi = new PropertyInfo();
            pi.setName("Birthsday");
            pi.setValue("2000-01-01T00:00:00");
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);

            pi = new PropertyInfo();
            pi.setName("pDate");
            pi.setValue("2000-01-01T00:00:00");
            pi.setType(String.class);
            csContactsSoapObject.addProperty(pi);

            pi = new PropertyInfo();
            pi.setName("Favorites");
            pi.setValue(contact.isFavorit());
            pi.setType(Boolean.class);
            csContactsSoapObject.addProperty(pi);

            contactsSoapObject.addProperty("csContacts", csContactsSoapObject);
        }

        request.addProperty("Contacts", contactsSoapObject);

        return request;

    }

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

                    List<String> list = new ArrayList<>();
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
                    user.setLanguage(Language.values()[Integer.valueOf(accountSetupSoapObject.getProperty("Language").toString())]);
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

                    new SendMessageTask(this, getAddMultiContactsParams()).execute();

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

        }else if (methodName.contentEquals(SendMessageTask.ADD_MULTI_CONTACT)) {
            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    User user = User.getInstance(this);
                    user.setPhoneNumber(etPhoneNumber.getText().toString());
                    user.setPassword(etPassword.getText().toString());
                    user.setName(etName.getText().toString());
                    user.setEmail(etEmail.getText().toString());
                    user.setLanguage(language);
                    user.setLogedIn(true);

                    Prefs.setUserData(this, User.getInstance(this));
                    Prefs.setLastContactCount(this, user.getContactList().size());

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    showErrorTryAgain();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorTryAgain();
            }
        }
    }
}
