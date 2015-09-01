package test.myprojects.com.callproject.tabFragments;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
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

import test.myprojects.com.callproject.R;
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

    @Override
    public void onResume() {
        super.onResume();
        refreshFavorits();
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
            SwipeMenuListView swipeMenuListView = (SwipeMenuListView) rootView.
                    findViewById(R.id.swipeMenuListView);
            favoritAdapter = new FavoritAdapter(getActivity());
            swipeMenuListView.setAdapter(favoritAdapter);


            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem openItem = new SwipeMenuItem(
                            getActivity().getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                            0xCE)));
                    // set item width
                    openItem.setWidth(120);
                    // set item title
                    openItem.setTitle("Open");
                    // set item title fontsize
                    openItem.setTitleSize(18);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);

                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getActivity().getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x3F, 0x25)));
                    // set item width
                    deleteItem.setWidth(120);
                    // set a icon
                    deleteItem.setIcon(R.mipmap.ic_launcher);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            };

            // set creator
            swipeMenuListView.setMenuCreator(creator);

            swipeMenuListView.setMinimumHeight(250);

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
            return rootView;
        }

        return rootView;
    }

    public class FavoritAdapter extends BaseAdapter {

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
                holder.image = (ImageView) convertView.findViewById(R.id.ivProfile);
                holder.infoButton = (ImageButton) convertView.findViewById(R.id.ibInfo);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contact contact = favoritList.get(position);

            holder.name.setText(contact.getName());


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


}
