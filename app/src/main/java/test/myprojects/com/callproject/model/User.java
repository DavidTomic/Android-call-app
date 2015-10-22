package test.myprojects.com.callproject.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import test.myprojects.com.callproject.Util.Language;
import test.myprojects.com.callproject.Util.Prefs;

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

    private String smsInviteText;

    private long statusStartTime;
    private long statusEndTime;

    private Status timerStatus;
    private String timerStatusText;

    private int iAmLiveSeconds;
    private int requestStatusInfoSeconds;

    private List<Contact> contactList = new ArrayList<>();
  //  private List<String> checkPhoneNumberList = new ArrayList<>();

  //  private boolean needRefreshStatus;

    private User() {

    }

    //Getters and Setters
    public Status getTimerStatus() {
        return timerStatus;
    }

    public void setTimerStatus(Status timerStatus) {
        this.timerStatus = timerStatus;
    }

    public int getRequestStatusInfoSeconds() {
        return requestStatusInfoSeconds;
    }

    public void setRequestStatusInfoSeconds(int requestStatusInfoSeconds) {
        this.requestStatusInfoSeconds = requestStatusInfoSeconds;
    }

    public long getStatusStartTime() {
        return statusStartTime;
    }

    public void setStatusStartTime(long statusStartTime) {
        this.statusStartTime = statusStartTime;
    }

    public long getStatusEndTime() {
        return statusEndTime;
    }

    public void setStatusEndTime(long statusEndTime) {
        this.statusEndTime = statusEndTime;
    }

//    public String getStatusEndTime() {
//        return statusEndTime;
//    }
//
//    public void setStatusEndTime(String statusEndTime) {
//        this.statusEndTime = statusEndTime;
//    }
//
//    public String getStatusStartTime() {
//        return statusStartTime;
//    }
//
//    public void setStatusStartTime(String statusStartTime) {
//        this.statusStartTime = statusStartTime;
//    }

    public String getSmsInviteText() {
        return smsInviteText;
    }

    public void setSmsInviteText(String smsInviteText) {
        this.smsInviteText = smsInviteText;
    }

//    public boolean isNeedRefreshStatus() {
//        return needRefreshStatus;
//    }
//
//    public void setNeedRefreshStatus(boolean needRefreshStatus) {
//        this.needRefreshStatus = needRefreshStatus;
//    }

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

   // public List<String> getCheckPhoneNumberList() {
   //     return checkPhoneNumberList;
   // }


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

        contactList.clear();

        while (phones.moveToNext()) {
            Contact contact = new Contact();
            contact.setName(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
            contact.setPhoneNumber(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

            if (contact.getPhoneNumber() == null || contact.getName() == null ||
                    TextUtils.isDigitsOnly(String.valueOf(contact.getName().charAt(0)))) continue;


            String phoneNumberOnlyDigit = contact.getPhoneNumber();
            String firstSign = phoneNumberOnlyDigit.substring(0,1);
            phoneNumberOnlyDigit = phoneNumberOnlyDigit.replaceAll("[^0-9.]", "");
            if (firstSign.contentEquals("+")){
                phoneNumberOnlyDigit = firstSign+phoneNumberOnlyDigit;
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

          //  Log.i(TAG, "id " + contact.getRecordId() + " name " + contact.getName() + " NUMBER " + contact.getPhoneNumber() + "  photoUri " + contact.getImage());
            contactList.add(contact);
        }
        phones.close();

        Log.i(TAG, "contactList.size() " + contactList.size());


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


    public String getTimerStatusText() {
        return timerStatusText;
    }

    public void setTimerStatusText(String timerStatusText) {
        this.timerStatusText = timerStatusText;
    }
}
