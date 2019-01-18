package SimpleTest.lifeCycle;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import fight.exception.FatalException;
import scala.Option;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * author yg
 * description
 * date 2019/1/18
 */
public class MyActor extends AbstractActor {
    @Override
    public void preStart() throws Exception {
        System.err.println("preStart");
        super.preStart();
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        System.err.println("preReStart");
        super.preRestart(reason, message);
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.err.println("postReStart");
        super.postRestart(reason);
    }

    @Override
    public void postStop() throws Exception {
        System.err.println("postStop");
        super.postStop();
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.matchAny(x -> {
            throw new FatalException("");
        }).build();
    }
}
