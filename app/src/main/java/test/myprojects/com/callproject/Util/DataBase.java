package test.myprojects.com.callproject.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import test.myprojects.com.callproject.model.Contact;
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
        super(context, DB_NAME, null, 3);

    }


    private static final String NOTIFICATION_TABLE = "notification_table";
    private static final String NOTIFICATION_ID = "notification_id";
    private static final String NAME = "name";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String STATUS = "status";

    private static final String CONTACT_TABLE = "contact_table";


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + NOTIFICATION_TABLE + " (" + NOTIFICATION_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME + " TEXT, " + PHONE_NUMBER + " TEXT, " + STATUS
                + " INTEGER);");

        db.execSQL("CREATE TABLE " + CONTACT_TABLE + " (" + PHONE_NUMBER + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACT_TABLE);
        Log.i(TAG, "onUpgrade");
        onCreate(db);
    }


    //notifications
    public static Notification addNotificationNumberToDb(SQLiteDatabase db, String name, String phoneNumber, int status) {

        Notification notification = new Notification();
        notification.setName(name);
        notification.setPhoneNumber(phoneNumber);
        notification.setStatus(Status.values()[status]);

        ContentValues value = new ContentValues(3);
        value.put(NAME, name);
        value.put(PHONE_NUMBER, phoneNumber);
        value.put(STATUS, status);

        notification.setNotificationID((int) db.insert(NOTIFICATION_TABLE, null, value));

        return notification;
    }

    public static void removeNotificationFromDb(SQLiteDatabase db, Notification notification) {

        db.delete(NOTIFICATION_TABLE, NOTIFICATION_ID + "='" + notification.getNotificationID() + "'", null);

    }

    public static List<Notification> getNotificationListFromDb(SQLiteDatabase db) {

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

    public static Notification getNotificationWithPhoneNumber(SQLiteDatabase db, String phoneNumber) {

        Cursor cursor = db.rawQuery("SELECT *" + " FROM "
                + NOTIFICATION_TABLE + " WHERE " + PHONE_NUMBER + " = ?", new String[] {phoneNumber});

        if (cursor.getCount() > 0) {
            cursor.moveToPosition(0);

            Notification notification = new Notification();

            notification.setNotificationID(cursor.getInt(cursor.getColumnIndex(NOTIFICATION_ID)));
            notification.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            notification.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PHONE_NUMBER)));
            notification.setStatus(Status.values()[cursor.getInt(cursor.getColumnIndex(STATUS))]);

            return notification;
        }

        return null;
    }


    //contacts
    public static void addContactsPhoneNumbersToDb(SQLiteDatabase db, List<Contact> list) {

        db.delete(CONTACT_TABLE, null, null);

        final String INSERT = "insert into "
                + CONTACT_TABLE+ " (" + PHONE_NUMBER + ") values (?)";

        final SQLiteStatement statement = db.compileStatement(INSERT);
        db.beginTransaction();
        try {
            for(Contact c : list){
                statement.clearBindings();
                statement.bindString(1, c.getPhoneNumber());
                // rest of bindings
                statement.execute(); //or executeInsert() if id is needed
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    public static List<String> getContactsPhoneNumberListFromDb(SQLiteDatabase db) {

        Cursor cursor = db.rawQuery("SELECT *" + " FROM "
                + CONTACT_TABLE, null);

        List<String> list = new ArrayList<>();

        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                list.add(cursor.getString(cursor.getColumnIndex(PHONE_NUMBER)));
            }
        }

        cursor.close();
        return list;
    }
}
