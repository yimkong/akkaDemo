package fight;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import fight.service.FighterSupervisor;

/**
 * author yg
 * description
 * date 2019/1/15
 */
public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("clientSystem", ConfigFactory.load("fight-application"));
        //初始化数据
        ActorRef fighter = system.actorOf(Props.create(FighterSupervisor.class));

    }
}
