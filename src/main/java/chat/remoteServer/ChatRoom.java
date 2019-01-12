package chat.remoteServer;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.cluster.Cluster;
import akka.japi.pf.ReceiveBuilder;
import chat.msg.CSMessage;
import chat.msg.MsgData;
import chat.msg.SimpleMsg;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.Date;

/**
 * author yg
 * description
 * date 2019/1/11
 */
public class ChatRoom extends AbstractActor {
    Cluster cluster = Cluster.get(getContext().system());
    private final ActorSelection roomsManagerRef;

    public ChatRoom(String roomsManagerRef) {
        this.roomsManagerRef = context().actorSelection(roomsManagerRef);
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(CSMessage.class, msg -> {
            System.err.println(msg.getContent());
            roomsManagerRef.forward(new MsgData(msg, new Date().getTime()), context());
        }).match(SimpleMsg.class, msg -> {
            roomsManagerRef.forward(msg, context());
        }).match(SimpleMsg.RegisterMsg.class, msg -> {
            roomsManagerRef.forward(msg, context());
        }).match(SimpleMsg.RequestMsg.class, msg -> {
            roomsManagerRef.forward(msg, context());
        })
                .build();
    }

}
