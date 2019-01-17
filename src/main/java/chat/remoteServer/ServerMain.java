package chat.remoteServer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.contrib.pattern.ClusterReceptionistExtension;
import akka.routing.BalancingPool;
import com.typesafe.config.ConfigFactory;

/**
 * author yg
 * description 聊天室集群:聊天室实现分布式、高容错
 * date 2019/1/11
 */
public class ServerMain {
    public static void main(String[] args) {//-Dcom.sum.management.jmxremote.port=9552
        ActorSystem actorSystem = ActorSystem.create("chatRoom", ConfigFactory.load("chat1-application"));
        ActorRef roomManager = actorSystem.actorOf(Props.create(RoomsManager.class), "roomManager");
        ActorRef workers = actorSystem.actorOf(new BalancingPool(8).props(Props.create(ChatRoom.class, roomManager.path().toString())).withDispatcher("pool-dispatcher"), "workers");
        ((ClusterReceptionistExtension) akka.contrib.pattern.ClusterReceptionistExtension.apply(actorSystem)).registerService(workers);
    }
}
