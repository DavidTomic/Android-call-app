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
import android.widget.LinearLayout;
import android.widget.TextView;


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
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.view.IndexView;
import test.myprojects.com.callproject.view.PullToRefreshStickyList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    private View rootView;
    private List<Contact> contactList = new ArrayList<Contact>();
    private StickyAdapter adapter;

//    @Bind(R.id.tvStatusText)
//    TextView tvStatusText;
//    @Bind(R.id.vStatusColor)
//    View vStatusColor;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @OnClick(R.id.ibAddContact)
    public void addContact() {
        Log.i(TAG, "here");

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        getActivity().startActivity(intent);
    }

//    @OnClick(R.id.llStatus)
//    public void setStatus() {
//        startActivity(new Intent(getActivity(), SetStatusActivity.class));
//    }

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

        getActivity().registerReceiver(statusUpdateBroadcastReceiver,
                new IntentFilter(MainActivity.BROADCAST_STATUS_APDATE_ACTION));

        refreshMyStatusUI();

        if (User.getInstance(getActivity()).isContactEdited()) {
            User.getInstance(getActivity()).setContactEdited(false);
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
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contact contact = contactList.get(position);

            holder.tvTitle.setText(contact.getName());

            String statusText = contact.getStatusText();
            if (statusText!=null){
                holder.tvStatusText.setText(contact.getStatusText());
            }else {
                holder.tvStatusText.setText("");
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
            adapter.notifyDataSetChanged();
        }
    };

}
