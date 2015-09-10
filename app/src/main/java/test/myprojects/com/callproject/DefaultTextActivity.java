package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.List;

import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.task.SendMessageTask;

public class DefaultTextActivity extends Activity {

    private static final String TAG = "DefaultTextActivity";
    private TextAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_text);

        (findViewById(R.id.bAdd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DefaultTextActivity.this, DefaultTextDetailActivity.class);
                intent.putExtra("addNewText" , true);
                startActivity(intent);
            }
        });

        SwipeMenuListView swipeMenuListView = (SwipeMenuListView)
                findViewById(R.id.swipeMenuListView);

        adapter = new TextAdapter(this);
        swipeMenuListView.setAdapter(adapter);


        swipeMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick");

                List<String> list = Prefs.getDefaultTexts(DefaultTextActivity.this);

                Intent intent = new Intent(DefaultTextActivity.this, DefaultTextDetailActivity.class);
                intent.putExtra("text", list.get(position));
                startActivity(intent);
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
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
                List<String> list = Prefs.getDefaultTexts(DefaultTextActivity.this);
                list.remove(position);

                Prefs.saveDefaultTexts(DefaultTextActivity.this, list);
                adapter.refreshList(DefaultTextActivity.this);

                SendMessageTask task = new SendMessageTask(null, getDefaultTextParams());
                task.execute();
                return false;
            }
        });
    }
    private class TextAdapter extends BaseAdapter {

        private List<String> defaultTextList;
        private LayoutInflater inflater;

        public void refreshList(Context context){
            defaultTextList = Prefs.getDefaultTexts(context);
            notifyDataSetChanged();

        }

        public TextAdapter(Context context){
            inflater = LayoutInflater.from(context);
            defaultTextList = Prefs.getDefaultTexts(context);
        }

        @Override
        public int getCount() {
            return defaultTextList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.default_text_list_item, parent, false);
                textView = (TextView) convertView.findViewById(R.id.tvName);
                convertView.setTag(textView);
            } else {
                textView = (TextView) convertView.getTag();
            }

            textView.setText(defaultTextList.get(position));

            return convertView;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        adapter.refreshList(this);
    }

    private SoapObject getDefaultTextParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.SET_DEFAULT_TEXT);

        PropertyInfo pi = new PropertyInfo();
        pi.setName("Phonenumber");
        pi.setValue(User.getInstance(this).getPhoneNumber());
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("password");
        pi.setValue(User.getInstance(this).getPassword());
        pi.setType(String.class);
        request.addProperty(pi);

        SoapObject piDefaultTextSoapObject = new SoapObject(SendMessageTask.NAMESPACE, "DefaultText");

        List<String> list = Prefs.getDefaultTexts(this);

        for (String text : list) {
            PropertyInfo piDefaultText = new PropertyInfo();
            piDefaultText.setName("string");
            piDefaultText.setValue(text);
            piDefaultText.setType(String.class);
            piDefaultTextSoapObject.addProperty(piDefaultText);
        }

        request.addProperty("DefaultText", piDefaultTextSoapObject);

        return request;
    }
}
