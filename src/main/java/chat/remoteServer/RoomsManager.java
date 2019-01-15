package chat.remoteServer;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import chat.msg.C2SMsgs;
import chat.msg.S2CMsgs;
import com.alibaba.fastjson.JSONObject;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static akka.cluster.ClusterEvent.initialStateAsEvents;

/**
 * author yg
 * description
 * date 2019/1/11
 */
public class RoomsManager extends AbstractActor {
    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
    Cluster cluster = Cluster.get(getContext().system());
    //历史消息(缓存->序列化)
    List<C2SMsgs.MsgData> msgDataList = new LinkedList<>();

    Set<String> clientActorPathSet = new HashSet<>();
    Set<String> serverActorPathSet = new HashSet<>();

    @Override
    public void preStart() {
        cluster.subscribe(self(), initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(ClusterEvent.MemberEvent.class, msg -> { //新节点加入
//                    log.info("MemberEvent: {}", msg);
                    register(msg.member());
                })
                .match(ClusterEvent.UnreachableMember.class, msg -> {//节点不可用
//                    log.info("UnreachableMember: {}", msg);
                    remove(msg.member());
                })
                .match(C2SMsgs.RegisterClient.class, msg -> {//新用户加入
                    String clientAddress = sender().path().toString();
                    String str = "新用户" + msg.getAddress() + "加入";
                    //本节点记录客户端
                    addClient(clientAddress);
                    //给所有在线用户发送进入房间消息
                    sendClient("系统", str);
                    //通知所有节点记录客户端
                    registerClient(clientAddress);
                })
                .match(S2CMsgs.NoticeRegisterClient.class, msg -> {//记录客户端
                    System.err.println("收到");
                    addClient(msg.getActorRef());
                })
                .match(C2SMsgs.MsgData.class, msg -> {
                    msgDataList.add(msg);//保存聊天历史
                    C2SMsgs.CSMessage csMessage = msg.getCsMessage();
                    sendClient(csMessage.getAddr(), csMessage.getContent());//发送新消息给所有用户
                })
                .build();
    }

    private void registerClient(String clientAddress) {
        ActorSystem system = getContext().system();
        System.err.println("通知" + JSONObject.toJSON(serverActorPathSet));
        for (String path : serverActorPathSet) {
            system.actorSelection(path).tell(new S2CMsgs.NoticeRegisterClient(clientAddress), self());
        }
    }

    private void addClient(String address) {
        clientActorPathSet.add(address);
    }

    private void sendClient(String sender, String msg) {
        ActorSystem system = getContext().system();
        for (String addr : clientActorPathSet) {
            system.actorSelection(addr).tell(new S2CMsgs.SCMessage(sender + ":" + msg), self());
        }
    }

    /**
     * 注册其它服务端节点的RoomsManagerActor
     */
    void register(Member member) {
        if (member.status().equals(MemberStatus.up())) {
            Address address = member.address();
            System.err.println("新节点加入" + address);
            String selection = getSelectionByThis(address);
            serverActorPathSet.add(selection);

        }
    }

    void remove(Member member) {
        String selection = getSelectionByThis(member.address());
        serverActorPathSet.remove(selection);
    }

    private String getSelectionByThis(Address address) {
        return address + "/user/roomManager";
    }

}
