package test.myprojects.com.callproject.model;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * Created by dtomic on 02/09/15.
 */
public class PhoneNumbers implements KvmSerializable {

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    private String string;

    @Override
    public Object getProperty(int i) {
        return string;
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @Override
    public void setProperty(int i, Object o) {

    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        propertyInfo.type = PropertyInfo.STRING_CLASS;
        propertyInfo.name = "string";
    }
}