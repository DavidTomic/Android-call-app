package test.myprojects.com.callproject.model;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

import test.myprojects.com.callproject.MainActivity;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.Util.Language;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.task.SendMessageTask;

/**
 * Created by dtomic on 24/08/15.
 */
public class User {

    private static final String TAG = "User";
    private static User instance;
    private String phoneNumber;
    private String name;
    private String email;
    private String password;
    private Language language;
    private boolean loggedIn;

    private Status status;
    private String statusText;

    private int iAmLiveSeconds;


    private String endTime;

    private List<Contact> contactList = new ArrayList<>();
    private List<String> checkPhoneNumberList = new ArrayList<>();

    private boolean needRefreshStatus;

    private User() {

    }

    //Getters and Setters
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isNeedRefreshStatus() {
        return needRefreshStatus;
    }

    public void setNeedRefreshStatus(boolean needRefreshStatus) {
        this.needRefreshStatus = needRefreshStatus;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static void empty() {
        instance = null;
    }

    private static void initInstance(Context context) {
        if (instance == null) {
            instance = new User();
            Prefs.loadUserData(context);
            instance.loadContactsToList(context);
        }
    }

    public static User getInstance(Context context) {
        initInstance(context);
        return instance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isLogedIn() {
        return loggedIn;
    }

    public void setLogedIn(boolean logedIn) {
        this.loggedIn = logedIn;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public List<String> getCheckPhoneNumberList() {
        return checkPhoneNumberList;
    }


    public int getiAmLiveSeconds() {
        return iAmLiveSeconds;
    }

    public void setiAmLiveSeconds(int iAmLiveSeconds) {
        this.iAmLiveSeconds = iAmLiveSeconds;
    }

    //methods
    public void loadContactsToList(Context context) {
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

        Cursor phones = context.getContentResolver().
                query(uri, projection, null, null, sortOrder);

        List<Contact> oldContactList = new ArrayList<>(contactList);

        contactList.clear();

        while (phones.moveToNext()) {
            Contact contact = new Contact();
            contact.setName(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
            contact.setPhoneNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

            if (contact.getPhoneNumber() == null || contact.getName() == null ||
                    TextUtils.isDigitsOnly(String.valueOf(contact.getName().charAt(0)))) continue;

            contact.setName(Character.toUpperCase(contact.getName().charAt(0)) + contact.getName().substring(1));

            contact.setRecordId(Integer.valueOf(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))));

            int colPhotoIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);
            if (colPhotoIndex != -1) {
                contact.setImage(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID)));
            }


            int starred = Integer.valueOf(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)));
            contact.setFavorit(starred == 1 ? true : false);

            contact.setLookupKey(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));

            Log.i(TAG, "id " + contact.getRecordId() + " name " + contact.getName() + " NUMBER " + contact.getPhoneNumber() + "  photoUri " + contact.getImage());
            contactList.add(contact);
        }
        phones.close();

        Log.i(TAG, "contactList.size() " + contactList.size());
        Log.i(TAG, "prefs.size() " + Prefs.getLastContactCount(context));

        if (Prefs.getLastContactCount(context) < contactList.size()){
            Prefs.setLastContactCount(context, contactList.size());

            new SendMessageTask(null, getAddContactsParams()).execute();

            if (context instanceof MainActivity){

                //for case if connection on server not working to show new added contact in list
                Intent returnIntent = new Intent(MainActivity.BROADCAST_STATUS_UPDATE_ACTION);
                context.sendBroadcast(returnIntent);

                ((MainActivity)context).refreshStatuses();
                ((MainActivity)context).refreshCheckPhoneNumbers();
            }
        }

//        if (context instanceof MainActivity){
//            Log.i(TAG, "instanceof");
//
//            if (oldContactList.size() != contactList.size()){
//                for (Contact newContact : contactList){
//                    boolean match = false;
//                    for (Contact oldContact : oldContactList){
//                        if (newContact.getPhoneNumber().contentEquals(oldContact.getPhoneNumber())){
//                            match = true;
//                            break;
//                        }
//                    }
//
//                    if (!match){
//                    //    Log.i(TAG, "not match " + newContact.getName());
//                        new SendMessageTask(null, getAddContactsParams(newContact.getPhoneNumber()));
//                        break;
//                    }
//                }
//            }
//
//        }

    }

    private SoapObject getAddContactsParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.ADD_CONTACT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("ContactsPhoneNumber");
        pi.setValue("38594111333");
        pi.setType(String.class);
        request.addProperty(pi);


        pi = new PropertyInfo();
        pi.setName("Name");
        pi.setValue("");
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

    public static int getContactIDFromNumber(String contactNumber, Context context) {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = -1;

        Cursor contactLookupCursor = context.getContentResolver().query(Uri.
                withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        contactNumber), new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID}, null, null, null);

        while (contactLookupCursor.moveToNext()) {
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();

        return phoneContactID;
    }

    public Contact getContactWithId(int id){

        for (Contact contact : contactList){
            if (contact.getRecordId() == id){
                return contact;
            }
        }

        return null;
    }



//    public int getStatusColor(){
//
//        if (status==null){
//            return R.drawable.green_circle;
//        }
//
//        switch (status){
//            case RED_STATUS:
//                return R.drawable.red_circle;
//            case GREEN_STATUS:
//                return R.drawable.green_circle;
//            case YELLOW_STATUS:
//                return R.drawable.yellow_circle;
//        }
//
//        return R.drawable.green_circle;
//    }



}
