package test.myprojects.com.callproject.Util;

import android.content.Context;
import android.content.SharedPreferences;

import test.myprojects.com.callproject.model.User;

/**
 * Created by dtomic on 24/08/15.
 */
public class Prefs {

    private static final String TAG = "Prefs";
    public static final String PREFS_FILE = "my_prefs.xml";

    public static final String PREFS_USER_PHONE_NUMBER= "user_phone_number";
    public static final String PREFS_USER_NAME= "user_name";
    public static final String PREFS_USER_EMAIL= "user_email";
    public static final String PREFS_USER_PASSWORD= "user_password";
    public static final String PREFS_USER_LANGUAGE= "user_language";
    public static final String PREFS_USER_DEFAULT_TEXT= "user_default_text";
    public static final String PREFS_USER_LOGED_IN= "user_loged_in";

    public static void setUserData(Context context, User user) {
        // TODO Auto-generated method stub
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putString(PREFS_USER_PHONE_NUMBER, user.getPhoneNumber());
        editor.putString(PREFS_USER_NAME, user.getName());
        editor.putString(PREFS_USER_EMAIL, user.getEmail());
        editor.putString(PREFS_USER_PASSWORD, user.getPassword());
        editor.putString(PREFS_USER_LANGUAGE, user.getLanguage());
        editor.putString(PREFS_USER_DEFAULT_TEXT, user.getDefaultText());
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
        user.setLanguage(prefs.getString(PREFS_USER_LANGUAGE, ""));
        user.setDefaultText(prefs.getString(PREFS_USER_DEFAULT_TEXT, ""));
        user.setLogedIn(prefs.getBoolean(PREFS_USER_LOGED_IN, false));
    }
}
