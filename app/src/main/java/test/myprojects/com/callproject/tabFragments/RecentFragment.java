package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.fortysevendeg.swipelistview.SwipeListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.ContactDetailActivity;
import test.myprojects.com.callproject.MainActivity;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.SetStatusActivity;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment {


    private static final String TAG = "RecentFragment";
    private View rootView;
    private List<Contact> recentList = new ArrayList<Contact>();
    private boolean showAllContacts;

    @Bind(R.id.swipeMenuListView)
    SwipeMenuListView swipeMenuListView;
    @Bind(R.id.bAllMissed)
    Button bAllMissed;
    private boolean editEnabled;


    @OnClick(R.id.bAllMissed)
    public void AllMissedClicked() {

        Log.i(TAG, "clicked");

        if (showAllContacts) {
            showAllContacts = false;

            String AllMissedText = getString(R.string.All_Missed);
            SpannableString spannablecontent = new SpannableString(AllMissedText);
            spannablecontent.setSpan(new ForegroundColorSpan(getResources().getColor
                    (R.color.black)), 0, AllMissedText.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannablecontent.setSpan(new ForegroundColorSpan(getResources().getColor
                    (R.color.blue_default)), AllMissedText.indexOf("/"), AllMissedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            bAllMissed.setText(spannablecontent);

        } else {
            showAllContacts = true;

            String AllMissedText = getString(R.string.All_Missed);
            SpannableString spannablecontent = new SpannableString(AllMissedText);
            spannablecontent.setSpan(new ForegroundColorSpan(getResources().getColor
                    (R.color.blue_default)), 0, AllMissedText.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannablecontent.setSpan(new ForegroundColorSpan(getResources().getColor
                    (R.color.black)), AllMissedText.indexOf("/"), AllMissedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            bAllMissed.setText(spannablecontent);
        }

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

            String AllMissedText = getString(R.string.All_Missed);
            SpannableString spannablecontent = new SpannableString(AllMissedText);
            spannablecontent.setSpan(new ForegroundColorSpan(getResources().getColor
                    (R.color.blue_default)), 0, AllMissedText.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            bAllMissed.setText(spannablecontent);
            showAllContacts = true;

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
                holder.vStatusRed = (View) convertView.findViewById(R.id.vStatusRed);
                holder.vStatusYellow = (View) convertView.findViewById(R.id.vStatusYellow);
                holder.vStatusGreen = (View) convertView.findViewById(R.id.vStatusGreen);
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
                    holder.image.setVisibility(View.VISIBLE);
                } else {
                    holder.image.setVisibility(View.INVISIBLE);
                }


            } else {
                holder.name.setTextColor(getResources().getColor(R.color.status_red_enabled));
                holder.image.setVisibility(View.GONE);
            }


            String date = new java.text.SimpleDateFormat("dd-MM-yyyy").format
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

            Log.i(TAG, "status " + contact.getStatus());

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
        refreshMyStatusUI();
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
            if (cId != -1) contact.setRecordId(cId);
            //  Log.i(TAG, "cID " + cId);

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


    public void DeleteCallById(String idd) {
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

    private void refreshMyStatusUI() {

        String statusText = User.getInstance(getActivity()).getStatusText();

//        if (statusText == null || statusText.length() < 1) {
//            tvStatusText.setVisibility(View.GONE);
//        } else {
//            tvStatusText.setText(statusText);
//            tvStatusText.setVisibility(View.VISIBLE);
//        }
//
//        vStatusColor.setBackgroundDrawable(getResources().
//                getDrawable(User.getInstance(getActivity()).getStatusColor()));
    }

    private BroadcastReceiver statusUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "statusUpdateBroadcastReceiver");
            recentAdapter.notifyDataSetChanged();
        }
    };

}
