package redPack.actor;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import redPack.exception.NoRedPackException;
import redPack.msg.GetRequest;
import redPack.msg.RedPackResponse;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * author yg
 * description 红包持有者
 * date 2019/1/10
 */
public class RedPackHolder extends AbstractActor {
    private List<Integer> redPacks = new LinkedList<>();
    private final Timeout timeout = Timeout.apply(1000, TimeUnit.MILLISECONDS);

    //初始化红包内存 10000个
    {
        for (int i = 0; i < 10000; i++) {
            redPacks.add(i);
        }
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(GetRequest.class, msg -> {
            if (redPacks.isEmpty()) {
                sender().tell(new Status.Failure(new NoRedPackException()), self());
            } else {
                Integer remove = redPacks.remove(0);
                sender().tell(new RedPackResponse(remove), self());
            }
        }).build();
    }

}
