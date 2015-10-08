package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import test.myprojects.com.callproject.Util.DataBase;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Notification;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.service.NotificationService;
import test.myprojects.com.callproject.task.SendMessageTask;


/**
 * Created by dtomic on 01/09/15.
 */
public class ContactDetailActivity extends Activity implements MessageInterface {

    private static final String TAG = "ContactDetailActivity";
    private Contact contact;

    private Notification notification;

    @Bind(R.id.tvName) TextView tvName;
    @Bind(R.id.tvPhoneNumber) TextView tvPhoneNumber;

    @Bind(R.id.ivProfile)
    CircleImageView ivProfile;
    @Bind(R.id.tvProfile)
    TextView tvProfile;
    @Bind(R.id.ibFavorit)
    ImageButton ibFavorit;

    @Bind(R.id.bConfirm)
    Button bConfirm;

    @OnClick(R.id.bConfirm)
    public void confimrClicked(){

        String text = bConfirm.getText().toString();

        if (text.contentEquals(getString(R.string.invite))){

            String smsText = User.getInstance(this).getSmsInviteText();

            if (smsText == null || smsText.length() == 0){
                smsText = getString(R.string.invite_user_text);
            }

            Uri uri = Uri.parse("smsto:" + contact.getPhoneNumber());
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
            it.putExtra("sms_body", smsText);
            it.putExtra(Intent.EXTRA_TEXT, smsText);
        //    it.putExtra("exit_on_sent", true);
            startActivity(it);


        }
//        else if (text.contentEquals(getString(R.string.add_contact))){
//            new SendMessageTask(this, getAddContactsParams(contact.getPhoneNumber())).execute();
//
//        }
        else if (text.contentEquals(getString(R.string.set_notification))){
            bConfirm.setText(getString(R.string.remove_notification));

            DataBase.addNotificationNumberToDb(DataBase.getInstance(this).getWritableDatabase(),
                    contact.getName(), contact.getPhoneNumber(), contact.getStatus().getValue());

            Intent pushIntent = new Intent(this, NotificationService.class);
            startService(pushIntent);
        }else if (text.contentEquals(getString(R.string.remove_notification))){
            bConfirm.setText(getString(R.string.set_notification));
            DataBase.removeNotificationNumberToDb(DataBase.getInstance(this).getWritableDatabase(),notification);
        }

    }

    @OnClick(R.id.bEdit)
    public void edit() {

        if (contact == null) {
            Toast.makeText(this, "not valid contact", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_EDIT);
        Uri mSelectedContactUri = ContactsContract.Contacts.getLookupUri(contact.getRecordId(), contact.getLookupKey());
        intent.setDataAndType(mSelectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.bBack)
    public void back() {
        finish();
    }

    @OnClick(R.id.ibFavorit)
    public void favoritClicked() {
        if (contact.isFavorit()) {
            contact.setFavorit(false);
            ibFavorit.setImageResource(R.drawable.star_empty);

            ContentValues v = new ContentValues();
            v.put(ContactsContract.Contacts.STARRED, 0);
            getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, v,
                    ContactsContract.Contacts._ID + "=?", new String[]{contact.getRecordId() + ""});

        } else {
            contact.setFavorit(true);
            ibFavorit.setImageResource(R.drawable.star_full);

            ContentValues v = new ContentValues();
            v.put(ContactsContract.Contacts.STARRED, 1);
            getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, v,
                    ContactsContract.Contacts._ID + "=?", new String[]{contact.getRecordId() + ""});

        }
    }

    @OnClick(R.id.ibCall)
    public void Call() {
        String number = contact.getPhoneNumber();
        if (number.length() > 0) {
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + number)));
        }
    }

    @OnClick(R.id.bSendSMS)
    public void sendSMS() {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", contact.getPhoneNumber());
        smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
        smsIntent.putExtra("exit_on_sent", true);
        startActivity(smsIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_detail_activity);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int contactid = bundle.getInt("contactId");
            this.contact = User.getInstance(this).getContactWithId(contactid);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        if (contact != null) {
            tvName.setText(contact.getName());
            tvPhoneNumber.setText(contact.getPhoneNumber());

            if (contact.isFavorit()) {
                ibFavorit.setImageResource(R.drawable.star_full);
            } else {
                ibFavorit.setImageResource(R.drawable.star_empty);
            }


            if (contact.getImage() != null && getUserImage(contact.getRecordId()) != null) {
                Log.i(TAG, "name " + contact.getName());
                ivProfile.setImageBitmap(getUserImage(contact.getRecordId()));
                ivProfile.setVisibility(View.VISIBLE);
                tvProfile.setVisibility(View.INVISIBLE);
            } else {
                tvProfile.setText(contact.getName().substring(0, 1).toUpperCase());

                ivProfile.setVisibility(View.INVISIBLE);
                tvProfile.setVisibility(View.VISIBLE);
            }

            List<String> checkPhoneList = User.getInstance(this).getCheckPhoneNumberList();

            if (checkPhoneList.contains(contact.getPhoneNumber())){

//                if (contact.getStatus()==null){
//                    bConfirm.setText(getString(R.string.add_contact));
//                }else {

                    notification = DataBase.getNotificationWithPhoneNumber
                            (DataBase.getInstance(this).getWritableDatabase(), contact.getPhoneNumber());

                    if (notification != null){
                        bConfirm.setText(getString(R.string.remove_notification));
                    }else {
                        bConfirm.setText(getString(R.string.set_notification));
                    }

              //  }

            }else {
                bConfirm.setText(getString(R.string.invite));
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requseCode " + requestCode);
        Log.i(TAG, "requseCode " + resultCode);

        if (contact == null)
            return;

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {

                String[] projection = new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                        ContactsContract.CommonDataKinds.Phone.STARRED,
                        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
                };


                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{""+contact.getRecordId()}, null);

                if (phones.moveToFirst()) {

                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (name == null || phoneNumber == null) {
                        phones.close();
                        return;
                    }


                    contact.setName(name);
                    contact.setPhoneNumber(phoneNumber);
                    contact.setName(Character.toUpperCase(contact.getName().charAt(0)) + contact.getName().substring(1));

                    int colPhotoIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);
                    if (colPhotoIndex != -1) {
                        contact.setImage(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID)));
                    }

                    int starred = Integer.valueOf(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)));
                    contact.setFavorit(starred == 1 ? true : false);

                    contact.setLookupKey(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));

                }

                phones.close();

                User.getInstance(this).setNeedRefreshStatus(true);
                refreshUI();
            }
        }
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

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {
        if (result == null) {
            showErrorTryAgain();
            return;
        }

        if (methodName.contentEquals(SendMessageTask.ADD_CONTACT)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {
                    Toast.makeText(this, getString(R.string.contact_added), Toast.LENGTH_SHORT).show();

                    if (contact.getStatus() != null && contact.getStatus() != Status.GREEN_STATUS){
                        bConfirm.setText(getString(R.string.set_notification));
                    }else {
                        bConfirm.setText(getString(R.string.remove_notification));
                    }

                    User.getInstance(this).setNeedRefreshStatus(true);

                } else {
                    showErrorTryAgain();
                }


            } catch (NullPointerException ne) {
                ne.printStackTrace();
                showErrorTryAgain();
            }


        }
    }

    private void showErrorTryAgain() {
        Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
    }

    private Bitmap getUserImage(int contactId){


        Bitmap bitmap = null;
        try {

            Uri photo = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            photo = Uri.withAppendedPath( photo, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY );

            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photo);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return bitmap;
    }
}
