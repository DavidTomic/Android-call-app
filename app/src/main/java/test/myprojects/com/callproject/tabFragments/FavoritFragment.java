package test.myprojects.com.callproject.tabFragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.SetStatusActivity;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritFragment extends Fragment {


    private static final String TAG = "FavoritFragment";
    private View rootView;
    private List<Contact> favoritList = new ArrayList<Contact>();
    private FavoritAdapter favoritAdapter;

    public FavoritFragment() {
        // Required empty public constructor
    }

    @Bind(R.id.tvStatusText) TextView tvStatusText;
    @Bind(R.id.vStatusColor) View vStatusColor;

    @OnClick(R.id.llStatus)
    public void setStatus(){
        startActivity(new Intent(getActivity(), SetStatusActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFavorits();
        refreshMyStatusUI();
    }

    private void refreshMyStatusUI() {

        String statusText = User.getInstance(getActivity()).getStatusText();

        if (statusText==null || statusText.length()<1){
            tvStatusText.setVisibility(View.GONE);
        }else {
            tvStatusText.setText(statusText);
            tvStatusText.setVisibility(View.VISIBLE);
        }

        vStatusColor.setBackgroundDrawable(getResources().
                getDrawable(User.getInstance(getActivity()).getStatusColor()));
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
            SwipeMenuListView swipeMenuListView = (SwipeMenuListView) rootView.
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
                holder.status = (TextView) convertView.findViewById(R.id.tvStatus);
                holder.tvProfile = (TextView) convertView.findViewById(R.id.tvProfile);
                holder.ivProfile = (CircleImageView) convertView.findViewById(R.id.ivProfile);
                holder.infoButton = (ImageButton) convertView.findViewById(R.id.ibInfo);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Contact contact = favoritList.get(position);

            holder.name.setText(contact.getName());

            if (contact.getImage()!=null){
                Log.i(TAG, "name " + contact.getName());
                Uri imageUri = Uri.parse(contact.getImage());
                holder.ivProfile.setImageURI(imageUri);
                holder.ivProfile.setVisibility(View.VISIBLE);
                holder.tvProfile.setVisibility(View.INVISIBLE);
            }else {
                holder.tvProfile.setText(contact.getName().substring(0,1).toUpperCase());

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

            return convertView;
        }

        class ViewHolder {
            //TextView text;
            TextView name;
            TextView status;
            ImageButton infoButton;
            CircleImageView ivProfile;
            TextView tvProfile;
        }
    }

    private void removeFromFavorites(Contact contact){
        User.getInstance(getActivity()).getContactWithId(contact.getRecordId()).setFavorit(false);
        ContentValues v = new ContentValues();
        v.put(ContactsContract.Contacts.STARRED, 0);
        getActivity().getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, v,
                ContactsContract.Contacts._ID + "=?", new String[]{contact.getRecordId() + ""});

        refreshFavorits();
    }
}
