package test.myprojects.com.callproject.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import test.myprojects.com.callproject.model.Notification;
import test.myprojects.com.callproject.model.Status;

/**
 * Created by dtomic on 09/09/15.
 */
public class DataBase extends SQLiteOpenHelper {

    private static final String TAG = "DataBase";
    private static DataBase instance;
    private static final String DB_NAME = "when2call_db";


    private static synchronized void initInstance(Context context) {
        if (instance == null)
            instance = new DataBase(context);

    }

    public static synchronized DataBase getInstance(Context context) {
        initInstance(context);
        return instance;
    }

    private DataBase(Context context) {
        super(context, DB_NAME, null, 2);

    }


    private static final String NOTIFICATION_TABLE = "notification_table";
    private static final String NOTIFICATION_ID = "notification_id";
    private static final String NAME = "name";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String STATUS = "status";


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + NOTIFICATION_TABLE + " (" + NOTIFICATION_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME + " TEXT, " + PHONE_NUMBER + " TEXT, " + STATUS
                + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_TABLE);
        Log.i(TAG, "onUpgrade");
        onCreate(db);
    }

    public static void addNotificationNumberToDb(SQLiteDatabase db, String name, String phoneNumber, int status) {

        ContentValues value = new ContentValues(3);
        value.put(NAME, name);
        value.put(PHONE_NUMBER, phoneNumber);
        value.put(STATUS, status);

        db.insert(NOTIFICATION_TABLE, null, value);

    }

    public static void removeNotificationNumberToDb(SQLiteDatabase db, Notification notification) {

        db.delete(NOTIFICATION_TABLE, NOTIFICATION_ID + "='" + notification.getNotificationID() + "'", null);

    }

    public static List<Notification> getNotificationNumberListFromDb(SQLiteDatabase db) {

        Cursor cursor = db.rawQuery("SELECT *" + " FROM "
                + NOTIFICATION_TABLE, null);

        List<Notification> list = new ArrayList<>();

        if (cursor.getCount() > 0) {
            Notification notification;
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                notification = new Notification();

                notification.setNotificationID(cursor.getInt(cursor.getColumnIndex(NOTIFICATION_ID)));
                notification.setName(cursor.getString(cursor.getColumnIndex(NAME)));
                notification.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PHONE_NUMBER)));
                notification.setStatus(Status.values()[cursor.getInt(cursor.getColumnIndex(STATUS))]);

                list.add(notification);

            }
        }

        cursor.close();
        return list;
    }


}
