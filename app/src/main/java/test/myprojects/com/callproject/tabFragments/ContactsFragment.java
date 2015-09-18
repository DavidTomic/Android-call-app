package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;



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
import test.myprojects.com.callproject.SettingsActivity;
import test.myprojects.com.callproject.Util.DataBase;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Notification;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.service.NotificationService;
import test.myprojects.com.callproject.task.SendMessageTask;
import test.myprojects.com.callproject.view.IndexView;
import test.myprojects.com.callproject.view.PullToRefreshStickyList;
import test.myprojects.com.callproject.view.SearchEditText;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment implements MessageInterface {

    private static final String TAG = "ContactsFragment";
    private View rootView;
    private List<Contact> contactList = new ArrayList<>();
    private StickyAdapter adapter;
    private int currentStatus;

    private TextView tvPhoneNumber;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Bind(R.id.inputSearch)
    SearchEditText inputSearch;
    @Bind(R.id.bStatusRed)
    ImageView bStatusRed;
    @Bind(R.id.bStatusYellow)
    ImageView bStatusYellow;
    @Bind(R.id.bStatusGreen)
    ImageView bStatusGreen;


    @OnClick(R.id.llRedStatus)
    public void bStatusRedClicked() {

        Log.i(TAG, "clicked red");

        bStatusRed.setSelected(true);
        bStatusYellow.setSelected(false);
        bStatusGreen.setSelected(false);

        currentStatus = 0;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }

    @OnClick(R.id.llYellowStatus)
    public void bStatusYellowClicked() {
        bStatusRed.setSelected(false);
        bStatusYellow.setSelected(true);
        bStatusGreen.setSelected(false);

        currentStatus = 2;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }

    @OnClick(R.id.llGreenStatus)
    public void bStatusGreenClicked() {
        bStatusRed.setSelected(false);
        bStatusYellow.setSelected(false);
        bStatusGreen.setSelected(true);

        currentStatus = 1;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }


    @OnClick(R.id.ibAddContact)
    public void addContact() {
      //  Log.i(TAG, "here");

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.llSettings)
    public void settingsClicked(){
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

            final PullToRefreshStickyList stlist = (PullToRefreshStickyList) rootView.findViewById(R.id.stickSwipeList);
            IndexView indexView = (IndexView) rootView.findViewById(R.id.indexView);

            adapter = new StickyAdapter(getActivity());
            stlist.getRefreshableView().setAdapter(adapter);


            View header = getActivity().getLayoutInflater().inflate(R.layout.contact_custom_header, null);
            tvPhoneNumber = (TextView) header.findViewById(R.id.tvPhoneNumber);
            stlist.getRefreshableView().addHeaderView(header);

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

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }

        }

        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();

        refreshStatusUI();

        getActivity().registerReceiver(statusUpdateBroadcastReceiver,
                new IntentFilter(MainActivity.BROADCAST_STATUS_UPDATE_ACTION));


        if (User.getInstance(getActivity()).isNeedRefreshStatus()) {
            User.getInstance(getActivity()).setNeedRefreshStatus(false);

            Prefs.setLastCallTime(getActivity(), 0);
            ((MainActivity) getActivity()).refreshStatuses();

        }

        adapter.notifyDataSetChanged();
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


    public class StickyAdapter extends BaseAdapter implements StickyListHeadersAdapter, Filterable {


        private LayoutInflater inflater;
        private List<Contact> mOriginalValues;
        private Context mContext;

        public StickyAdapter(Context context) {
            mContext = context;
            inflater = LayoutInflater.from(context);
            contactList = new ArrayList<>(User.getInstance(context).getContactList());
        }

        public void refreshOnContactCountChange(){
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
     //       Log.i(TAG, "contactList getCount " + contactList.size());
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
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.contact_swipe_list_item, parent, false);
                holder.swipelist = (SwipeListView) convertView.findViewById(R.id.swipe_lv_list);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.swipelist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

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
                            if (data.toLowerCase().startsWith(constraint.toString())) {
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
                    contactList = (List<Contact>) results.values;
                    adapter.notifyDataSetChanged();
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
        RelativeLayout rlHolder;
    }

    public class SwipeAdapter extends BaseAdapter {
        Context context;
        int parent_postion;


        List<Notification> nList = DataBase.getNotificationNumberListFromDb(DataBase.
                getInstance(getActivity()).getWritableDatabase());

        public SwipeAdapter(Context context, int parent_postion) {
            this.context = context;
            this.parent_postion = parent_postion;
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

                /* button action */


	            /* item click action*/

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

                            Uri uri = Uri.parse("smsto:" + contactList.get(0).getPhoneNumber());
                            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                            it.putExtra("sms_body", getString(R.string.invite_user_text));
                            startActivity(it);


                        } else if (text.contentEquals(getString(R.string.add_contact))) {
                            new SendMessageTask(ContactsFragment.this, getAddContactsParams(contactList.get(parent_postion).getPhoneNumber())).execute();

                        } else if (text.contentEquals(getString(R.string.set_notification))) {
                            swipeholder.edit_btn.setText(getString(R.string.notification_already_added));

                            DataBase.addNotificationNumberToDb(DataBase.getInstance(getActivity()).getWritableDatabase(),
                                    contactList.get(parent_postion).getName(), contactList.get(parent_postion).getPhoneNumber(), contactList.get(parent_postion).getStatus().getValue());
                         //   swipeholder.edit_btn.setEnabled(false);

                            Intent pushIntent = new Intent(getActivity(), NotificationService.class);
                            getActivity().startService(pushIntent);
                        }


                        adapter.notifyDataSetChanged();
                        notifyDataSetChanged();

                    }
                });



                convertView.setTag(swipeholder);
            } else {
                swipeholder = (SwipeViewHolder) convertView.getTag();
            }




            Contact contact = contactList.get(parent_postion);

            swipeholder.tvTitle.setText(contact.getName());

            String statusText = contact.getStatusText();
            if (statusText != null) {
                swipeholder.tvStatusText.setText(contact.getStatusText());
            } else {
                swipeholder.tvStatusText.setText("");
            }

            Status status = contact.getStatus();

            swipeholder.vStatus.setVisibility(View.VISIBLE);
            swipeholder.tvOnPhone.setVisibility(View.GONE);

            if (status != null) {
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
            }


            Log.i(TAG, "status " + contact.getStatus());


            List<String> checkPhoneList = User.getInstance(getActivity()).getCheckPhoneNumberList();

            if (checkPhoneList.contains(contact.getPhoneNumber())) {

                if (contact.getStatus() == null) {
                    swipeholder.edit_btn.setText(getString(R.string.add_contact));
                } else {

                    boolean contains = false;

                    for (Notification notification : nList) {
                        if (notification.getPhoneNumber().contentEquals(contact.getPhoneNumber())) {
                            contains = true;
                            break;
                        }
                    }

                    if (contains) {
                        swipeholder.edit_btn.setText(getString(R.string.notification_already_added));
                        swipeholder.edit_btn.setEnabled(false);
                    } else {
                        swipeholder.edit_btn.setText(getString(R.string.set_notification));
                    }

                }

            } else {
                swipeholder.edit_btn.setText(getString(R.string.invite));
            }


            return convertView;
        }
    }




    private BroadcastReceiver statusUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "statusUpdateBroadcastReceiver");
            adapter.refreshOnContactCountChange();
        }
    };



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
        pi.setName("EndTime");
        pi.setValue("2000-01-01T00:00:00");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("StartTime");
        pi.setValue("2000-01-01T00:00:00");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Text");
        pi.setValue(User.getInstance(getActivity()).getStatusText());
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }
    private SoapObject getAddContactsParams(String number) {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.ADD_CONTACT);

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
            return;
        }


        if (methodName.contentEquals(SendMessageTask.ADD_CONTACT)) {

            try {

                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    ((MainActivity) getActivity()).refreshStatuses();

                }

            } catch (NullPointerException ne) {
                ne.printStackTrace();
            }


        } else if (methodName.contentEquals(SendMessageTask.UPDATE_STATUS)) {

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



}
