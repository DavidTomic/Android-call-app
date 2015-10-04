package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import test.myprojects.com.callproject.ContactDetailActivity;
import test.myprojects.com.callproject.MainActivity;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.SetStatusActivity;
import test.myprojects.com.callproject.SettingsActivity;
import test.myprojects.com.callproject.Util.DataBase;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.Util.WindowSize;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Notification;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritFragment extends Fragment implements MessageInterface, View.OnTouchListener {

    private static final String TAG = "FavoritFragment";
    private View rootView;
    private List<Contact> favoritList = new ArrayList<>();
    private FavoritAdapter favoritAdapter;
    private SwipeMenuListView swipeMenuListView;
    private boolean editEnabled, swipeCreatorCreated;

    private int currentStatus;

    private LinearLayout currentView;
    private int startX = 0;
    private int rightMargin = 0;
    private boolean moveWasActive = false;

    public FavoritFragment() {
        // Required empty public constructor
    }


    @Bind(R.id.bEdit) Button bEdit;

    @OnClick(R.id.bEdit)
    public void editFavorites(){
        if (editEnabled){
            bEdit.setText(getString(R.string.edit));
            editEnabled=false;
            swipeMenuListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        }else {
            swipeMenuListView.setSwipeDirection(0);
            bEdit.setText(getString(R.string.done));
            editEnabled=true;
        }

        favoritAdapter.notifyDataSetChanged();
    }

    @Bind(R.id.bStatusRed)
    ImageView bStatusRed;
    @Bind(R.id.bStatusYellow) ImageView bStatusYellow;
    @Bind(R.id.bStatusGreen) ImageView bStatusGreen;

    @Bind(R.id.llStatusHolder)
    LinearLayout llStatusHolder;

    @Bind(R.id.llRedStatus)
    LinearLayout llRedStatus;
    @Bind(R.id.llYellowStatus)
    LinearLayout llYellowStatus;
    @Bind(R.id.llGreenStatus)
    LinearLayout llGreenStatus;

    @OnClick(R.id.bSetTime)
    public void bSetTimeClicked() {
        closeSwipeView();
        startActivity(new Intent(getActivity(), SetStatusActivity.class));
    }

    @OnClick(R.id.llSettings)
    public void settingsClicked(){
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(statusUpdateBroadcastReceiver,
                new IntentFilter(MainActivity.BROADCAST_STATUS_UPDATE_ACTION));
        refreshFavorits();
        refreshStatusUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(statusUpdateBroadcastReceiver);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void refreshFavorits() {
        favoritList.clear();

    //    Log.i(TAG, "refreshFavorits getContactList() " + User.getInstance(getActivity()).getContactList().size());

        for (Contact c : User.getInstance(getActivity()).getContactList()) {
            if (c.isFavorit()) {
                favoritList.add(c);
            }
        }

        favoritAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {

            //       Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_favorit, container, false);
            ButterKnife.bind(this, rootView);
            swipeMenuListView = (SwipeMenuListView) rootView.
                    findViewById(R.id.swipeMenuListView);
            favoritAdapter = new FavoritAdapter(getActivity());
            swipeMenuListView.setAdapter(favoritAdapter);


            swipeMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "onItemClick");
                    dialNumber(favoritList.get(position).getPhoneNumber());
                }
            });

            setMenuCreator();


            llStatusHolder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    // Log.i(TAG, "onTouch");
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            startX = (int) event.getRawX() + rightMargin;
                            //  Log.i(TAG, "startX " + startX);
                            //  Log.i(TAG, "rightMargin " + rightMargin);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            rightMargin = -((int) event.getRawX() - startX);
                            //    Log.i(TAG, "event.getRawX() " + event.getRawX());
                        //    Log.i(TAG, "rMargin " + rightMargin);

                            if (rightMargin >= 0 && rightMargin < WindowSize.convertDpToPixel(120)) {
                                params.rightMargin = rightMargin;
                                params.leftMargin = -rightMargin;
                                llStatusHolder.setLayoutParams(params);
                            }

                            moveWasActive = true;
                            break;

                        case MotionEvent.ACTION_UP:

                            if (!moveWasActive) {
                                if (currentView == llRedStatus) {
                                    //     Log.i(TAG, "llRedStatus");
                                    bStatusRed.setSelected(true);
                                    bStatusYellow.setSelected(false);
                                    bStatusGreen.setSelected(false);

                                    currentStatus = 0;
                                    new SendMessageTask(FavoritFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                } else if (currentView == llYellowStatus) {
                                    //   Log.i(TAG, "llYellowStatus");
                                    bStatusRed.setSelected(false);
                                    bStatusYellow.setSelected(true);
                                    bStatusGreen.setSelected(false);

                                    currentStatus = 2;
                                    new SendMessageTask(FavoritFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                } else {
                                    //    Log.i(TAG, "llGreenStatus");
                                    bStatusRed.setSelected(false);
                                    bStatusYellow.setSelected(false);
                                    bStatusGreen.setSelected(true);

                                    currentStatus = 1;
                                    new SendMessageTask(FavoritFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                }
                            } else {
                                moveWasActive = false;
                            }

                            if (rightMargin < 0) {
                                rightMargin = 0;
                                closeSwipeView();
                            } else {

                                if (rightMargin > WindowSize.convertDpToPixel(30)) {
                                    rightMargin = WindowSize.convertDpToPixel(100);
                                    params.rightMargin = rightMargin;
                                    params.leftMargin = -rightMargin;
                                    llStatusHolder.setLayoutParams(params);
                                } else {
                                    closeSwipeView();
                                }

                            }

                            break;

                    }

                    return true;
                }
            });

            llRedStatus.setOnTouchListener(this);
            llYellowStatus.setOnTouchListener(this);
            llGreenStatus.setOnTouchListener(this);

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
            return rootView;
        }

        return rootView;
    }

    private void setMenuCreator(){

        final List<String> checkPhoneList = User.getInstance(getActivity()).getCheckPhoneNumberList();

//        if (checkPhoneList.size() == 0){
//            return;
//        }
//
//        if (swipeCreatorCreated){
//            return;
//        }
//        swipeCreatorCreated = true;


        Log.i(TAG, "HERE");

        final SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                Log.i(TAG, "CREATE " + menu.getViewType());

                Contact contact = favoritList.get(menu.getViewType());
                String text = getString(R.string.invite);

                if (checkPhoneList.contains(contact.getPhoneNumber())) {

                    if (contact.getStatus() == null) {
                        text = getString(R.string.add_contact);
                    } else {

                        Notification notification = DataBase.getNotificationWithPhoneNumber
                                (DataBase.getInstance(getActivity()).getWritableDatabase(), contact.getPhoneNumber());

                        if (notification != null){
                            text = getString(R.string.remove_notification);
                        }else {
                            text = getString(R.string.set_notification);
                        }

                    }

                }

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(getResources().getColor(R.color.blue_default)));
                // set item width
                deleteItem.setWidth(120);

                deleteItem.setTitle(text);

                deleteItem.setTitleSize(16);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        swipeMenuListView.setMenuCreator(creator);

        swipeMenuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                removeFromFavorites(favoritList.get(position));
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void closeSwipeView(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llStatusHolder.getLayoutParams();
        rightMargin = 0;
        params.rightMargin = rightMargin;
        params.leftMargin = -rightMargin;
        llStatusHolder.setLayoutParams(params);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        currentView = (LinearLayout) v;
        return false;
    }

    private void dialNumber(String phoneNumber) {

        if (phoneNumber.length() > 0) {
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + phoneNumber)));
        }
    }

    private class FavoritAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public FavoritAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return favoritList.size();
        }

        @Override
        public Object getItem(int position) {
            return favoritList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.favorit_list_item, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.tvName);
                holder.statusText = (TextView) convertView.findViewById(R.id.tvStatus);
                holder.tvProfile = (TextView) convertView.findViewById(R.id.tvProfile);
                holder.ivProfile = (CircleImageView) convertView.findViewById(R.id.ivProfile);
                holder.infoButton = (ImageButton) convertView.findViewById(R.id.ibInfo);
                holder.bDelete = (Button) convertView.findViewById(R.id.bDelete);
                holder.vStatus = (LinearLayout) convertView.findViewById(R.id.vStatus);
                holder.vStatusRed = convertView.findViewById(R.id.vStatusRed);
                holder.vStatusYellow = convertView.findViewById(R.id.vStatusYellow);
                holder.vStatusGreen = convertView.findViewById(R.id.vStatusGreen);
                holder.tvOnPhone = (TextView) convertView.findViewById(R.id.tvOnPhone);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Contact contact = favoritList.get(position);

            holder.name.setText(contact.getName());

            if (contact.getImage() != null && getUserImage(contact.getRecordId()) != null) {
             //
             //
             //  Log.i(TAG, "name " + contact.getName());
                holder.ivProfile.setImageBitmap(getUserImage(contact.getRecordId()));
                holder.ivProfile.setVisibility(View.VISIBLE);
                holder.tvProfile.setVisibility(View.INVISIBLE);
            } else {
                holder.tvProfile.setText(contact.getName().substring(0, 1).toUpperCase());

                holder.ivProfile.setVisibility(View.INVISIBLE);
                holder.tvProfile.setVisibility(View.VISIBLE);
            }

            holder.infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "info " + contact.getName());
                    Log.i(TAG, "contactId " + contact.getRecordId());

                    Intent contactDetailIntent = new Intent(getActivity(), ContactDetailActivity.class);
                    contactDetailIntent.putExtra("contactId", contact.getRecordId());
                    getActivity().startActivity(contactDetailIntent);
                }
            });

            holder.bDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeFromFavorites(contact);
                }
            });


            if (editEnabled){
                holder.bDelete.setVisibility(View.VISIBLE);
            }else{
                holder.bDelete.setVisibility(View.GONE);
            }


            String statusText = contact.getStatusText();
            if (statusText!=null){
                holder.statusText.setText(contact.getStatusText());
                holder.statusText.setVisibility(View.VISIBLE);
            }else {
                holder.statusText.setVisibility(View.GONE);
            }

            Status status = contact.getStatus();

            holder.vStatus.setVisibility(View.VISIBLE);
            holder.tvOnPhone.setVisibility(View.GONE);

            if (status != null) {
                switch (status) {
                    case RED_STATUS:
                        holder.vStatusRed.setSelected(true);
                        holder.vStatusYellow.setSelected(false);
                        holder.vStatusGreen.setSelected(false);
                        break;
                    case YELLOW_STATUS:
                        holder.vStatusRed.setSelected(false);
                        holder.vStatusYellow.setSelected(true);
                        holder.vStatusGreen.setSelected(false);
                        break;
                    case GREEN_STATUS:
                        holder.vStatusRed.setSelected(false);
                        holder.vStatusYellow.setSelected(false);
                        holder.vStatusGreen.setSelected(true);
                        break;
                    case ON_PHONE:
                        holder.vStatus.setVisibility(View.GONE);
                        holder.tvOnPhone.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                holder.vStatusRed.setSelected(false);
                holder.vStatusYellow.setSelected(false);
                holder.vStatusGreen.setSelected(false);
            }


            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView statusText;
            ImageButton infoButton;
            CircleImageView ivProfile;
            TextView tvProfile;
            Button bDelete;
            LinearLayout vStatus;
            View vStatusRed;
            View vStatusYellow;
            View vStatusGreen;
            TextView tvOnPhone;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    private void removeFromFavorites(Contact contact) {
        User.getInstance(getActivity()).getContactWithId(contact.getRecordId()).setFavorit(false);
        ContentValues v = new ContentValues();
        v.put(ContactsContract.Contacts.STARRED, 0);
        getActivity().getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, v,
                ContactsContract.Contacts._ID + "=?", new String[]{contact.getRecordId() + ""});

        refreshFavorits();
    }

    private BroadcastReceiver statusUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

         //   Log.i(TAG, "statusUpdateBroadcastReceiver");
            refreshFavorits();
            refreshStatusUI();
          //  setMenuCreator();
        }
    };

    private Bitmap getUserImage(int contactId){


        Bitmap bitmap = null;
        try {

            Uri photo = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            photo = Uri.withAppendedPath( photo, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY );

            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photo);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return bitmap;
    }

    private void refreshStatusUI(){

        Status status = User.getInstance(getActivity()).getStatus();

        long currentMillies = System.currentTimeMillis();

        if (currentMillies > User.getInstance(getActivity()).getStatusStartTime()
                && currentMillies < User.getInstance(getActivity()).getStatusEndTime()){
            status = User.getInstance(getActivity()).getTimerStatus();

     //       Log.i(TAG, "refreshStatusUI getTimerStatus " + status);
        }
     //   Log.i(TAG, "refreshStatusUI " + status);

        if (status != null) {
            switch (status) {
                case RED_STATUS:
                    bStatusRed.setSelected(true);
                    bStatusYellow.setSelected(false);
                    bStatusGreen.setSelected(false);
                    break;
                case YELLOW_STATUS:
                    bStatusRed.setSelected(false);
                    bStatusYellow.setSelected(true);
                    bStatusGreen.setSelected(false);
                    break;
                case GREEN_STATUS:
                    bStatusRed.setSelected(false);
                    bStatusYellow.setSelected(false);
                    bStatusGreen.setSelected(true);
                    break;
                case ON_PHONE:
                    bStatusRed.setSelected(false);
                    bStatusYellow.setSelected(false);
                    bStatusGreen.setSelected(true);
                    new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
                    break;
            }
        }
    }

    private SoapObject getUpdateStatusParams(int status) {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.UPDATE_STATUS);


        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(getActivity()).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(getActivity()).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Status");
        pi.setValue(status);
        pi.setType(Integer.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Text");
        pi.setValue(User.getInstance(getActivity()).getStatusText());
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        if (result == null){
            return;
        }

        try {

            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {

                User.getInstance(getActivity()).setStatus(Status.values()[currentStatus]);
                Prefs.setUserData(getActivity(), User.getInstance(getActivity()));

            }

        } catch (NullPointerException ne) {
            ne.printStackTrace();
//            Toast.makeText(getActivity(), getString(R.string.status_not_updated),
//                    Toast.LENGTH_SHORT).show();
        }
    }
}
