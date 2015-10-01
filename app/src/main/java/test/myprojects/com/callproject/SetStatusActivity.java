package test.myprojects.com.callproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

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

    private long selectedEndTime;
    private long selectedStartTime = System.currentTimeMillis();
    private boolean startTimeclicked;

    @Bind(R.id.etStatus)
    EditText etStatus;
    @OnClick(R.id.bSelect)
    public void setStatus() {
        showStatusTextDialog();
    }

    @Bind(R.id.llRedStatus)
    LinearLayout llRedStatus;
    @Bind(R.id.llYellowStatus)
    LinearLayout llYellowStatus;
    @Bind(R.id.llGreenStatus)
    LinearLayout llGreenStatus;


    @Bind(R.id.rlStartTime)
    RelativeLayout rlStartTime;
    @Bind(R.id.tvStartTimeLabel) TextView tvStartTimeLabel;
    @Bind(R.id.tvStartTime) TextView tvStartTime;
    @OnClick(R.id.bSetStartTime)
    public void setStartTimeClicked(){
        startTimeclicked = true;
        showDateTimeDialog();
    }



    @Bind(R.id.rlEndTime)
    RelativeLayout rlEndTime;
    @Bind(R.id.tvEndTime)
    TextView tvEndTime;
    @Bind(R.id.tvEndTimeLabel)
    TextView tvEndTimeLabel;
    @OnClick(R.id.bSetEndTime)
    public void setEndTime() {
        startTimeclicked = false;
        showDateTimeDialog();
    }



    @OnClick(R.id.bConfirm)
    public void confirm() {

        List<String> defaultTextList = Prefs.getDefaultTexts(this);

        if (etStatus.getText() != null && etStatus.getText().toString().length() > 0) {
            String currentText = etStatus.getText().toString();

            if (!defaultTextList.contains(currentText)) {

                defaultTextList.add(currentText);

                Prefs.saveDefaultTexts(this, defaultTextList);

                SendMessageTask task = new SendMessageTask(this, getDefaultTextParams());
                task.execute();
            }
        }


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

        if (result == null){
            Toast.makeText(SetStatusActivity.this, getString(R.string.status_not_updated),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (methodName.contentEquals(SendMessageTask.UPDATE_STATUS_WITH_TIMESTAMP)) {
            try {


                int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

                if (resultStatus == 2) {

                    if (currentStatus == Status.RED_STATUS || currentStatus == Status.YELLOW_STATUS){

                        if (tvStartTime.getText().toString().contentEquals(getString(R.string.now))){
                            User.getInstance(this).setStatus(currentStatus);
                            User.getInstance(this).setStatusText(etStatus.getText().toString());
                        }

                    }else {
                        User.getInstance(this).setStatus(currentStatus);
                        User.getInstance(this).setStatusText(etStatus.getText().toString());
                    }






                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));


                    String startTime;
                    if (selectedStartTime - System.currentTimeMillis() <= 0) {
                        startTime = "2000-01-01T00:00:00";

                    } else {
                        startTime = sdf.format(new Date(selectedStartTime));
                    }
                    User.getInstance(this).setStatusStartTime(startTime);



                    String endTime;
                    if (selectedEndTime - System.currentTimeMillis() <= 0) {
                        endTime = "2000-01-01T00:00:00";

                    } else {
                        endTime = sdf.format(new Date(selectedEndTime));
                    }
                    User.getInstance(this).setStatusEndTime(endTime);




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

        if (User.getInstance(this).getStatusText() !=null &&
                !User.getInstance(this).getStatusText().contentEquals("(null)"))
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


                if (startTimeclicked){
                    selectedStartTime = calendar.getTimeInMillis();

                    Log.i(TAG, "time " + selectedStartTime);

                    if (selectedStartTime - System.currentTimeMillis() > 0) {

                        tvStartTime.setText(new java.text.SimpleDateFormat("dd-MM·HH:mm").format
                                (new java.util.Date(selectedStartTime)));
//                        int minutes = (int) (((selectedTime - System.currentTimeMillis()) / (1000 * 60)) % 60);
//                        int hours = (int) (((selectedTime - System.currentTimeMillis()) / (1000 * 60 * 60)) % 24);
//
//                        if (hours == 0) {
//                            tvTime.setText((minutes+1) + " min");
//                        } else {
//                            tvTime.setText(String.format("%02d", hours) + ":"
//                                    + String.format("%02d", (minutes+1)) + " min");
//                        }
                    }else {
                        selectedStartTime = 0;
                        tvStartTime.setText(getString(R.string.now));
                    }
                }else {
                    selectedEndTime = calendar.getTimeInMillis();

                    Log.i(TAG, "time " + selectedEndTime);

                    if (selectedEndTime - System.currentTimeMillis() > 0) {
                        tvEndTime.setText(new java.text.SimpleDateFormat("dd-MM·HH:mm").format
                                (new java.util.Date(selectedEndTime)));
                    }else {
                        selectedEndTime = 0;
                        tvEndTime.setText("-:-");
                    }
                }

                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    private void showStatusTextDialog() {

        List<String> itemList = Prefs.getDefaultTexts(this);

        if (itemList.size() > 0) {

            AlertDialog.Builder adb = new AlertDialog.Builder(this);

            final CharSequence items[] = itemList.toArray(new CharSequence[itemList.size()]);
            adb.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface d, int n) {
                    etStatus.setText(items[n]);
                    d.dismiss();
                }

            });
            adb.setNegativeButton(getString(R.string.cancel), null);
            adb.setTitle(getString(R.string.choose_status_text));
            adb.show();
        } else {
            Toast.makeText(this, getString(R.string.add_some_default_text),
                    Toast.LENGTH_LONG).show();
        }

    }

    private SoapObject getUpdateStatusParams() {

        SoapObject request = new SoapObject(SendMessageTask.NAMESPACE, SendMessageTask.UPDATE_STATUS_WITH_TIMESTAMP);

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


        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startTime;
        if (selectedStartTime - System.currentTimeMillis() <= 0) {
            startTime = "2000-01-01T00:00:00";

        } else {
            startTime = sdf.format(new Date(selectedStartTime));
        }

        Log.i(TAG, "startTime " + startTime);

        pi = new PropertyInfo();
        pi.setName("StartTime");
        pi.setValue(startTime);
        pi.setType(String.class);
        request.addProperty(pi);


        String endTime;
        if (selectedEndTime - System.currentTimeMillis() <= 0) {
            endTime = "2000-01-01T00:00:00";

        } else {
            endTime = sdf.format(new Date(selectedEndTime));
        }

        Log.i(TAG, "endTime " + endTime);

        pi = new PropertyInfo();
        pi.setName("EndTime");
        pi.setValue(endTime);
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
                llRedStatus.getChildAt(0).setSelected(true);
                llYellowStatus.getChildAt(0).setSelected(false);
                llGreenStatus.getChildAt(0).setSelected(false);
                rlEndTime.setVisibility(View.VISIBLE);
                rlStartTime.setVisibility(View.VISIBLE);
                tvStartTimeLabel.setText(getString(R.string.set_red_status_from));
                tvEndTimeLabel.setText(getString(R.string.set_red_status_to));


                break;
            case YELLOW_STATUS:
                llRedStatus.getChildAt(0).setSelected(false);
                llYellowStatus.getChildAt(0).setSelected(true);
                llGreenStatus.getChildAt(0).setSelected(false);
                rlEndTime.setVisibility(View.VISIBLE);
                rlStartTime.setVisibility(View.VISIBLE);
                tvStartTimeLabel.setText(getString(R.string.set_yellow_status_from));
                tvEndTimeLabel.setText(getString(R.string.set_yellow_status_to));


                break;
            case GREEN_STATUS:
                llRedStatus.getChildAt(0).setSelected(false);
                llYellowStatus.getChildAt(0).setSelected(false);
                llGreenStatus.getChildAt(0).setSelected(true);
                rlEndTime.setVisibility(View.GONE);
                rlStartTime.setVisibility(View.GONE);
                break;
        }
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
