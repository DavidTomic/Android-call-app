package test.myprojects.com.callproject.tabFragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import test.myprojects.com.callproject.ContactDetailActivity;
import test.myprojects.com.callproject.R;
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

    public ContactsFragment() {
        // Required empty public constructor
    }

    @OnClick(R.id.ibAddContact)
    public void addContact(){
        Log.i(TAG, "here");

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        getActivity().startActivity(intent);
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

            final PullToRefreshStickyList stlist = (PullToRefreshStickyList)rootView.findViewById(R.id.stickSwipeList);
            IndexView indexView = (IndexView)rootView.findViewById(R.id.indexView);

            StickyAdapter adapter = new StickyAdapter(getActivity());
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
            indexView.init(stlist,null);

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
            {
                parent.removeView(rootView);
            }

        }

        return  rootView;
    }

    public class StickyAdapter extends BaseAdapter implements StickyListHeadersAdapter {


        private LayoutInflater inflater;

        public StickyAdapter(Context context){
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
            if(convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.contact_list_item, parent , false);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.tvStatusText = (TextView) convertView.findViewById(R.id.tvStatusText);
                holder.vStatus = (View) convertView.findViewById(R.id.vStatus);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contact contact = contactList.get(position);

            holder.tvTitle.setText(contact.getName());

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            HeaderViewHolder holder;
            if(convertView == null){
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.header, parent , false);
                holder.text = (TextView) convertView.findViewById(R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            //set header text first char
            String headerText = ""+((Contact)contactList.get(position)).getName().subSequence(0, 1).charAt(0);
            holder.text.setText(headerText);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            //return the first character of the country as ID because this is what headers are based upon
            return ((Contact)contactList.get(position)).getName().subSequence(0, 1).charAt(0);
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView tvTitle;
            TextView tvStatusText;
            View vStatus;
        }

    }
}
