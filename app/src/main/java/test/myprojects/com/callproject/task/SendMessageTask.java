package test.myprojects.com.callproject.task;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import test.myprojects.com.callproject.model.PhoneNumbers;
import test.myprojects.com.callproject.myInterfaces.MessageInterface;

/**
 * Created by dtomic on 25/08/15.
 */
public class SendMessageTask extends AsyncTask<Void, Void, SoapObject> {

    private static final String TAG = "SendMessageTask";

    //All method names
    public static final String CREATE_ACCOUNT = "CreateAccount";
    public static final String GET_ACCOUNT_SETUP = "GetAccountSetup";
    public static final String CHECK_PHONE_NUMBERS = "CheckPhoneNumbers";
    public static final String UPDATE_STATUS = "UpdateStatus";
    public static final String REQUEST_STATUS_INFO = "RequestStatusInfo";
    public static final String UPDATE_ACCOUNT = "UpdateAccount";
    public static final String SET_DEFAULT_TEXT = "SetDefaultText";
    public static final String GET_DEFAULT_TEXT = "GetDefaultText";
    public static final String ADD_CONTACT = "AddContacts";
    public static final String DELETE_CONTACT = "DeleteContact";
    public static final String GET_CONTACT = "GetContact";
    public static final String LOG_IN = "Login";


    public static final String NAMESPACE = "http://tempuri.org/";
    public static final String URL = "http://call.celox.dk/wsCall.asmx";
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
    protected SoapObject doInBackground(Void... params) {

        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        soapEnvelope.dotNet = true;
        soapEnvelope.setOutputSoapObject(request);

//        //mozda
//        if (request.getName().contentEquals(CHECK_PHONE_NUMBERS)){
//            soapEnvelope.implicitTypes = true;
//            soapEnvelope.addMapping(NAMESPACE, "ActionRequest", new PhoneNumbers().getClass());
//        }


        aht = new HttpTransportSE(URL);
        aht.debug = true;

        try {
            aht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            aht.call(SOAP_ACTION, soapEnvelope);

            SoapObject result = (SoapObject) soapEnvelope.getResponse();


            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(SoapObject result) {
        super.onPostExecute(result);
        Log.i(TAG, "result " + result + " methodName " + request.getName());

        if (aht != null){
            Log.d("dump Request: ", aht.requestDump);
            Log.w("dump Response: ", aht.responseDump);
        }

        if (messageInterface != null)
            messageInterface.responseToSendMessage(result, request.getName());

    }
}
