package common;

public class Utils {
    public static <T> T getOrElse(T value, T defaultValue) {
        return value == null? defaultValue: value;
    }
}
