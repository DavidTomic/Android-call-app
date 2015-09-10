package test.myprojects.com.callproject;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;

public class AddContactsActivity extends ListActivity implements MessageInterface {

    private static final String TAG = "AddContactsActivity";

    private final List<String> numbers = new ArrayList<>();
    private List<Contact> contacts = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    private Button bDone;
    private ProgressBar progressBar;
    private ListView listview;
    private TextView textView;

    private int currentPosition;
    private boolean cameFromCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);


        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            cameFromCreateAccount = bundle.getBoolean("cameFromCreateAccount", false);
        }

        bDone = (Button) findViewById(R.id.bDone);
        progressBar = (ProgressBar) findViewById(R.id.pbProgressBar);
        textView = (TextView)findViewById(R.id.tvNoContacts);

        bDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cameFromCreateAccount) {
                    startActivity(new Intent(AddContactsActivity.this, MainActivity.class));
                }

                finish();
            }
        });


        listview = getListView();
        listview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        //--	text filtering
        listview.setTextFilterEnabled(true);


        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_checked, names));

        new SendMessageTask(this, getCheckPhoneParams()).execute();

    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        CheckedTextView item = (CheckedTextView) v;
//        Toast.makeText(this, numbers.get(position) + " checked : " +
//                item.isChecked(), Toast.LENGTH_SHORT).show();
        currentPosition = position;

        listview.setEnabled(false);
        bDone.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (item.isChecked()) {

            new SendMessageTask(this, getAddContactsParams(contacts.get(position).getPhoneNumber())).execute();

        } else {

            new SendMessageTask(this, getDeleteContactParams(contacts.get(position).getPhoneNumber())).execute();

        }

        listview.setItemChecked(position, false);

    }

    private SoapObject getAddContactsParams(String number) {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.ADD_CONTACT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(this).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(this).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("ContactsPhoneNumber");
        pi.setValue(number);
        pi.setType(String.class);
        request.addProperty(pi);


        pi = new PropertyInfo();
        pi.setName("Name");
        pi.setValue(number);
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Noter");
        pi.setValue("");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Number");
        pi.setValue("");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("URL");
        pi.setValue("");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Adress");
        pi.setValue("");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Birthsday");
        pi.setValue("2000-01-01T00:00:00");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("pDate");
        pi.setValue("2000-01-01T00:00:00");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Favorites");
        pi.setValue(false);
        pi.setType(Boolean.class);
        request.addProperty(pi);

        return request;
    }

    private SoapObject getDeleteContactParams(String number) {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.DELETE_CONTACT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(this).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(this).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("PhoneNumberToDelete");
        pi.setValue(number);
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

    private SoapObject getCheckPhoneParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.CHECK_PHONE_NUMBERS);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(this).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(this).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        SoapObject phoneNumbersSoapObject = new SoapObject(SendMessageTask.NAMESPACE, "PhoneNumbers");

        List<Contact> cList = User.getInstance(this).getContactList();

        for (Contact contact : cList) {
            PropertyInfo piPhoneNumber = new PropertyInfo();
            piPhoneNumber.setName("string");
            piPhoneNumber.setValue(contact.getPhoneNumber());
            piPhoneNumber.setType(String.class);
            phoneNumbersSoapObject.addProperty(piPhoneNumber);
        }

        request.addProperty("PhoneNumbers", phoneNumbersSoapObject);

        return request;
    }

    private SoapObject getGetContactParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.GET_CONTACT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(this).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(this).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        bDone.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        listview.setEnabled(true);

        if (result == null) {
            showErrorTryAgain();
            return;
        }

        if (methodName.contentEquals(SendMessageTask.ADD_CONTACT)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {
                    Toast.makeText(this, getString(R.string.contact_added), Toast.LENGTH_SHORT).show();
                    listview.setItemChecked(currentPosition, true);
                    Prefs.setLastCallTime(AddContactsActivity.this, 0);
                } else {
                    showErrorTryAgain();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorTryAgain();
            }


        } else if (methodName.contentEquals(SendMessageTask.DELETE_CONTACT)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {
                    Toast.makeText(this, getString(R.string.contact_deleted), Toast.LENGTH_SHORT).show();
                    listview.setItemChecked(currentPosition, false);
                    Prefs.setLastCallTime(AddContactsActivity.this, 0);
                } else {
                    showErrorTryAgain();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorTryAgain();
            }


        } else if (methodName.contentEquals(SendMessageTask.CHECK_PHONE_NUMBERS)) {
            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    SoapObject phoneNumbersSoapObject = (SoapObject) result.getProperty("PhoneNumbers");

                    if (phoneNumbersSoapObject.getPropertyCount()==0){
                        if (cameFromCreateAccount) {
                            startActivity(new Intent(AddContactsActivity.this, MainActivity.class));
                            Toast.makeText(AddContactsActivity.this,
                                    getString(R.string.no_contact_using_app), Toast.LENGTH_SHORT).show();
                        }else {
                            textView.setVisibility(View.VISIBLE);
                        }

                        return;
                    }

                    for (int i = 0; i < phoneNumbersSoapObject.getPropertyCount(); i++) {
                        Log.i(TAG, "phoneNumbersSoapObject " + phoneNumbersSoapObject.getProperty(i));
                        numbers.add("" + phoneNumbersSoapObject.getProperty(i));
                    }


                    List<Contact> contactList = User.getInstance(this).getContactList();

                    for (String number : numbers) {
                        for (Contact contact : contactList) {
                            if (number.contentEquals(contact.getPhoneNumber())) {
                                contacts.add(contact);
                                break;
                            }
                        }
                    }

                    for (Contact c : contacts) {
                        names.add(c.getName());
                    }

                    Log.i(TAG, "numbers " + numbers);
                    Log.i(TAG, "names " + names);

                    ((ArrayAdapter) listview.getAdapter()).notifyDataSetChanged();

                    if (!cameFromCreateAccount){
                        new SendMessageTask(this, getGetContactParams()).execute();
                    }


                } else {
                    showErrorTryAgain();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorTryAgain();
            }

        } else if (methodName.contentEquals(SendMessageTask.GET_CONTACT)) {
            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    SoapObject contactsSoapObject = (SoapObject) result.getProperty("Contacts");

                    for (int i = 0; i < contactsSoapObject.getPropertyCount(); i++) {

                        SoapObject csContactsSoapObject = (SoapObject) contactsSoapObject.getProperty(i);

                        String phoneNumber = ""+csContactsSoapObject.getProperty("Phonenumber");
                        Log.i(TAG, "phoneNumber " + phoneNumber);

                        for (int j=0; j<contacts.size();j++){
                            if (phoneNumber.contentEquals(contacts.get(j).getPhoneNumber())){
                                listview.setItemChecked(j, true);
                                break;
                            }
                        }

                    }

                    ((ArrayAdapter) listview.getAdapter()).notifyDataSetChanged();

                } else {
                    showErrorTryAgain();
                }


            } catch (Exception e) {
                e.printStackTrace();
                showErrorTryAgain();
            }
        }
    }

    private void showErrorTryAgain() {
        Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
    }
}
