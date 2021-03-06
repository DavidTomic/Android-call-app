package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.DataBase;
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
    @Bind(R.id.pbProgress)
    ProgressBar pbProgress;

    @OnClick(R.id.bSignUp)
    public void bSignUpClicked() {
        isLogIn = false;

        refreshUI();
    }

    @OnClick(R.id.bLogIn)
    public void bLogInClicked() {
        isLogIn = true;

        refreshUI();
    }

    @OnClick(R.id.bConfirm)
    public void confirmClicked() {

        if (isLogIn) {

            if (etPhoneNumber.getText().toString().length() < 5) {
                showErrorToast(getString(R.string.enter_phone_number));
                return;
            }else if (etPassword.getText()
                    .toString().length() < 3){
                showErrorToast(getString(R.string.enter_password));
                return;
            }

            bConfirm.setVisibility(View.INVISIBLE);
            pbProgress.setVisibility(View.VISIBLE);

            SendMessageTask task = new SendMessageTask(this, getLogInParams());
            task.execute();

        } else {

            if (etPhoneNumber.getText().toString().length() < 5) {
                showErrorToast(getString(R.string.enter_phone_number));
                return;
            }else if (etPassword.getText()
                    .toString().length() < 3){
                showErrorToast(getString(R.string.enter_password));
                return;
            }else if (etName.getText()
                    .toString().length() < 2){
                showErrorToast(getString(R.string.enter_name));
                return;
            }else if (etEmail.getText()
                    .toString().length() < 5){
                showErrorToast(getString(R.string.enter_email));
                return;
            }

            bConfirm.setVisibility(View.INVISIBLE);
            pbProgress.setVisibility(View.VISIBLE);
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


        if (isLogIn) {

            etName.setVisibility(View.INVISIBLE);
            etEmail.setVisibility(View.INVISIBLE);
            bConfirm.setText(getString(R.string.log_in));
        } else {

            etName.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            bConfirm.setText(getString(R.string.sign_up));
        }


        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();

        if (mPhoneNumber != null && mPhoneNumber.length()>3){
            etPhoneNumber.setText(mPhoneNumber);
        }
    }

    private void showErrorToast(String message) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        pbProgress.setVisibility(View.GONE);
        bConfirm.setVisibility(View.VISIBLE);
    }


    private SoapObject getCreateAccountParams() {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CREATE_ACCOUNT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        String phoneNumberOnlyDigit = etPhoneNumber.getText().toString();
        String firstSign = phoneNumberOnlyDigit.substring(0, 1);
        phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
        if (firstSign.contentEquals("+")){
            phoneNumberOnlyDigit = firstSign+phoneNumberOnlyDigit;
        }
        pi.setValue(phoneNumberOnlyDigit);
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

        String countryCode = Locale.getDefault().getCountry().toLowerCase();

        if (countryCode.contentEquals("da") || countryCode.contentEquals("dk")){
            language = Language.DANISH;
        }else {
            language = Language.ENGLISH;
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
        String phoneNumberOnlyDigit = etPhoneNumber.getText().toString();
        String firstSign = phoneNumberOnlyDigit.substring(0, 1);
        phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
        if (firstSign.contentEquals("+")){
            phoneNumberOnlyDigit = firstSign+phoneNumberOnlyDigit;
        }
        pi.setValue(phoneNumberOnlyDigit);
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
        String phoneNumberOnlyDigit = etPhoneNumber.getText().toString();
        String firstSign = phoneNumberOnlyDigit.substring(0, 1);
        phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
        if (firstSign.contentEquals("+")){
            phoneNumberOnlyDigit = firstSign+phoneNumberOnlyDigit;
        }
        pi1.setValue(phoneNumberOnlyDigit);
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
            showErrorToast(getString(R.string.please_try_again));
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

                    String phoneNumberOnlyDigit = etPhoneNumber.getText().toString();
                    String firstSign = phoneNumberOnlyDigit.substring(0, 1);
                    phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
                    if (firstSign.contentEquals("+")){
                        phoneNumberOnlyDigit = firstSign+phoneNumberOnlyDigit;
                    }
                    user.setPhoneNumber(phoneNumberOnlyDigit);
                    user.setPassword(etPassword.getText().toString());
                    user.setName(accountSetupSoapObject.getProperty("Name").toString());
                    user.setEmail(accountSetupSoapObject.getProperty("Email").toString());
                    user.setLogedIn(true);


                    String countryCode = Locale.getDefault().getCountry().toLowerCase();

                    Log.i(TAG, "CCC + " + countryCode);

                    if (countryCode.contentEquals("da") || countryCode.contentEquals("dk")){
                        user.setLanguage(Language.DANISH);
                        Prefs.setLanguageCountryCode(this, "da");
                    }else {
                        user.setLanguage(Language.ENGLISH);
                        Prefs.setLanguageCountryCode(this, "en");
                    }

                    Intent intent = new Intent(this, SelectLanguageActivity.class);
                    intent.putExtra("startedFromRegistration", true);
                    startActivity(intent);
                    finish();


                } else {
                    showErrorToast(getString(R.string.please_try_again));
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorToast(getString(R.string.please_try_again));
            }


        } else if (methodName.contentEquals(SendMessageTask.CREATE_ACCOUNT)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    new SendMessageTask(this, getAddMultiContactsParams()).execute();

                } else if (resultStatus == 0) {
                    showErrorToast(getString(R.string.user_already_exists));
                } else {
                    showErrorToast(getString(R.string.please_try_again));
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorToast(getString(R.string.please_try_again));
            }

        }else if (methodName.contentEquals(SendMessageTask.ADD_MULTI_CONTACT)) {
            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    User user = User.getInstance(this);
                    String phoneNumberOnlyDigit = etPhoneNumber.getText().toString();
                    String firstSign = phoneNumberOnlyDigit.substring(0, 1);
                    phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
                    if (firstSign.contentEquals("+")){
                        phoneNumberOnlyDigit = firstSign+phoneNumberOnlyDigit;
                    }

                    user.setPhoneNumber(phoneNumberOnlyDigit);
                    user.setPassword(etPassword.getText().toString());
                    user.setName(etName.getText().toString());
                    user.setEmail(etEmail.getText().toString());
                    user.setLanguage(language);
                    user.setLogedIn(true);

                    Prefs.setUserData(this, User.getInstance(this));
                    DataBase.addContactsPhoneNumbersToDb(DataBase.getInstance(this).getWritableDatabase(),
                            user.getContactList());

                    String lang = "en";
                    if (user.getLanguage() == Language.DANISH){
                        lang = Locale.getDefault().getCountry().toLowerCase();
                    }
                    Prefs.setLanguageCountryCode(this, lang);

                    Intent intent = new Intent(this, SelectLanguageActivity.class);
                    intent.putExtra("startedFromRegistration", true);
                    startActivity(intent);
                    finish();

                } else {
                    showErrorToast(getString(R.string.please_try_again));
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorToast(getString(R.string.please_try_again));
            }
        }
    }
}
