package redPack.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import redPack.exception.NoRedPackException;
import redPack.msg.GetRequest;
import redPack.msg.RedPackResponse;
import redPack.msg.RedPackResult;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * author yg
 * description
 * date 2019/1/10
 */
public class PlayerActor extends AbstractActor {

    private final ActorSelection checker; //检查并发actor
    private final ActorSelection redPackHolder;
    private final long playerId;

    PlayerActor(long playerId, String checkerRef, String redPackActorRef) {
        this.checker = context().actorSelection(checkerRef);
        this.redPackHolder = context().actorSelection(redPackActorRef);
        this.playerId = playerId;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.match(Status.Failure.class, msg -> {
                    Throwable cause = msg.cause().getCause();
                    if (cause == null || cause.getClass().equals(NoRedPackException.class)) {
                        System.err.println("没有红包,玩家" + playerId + "对象销毁!");
                        context().stop(self());
                    } else {
                        System.err.println(cause);
                        context().stop(self());
                    }
                }
        ).match(RedPackResponse.class, msg -> {
            int id = msg.getId();
            checker.tell(new RedPackResult(playerId, id), self());//记录红包
        }).match(GetRequest.class, msg -> {
            redPackHolder.tell(msg, self()); //抢红包
        }).build();
    }
}
