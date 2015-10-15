package test.myprojects.com.callproject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.content.IntentCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.Language;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.task.SendMessageTask;


public class SelectLanguageActivity extends ListActivity  {

    private static final String TAG = "SelectLanguageActivity";
    private List<String> langList = new ArrayList<String>();
    private int currentCountryPosition;
    private boolean startedFromRegistration;

    @OnClick(R.id.bDone)
    public void doneClicked(){

        int position = getListView().getCheckedItemPosition();

        if (currentCountryPosition != position){

            if (langList.get(position).contentEquals(getString(R.string.english))){
                setLocale("en");
            }else {
                setLocale("da");
            }
        }

        if (startedFromRegistration){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else {

            if (currentCountryPosition == position){
                finish();
            }else {
                Intent i = new Intent(this, StartActivity.class);
                i.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);
        ButterKnife.bind(this);

        langList.add(getString(R.string.english));
        langList.add(getString(R.string.danish));

        getListView().setChoiceMode(getListView().CHOICE_MODE_SINGLE);

        setListAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_checked, langList));

        String countryCode = Prefs.getLanguageCountryCode(this);

        Log.i(TAG, "countryCode " + countryCode);

        if (countryCode.contentEquals("da") || countryCode.contentEquals("dk")){
            getListView().setItemChecked(1, true);
            currentCountryPosition = 1;
        }else {
            getListView().setItemChecked(0, true);
            currentCountryPosition = 0;
        }

        if (getIntent().getExtras() != null){
            startedFromRegistration = getIntent().getExtras().getBoolean("startedFromRegistration", false);
        }
    }

    public void setLocale(String lang) {

        Prefs.setLanguageCountryCode(this, lang);

        Language language = Language.ENGLISH;
        if (lang.contentEquals("da")) {
            language = Language.DANISH;
        }

        User.getInstance(this).setLanguage(language);
        SendMessageTask task3 = new SendMessageTask(null, getUpdateAccountParams(language));
        task3.execute();

        Prefs.setUserData(this, User.getInstance(this));

        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

    }

    private SoapObject getUpdateAccountParams(Language lang) {
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
        pi.setValue(user.getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(user.getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Name");
        pi.setValue(user.getName());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Email");
        pi.setValue(user.getEmail());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Language");
        pi.setValue(lang.getValue());
        pi.setType(Integer.class);
        request.addProperty(pi);

        return request;
    }

}
