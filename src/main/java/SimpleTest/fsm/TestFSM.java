package SimpleTest.fsm;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import fight.msg.LoginMsg;

/**
 * author yg
 * description 测试fsm消息队列
 * date 2019/1/17
 */
public class TestFSM {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("testFSM", ConfigFactory.load("fight-application"));
        ActorRef actorRef = system.actorOf(Props.create(Player.class));
        for (int i = 0; i < 100; i++) {
            actorRef.tell(new LoginMsg(),ActorRef.noSender());
        }
    }
}
