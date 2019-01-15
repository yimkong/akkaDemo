package chat.client;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.contrib.pattern.ClusterClient;
import chat.msg.C2SMsgs;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ExecutorService service = Executors.newSingleThreadExecutor();
        /*Timeout timeout1 = new Timeout(500, TimeUnit.MILLISECONDS);
        service.submit(new Runnable() {//轮询,最差的方法
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
        });*/
        ActorRef player = system.actorOf(Props.create(Player.class), "player");
        //注册客户端
        final String addr = getAddr();
        System.err.println(addr);
//        try {
//            Thread.sleep(10000*1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        final String remoteRouter = "/user/workers";
        ClusterClient.Send msg = new ClusterClient.Send(remoteRouter, new C2SMsgs.RegisterClient(addr));
        receptionist.tell(msg, player);

        //读取输入
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
                    ClusterClient.Send msg = new ClusterClient.Send(remoteRouter, new C2SMsgs.CSMessage(content, addr), false);
                    receptionist.tell(msg, receptionist);
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

    private static String getAddr() {
        ServerSocket serverSocket = null; //读取空闲的可用端口
        String localIp = null;
        int port = -1;
        try {
            serverSocket = new ServerSocket(0);
            localIp = serverSocket.getInetAddress().getLocalHost().getHostAddress();
            port = serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//      return localIp + ":" + port;
        return "127.0.0.1" + ":" + port;
    }
}
