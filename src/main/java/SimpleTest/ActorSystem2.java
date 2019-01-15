package SimpleTest;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import chat.msg.S2CMsgs;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * author yg
 * description
 * date 2019/1/14
 */
public class ActorSystem2 {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("test", ConfigFactory.load("client-application"));
        ActorSelection actorSelection = system.actorSelection("akka.tcp://test@192.168.11.90:111/user/player");
        actorSelection.tell(new S2CMsgs.SCMessage("aaa"), ActorRef.noSender());
        system.actorSelection("akka.tcp://test@127.0.0.1:111/user/player").tell(new S2CMsgs.SCMessage("bbb"), ActorRef.noSender());//防火墙导致的阻断?是因为配置的ip
        system.actorSelection("akka.tcp://clientSystem@127.0.0.1:51433/user/player").tell(new S2CMsgs.SCMessage("bbb"), ActorRef.noSender());//防火墙导致的阻断?是因为配置的ip
        CompletableFuture<Object> aaa = (CompletableFuture) toJava(ask(actorSelection, new S2CMsgs.SCMessage("ccc"), Timeout.apply(1000, TimeUnit.MILLISECONDS)));
    }
}
