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

import test.myprojects.com.callproject.myInterfaces.MessageInterface;

/**
 * Created by dtomic on 25/08/15.
 */
public class SendMessageTask extends AsyncTask<ArrayList<PropertyInfo>, Void, SoapObject> {

    private static final String TAG = "SendMessageTask";

    //All method names
    public static final String CREATE_ACCOUNT = "CreateAccount";
    public static final String LOGIN = "GetAccountSetup";
    public static final String UPDATE_STATUS = "UpdateStatus";


    private static final String NAMESPACE = "http://tempuri.org/";
    private static final String URL = "http://call.celox.dk/wsCall.asmx";
    private String SOAP_ACTION;
    private String METHOD_NAME;

    private MessageInterface messageInterface;
    private String methodName;
    private SoapObject request;

    public SendMessageTask(MessageInterface messageInterface, String methodName) {
        this.messageInterface = messageInterface;
        this.methodName = methodName;
        this.METHOD_NAME = methodName;
        this.SOAP_ACTION = NAMESPACE + methodName;
      //  this.request
    }

    @Override
    protected SoapObject doInBackground(ArrayList<PropertyInfo>...params) {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        ArrayList<PropertyInfo> list = params[0];

        if (list!=null && list.size() > 0) {
            for (PropertyInfo pi : list) {
                request.addProperty(pi);
            }
        }

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.dotNet = true;
        soapEnvelope.setOutputSoapObject(request);

        HttpTransportSE aht = new HttpTransportSE(URL);

        try
        {
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
        Log.i(TAG, "result " + result + " methodName " + methodName);

        if (messageInterface!=null)
        messageInterface.responseToSendMessage(result, methodName);

    }
}
