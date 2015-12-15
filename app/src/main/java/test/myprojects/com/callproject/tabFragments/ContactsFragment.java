package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;



import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
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
import test.myprojects.com.callproject.swipelistview.SwipeListView;
import test.myprojects.com.callproject.task.SendMessageTask;
import test.myprojects.com.callproject.view.IndexView;
import test.myprojects.com.callproject.view.PullToRefreshStickyList;
import test.myprojects.com.callproject.view.SearchEditText;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment implements MessageInterface, View.OnTouchListener {

    private static final String TAG = "ContactsFragment";
    private View rootView;
    private PullToRefreshStickyList stlist;
    private List<Contact> contactList = new ArrayList<>();
    private StickyAdapter adapter;
    private int currentStatus = 1;

    private TextView tvPhoneNumber;

    private LinearLayout currentView;
    private int startX = 0;
    private int rightMargin = 0;
    private boolean moveWasActive = false;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Bind(R.id.llStatusHolder)
    LinearLayout llStatusHolder;

    @Bind(R.id.inputSearch)
    SearchEditText inputSearch;
    @Bind(R.id.bStatusRed)
    ImageView bStatusRed;
    @Bind(R.id.bStatusYellow)
    ImageView bStatusYellow;
    @Bind(R.id.bStatusGreen)
    ImageView bStatusGreen;

    @Bind(R.id.llRedStatus)
    LinearLayout llRedStatus;
    @Bind(R.id.llYellowStatus)
    LinearLayout llYellowStatus;
    @Bind(R.id.llGreenStatus)
    LinearLayout llGreenStatus;

    @OnClick(R.id.ibAddContact)
    public void addContact() {

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.llSettings)
    public void settingsClicked() {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        if (rootView == null) {

            //       Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
            ButterKnife.bind(this, rootView);
            // Initialise your layout here

            stlist = (PullToRefreshStickyList) rootView.findViewById(R.id.stickSwipeList);
            IndexView indexView = (IndexView) rootView.findViewById(R.id.indexView);


            View header = getActivity().getLayoutInflater().inflate(R.layout.contact_custom_header, null);
            tvPhoneNumber = (TextView) header.findViewById(R.id.tvPhoneNumber);
            stlist.getRefreshableView().addHeaderView(header);

            createListAdapter(0);

            /** indexable listview */
            indexView.init(stlist);


            inputSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    adapter.getFilter().filter(cs);
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    // TODO Auto-generated method stub
                }
            });

            inputSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    Log.i(TAG, "hasFocus " + hasFocus);

                    if (hasFocus) {
                        inputSearch.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    }
                }
            });

            inputSearch.setOnEditTextImeBackListener(new SearchEditText.EditTextImeBackListener() {
                @Override
                public void onImeBack(SearchEditText ctrl, String text) {
                    if (inputSearch.getText() != null && inputSearch.getText().toString().length() == 0) {
                        inputSearch.clearFocus();
                        inputSearch.setGravity(Gravity.CENTER);
                    }
                }
            });


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
                            if (Math.abs(event.getRawX() - startX) < 3)
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
                                    new SendMessageTask(ContactsFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                } else if (currentView == llYellowStatus) {
                                    //   Log.i(TAG, "llYellowStatus");
                                    bStatusRed.setSelected(false);
                                    bStatusYellow.setSelected(true);
                                    bStatusGreen.setSelected(false);

                                    currentStatus = 2;
                                    new SendMessageTask(ContactsFragment.this, getUpdateStatusParams(currentStatus)).execute();
                                } else {
                                    //    Log.i(TAG, "llGreenStatus");
                                    bStatusRed.setSelected(false);
                                    bStatusYellow.setSelected(false);
                                    bStatusGreen.setSelected(true);

                                    currentStatus = 1;
                                    new SendMessageTask(ContactsFragment.this, getUpdateStatusParams(currentStatus)).execute();
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

        }

        return rootView;
    }

    private void closeSwipeView() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llStatusHolder.getLayoutParams();
        rightMargin = 0;
        params.rightMargin = rightMargin;
        params.leftMargin = -rightMargin;
        llStatusHolder.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshStatusUI();

        getActivity().registerReceiver(statusUpdateBroadcastReceiver,
                new IntentFilter(MainActivity.BROADCAST_STATUS_UPDATE_ACTION));

        createListAdapter(0);
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

    private void refreshStatusUI() {
        tvPhoneNumber.setText(getString(R.string.my_number) + ": " +
                User.getInstance(getActivity()).getPhoneNumber());

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

    private void createListAdapter(int position) {

        adapter = new StickyAdapter(getActivity());
        stlist.getRefreshableView().setAdapter(adapter);
        //  stlist.setScrollingCacheEnabled( false );

        stlist.getRefreshableView().setSelection(position);

    }

    public class StickyAdapter extends BaseAdapter implements StickyListHeadersAdapter, Filterable {


        private LayoutInflater inflater;
        private List<Contact> mOriginalValues;
        private Context mContext;

        public StickyAdapter(Context context) {
            mContext = context;
            inflater = LayoutInflater.from(context);
            contactList = new ArrayList<>(User.getInstance(context).getContactList());
        }

        private void refreshOnContactCountChange() {
            inputSearch.setText("");
            inputSearch.clearFocus();
            inputSearch.setGravity(Gravity.CENTER);

            mOriginalValues = null;


            contactList = new ArrayList<>(User.getInstance(mContext).getContactList());
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            //      Log.i(TAG, "contactList getCount " + contactList.size());
            //        Log.i(TAG, "contactListid " + java.lang.System.identityHashCode(contactList));
            return contactList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return contactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final ViewHolder holder;
            //           if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.common_list_item, parent, false);
            holder.swipelist = (SwipeListView) convertView.findViewById(R.id.swipe_lv_list);
            convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }

            holder.swipelist.setOffsetLeft(WindowSize.convertDpToPixel(100));
            ((ListView) holder.swipelist).setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);

            SwipeAdapter sadapter = new SwipeAdapter(getActivity().getApplicationContext(), position);
            holder.swipelist.setAdapter(sadapter);

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            //set header text first char
            String headerText = "" + contactList.get(position).getName().subSequence(0, 1).charAt(0);
            holder.text.setText(headerText);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            //return the first character of the country as ID because this is what headers are based upon
            return contactList.get(position).getName().subSequence(0, 1).charAt(0);
        }

        @Override
        public Filter getFilter() {

            Log.i(TAG, "filter");

            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    List<Contact> filteredArrList = new ArrayList<Contact>();

                    if (mOriginalValues == null) {
                        mOriginalValues = new ArrayList<>(contactList); // saves the original data in mOriginalValues
                    }

                    if (constraint == null || constraint.length() == 0) {
                        Log.i(TAG, "constraint length 0");
                        results.count = mOriginalValues.size();
                        results.values = mOriginalValues;
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        for (int i = 0; i < mOriginalValues.size(); i++) {
                            String data = mOriginalValues.get(i).getName();
                            String data2 = mOriginalValues.get(i).getPhoneNumber();
                            if (data.toLowerCase().startsWith(constraint.toString()) ||
                                    data2.toLowerCase().contains(constraint.toString())) {
                                filteredArrList.add(mOriginalValues.get(i));
                            }
                        }

                        results.count = filteredArrList.size();
                        results.values = filteredArrList;
                    }

//                    Log.i(TAG, "filteredArrList " + filteredArrList.size());
//                    Log.i(TAG, "contactList " + contactList.size());
//                    Log.i(TAG, "mOriginalValues " + mOriginalValues.size());


                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    if (results.values != null) {
                        contactList = (List<Contact>) results.values;
                        adapter.notifyDataSetChanged();
                    }

                }
            };
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            SwipeListView swipelist;
        }

    }

    public class SwipeViewHolder {
        TextView tvTitle;
        TextView tvStatusText;
        LinearLayout vStatus;
        View vStatusRed;
        View vStatusYellow;
        View vStatusGreen;
        TextView tvOnPhone;
        Button edit_btn;
        Button delete_btn;
        RelativeLayout rlHolder;
        ImageView ivEnvelop;
    }

    public class SwipeAdapter extends BaseAdapter {
        Context context;
        int parent_postion;
        SQLiteDatabase db;


        public SwipeAdapter(Context context, int parent_postion) {
            this.context = context;
            this.parent_postion = parent_postion;
            db = DataBase.getInstance(context).getWritableDatabase();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup viewGroup) {

            final SwipeViewHolder swipeholder;

            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.contact_list_item, viewGroup, false);
                swipeholder = new SwipeViewHolder();

                swipeholder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                swipeholder.tvStatusText = (TextView) convertView.findViewById(R.id.tvStatusText);
                swipeholder.vStatus = (LinearLayout) convertView.findViewById(R.id.vStatus);
                swipeholder.vStatusRed = convertView.findViewById(R.id.vStatusRed);
                swipeholder.vStatusYellow = convertView.findViewById(R.id.vStatusYellow);
                swipeholder.vStatusGreen = convertView.findViewById(R.id.vStatusGreen);
                swipeholder.tvOnPhone = (TextView) convertView.findViewById(R.id.tvOnPhone);

                swipeholder.rlHolder = (RelativeLayout) convertView.findViewById(R.id.rlHolder);
                swipeholder.edit_btn = (Button) convertView.findViewById(R.id.btn_edit);
                swipeholder.delete_btn = (Button) convertView.findViewById(R.id.btn_delete);
                swipeholder.ivEnvelop = (ImageView) convertView.findViewById(R.id.ivEnvelop);


                convertView.setTag(swipeholder);
            } else {
                swipeholder = (SwipeViewHolder) convertView.getTag();
            }


            Contact contact = contactList.get(parent_postion);

            swipeholder.tvTitle.setText(contact.getName());

            String statusText = contact.getStatusText();
            if (statusText != null && !statusText.contentEquals("(null)")) {
                swipeholder.tvStatusText.setText(contact.getStatusText());
            } else {
                swipeholder.tvStatusText.setText("");
            }


            Status status = contact.getStatus();

            swipeholder.vStatus.setVisibility(View.VISIBLE);
            swipeholder.tvOnPhone.setVisibility(View.GONE);

            if (status != null) {

                Notification notification = DataBase.getNotificationWithPhoneNumber
                        (db, contact.getPhoneNumber());

                if (notification != null) {
                    swipeholder.edit_btn.setText(getString(R.string.remove_notification));
                    swipeholder.ivEnvelop.setVisibility(View.VISIBLE);
                } else {
                    swipeholder.edit_btn.setText(getString(R.string.set_notification));
                    swipeholder.ivEnvelop.setVisibility(View.GONE);
                }


                switch (status) {
                    case RED_STATUS:
                        swipeholder.vStatusRed.setSelected(true);
                        swipeholder.vStatusYellow.setSelected(false);
                        swipeholder.vStatusGreen.setSelected(false);
                        break;
                    case YELLOW_STATUS:
                        swipeholder.vStatusRed.setSelected(false);
                        swipeholder.vStatusYellow.setSelected(true);
                        swipeholder.vStatusGreen.setSelected(false);
                        break;
                    case GREEN_STATUS:
                        swipeholder.vStatusRed.setSelected(false);
                        swipeholder.vStatusYellow.setSelected(false);
                        swipeholder.vStatusGreen.setSelected(true);
                        break;
                    case ON_PHONE:
                        swipeholder.vStatus.setVisibility(View.GONE);
                        swipeholder.tvOnPhone.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                swipeholder.vStatusRed.setSelected(false);
                swipeholder.vStatusYellow.setSelected(false);
                swipeholder.vStatusGreen.setSelected(false);

                swipeholder.edit_btn.setText(getString(R.string.invite));
                swipeholder.ivEnvelop.setVisibility(View.GONE);
            }


            //  Log.i(TAG, "status " + contact.getStatus());


            swipeholder.rlHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent contactDetailIntent = new Intent(getActivity(), ContactDetailActivity.class);
                    contactDetailIntent.putExtra("contactId", contactList.get(parent_postion).getRecordId());
                    getActivity().startActivity(contactDetailIntent);
                }
            });

            swipeholder.edit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //      Toast.makeText(getActivity().getApplicationContext(), "Edit " + contactList.get(parent_postion).getName(), Toast.LENGTH_SHORT).show();

                    String text = swipeholder.edit_btn.getText().toString();

                    Log.i(TAG, "text " + text);

                    if (text.contentEquals(getString(R.string.invite))) {

                        String smsText = User.getInstance(getActivity()).getSmsInviteText();

                        Log.i(TAG, "smsText " + smsText);

                        if (smsText == null || smsText.length() == 0) {
                            smsText = getString(R.string.invite_user_text);
                        }

                        Uri uri = Uri.parse("smsto:" + contactList.get(parent_postion).getPhoneNumber());
                        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                        it.putExtra("sms_body", smsText);
                        it.putExtra(Intent.EXTRA_TEXT, smsText);
                        it.putExtra("exit_on_sent", true);
                        startActivity(it);


                    }

                    else if (text.contentEquals(getString(R.string.set_notification))) {


                        try {
                            DataBase.addNotificationNumberToDb(DataBase.getInstance(getActivity()).getWritableDatabase(),
                                    contactList.get(parent_postion).getName(), contactList.get(parent_postion).getPhoneNumber(),
                                    contactList.get(parent_postion).getStatus().getValue());

                            swipeholder.edit_btn.setText(getString(R.string.remove_notification));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (text.contentEquals(getString(R.string.remove_notification))) {
                        swipeholder.edit_btn.setText(getString(R.string.set_notification));

                        Notification notification = DataBase.getNotificationWithPhoneNumber(DataBase.getInstance(getActivity()).getWritableDatabase(),
                                contactList.get(parent_postion).getPhoneNumber());

                        if (notification != null)
                            DataBase.removeNotificationFromDb(DataBase.getInstance(getActivity()).getWritableDatabase(), notification);

                    }


                    createListAdapter(stlist.getRefreshableView().getFirstVisiblePosition());

                }
            });

            swipeholder.delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteContact(contactList.get(parent_postion));
                }
            });

            return convertView;
        }
    }

    private void deleteContact(Contact contact) {

        try {
            new SendMessageTask(null, getDeleteContactParams(contact.getPhoneNumber())).execute();
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, contact.getLookupKey());
            getActivity().getContentResolver().delete(uri, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        contactList.remove(contact);
        User.getInstance(getActivity()).getContactList().remove(contact);
        adapter.notifyDataSetChanged();

    }

    private BroadcastReceiver statusUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "statusUpdateBroadcastReceiver");
            adapter.refreshOnContactCountChange();
            refreshStatusUI();
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        currentView = (LinearLayout) v;
        return false;
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

    private SoapObject getDeleteContactParams(String number) {
        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.DELETE_CONTACT);

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
        pi.setName("PhoneNumberToDelete");
        pi.setValue(number);
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        if (result == null) {
            return;
        }

        if (methodName.contentEquals(SendMessageTask.UPDATE_STATUS)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    //    Toast.makeText(getActivity(), "New status " + currentStatus, Toast.LENGTH_SHORT).show();

                    User.getInstance(getActivity()).setStatus(Status.values()[currentStatus]);
                    Prefs.setUserData(getActivity(), User.getInstance(getActivity()));
                    refreshStatusUI();

                    TimerBroadcastReceiver.CancelAlarmIfNeed(getActivity());
                }

            } catch (NullPointerException ne) {
                ne.printStackTrace();
            }

        }
    }


}
