package fight.exception;

/**
 * author yg
 * description
 * date 2019/1/17
 */
public class MyRuntimeException extends Exception {
    private final String ref;

    public MyRuntimeException(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }
}
