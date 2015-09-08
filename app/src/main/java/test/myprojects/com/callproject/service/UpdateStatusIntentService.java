package test.myprojects.com.callproject.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.task.SendMessageTask;

/**
 * Created by davidtomic on 08/09/15.
 */
public class UpdateStatusIntentService extends IntentService {


    private static final String TAG = "UpdateStatusService";

    public UpdateStatusIntentService() {
        super("UpdateStatusIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "onHandleIntent");

        int status = intent.getIntExtra("status", 1);

        SoapObject request = getUpdateStatusParams(status);

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.dotNet = true;
        soapEnvelope.setOutputSoapObject(request);


        HttpTransportSE aht = new HttpTransportSE(SendMessageTask.URL);
        aht.debug = true;

        try {
            aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            aht.call(SendMessageTask.NAMESPACE + request.getName(), soapEnvelope);

            SoapObject result = (SoapObject) soapEnvelope.getResponse();

            Log.i(TAG, "result " + result);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private SoapObject getUpdateStatusParams(int status) {

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
        pi.setValue(status);
        pi.setType(Integer.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("EndTime");

        String endTime = User.getInstance(this).getEndTime();

        if (endTime ==null || endTime.length() == 0){
            endTime = "2000-01-01T00:00:00";
        }

        pi.setValue(endTime);
        pi.setType(String.class);
        request.addProperty(pi);

        pi = new PropertyInfo();
        pi.setName("Text");
        pi.setValue(User.getInstance(this).getStatusText());
        pi.setType(String.class);
        request.addProperty(pi);

        return request;
    }

}
