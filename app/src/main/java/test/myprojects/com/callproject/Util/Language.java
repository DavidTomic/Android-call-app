package test.myprojects.com.callproject.Util;

/**
 * Created by dtomic on 07/09/15.
 */
public enum Language {
    DEFAULT(0), ENGLISH(1), DANISH(2);
    private final int value;

    private Language(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

