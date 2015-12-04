package test.myprojects.com.callproject.task;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

import test.myprojects.com.callproject.MainActivity;
import test.myprojects.com.callproject.Util.DataBase;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;


/**
 * Created by developer dtomic on 17/09/15.
 */
public class CheckAndUpdateAllContactsTask extends AsyncTask<Void, Void, Boolean> implements MessageInterface {

    private static final String TAG = "CheckAllContactsTask";
    private MainActivity mainActivity;
    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> newContactsList = new ArrayList<>();

    public CheckAndUpdateAllContactsTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                    ContactsContract.CommonDataKinds.Phone.STARRED,
                    ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
            };

            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

            Cursor phones = mainActivity.getContentResolver().
                    query(uri, projection, null, null, sortOrder);


            while (phones.moveToNext()) {
                Contact contact = new Contact();
                contact.setName(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contact.setPhoneNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                if (contact.getPhoneNumber() == null || contact.getName() == null ||
                        TextUtils.isDigitsOnly(String.valueOf(contact.getName().charAt(0))))
                    continue;


                String phoneNumberOnlyDigit = contact.getPhoneNumber();
                String firstSign = phoneNumberOnlyDigit.substring(0, 1);
                phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
                if (firstSign.contentEquals("+")) {
                    phoneNumberOnlyDigit = firstSign + phoneNumberOnlyDigit;
                }

                contact.setPhoneNumber(phoneNumberOnlyDigit);

                contact.setName(Character.toUpperCase(contact.getName().charAt(0)) + contact.getName().substring(1));

                contact.setRecordId(Integer.valueOf(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))));

                int colPhotoIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);
                if (colPhotoIndex != -1) {
                    contact.setImage(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID)));
                }


                int starred = Integer.valueOf(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)));
                contact.setFavorit(starred == 1 ? true : false);

                contact.setLookupKey(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));

                //      Log.i(TAG, "id " + contact.getRecordId() + " name " + contact.getName() + " NUMBER " + contact.getPhoneNumber() + "  photoUri " + contact.getImage());
                contactList.add(contact);
            }
            phones.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }



        List<String> currentList = DataBase.getContactsPhoneNumberListFromDb
                (DataBase.getInstance(mainActivity).getWritableDatabase());

        // add new contacts to server (user add contact out of this app)
        for (Contact contact : contactList) {
            if (!currentList.contains(contact.getPhoneNumber())) {
                newContactsList.add(contact);
            }
        }
        Log.i(TAG, "newContactsList.size() " + newContactsList.size());


        // delete old contacts from server
        List<String> oldPhoneNumbers = new ArrayList<>();
        List<String> phoneNumberList = new ArrayList<>();

        for (Contact contact : contactList) {
            phoneNumberList.add(contact.getPhoneNumber());
        }

        for (String phoneNumber : currentList) {
            if (!phoneNumberList.contains(phoneNumber)) {
                oldPhoneNumbers.add(phoneNumber);
            }
        }

        if (oldPhoneNumbers.size() > 0) {
            deleteContactsOnServer(oldPhoneNumbers);
        }

        Log.i(TAG, "oldPhoneNumbers.size() " + oldPhoneNumbers.size());

        if (newContactsList.size() > 0 || oldPhoneNumbers.size() > 0)
            return true;


        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        //   Log.i(TAG, "result " + result);

        if (result) {

            List<Contact> originalList = User.getInstance(mainActivity).getContactList();

            for (Contact originalContact : originalList) {
                for (Contact c : contactList) {
                    if (originalContact.getRecordId() == c.getRecordId()) {
                        c.setStatus(originalContact.getStatus());
                        c.setStatusText(originalContact.getStatusText());
                        break;
                    }
                }
            }

            originalList.clear();

            for (Contact c : contactList) {
                originalList.add(c);
            }

            //for case if connection on server not working to show new added contact in list
            Intent returnIntent = new Intent(MainActivity.BROADCAST_STATUS_UPDATE_ACTION);
            mainActivity.sendBroadcast(returnIntent);

            if (newContactsList.size() > 0)
                new SendMessageTask(this, getAddMultiContactsParams()).execute();

        }

    }

    private SoapObject getAddMultiContactsParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.ADD_MULTI_CONTACT);

        PropertyInfo pi1 = new PropertyInfo();
        pi1.setName("Phonenumber");
        pi1.setValue(User.getInstance(mainActivity).getPhoneNumber());
        pi1.setType(String.class);
        request.addProperty(pi1);

        pi1 = new PropertyInfo();
        pi1.setName("password");
        pi1.setValue(User.getInstance(mainActivity).getPassword());
        pi1.setType(String.class);
        request.addProperty(pi1);

        SoapObject contactsSoapObject = new SoapObject(SendMessageTask.NAMESPACE, "Contacts");


        for (Contact contact : newContactsList) {

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
            //   Prefs.setLastContactCount(mainActivity, 0);
            return;
        }

        if (methodName.contentEquals(SendMessageTask.ADD_MULTI_CONTACT)) {
            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    if (mainActivity != null && !mainActivity.isFinishing()) {
                        DataBase.addContactsPhoneNumbersToDb(DataBase.getInstance(mainActivity).getWritableDatabase(), contactList);
                        Prefs.setLastCallTime(mainActivity, "2000-01-01T00:00:00");
                        mainActivity.refreshStatuses();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (methodName.contentEquals(SendMessageTask.DELETE_CONTACT)) {
            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {
                    if (mainActivity != null && !mainActivity.isFinishing()) {
                        DataBase.addContactsPhoneNumbersToDb(DataBase.getInstance(mainActivity).getWritableDatabase(), contactList);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteContactsOnServer(List<String> phoneNumbers) {

        try {
            for (String phoneNumber : phoneNumbers) {
                new SendMessageTask(this, getDeleteContactParams(phoneNumber)).execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private SoapObject getDeleteContactParams(String number) {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.DELETE_CONTACT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(mainActivity).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(mainActivity).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("PhoneNumberToDelete");
        pi.setValue(number);
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }
}

