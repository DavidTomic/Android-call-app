package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


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
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;
import test.myprojects.com.callproject.view.IndexView;
import test.myprojects.com.callproject.view.PullToRefreshStickyList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment implements MessageInterface {

    private static final String TAG = "ContactsFragment";
    private View rootView;
    private List<Contact> contactList = new ArrayList<Contact>();
    private StickyAdapter adapter;

    private boolean refreshContactsFromPhoneBook;

    @Bind(R.id.ibRefresh) ImageButton ibRefresh;
    @Bind(R.id.pbProgressBar)
    ProgressBar progressBar;

    @Bind(R.id.tvPhoneNumber) TextView tvPhoneNumber;

    private int currentStatus;

    @Bind(R.id.bStatusRed) ImageView bStatusRed;
    @Bind(R.id.bStatusYellow)
    ImageView bStatusYellow;
    @Bind(R.id.bStatusGreen) ImageView bStatusGreen;

    @OnClick(R.id.bStatusRed)
    public void bStatusRedClicked(){

        Log.i(TAG, "clicked red");

        bStatusRed.setSelected(true);
        bStatusYellow.setSelected(false);
        bStatusGreen.setSelected(false);

        currentStatus = 0;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }
    @OnClick(R.id.bStatusYellow)
    public void bStatusYellowClicked(){
        bStatusRed.setSelected(false);
        bStatusYellow.setSelected(true);
        bStatusGreen.setSelected(false);

        currentStatus = 2;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }
    @OnClick(R.id.bStatusGreen)
    public void bStatusGreenClicked(){
        bStatusRed.setSelected(false);
        bStatusYellow.setSelected(false);
        bStatusGreen.setSelected(true);

        currentStatus = 1;
        new SendMessageTask(this, getUpdateStatusParams(currentStatus)).execute();
    }


    public ContactsFragment() {
        // Required empty public constructor
    }


    @OnClick(R.id.ibAddContact)
    public void addContact() {
        Log.i(TAG, "here");

        refreshContactsFromPhoneBook = true;

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.ibRefresh)
    public void refreshClicked(){
        ibRefresh.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).refreshStatuses();
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

            stlist.StickySwipe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "Item " + contactList.get(position).getName());

                    Intent contactDetailIntent = new Intent(getActivity(), ContactDetailActivity.class);
                    contactDetailIntent.putExtra("contactId", contactList.get(position).getRecordId());
                    getActivity().startActivity(contactDetailIntent);

                }
            });

            /** indexable listview */
            indexView.init(stlist);

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

            ibRefresh.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            Prefs.setLastCallTime(getActivity(), 0);
            ((MainActivity)getActivity()).refreshStatuses();

        }

        if (refreshContactsFromPhoneBook){
            refreshContactsFromPhoneBook=false;
            User.getInstance(getActivity()).loadContactsToList(getActivity());
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

        ibRefresh.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }


    public class StickyAdapter extends BaseAdapter implements StickyListHeadersAdapter {


        private LayoutInflater inflater;

        public StickyAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            contactList = User.getInstance(context).getContactList();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
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
                convertView = inflater.inflate(R.layout.contact_list_item, parent, false);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.tvStatusText = (TextView) convertView.findViewById(R.id.tvStatusText);
                holder.vStatus = (LinearLayout) convertView.findViewById(R.id.vStatus);
                holder.vStatusRed = (View) convertView.findViewById(R.id.vStatusRed);
                holder.vStatusYellow = (View) convertView.findViewById(R.id.vStatusYellow);
                holder.vStatusGreen = (View) convertView.findViewById(R.id.vStatusGreen);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contact contact = contactList.get(position);

            holder.tvTitle.setText(contact.getName());

            String statusText = contact.getStatusText();
            if (statusText != null) {
                holder.tvStatusText.setText(contact.getStatusText());
            } else {
                holder.tvStatusText.setText("");
            }

            Status status = contact.getStatus();

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
                        holder.vStatusRed.setSelected(false);
                        holder.vStatusYellow.setSelected(false);
                        holder.vStatusGreen.setSelected(false);
                        break;
                }
            } else {
                holder.vStatusRed.setSelected(false);
                holder.vStatusYellow.setSelected(false);
                holder.vStatusGreen.setSelected(false);
            }


            Log.i(TAG, "status " + contact.getStatus());

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
            String headerText = "" + ((Contact) contactList.get(position)).getName().subSequence(0, 1).charAt(0);
            holder.text.setText(headerText);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            //return the first character of the country as ID because this is what headers are based upon
            return ((Contact) contactList.get(position)).getName().subSequence(0, 1).charAt(0);
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView tvTitle;
            TextView tvStatusText;
            LinearLayout vStatus;
            View vStatusRed;
            View vStatusYellow;
            View vStatusGreen;
        }

    }

    private BroadcastReceiver statusUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "statusUpdateBroadcastReceiver");
            ibRefresh.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
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

        String endTime = User.getInstance(getActivity()).getEndTime();

        if (endTime ==null || endTime.length() == 0){
            endTime = "2000-01-01T00:00:00";
        }

        pi.setValue(endTime);
        pi.setType(String.class);
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
            Toast.makeText(getActivity(), getString(R.string.status_not_updated),
                    Toast.LENGTH_SHORT).show();
            return;
        }


        try {

            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {

                User.getInstance(getActivity()).setStatus(Status.values()[currentStatus]);
                Prefs.setUserData(getActivity(), User.getInstance(getActivity()));

                Toast.makeText(getActivity(), getString(R.string.status_updated),
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), getString(R.string.status_not_updated),
                        Toast.LENGTH_SHORT).show();
            }


        } catch (NullPointerException ne) {
            ne.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.status_not_updated),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshStatusUI(){
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
            }
        }
    }

}
