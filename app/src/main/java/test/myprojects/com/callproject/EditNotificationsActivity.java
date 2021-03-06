package test.myprojects.com.callproject;


import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.DataBase;
import test.myprojects.com.callproject.model.Notification;

public class EditNotificationsActivity extends ListActivity {

    private List<String> names = new ArrayList<String>();
    private List<Notification> nList;

    @OnClick(R.id.bDone)
    public void doneClicked(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notifications);
        ButterKnife.bind(this);

        nList = DataBase.getNotificationListFromDb(DataBase.
                getInstance(this).getWritableDatabase());

        for (Notification notification : nList){
            names.add(notification.getName());
        }

        getListView().setChoiceMode(getListView().CHOICE_MODE_MULTIPLE);
        //--	text filtering
        getListView().setTextFilterEnabled(true);


        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_checked, names));

        for (int i =0 ; i< names.size(); i++){
            getListView().setItemChecked(i, true);
        }
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {


        DataBase.removeNotificationFromDb(DataBase.getInstance(this).
                getWritableDatabase(), nList.get(position));

        names.remove(position);


        ((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();

        for (int i =0 ; i< names.size(); i++){
            getListView().setItemChecked(i, true);
        }


        //NotificationService will stop automatically
        // when he sees that notificationList from database is empty,
        // see in NotificationService class
    }

}
