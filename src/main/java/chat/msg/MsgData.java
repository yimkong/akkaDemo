package chat.msg;

import chat.msg.CSMessage;

import java.io.Serializable;

/**
 * author yg
 * description
 * date 2019/1/12
 */
public class MsgData  implements Serializable {
    private final CSMessage csMessage;
    private final long timeStamp;

    public MsgData(CSMessage csMessage, long timeStamp) {
        this.csMessage = csMessage;
        this.timeStamp = timeStamp;
    }

    public CSMessage getCsMessage() {
        return csMessage;
    }
}
