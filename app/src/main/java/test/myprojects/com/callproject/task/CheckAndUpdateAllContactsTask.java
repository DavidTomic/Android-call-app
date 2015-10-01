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

    public CheckAndUpdateAllContactsTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

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
                    TextUtils.isDigitsOnly(String.valueOf(contact.getName().charAt(0)))) continue;


            String phoneNumberOnlyDigit = contact.getPhoneNumber();
            phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
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

        Log.i(TAG, "contactList.size() " + contactList.size());
        Log.i(TAG, "prefs.size() " + Prefs.getLastContactCount(mainActivity));

        if (Prefs.getLastContactCount(mainActivity) < contactList.size()) {
            return true;
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
     //   Log.i(TAG, "result " + result);

        if (result) {
            Prefs.setLastContactCount(mainActivity, contactList.size());

            List<Contact> originalList = User.getInstance(mainActivity).getContactList();
        //    Log.i(TAG, "originalListBid " + java.lang.System.identityHashCode(originalList));
            originalList.clear();
         //   Log.i(TAG, "originalListAid " + java.lang.System.identityHashCode(originalList));

            for (Contact c : contactList) {
                originalList.add(c);
            }

       //     Log.i(TAG, "originalList size " + originalList.size());
       //     Log.i(TAG, "originalListid " + java.lang.System.identityHashCode(originalList));


            //for case if connection on server not working to show new added contact in list
            Intent returnIntent = new Intent(MainActivity.BROADCAST_STATUS_UPDATE_ACTION);
            mainActivity.sendBroadcast(returnIntent);

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

        List<Contact> cList = User.getInstance(mainActivity).getContactList();

//        int i = 0;
        for (Contact contact : cList) {
//            if (++i == 3){
//                break;
//            }

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
            Prefs.setLastContactCount(mainActivity, 0);
            return;
        }

        try {

            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {

                Prefs.setLastCallTime(mainActivity, 0);
                mainActivity.refreshStatuses();
                mainActivity.refreshCheckPhoneNumbers();

            }else {
                Prefs.setLastContactCount(mainActivity, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

