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
    private static final String PREFS_FILE = "my_prefs.xml";

    private static final String PREFS_USER_PHONE_NUMBER = "user_phone_number";
    private static final String PREFS_USER_NAME = "user_name";
    private static final String PREFS_USER_EMAIL = "user_email";
    private static final String PREFS_USER_PASSWORD = "user_password";
    private static final String PREFS_USER_LANGUAGE = "user_language_v2";
    private static final String PREFS_USER_STATUS_TEXT = "user_status_text";
    private static final String PREFS_USER_STATUS = "user_status";
    private static final String PREFS_USER_SMS_INVITE_TEXT = "user_sms_invite_text";
    private static final String PREFS_USER_LOGED_IN = "user_loged_in";
    private static final String PREFS_I_AM_LIVE_SECONS = "i_am_live_seconds";
    private static final String PREFS_REQUEST_STATUS_INFO_SECONDS = "request_status_info_seconds";
    private static final String PREFS_USER_STATUS_START_TIME = "status_start_time_2";
    private static final String PREFS_USER_STATUS_END_TIME = "status_end_time_2";
    private static final String PREFS_USER_TIMER_STATUS = "status_timer";
    private static final String PREFS_USER_TIMER_STATUS_TEXT = "status_timer_text";

    public static final String PREFS_LAST_CALL_TIME = "last_call_time_v2";

    public static final String PREFS_VOICEMAIL_NUMBER = "voice_mail_number";
    public static final String PREFS_COUNTRY_CODE = "country_code";

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
        editor.putString(PREFS_USER_SMS_INVITE_TEXT, user.getSmsInviteText());
        editor.putInt(PREFS_USER_STATUS, user.getStatus().getValue());
        editor.putBoolean(PREFS_USER_LOGED_IN, user.isLogedIn());
        editor.putInt(PREFS_I_AM_LIVE_SECONS, user.getiAmLiveSeconds());
        editor.putInt(PREFS_REQUEST_STATUS_INFO_SECONDS, user.getRequestStatusInfoSeconds());
        editor.putLong(PREFS_USER_STATUS_START_TIME, user.getStatusStartTime());
        editor.putLong(PREFS_USER_STATUS_END_TIME, user.getStatusEndTime());
        editor.putInt(PREFS_USER_TIMER_STATUS, user.getTimerStatus().getValue());
        editor.putString(PREFS_USER_TIMER_STATUS_TEXT, user.getTimerStatusText());
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
        user.setSmsInviteText(prefs.getString(PREFS_USER_SMS_INVITE_TEXT, ""));
        user.setStatus(Status.values()[prefs.getInt(PREFS_USER_STATUS, 1)]);
        user.setLogedIn(prefs.getBoolean(PREFS_USER_LOGED_IN, false));
        user.setiAmLiveSeconds(prefs.getInt(PREFS_I_AM_LIVE_SECONS, 240));
        user.setRequestStatusInfoSeconds(prefs.getInt(PREFS_REQUEST_STATUS_INFO_SECONDS, 60));
        user.setStatusStartTime(prefs.getLong(PREFS_USER_STATUS_START_TIME, 0));
        user.setStatusEndTime(prefs.getLong(PREFS_USER_STATUS_END_TIME, 0));
        user.setTimerStatus(Status.values()[prefs.getInt(PREFS_USER_TIMER_STATUS, 1)]);
        user.setTimerStatusText(prefs.getString(PREFS_USER_TIMER_STATUS_TEXT, ""));
    }


    public static void setLastCallTime(Context context, String time) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putString(PREFS_LAST_CALL_TIME, time);
        editor.commit();
    }
    public static String getLastCallTime(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_FILE, 0);

        return prefs.getString(PREFS_LAST_CALL_TIME, "2000-01-01T00:00:00");
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

        List<String> list = new ArrayList<>();
        int size = prefs.getInt("DefaultText_size", 0);

        for(int i=0;i<size;i++)
        {
            list.add(prefs.getString("DefaultText_" + i, null));
        }

        return list;
    }


    public static void setVoiceMailNumber(Context context, String voicemailNumber) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putString(PREFS_VOICEMAIL_NUMBER, voicemailNumber);
        editor.commit();
    }
    public static String getVoiceMailNumber(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_FILE, 0);

        return prefs.getString(PREFS_VOICEMAIL_NUMBER, "");
    }

    public static void setLanguageCountryCode(Context context, String countryCode) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putString(PREFS_COUNTRY_CODE, countryCode);
        editor.commit();
    }
    public static String getLanguageCountryCode(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_FILE, 0);

        return prefs.getString(PREFS_COUNTRY_CODE, "");
    }


    public static void deleteUserSettings(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFS_FILE, 0).edit();

        editor.putString(PREFS_USER_PHONE_NUMBER, "");
        editor.putString(PREFS_USER_NAME, "");
        editor.putString(PREFS_USER_EMAIL, "");
        editor.putString(PREFS_USER_PASSWORD, "");
        editor.putInt(PREFS_USER_LANGUAGE, 1);
        editor.putString(PREFS_USER_STATUS_TEXT, "");
        editor.putInt(PREFS_USER_STATUS, 1);
        editor.putBoolean(PREFS_USER_LOGED_IN, false);

        editor.putString(PREFS_USER_SMS_INVITE_TEXT, "");
        editor.putLong(PREFS_USER_STATUS_START_TIME, 0);
        editor.putLong(PREFS_USER_STATUS_END_TIME, 0);
        editor.putInt(PREFS_USER_TIMER_STATUS, 1);
        editor.putString(PREFS_USER_TIMER_STATUS_TEXT, "");

        editor.putLong(PREFS_LAST_CALL_TIME, 0);
        editor.putString(PREFS_VOICEMAIL_NUMBER, "");

       // editor.putLong(PREFS_LAST_CONTACTS_COUNT, 0);

        editor.putInt("DefaultText_size", 0);

        editor.commit();
    }
}
