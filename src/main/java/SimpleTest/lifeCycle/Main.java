package SimpleTest.lifeCycle;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import com.typesafe.config.ConfigFactory;
import redPack.msg.GetRequest;
import scala.concurrent.Future;

/**
 * author yg
 * description
 * date 2019/1/18
 */
public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("test");
        ActorRef actorRef = system.actorOf(Props.create(MySup.class));
        actorRef.tell(new GetRequest(),ActorRef.noSender());
//        Future<Terminated> terminate = system.terminate();
//        while (!terminate.isCompleted()) {
//        }
    }
}
