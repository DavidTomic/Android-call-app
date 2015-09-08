package test.myprojects.com.callproject.tabFragments;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

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
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.task.SendMessageTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritFragment extends Fragment {

    private static final String TAG = "FavoritFragment";
    private View rootView;
    private List<Contact> favoritList = new ArrayList<Contact>();
    private FavoritAdapter favoritAdapter;
    private SwipeMenuListView swipeMenuListView;
    private boolean editEnabled;

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


    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(statusUpdateBroadcastReceiver,
                new IntentFilter(MainActivity.BROADCAST_STATUS_UPDATE_ACTION));
        refreshFavorits();
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

    private void refreshFavorits() {
        favoritList.clear();
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
                    removeFromFavorites(favoritList.get(position));
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
                holder.vStatusRed = (View) convertView.findViewById(R.id.vStatusRed);
                holder.vStatusYellow = (View) convertView.findViewById(R.id.vStatusYellow);
                holder.vStatusGreen = (View) convertView.findViewById(R.id.vStatusGreen);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Contact contact = favoritList.get(position);

            holder.name.setText(contact.getName());

            if (contact.getImage() != null) {
                Log.i(TAG, "name " + contact.getName());
                Uri imageUri = Uri.parse(contact.getImage());
                holder.ivProfile.setImageURI(imageUri);
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

            Log.i(TAG, "statusUpdateBroadcastReceiver");
            refreshFavorits();
        }
    };

}
