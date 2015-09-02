package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenuListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;

/**
 * Created by dtomic on 01/09/15.
 */
public class ContactDetailActivity extends Activity {

    private static final String TAG = "ContactDetailActivity";
    private Contact contact;
    @Bind(R.id.tvName)
    TextView tvName;
    @Bind(R.id.tvPhoneNumber)
    TextView tvPhoneNumber;
    @Bind(R.id.ivProfile)
    CircleImageView ivProfile;
    @Bind(R.id.tvProfile)
    TextView tvProfile;
    @Bind(R.id.ibFavorit)
    ImageButton ibFavorit;

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


            if (contact.getImage() != null) {
                Log.i(TAG, "name " + contact.getName());
                Uri imageUri = Uri.parse(contact.getImage());
                ivProfile.setImageURI(imageUri);
                ivProfile.setVisibility(View.VISIBLE);
                tvProfile.setVisibility(View.INVISIBLE);
            } else {
                tvProfile.setText(contact.getName().substring(0, 1).toUpperCase());

                ivProfile.setVisibility(View.INVISIBLE);
                tvProfile.setVisibility(View.VISIBLE);
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
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
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

                    int colPhotoIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI);
                    if (colPhotoIndex != -1) {
                        contact.setImage(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)));
                    }

                    int starred = Integer.valueOf(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)));
                    contact.setFavorit(starred == 1 ? true : false);

                    contact.setLookupKey(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)));

                }

                phones.close();

                User.getInstance(this).setContactEdited(true);
                refreshUI();
            }
        }
    }
}
