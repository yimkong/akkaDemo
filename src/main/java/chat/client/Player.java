package chat.client;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.cluster.Cluster;
import akka.japi.pf.ReceiveBuilder;
import chat.msg.SCMessage;
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
    Cluster cluster = Cluster.get(getContext().system());

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(SCMessage.class, msg -> {
            System.err.println(msg.getContent());
            sender().tell(new Status.Success(msg.getContent()), self());
        }).match(List.class, msg -> {
            System.err.println(JSONObject.toJSON(msg));
        })
                .build();
    }
}
