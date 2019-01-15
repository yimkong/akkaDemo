package SimpleTest;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import chat.client.Player;
import com.typesafe.config.ConfigFactory;

/**
 * author yg
 * description
 * date 2019/1/14
 */
public class ActorSystem1 {
    public static void main(String[] args) {//设置端口为111
        ActorSystem system = ActorSystem.create("test", ConfigFactory.load("client-application"));
        ActorRef player = system.actorOf(Props.create(Player.class), "player");//akka.tcp://test@192.168.11.90:111/user/player
        String s = player.path().toString();
        System.err.println(s);
    }
}
