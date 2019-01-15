package chat.client;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import chat.msg.C2SMsgs;
import chat.msg.S2CMsgs;
import com.alibaba.fastjson.JSONObject;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.List;

/**
 * author yg
 * description
 * date 2019/1/11
 */
public class Player extends AbstractActor {

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(S2CMsgs.SCMessage.class, msg -> {
            System.err.println(msg.getContent());
            sender().tell(new Status.Success(msg.getContent()), self());
        }).match(List.class, msg -> {
            System.err.println(JSONObject.toJSON(msg));
        })
                .build();
    }
}
