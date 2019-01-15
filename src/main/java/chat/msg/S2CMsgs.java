package chat.msg;

import java.io.Serializable;

/**
 * author yg
 * description 服务端向客户端
 * date 2019/1/15
 */
public class S2CMsgs {

    /**
     * author yg
     * description 通知其他节点注册
     * date 2019/1/15
     */
    public static class NoticeRegisterClient implements Serializable {
        private final String actorRef;

        public NoticeRegisterClient(String actorRef) {
            this.actorRef = actorRef;
        }

        public String getActorRef() {
            return actorRef;
        }
    }

    /**
     * author yg
     * description 聊天消息
     * date 2019/1/11
     */
    public static class SCMessage implements Serializable {
        private final String content;

        public SCMessage(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }
}
