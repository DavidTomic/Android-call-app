package test.myprojects.com.callproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Status;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;

public class SetStatusActivity extends Activity implements View.OnClickListener, MessageInterface {

    private static final String TAG = "SetStatusActivity";

    @Bind(R.id.etStatus)
    EditText etStatus;

    @Bind(R.id.llRedStatus)
    LinearLayout llRedStatus;
    @Bind(R.id.llYellowStatus)
    LinearLayout llYellowStatus;
    @Bind(R.id.llGreenStatus)
    LinearLayout llGreenStatus;

    @OnClick(R.id.bSelect)
    public void setStatus() {
        showStatusTextDialog();
    }

    @OnClick(R.id.bSetTime)
    public void setTime() {
        showDateTimeDialog();
    }

    @OnClick(R.id.bConfirm)
    public void confirm() {
        SendMessageTask task = new SendMessageTask(this, getUpdateStatusParams());
        task.execute();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.llRedStatus:
                currentStatus = Status.RED_STATUS;
                break;
            case R.id.llYellowStatus:
                currentStatus = Status.YELLOW_STATUS;
                break;
            case R.id.llGreenStatus:
                currentStatus = Status.GREEN_STATUS;
                break;
        }

        setStatusBackgrounds();

    }

    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        if (result == null)
            return;

        try {

            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {

                User.getInstance(this).setStatus(currentStatus);
                User.getInstance(this).setStatusText(etStatus.getText().toString());
                Prefs.setUserData(this, User.getInstance(this));

                Toast.makeText(SetStatusActivity.this, getString(R.string.status_updated),
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(SetStatusActivity.this, getString(R.string.status_not_updated),
                        Toast.LENGTH_SHORT).show();
            }


        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }

        finish();
    }


    private Status currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_status);
        ButterKnife.bind(this);

        llRedStatus.setOnClickListener(this);
        llYellowStatus.setOnClickListener(this);
        llGreenStatus.setOnClickListener(this);

        currentStatus = User.getInstance(this).getStatus();
        setStatusBackgrounds();

        etStatus.setText(User.getInstance(this).getStatusText());

    }

    private void showDateTimeDialog() {
        final View dialogView = View.inflate(this, R.layout.date_time_picker_dialog, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute());

                long time = calendar.getTimeInMillis();
                Log.i(TAG, "time " + time);
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    private void showStatusTextDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final CharSequence items[] = new CharSequence[]{"First", "Second", "Third"};
        adb.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                etStatus.setText(items[n]);
                d.dismiss();
            }

        });
        adb.setNegativeButton("Cancel", null);
        adb.setTitle("Choose status");
        adb.show();
    }

    private SoapObject getUpdateStatusParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.UPDATE_STATUS);


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

        pi = new PropertyInfo();
        pi.setName("Status");
        pi.setValue(currentStatus.getValue());
        pi.setType(Integer.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("EndTime");
        pi.setValue("2000-01-01T00:00:00");
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Text");
        pi.setValue(etStatus.getText().toString());
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

    private void setStatusBackgrounds() {

        switch (currentStatus) {
            case RED_STATUS:
                llRedStatus.setBackgroundColor(getResources().getColor(R.color.gray_light_80));
                llYellowStatus.setBackgroundColor(getResources().getColor(R.color.transparent));
                llGreenStatus.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
            case YELLOW_STATUS:
                llRedStatus.setBackgroundColor(getResources().getColor(R.color.transparent));
                llYellowStatus.setBackgroundColor(getResources().getColor(R.color.gray_light_80));
                llGreenStatus.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;
            case GREEN_STATUS:
                llRedStatus.setBackgroundColor(getResources().getColor(R.color.transparent));
                llYellowStatus.setBackgroundColor(getResources().getColor(R.color.transparent));
                llGreenStatus.setBackgroundColor(getResources().getColor(R.color.gray_light_80));
                break;
        }
    }
}
