package test.myprojects.com.callproject.task;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import test.myprojects.com.callproject.model.PhoneNumbers;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;

/**
 * Created by dtomic on 25/08/15.
 */
public class SendMessageTask extends AsyncTask<Void, Void, SoapObject> {

    private static final String TAG = "SendMessageTask";

    //All method names
    public static final String CREATE_ACCOUNT = "CreateAccount";
    public static final String LOGIN = "GetAccountSetup";
    public static final String CHECK_PHONE_NUMBERS = "CheckPhoneNumbers";
    public static final String UPDATE_STATUS = "UpdateStatus";


    public static final String NAMESPACE = "http://tempuri.org/";
    private static final String URL = "http://call.celox.dk/wsCall.asmx";
    private String SOAP_ACTION;

    private MessageInterface messageInterface;
    private SoapObject request;
    HttpTransportSE aht;

    public SendMessageTask(MessageInterface messageInterface, SoapObject request) {
        this.messageInterface = messageInterface;
        this.SOAP_ACTION = NAMESPACE + request.getName();
        this.request = request;
    }

    @Override
    protected SoapObject doInBackground(Void...params) {

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.dotNet = true;
        soapEnvelope.setOutputSoapObject(request);

        //mozda
        soapEnvelope.implicitTypes = true;
        soapEnvelope.addMapping(NAMESPACE, "ActionRequest", new PhoneNumbers().getClass());

        aht = new HttpTransportSE(URL);
        aht.debug=true;

        try {
            aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            aht.call(SOAP_ACTION, soapEnvelope);

            SoapObject result = (SoapObject)soapEnvelope.getResponse();



            return result;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(SoapObject result) {
        super.onPostExecute(result);
        Log.i(TAG, "result " + result + " methodName " + request.getName());

        Log.d("dump Request: ", aht.requestDump);

        if (messageInterface!=null)
        messageInterface.responseToSendMessage(result, request.getName());

    }
}
