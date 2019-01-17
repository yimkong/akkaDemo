package fight.exception;

/**
 * author yg
 * description
 * date 2019/1/17
 */
public class FatalException extends Exception {
    private final String ref;

    public FatalException(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }
}
