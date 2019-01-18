package SimpleTest.lifeCycle;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import fight.exception.FatalException;
import redPack.msg.GetRequest;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

/**
 * author yg
 * description
 * date 2019/1/18
 */
public class MySup extends AbstractActor {
    ActorRef actorRef ;
    public MySup() {
        actorRef = context().actorOf(Props.create(MyActor.class));
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.create("1 minute"),
                akka.japi.pf.DeciderBuilder.match(FatalException.class, e -> {//销毁掉重启一个actor
                    System.err.println("监督者收到" + e.getRef() + "致命异常,重启actor");
                    return SupervisorStrategy.restart();
                }).build());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.matchAny(x -> {
            actorRef.tell(new GetRequest(),self());
        }).build();
    }
}
