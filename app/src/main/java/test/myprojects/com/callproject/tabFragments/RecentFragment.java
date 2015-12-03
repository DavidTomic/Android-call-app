package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.fortysevendeg.swipelistview.SwipeListView;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
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
import test.myprojects.com.callproject.receiver.TimerBroadcastReceiver;
import test.myprojects.com.callproject.service.NotificationService;
import test.myprojects.com.callproject.task.SendMessageTask;
import test.myprojects.com.callproject.view.PullToRefreshStickyList;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment implements MessageInterface, View.OnTouchListener {


    private static final String TAG = "RecentFragment";
    private View rootView;
    private List<Contact> recentList = new ArrayList<>();
    private boolean showAllContacts = true;

    private int currentStatus;

    private LinearLayout currentView;
    private int startX = 0;
    private int rightMargin = 0;
    private boolean moveWasActive = false;

    private PullToRefreshStickyList stlist;
    private StickyAdapter adapter;

    @Bind(R.id.llStatusHolder)
    LinearLayout llStatusHolder;

    @Bind(R.id.llRedStatus)
    LinearLayout llRedStatus;
    @Bind(R.id.llYellowStatus)
    LinearLayout llYellowStatus;
    @Bind(R.id.llGreenStatus)
    LinearLayout llGreenStatus;


    @Bind(R.id.bAll)
    Button bAll;
    @Bind(R.id.bUnanswer)
    Button bUnanswer;

    private boolean editEnabled;

    @OnClick(R.id.bAll)
    public void bAllClicked() {
        bAll.setBackgroundDrawable(getResources().getDrawable(R.drawable.all_contacts_bg_full));
        bAll.setTextColor(getResources().getColor(R.color.black));
        bUnanswer.setTextColor(getResources().getColor(R.color.nav_bar_button_color));
        bUnanswer.setBackgroundDrawable(null);
        showAllContacts = true;

        refreshRecents();
    }

    @OnClick(R.id.bUnanswer)
    public void bUnanswerClicked() {
        bAll.setBackgroundDrawable(null);
        bUnanswer.setBackgroundDrawable(getResources().getDrawable(R.drawable.unanswer_bg_full));
        bAll.setTextColor(getResources().getColor(R.color.nav_bar_button_color));
        bUnanswer.setTextColor(getResources().getColor(R.color.black));
        showAllContacts = false;

        refreshRecents();
    }


    @Bind(R.id.bEdit)
    Button bEdit;

    @OnClick(R.id.bEdit)
    public void editFavorites() {
        if (editEnabled) {
            bEdit.setText(getString(R.string.edit));
            editEnabled = false;
            //   swipeMenuListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        } else {
            //  swipeMenuListView.setSwipeDirection(0);
            bEdit.setText(getString(R.string.done));
            editEnabled = true;
        }

        adapter.notifyDataSetChanged();
    }

    private void createListAdapter(int position) {

        adapter = new StickyAdapter(getActivity());
        stlist.getRefreshableView().setAdapter(adapter);

        stlist.getRefreshableView().setSelection(position);
    }

    @Bind(R.id.bStatusRed)
    ImageView bStatusRed;
    @Bind(R.id.bStatusYellow)
    ImageView bStatusYellow;
    @Bind(R.id.bStatusGreen)
    ImageView bStatusGreen;


    @OnClick(R.id.llSettings)
    public void settingsClicked() {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }

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
            stlist = (PullToRefreshStickyList) rootView.findViewById(R.id.stickSwipeList);

            createListAdapter(0);

//            swipeMenuListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
//                    DeleteCallById(recentList.get(position).getCallId() + "");
//
//                    // false : close the menu; true : not close the menu
//                    return false;
//                }
//            });

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
                            if(Math.abs(event.getRawX() - startX) < 3)
                                break;

                            rightMargin = -((int) event.getRawX() - startX);
                            //    Log.i(TAG, "event.getRawX() " + event.getRawX());
                            //  Log.i(TAG, "rMargin " + rightMargin);

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
                                    new SendMessageTask(RecentFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                } else if (currentView == llYellowStatus) {
                                    //   Log.i(TAG, "llYellowStatus");
                                    bStatusRed.setSelected(false);
                                    bStatusYellow.setSelected(true);
                                    bStatusGreen.setSelected(false);

                                    currentStatus = 2;
                                    new SendMessageTask(RecentFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                } else {
                                    //    Log.i(TAG, "llGreenStatus");
                                    bStatusRed.setSelected(false);
                                    bStatusYellow.setSelected(false);
                                    bStatusGreen.setSelected(true);

                                    currentStatus = 1;
                                    new SendMessageTask(RecentFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                }
                            } else {
                                moveWasActive = false;
                            }

                            if (rightMargin < 0) {
                                rightMargin = 0;
                                closeSwipeView();
                            } else {

                                if (rightMargin > WindowSize.convertDpToPixel(30)) {
                                    startActivity(new Intent(getActivity(), SetStatusActivity.class));
                                }
                                closeSwipeView();


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


    public class StickyAdapter extends BaseAdapter implements StickyListHeadersAdapter {


        private LayoutInflater inflater;
        //  private Context mContext;

        public StickyAdapter(Context context) {
            //  mContext = context;
            inflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            //       Log.i(TAG, "contactList getCount " + contactList.size());
            //        Log.i(TAG, "contactListid " + java.lang.System.identityHashCode(contactList));
            return recentList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return recentList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
 //           if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.common_list_item, parent, false);
                holder.swipelist = (SwipeListView) convertView.findViewById(R.id.swipe_lv_list);
                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }

            holder.swipelist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

            RecentAdapter sadapter = new RecentAdapter(getActivity().getApplicationContext(), position);
            holder.swipelist.setAdapter(sadapter);

            return convertView;
        }

        class ViewHolder {
            SwipeListView swipelist;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            return new View(parent.getContext());
        }

        @Override
        public long getHeaderId(int i) {
            return 0;
        }
    }


    private void closeSwipeView() {
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

    public class RecentAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        int parent_postion;
        SQLiteDatabase db;

        public RecentAdapter(Context context, int parent_postion) {
            inflater = LayoutInflater.from(context);
            this.parent_postion = parent_postion;
            db = DataBase.getInstance(context).getWritableDatabase();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
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
                holder.edit_btn = (Button) convertView.findViewById(R.id.btn_edit);
                holder.rlHolder = (RelativeLayout) convertView.findViewById(R.id.rlHolder);
                holder.ivEnvelop = (ImageView) convertView.findViewById(R.id.ivEnvelop);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Contact contact = recentList.get(parent_postion);

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
                } else if (contact.getContactType() == Contact.ContactType.INCOMING) {
                    holder.image.setImageDrawable(getResources().getDrawable(R.drawable.incoming_call_icon));
                    holder.image.setVisibility(View.VISIBLE);
                } else {
                    holder.image.setVisibility(View.INVISIBLE);
                }


            } else {
                holder.name.setTextColor(getResources().getColor(R.color.status_red_enabled));
                holder.image.setVisibility(View.GONE);
            }


            String date = new java.text.SimpleDateFormat("dd-MM HH:mm").format
                    (new java.util.Date(contact.getDate()));
            holder.date.setText(date);


            if (editEnabled) {
                holder.bDelete.setVisibility(View.VISIBLE);
            } else {
                holder.bDelete.setVisibility(View.GONE);
            }


            String statusText = contact.getStatusText();
      //      Log.i(TAG, "statusText " + statusText);
            if (statusText != null) {
                holder.statusText.setText(contact.getStatusText());
                holder.statusText.setVisibility(View.VISIBLE);
            } else {
                holder.statusText.setText("");
                holder.statusText.setVisibility(View.GONE);
            }

            //  Log.i(TAG, "status " + contact.getStatus());

            Status status = contact.getStatus();

//            Log.i(TAG, "getRecordId " + contact.getRecordId());
//            Log.i(TAG, "name " + name);

            if (contact.getRecordId() == -1) {
                holder.infoButton.setVisibility(View.INVISIBLE);
//                holder.vStatus.setVisibility(View.GONE);
                holder.tvOnPhone.setVisibility(View.GONE);

                holder.edit_btn.setText(getString(R.string.invite));
                holder.ivEnvelop.setVisibility(View.GONE);

//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
//                        ((int) RelativeLayout.LayoutParams.WRAP_CONTENT, (int) RelativeLayout.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.CENTER_VERTICAL);
//                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                params.setMargins(0,0,WindowSize.convertDpToPixel(14),0);
//                holder.date.setLayoutParams(params);

            } else {
                holder.infoButton.setVisibility(View.VISIBLE);

//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
//                        ((int) RelativeLayout.LayoutParams.WRAP_CONTENT, (int) RelativeLayout.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                params.setMargins(0,0,WindowSize.convertDpToPixel(14),WindowSize.convertDpToPixel(2));
//                holder.date.setLayoutParams(params);
//
//                holder.vStatus.setVisibility(View.VISIBLE);
                holder.tvOnPhone.setVisibility(View.GONE);

                if (status != null) {

                    Notification notification = DataBase.getNotificationWithPhoneNumber
                            (db, contact.getPhoneNumber());

                    if (notification != null) {
                        holder.edit_btn.setText(getString(R.string.remove_notification));
                        holder.ivEnvelop.setVisibility(View.VISIBLE);
                    } else {
                        holder.edit_btn.setText(getString(R.string.set_notification));
                        holder.ivEnvelop.setVisibility(View.GONE);
                    }

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

                    holder.edit_btn.setText(getString(R.string.invite));
                    holder.ivEnvelop.setVisibility(View.GONE);
                }
            }

            holder.edit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //      Toast.makeText(getActivity().getApplicationContext(), "Edit " + contactList.get(parent_postion).getName(), Toast.LENGTH_SHORT).show();

                    String text = holder.edit_btn.getText().toString();

                    Log.i(TAG, "text " + text);

                    if (text.contentEquals(getString(R.string.invite))) {

                        String smsText = User.getInstance(getActivity()).getSmsInviteText();

                        if (smsText == null || smsText.length() == 0) {
                            smsText = getString(R.string.invite_user_text);
                        }

                        Uri uri = Uri.parse("smsto:" + recentList.get(parent_postion).getPhoneNumber());
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        it.putExtra("sms_body", smsText);
                        it.putExtra(Intent.EXTRA_TEXT, smsText);
                        it.putExtra("exit_on_sent", true);
                        startActivity(it);


                    } else if (text.contentEquals(getString(R.string.set_notification))) {


                        try {
                            DataBase.addNotificationNumberToDb(DataBase.getInstance(getActivity()).getWritableDatabase(),
                                    recentList.get(parent_postion).getName(), recentList.get(parent_postion).getPhoneNumber(),
                                    recentList.get(parent_postion).getStatus().getValue());
                            holder.edit_btn.setText(getString(R.string.remove_notification));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (text.contentEquals(getString(R.string.remove_notification))) {
                        holder.edit_btn.setText(getString(R.string.set_notification));

                        Notification notification = DataBase.getNotificationWithPhoneNumber(DataBase.getInstance(getActivity()).getWritableDatabase(),
                                recentList.get(parent_postion).getPhoneNumber());

                        if (notification != null)
                            DataBase.removeNotificationFromDb(DataBase.getInstance(getActivity()).getWritableDatabase(), notification);

                    }

                    createListAdapter(stlist.getRefreshableView().getFirstVisiblePosition());

                }
            });


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


            holder.rlHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialNumber(recentList.get(parent_postion).getPhoneNumber());
                }
            });

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
            Button edit_btn;
            RelativeLayout rlHolder;
            ImageView ivEnvelop;
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

            if (!(contact.getPhoneNumber() != null && contact.getPhoneNumber().length() > 4)) continue;

            contact.setName(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
            contact.setDuration(Float.parseFloat(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))));
            contact.setDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))));


            if (!showAllContacts && Integer.parseInt(cursor.getString(cursor.
                    getColumnIndex(CallLog.Calls.TYPE))) != CallLog.Calls.MISSED_TYPE) {
                continue;
            }

            int cId = User.getContactIDFromNumber(contact.getPhoneNumber(), getActivity());
            contact.setRecordId(cId);


            if (cId != -1) {
                for (Contact c : User.getInstance(getActivity()).getContactList()) {
                    if (c.getRecordId() == contact.getRecordId()) {
                        contact.setStatus(c.getStatus());
                        contact.setStatusText(c.getStatusText());

                        if (contact.getName() == null){
                            contact.setName(c.getName());
                        }

                        break;
                    }
                }
            }

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

        adapter.notifyDataSetChanged();

     //   createListAdapter(stlist.getRefreshableView().getFirstVisiblePosition());

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

            refreshRecents();
            refreshStatusUI();
        }
    };

    private void refreshStatusUI() {

        Status status = User.getInstance(getActivity()).getStatus();

        long currentMillies = System.currentTimeMillis();

        if (currentMillies > User.getInstance(getActivity()).getStatusStartTime()
                && currentMillies < User.getInstance(getActivity()).getStatusEndTime()) {
            status = User.getInstance(getActivity()).getTimerStatus();

            //       Log.i(TAG, "refreshStatusUI getTimerStatus " + status);
        }
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

        if (result == null) {
            return;
        }

        try {

            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {

                User.getInstance(getActivity()).setStatus(Status.values()[currentStatus]);
                Prefs.setUserData(getActivity(), User.getInstance(getActivity()));

                TimerBroadcastReceiver.CancelAlarmIfNeed(getActivity());
            }

        } catch (NullPointerException ne) {
            ne.printStackTrace();
//            Toast.makeText(getActivity(), getString(R.string.status_not_updated),
//                    Toast.LENGTH_SHORT).show();
        }
    }

}
