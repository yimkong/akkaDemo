package chat.msg;

import java.io.Serializable;

/**
 * author yg
 * description
 * date 2019/1/11
 */
public class SCMessage implements Serializable {
    private final String content;

    public SCMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
