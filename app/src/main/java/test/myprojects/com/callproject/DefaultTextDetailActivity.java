package test.myprojects.com.callproject;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.Util.Prefs;
import test.myprojects.com.callproject.model.Contact;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;
import test.myprojects.com.callproject.task.SendMessageTask;

public class DefaultTextDetailActivity extends Activity implements MessageInterface {

    private static final String TAG = "DefaultTextDetail";
    private boolean addNewText;
    private String text;

    @Bind(R.id.tvLabel)
    TextView tvLabel;
    @Bind(R.id.etValue)
    EditText etValue;

    @Bind(R.id.bSave)
    Button bSave;

    @Bind(R.id.rlProgress)
    RelativeLayout rlProgress;

    @OnClick(R.id.bSave)
    public void save() {

        if (etValue.getText().toString().length() < 4) {
            Toast.makeText(this, getString(R.string.enter_correct_data), Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etValue.getWindowToken(), 0);

        rlProgress.setVisibility(View.VISIBLE);

        List<String> list = Prefs.getDefaultTexts(this);

        if (addNewText) {
            list.add(etValue.getText().toString());
        } else {
            int position = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).contentEquals(text)) {
                    position = i;
                    Log.i(TAG, "position " + position);
                }
            }

            if (position != -1)
                list.set(position, etValue.getText().toString());

        }

        Prefs.saveDefaultTexts(this, list);

        SendMessageTask task = new SendMessageTask(this, getDefaultTextParams());
        task.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_text_detail);
        ButterKnife.bind(this);

        Bundle extra = getIntent().getExtras();

        if (extra != null && extra.getBoolean("addNewText")) {
            addNewText = true;
            tvLabel.setText(getString(R.string.add_new_text));

        } else if (extra != null) {
            tvLabel.setText(getString(R.string.edit_text));
            text = extra.getString("text");
            etValue.setText(text);


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


    @Override
    public void responseToSendMessage(SoapObject result, String methodName) {

        rlProgress.setVisibility(View.GONE);

        if (result == null) {
            showErrorTryAgain();
            return;
        }

        try {

            int resultStatus = Integer.valueOf(result.getProperty("Result").toString());

            if (resultStatus == 2) {
                finish();
            } else {
                showErrorTryAgain();
            }

        } catch (NullPointerException ne) {
            ne.printStackTrace();
            showErrorTryAgain();
        }

    }


    private void showErrorTryAgain() {
        Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
    }
}
