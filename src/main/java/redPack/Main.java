package redPack;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import redPack.actor.Checker;
import redPack.actor.PlayerActor;
import redPack.actor.RedPackHolder;
import redPack.msg.GetRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * author yg
 * description 抢红包:akka无锁并发
 * date 2019/1/10
 */
public class Main {

    //5个玩家抢1万个红包
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();
        ActorRef checkerActorRef = system.actorOf(Props.create(Checker.class));
        List<ActorRef> playerList = new ArrayList<>();
        ActorRef redPackActorRef = system.actorOf(Props.create(RedPackHolder.class));
        for (int j = 1; j <= 5; j++) {
            ActorRef actorRef = system.actorOf(Props.create(PlayerActor.class, (long)j, checkerActorRef.path().toString(), redPackActorRef.path().toString()));
            playerList.add(actorRef);
        }
        int count = 0;
        while (count++ < 11000) {
            Iterator<ActorRef> iterator = playerList.iterator();
            while (iterator.hasNext()) {
                ActorRef next = iterator.next();
                next.tell(new GetRequest(), ActorRef.noSender());
            }
        }
        System.err.println("等待异步抢红包完成");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        system.terminate();
    }
}
