package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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

    private Contact contact;
    @Bind(R.id.tvName) TextView tvName;
    @Bind(R.id.tvPhoneNumber) TextView tvPhoneNumber;
    @Bind(R.id.ivProfile)
    CircleImageView ivProfile;
    @Bind(R.id.ibFavorit) ImageButton ibFavorit;

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
        startActivity(intent);
    }

    @OnClick(R.id.bBack)
    public void back() {
        finish();
    }

    @OnClick(R.id.ibFavorit)
    public void favoritClicked(){
        if (contact.isFavorit()){
            contact.setFavorit(false);
            ibFavorit.setImageResource(R.drawable.star_empty);

            ContentValues v = new ContentValues();
            v.put(ContactsContract.Contacts.STARRED, 0);
            getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, v,
                    ContactsContract.Contacts._ID + "=?", new String[]{contact.getRecordId() + ""});

        }else {
            contact.setFavorit(true);
            ibFavorit.setImageResource(R.drawable.star_full);

            ContentValues v = new ContentValues();
            v.put(ContactsContract.Contacts.STARRED, 1);
            getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, v,
                    ContactsContract.Contacts._ID + "=?", new String[]{contact.getRecordId() + ""});

        }
    }

    @OnClick(R.id.ibCall)
    public void Call(){
        String number = contact.getPhoneNumber();
        if (number.length() > 0) {
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + number)));
        }
    }

    @OnClick(R.id.bSendSMS)
    public void sendSMS(){
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
            String name = bundle.getString("name");
            this.contact = User.getInstance(this).getContactWithId(contactid);
        }

        if (contact != null){
            tvName.setText(contact.getName());
            tvPhoneNumber.setText(contact.getPhoneNumber());

            if (contact.isFavorit()){
                ibFavorit.setImageResource(R.drawable.star_full);
            }else {
                ibFavorit.setImageResource(R.drawable.star_empty);
            }
        }

    }
}
