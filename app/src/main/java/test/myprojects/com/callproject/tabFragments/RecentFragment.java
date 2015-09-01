package test.myprojects.com.callproject.tabFragments;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment {


    private static final String TAG = "RecentFragment";
    private View rootView;
    private List<Contact> recentList = new ArrayList<Contact>();

    @Bind(R.id.swipeMenuListView) SwipeMenuListView swipeMenuListView;

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
                convertView = inflater.inflate(R.layout.favorit_list_item, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.tvName);
                holder.status = (TextView) convertView.findViewById(R.id.tvStatus);
                holder.image = (ImageView) convertView.findViewById(R.id.ivProfile);
                holder.infoButton = (ImageButton) convertView.findViewById(R.id.ibInfo);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contact contact = recentList.get(position);

            String name = contact.getName();
            if (name == null) name = contact.getPhoneNumber();
            holder.name.setText(name);


            return convertView;
        }

        class ViewHolder {
            //TextView text;
            TextView name;
            TextView status;
            ImageButton infoButton;
            ImageView image;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getContactsFromLog();
    }

    private void getContactsFromLog(){
        Uri queryUri = android.provider.CallLog.Calls.CONTENT_URI;

        String[] projection = new String[] {
                CallLog.Calls._ID,
                CallLog.Calls.TYPE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE};

        String sortOrder = CallLog.Calls.CACHED_NAME + " ASC";

        Cursor cursor = getActivity().getContentResolver().query(queryUri, projection, null, null, sortOrder);

//        Log.i(TAG, "COUNT: " + cursor.getCount());

        List<Contact> cList  = User.getInstance(getActivity()).getContactList();

        recentList.clear();
        while (cursor.moveToNext()) {


//            Log.i(TAG, "TYPE: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
            Log.i(TAG, "CACHED_NAME: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
//            Log.i(TAG, "NUMBER: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
//            Log.i(TAG, "ID: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));
//            Log.i(TAG, "ID: " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));

            Contact contact = new Contact();
            contact.setPhoneNumber(cursor.getString(cursor
                    .getColumnIndex(CallLog.Calls.NUMBER)));
            if (contact.getPhoneNumber()==null || contact.getPhoneNumber().length()<5) continue;

            contact.setName(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
            contact.setDuration(Float.parseFloat(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))));
            contact.setDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))));

            int cId = User.getContactIDFromNumber(contact.getPhoneNumber(), getActivity());
            if (cId!=-1) contact.setRecordId(cId);
          //  Log.i(TAG, "cID " + cId);

            switch (Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)))){
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
}
