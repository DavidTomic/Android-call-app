package test.myprojects.com.callproject.tabFragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.view.IndexView;
import test.myprojects.com.callproject.view.PullToRefreshStickyList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View rootView;
    private List<Contact> contactList = new ArrayList<Contact>();

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        if (rootView == null) {

            //       Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

            // Initialise your layout here

            final PullToRefreshStickyList stlist = (PullToRefreshStickyList)rootView.findViewById(R.id.stickSwipeList);
            IndexView indexView = (IndexView)rootView.findViewById(R.id.indexView);

            StickyAdapter adapter = new StickyAdapter(getActivity());
            stlist.getRefreshableView().setAdapter(adapter);

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
                convertView = inflater.inflate(R.layout.contacts_list_item, parent , false);
                holder.swipelist = (SwipeListView) convertView.findViewById(R.id.example_lv_list);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.swipelist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            List<SwipeItem> dataItem = new ArrayList<SwipeItem>();
            SwipeItem currentItem = new SwipeItem();
            currentItem.setLabel(((Contact) contactList.get(position)).getName());
            dataItem.add(currentItem);
            SwipeAdapter sadapter = new SwipeAdapter(getActivity() , dataItem , position);

            holder.swipelist.setAdapter(sadapter);
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
            //TextView text;
            SwipeListView swipelist;
        }

    }

    public class SwipeItem {
        private String label;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public class SwipeViewHolder {
        TextView label;
        Button edit_btn;
        Button delete_btn;
    }

    public class SwipeAdapter extends BaseAdapter {
        List<SwipeItem> data;
        Context context;
        int parent_postion;

        public SwipeAdapter(Context context, List<SwipeItem> data , int parent_postion){
            this.context = context;
            this.data = data;
            this.parent_postion = parent_postion;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int pos, View cview, ViewGroup viewGroup) {
            SwipeItem curitem = this.data.get(pos);
            SwipeViewHolder swipeholder;
            if(cview == null){
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                cview = li.inflate(R.layout.contacts_package_row, viewGroup , false);
                swipeholder = new SwipeViewHolder();
                swipeholder.label = (TextView)cview.findViewById(R.id.front_label);
                swipeholder.label.setText(((Contact)contactList.get(parent_postion)).getName());
                swipeholder.edit_btn = (Button) cview.findViewById(R.id.btn_edit);
                swipeholder.delete_btn = (Button) cview.findViewById(R.id.btn_delete);

	            /* item click action*/

                swipeholder.label.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), "Item Click " + ((Contact)contactList.get(parent_postion)).getName(), Toast.LENGTH_SHORT).show();
                    }
                });

                /* button action */

                swipeholder.edit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity() , "Edit "+((Contact)contactList.get(parent_postion)).getName() , Toast.LENGTH_SHORT).show();
                    }
                });

                swipeholder.delete_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity() , "Delete "+((Contact)contactList.get(parent_postion)).getName() , Toast.LENGTH_SHORT).show();
                    }
                });

                cview.setTag(swipeholder);
            } else {
                swipeholder = (SwipeViewHolder)cview.getTag();
            }

            return cview;
        }
    }

}
