package chat.msg;


import scala.Serializable;

/**
 * author yg
 * description 客户端向服务端
 * date 2019/1/12
 */
public class C2SMsgs {

    public static class RequestMsg implements Serializable {
    }

    /**
     * author yg
     * description 消息
     * date 2019/1/11
     */
    public static class CSMessage implements Serializable {
        private final String content;
        private final String addr;

        public CSMessage(String content, String addr) {
            this.content = content;
            this.addr = addr;
        }


        public String getContent() {
            return content;
        }

        public String getAddr() {
            return addr;
        }
    }

    /**
     * author yg
     * description 注册客户端
     * date 2019/1/14
     */
    public static class RegisterClient implements Serializable {
        private final String address;

        public RegisterClient(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }
    }

    /**
     * author yg
     * description 历史消息
     * date 2019/1/12
     */
    public static class MsgData implements Serializable {
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

}
