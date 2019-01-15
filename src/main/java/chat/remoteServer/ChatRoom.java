package chat.remoteServer;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.japi.pf.ReceiveBuilder;
import chat.msg.C2SMsgs;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Date;

/**
 * author yg
 * description
 * date 2019/1/11
 */
public class ChatRoom extends AbstractActor {
    private final ActorSelection roomsManagerRef;

    public ChatRoom(String roomsManagerRef) {
        this.roomsManagerRef = context().actorSelection(roomsManagerRef);
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(C2SMsgs.CSMessage.class, msg -> {
            System.err.println(msg.getContent());
            roomsManagerRef.forward(new C2SMsgs.MsgData(msg, new Date().getTime()), context());
        }).match(C2SMsgs.RegisterClient.class, msg -> {
            roomsManagerRef.forward(msg, context());
        })
                .build();
    }

}
