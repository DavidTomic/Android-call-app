package test.myprojects.com.callproject.myInterfaces;

import org.ksoap2.serialization.SoapObject;

/**
 * Created by dtomic on 25/08/15.
 */
public interface MessageInterface {

    void responseToSendMessage(SoapObject result, String methodName);
}
