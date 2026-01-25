package src;

public class Result<V, E> {
    private final V value;
    private final E error;

    private Result(V value, E error) {
        this.value = value;
        this.error = error;
    }

    public boolean hasValue() { return value != null; }
    public boolean hasError() { return error != null; }
    public V getValue() { return value; }
    public E getError() { return error; }

    public static <V, E> Result<V, E> ofNone() {
        return new Result<V,E>(null, null);
    }
    public static <V, E> Result<V, E> ofValue(final V value) {
        return new Result<V,E>(value, null);
    }
    public static <V, E> Result<V, E> ofError(final E error) {
        return new Result<V,E>(null, error);
    }
    public static <V, E> Result<V, E> ofBoth(final V value, final E error) {
        return new Result<V,E>(value, error);
    }

    @Override
    public String toString() {
        String s = "Result[";
        if (value != null)
            s += "V: " + value.toString() + ", ";
        if (error != null)
            s += "E: " + error.toString() + ", ";
        return s.substring(0, s.length() - 2) + "]";
    }
}
