package test.myprojects.com.callproject.Util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;

/**
 * Created by dtomic on 24/08/15.
 */
public class Prefs {

    private static final String TAG = "Prefs";
    public static final String PREFS_FILE = "my_prefs.xml";

    public static final String PREFS_USER_PHONE_NUMBER = "user_phone_number";
    public static final String PREFS_USER_NAME = "user_name";
    public static final String PREFS_USER_EMAIL = "user_email";
    public static final String PREFS_USER_PASSWORD = "user_password";
    public static final String PREFS_USER_LANGUAGE = "user_language_v2";
    public static final String PREFS_USER_STATUS_TEXT = "user_status_text";
    public static final String PREFS_USER_STATUS = "user_status";
    public static final String PREFS_USER_LOGED_IN = "user_loged_in";

    public static final String PREFS_LAST_CALL_TIME = "last_call_time";

    public static void setUserData(Context context, User user) {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putString(PREFS_USER_PHONE_NUMBER, user.getPhoneNumber());
        editor.putString(PREFS_USER_NAME, user.getName());
        editor.putString(PREFS_USER_EMAIL, user.getEmail());
        editor.putString(PREFS_USER_PASSWORD, user.getPassword());
        editor.putInt(PREFS_USER_LANGUAGE, user.getLanguage().getValue());
        editor.putString(PREFS_USER_STATUS_TEXT, user.getStatusText());
        editor.putInt(PREFS_USER_STATUS, user.getStatus().getValue());
        editor.putBoolean(PREFS_USER_LOGED_IN, user.isLogedIn());
        editor.commit();
    }

    public static void loadUserData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_FILE, 0);
        User user = User.getInstance(context);

        user.setPhoneNumber(prefs.getString(PREFS_USER_PHONE_NUMBER, ""));
        user.setName(prefs.getString(PREFS_USER_NAME, ""));
        user.setEmail(prefs.getString(PREFS_USER_EMAIL, ""));
        user.setPassword(prefs.getString(PREFS_USER_PASSWORD, ""));
        user.setLanguage(Language.values()[prefs.getInt(PREFS_USER_LANGUAGE, 1)]);
        user.setStatusText(prefs.getString(PREFS_USER_STATUS_TEXT, ""));
        user.setStatus(Status.values()[prefs.getInt(PREFS_USER_STATUS, 1)]);
        user.setLogedIn(prefs.getBoolean(PREFS_USER_LOGED_IN, false));
    }

    public static void setLastCallTime(Context context, long time) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putLong(PREFS_LAST_CALL_TIME, time);
        editor.commit();
    }

    public static long getLastCallTime(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_FILE, 0);

        return prefs.getLong(PREFS_LAST_CALL_TIME, 0);
    }

    public static boolean saveDefaultTexts(Context context, List<String> list)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putInt("DefaultText_size", list.size()); /* sKey is an array */

        for(int i=0;i<list.size();i++)
        {
            editor.remove("DefaultText_" + i);
            editor.putString("DefaultText_" + i, list.get(i));
        }

        return editor.commit();
    }

    public static List<String> getDefaultTexts(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_FILE, 0);

        List<String> list = new ArrayList<String>();
        int size = prefs.getInt("DefaultText_size", 0);

        for(int i=0;i<size;i++)
        {
            list.add(prefs.getString("DefaultText_" + i, null));
        }

        return list;
    }
}
