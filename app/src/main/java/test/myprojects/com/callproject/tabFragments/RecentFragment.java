package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.ContactDetailActivity;
import test.myprojects.com.callproject.MainActivity;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.SettingsActivity;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.Util.WindowSize;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment implements MessageInterface {


    private static final String TAG = "RecentFragment";
    private View rootView;
    private List<Contact> recentList = new ArrayList<>();
    private boolean showAllContacts = true;

    private int currentStatus;

    @Bind(R.id.swipeMenuListView)
    SwipeMenuListView swipeMenuListView;

    @Bind(R.id.bAll) Button bAll;
    @Bind(R.id.bUnanswer) Button bUnanswer;

    private boolean editEnabled;

    @OnClick(R.id.bAll)
    public void bAllClicked(){
        bAll.setBackgroundDrawable(getResources().getDrawable(R.drawable.all_contacts_bg_full));
        bAll.setTextColor(getResources().getColor(R.color.black));
        bUnanswer.setTextColor(getResources().getColor(R.color.nav_bar_button_color));
        bUnanswer.setBackgroundDrawable(null);
        showAllContacts = true;

        refreshRecents();
    }

    @OnClick(R.id.bUnanswer)
    public void bUnanswerClicked(){
        bAll.setBackgroundDrawable(null);
        bUnanswer.setBackgroundDrawable(getResources().getDrawable(R.drawable.unanswer_bg_full));
        bAll.setTextColor(getResources().getColor(R.color.nav_bar_button_color));
        bUnanswer.setTextColor(getResources().getColor(R.color.black));
        showAllContacts = false;

        refreshRecents();
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

        recentAdapter.notifyDataSetChanged();
    }

    @Bind(R.id.bStatusRed) ImageView bStatusRed;
    @Bind(R.id.bStatusYellow) ImageView bStatusYellow;
    @Bind(R.id.bStatusGreen) ImageView bStatusGreen;

    @OnClick(R.id.llRedStatus)
    public void bStatusRedClicked(){

        Log.i(TAG, "clicked red");

        bStatusRed.setSelected(true);
        bStatusYellow.setSelected(false);
        bStatusGreen.setSelected(false);

        currentStatus = 0;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }
    @OnClick(R.id.llYellowStatus)
    public void bStatusYellowClicked(){
        bStatusRed.setSelected(false);
        bStatusYellow.setSelected(true);
        bStatusGreen.setSelected(false);

        currentStatus = 2;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }
    @OnClick(R.id.llGreenStatus)
    public void bStatusGreenClicked(){
        bStatusRed.setSelected(false);
        bStatusYellow.setSelected(false);
        bStatusGreen.setSelected(true);

        currentStatus = 1;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }



    @OnClick(R.id.llSettings)
    public void settingsClicked(){
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }

    private RecentAdapter recentAdapter;

    public RecentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {

            //       Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_recent, container, false);
            ButterKnife.bind(this, rootView);
            recentAdapter = new RecentAdapter(getActivity());
            swipeMenuListView.setAdapter(recentAdapter);

            swipeMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialNumber(recentList.get(position).getPhoneNumber());
                }
            });


            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getActivity().getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x3F, 0x25)));
                    // set item width
                    deleteItem.setWidth(120);

                    deleteItem.setTitle("Delete");

                    deleteItem.setTitleSize(18);
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
                    DeleteCallById(recentList.get(position).getCallId() + "");

                    // false : close the menu; true : not close the menu
                    return false;
                }
            });

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
            return rootView;
        }

        return rootView;
    }

    public class RecentAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public RecentAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return recentList.size();
        }

        @Override
        public Object getItem(int position) {
            return recentList.get(position);
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
                convertView = inflater.inflate(R.layout.recent_list_item, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.tvName);
                holder.date = (TextView) convertView.findViewById(R.id.tvDate);
                holder.statusText = (TextView) convertView.findViewById(R.id.tvStatus);
                holder.image = (ImageView) convertView.findViewById(R.id.ivProfile);
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

            final Contact contact = recentList.get(position);

            String name = contact.getName();
            if (name == null) name = contact.getPhoneNumber();
            holder.name.setText(name);

            if (showAllContacts) {

                if (contact.getContactType() == Contact.ContactType.MISSED) {
                    holder.name.setTextColor(getResources().getColor(R.color.status_red_enabled));
                } else {
                    holder.name.setTextColor(getResources().getColor(R.color.black));
                }

                if (contact.getContactType() == Contact.ContactType.OUTGOING) {
                    holder.image.setImageDrawable(getResources().getDrawable(R.drawable.outcoming_call_icon));
                    holder.image.setVisibility(View.VISIBLE);
                } else if (contact.getContactType() == Contact.ContactType.INCOMING){
                    holder.image.setImageDrawable(getResources().getDrawable(R.drawable.incoming_call_icon));
                    holder.image.setVisibility(View.VISIBLE);
                }else {
                    holder.image.setVisibility(View.INVISIBLE);
                }


            } else {
                holder.name.setTextColor(getResources().getColor(R.color.status_red_enabled));
                holder.image.setVisibility(View.GONE);
            }


            String date = new java.text.SimpleDateFormat("dd-MM HH:mm").format
                    (new java.util.Date(contact.getDate()));
            holder.date.setText(date);

            holder.infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "info " + contact.getName());
                    Log.i(TAG, "contactId " + contact.getRecordId());

                    Intent contactDetailIntent = new Intent(getActivity(), ContactDetailActivity.class);
                    contactDetailIntent.putExtra("name", contact.getName());
                    contactDetailIntent.putExtra("contactId", contact.getRecordId());
                    getActivity().startActivity(contactDetailIntent);
                }
            });

            holder.bDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeleteCallById(contact.getCallId() + "");
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
                holder.statusText.setText("");
                holder.statusText.setVisibility(View.GONE);
            }

         //   Log.i(TAG, "status " + contact.getStatus());

            Status status = contact.getStatus();

            Log.i(TAG, "getRecordId " + contact.getRecordId());
            Log.i(TAG, "name " + name);

            if (contact.getRecordId() == -1){
                holder.infoButton.setVisibility(View.INVISIBLE);
                holder.vStatus.setVisibility(View.GONE);
                holder.tvOnPhone.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                        ((int) RelativeLayout.LayoutParams.WRAP_CONTENT, (int) RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.setMargins(0,0,WindowSize.convertDpToPixel(14),0);
                holder.date.setLayoutParams(params);

            }else {
                holder.infoButton.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                        ((int) RelativeLayout.LayoutParams.WRAP_CONTENT, (int) RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.setMargins(0,0,WindowSize.convertDpToPixel(14),WindowSize.convertDpToPixel(2));
                holder.date.setLayoutParams(params);

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
            }

            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView statusText;
            TextView date;
            ImageButton infoButton;
            ImageView image;
            Button bDelete;
            LinearLayout vStatus;
            View vStatusRed;
            View vStatusYellow;
            View vStatusGreen;
            TextView tvOnPhone;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(statusUpdateBroadcastReceiver,
                new IntentFilter(MainActivity.BROADCAST_STATUS_UPDATE_ACTION));
        refreshRecents();
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

    private void refreshRecents() {
        Uri queryUri = android.provider.CallLog.Calls.CONTENT_URI;

        String[] projection = new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.TYPE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE};

        String sortOrder = CallLog.Calls.DATE + " DESC";

        Cursor cursor = getActivity().getContentResolver().query(queryUri, projection, null, null, sortOrder);

        Log.i(TAG, "COUNT: " + cursor.getCount());


        recentList.clear();
        int i = 0;
        while (cursor.moveToNext() && i < 20) {
            i++;

//            Log.i(TAG, "TYPE: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
//            Log.i(TAG, "CACHED_NAME: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
//            Log.i(TAG, "NUMBER: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
//            Log.i(TAG, "ID: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));
//            Log.i(TAG, "ID: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));

            Contact contact = new Contact();
            contact.setCallId(Integer.parseInt(cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls._ID))));

            contact.setPhoneNumber(cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.NUMBER)));

            //   Log.i(TAG, "NAME: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)) + " PN: " + contact.getPhoneNumber());

            if (contact.getPhoneNumber() == null || contact.getPhoneNumber().length() < 5) continue;

            contact.setName(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
            contact.setDuration(Float.parseFloat(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))));
            contact.setDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))));


            if (!showAllContacts && Integer.parseInt(cursor.getString(cursor.
                    getColumnIndex(CallLog.Calls.TYPE))) != CallLog.Calls.MISSED_TYPE) {
                continue;
            }

            int cId = User.getContactIDFromNumber(contact.getPhoneNumber(), getActivity());
            contact.setRecordId(cId);

          //  Log.i(TAG, "cID " + contact.getRecordId());

            switch (Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)))) {
                case CallLog.Calls.INCOMING_TYPE:
                    contact.setContactType(Contact.ContactType.INCOMING);
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    contact.setContactType(Contact.ContactType.OUTGOING);
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    contact.setContactType(Contact.ContactType.MISSED);
                    break;
            }

            recentList.add(contact);

        }
        cursor.close();
        recentAdapter.notifyDataSetChanged();

    }


    private void DeleteCallById(String idd) {
        try {
            getActivity().getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + " = ? ",
                    new String[]{String.valueOf(idd)});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        refreshRecents();
    }

    private void dialNumber(String phoneNumber) {

        Log.i(TAG, "Dial " + phoneNumber);

        if (phoneNumber.length() > 0) {
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + phoneNumber)));
        }
    }

    private BroadcastReceiver statusUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "statusUpdateBroadcastReceiver");
            recentAdapter.notifyDataSetChanged();
        }
    };

    private void refreshStatusUI(){

        Status status = User.getInstance(getActivity()).getStatus();

        Log.i(TAG, "myStatus " + status);

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
