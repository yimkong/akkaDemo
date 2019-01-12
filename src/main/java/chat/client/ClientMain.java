package chat.client;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.contrib.pattern.ClusterClient;
import akka.pattern.Patterns;
import akka.util.Timeout;
import chat.msg.CSMessage;
import chat.msg.MsgData;
import chat.msg.SimpleMsg;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * author yg
 * description 聊天客户端
 * date 2019/1/11
 */
public class ClientMain {
    public static void main(String[] args) throws UnknownHostException {


        ActorSystem system = ActorSystem.create("clientSystem", ConfigFactory.load("client-application"));

        Set<ActorSelection> initialContacts = new HashSet<ActorSelection>();
        initialContacts.add(system.actorSelection("akka.tcp://chatRoom@127.0.0.1:2552/user/receptionist"));
        initialContacts.add(system.actorSelection("akka.tcp://chatRoom@127.0.0.1:2551/user/receptionist"));
        ActorRef receptionist = system.actorOf(ClusterClient.defaultProps(initialContacts), "client"); //客户端节点
        System.err.println(receptionist.path().toString());
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        Patterns.ask(receptionist, new ClusterClient.SendToAll("/user/workers", new SimpleMsg.RegisterMsg()), timeout);//注册客户端
//        ActorRef actorRef = system.actorOf(Props.create(Player.class));
        ExecutorService service = Executors.newSingleThreadExecutor();
        Timeout timeout1 = new Timeout(500, TimeUnit.MILLISECONDS);
        service.submit(new Runnable() {
            @Override
            public void run() {
                long time = new Date().getTime();
                int num = 0;
                while (true) {
                    while ((new Date().getTime() - time) < 500) {
                        Thread.yield();
                    }
                    time = new Date().getTime();
                    ClusterClient.Send msg = new ClusterClient.Send("/user/workers", new SimpleMsg.RequestMsg(), false);
                    Future f = Patterns.ask(receptionist, msg, timeout1);
                    List<MsgData> result = null;
                    try {
                        result = (List<MsgData>) Await.result(f, timeout1.duration());
                    } catch (Exception e) {
                        continue;
                    }
                    if (!result.isEmpty() || result.size() != num) {
                        System.out.println("result: " + result);
                        num = result.size();
                    }
                }
            }
        });
        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        service.submit(new Runnable() {
            @Override
            public void run() {
                String content = null;
                while (content == null || !content.equals("exit")) {
                    try {
                        content = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ClusterClient.Send msg = new ClusterClient.Send("/user/workers", new CSMessage(content), false);
                    Patterns.ask(receptionist, msg, timeout);
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.err.println("退出聊天应用");
                System.exit(0);
            }
        });
    }
}
