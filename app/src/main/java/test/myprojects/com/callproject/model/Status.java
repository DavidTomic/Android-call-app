package test.myprojects.com.callproject.model;

/**
 * Created by davidtomic on 07/09/15.
 */
public enum Status {
    RED_STATUS(0), GREEN_STATUS(1), YELLOW_STATUS(2),;
    private final int value;

    private Status(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
