package chat.remoteServer;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import chat.msg.MsgData;
import chat.msg.SimpleMsg;

import java.util.LinkedList;
import java.util.List;

import static akka.cluster.ClusterEvent.initialStateAsEvents;

/**
 * author yg
 * description
 * date 2019/1/11
 */
public class RoomsManager extends AbstractActor {
    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
    Cluster cluster = Cluster.get(getContext().system());
    List<MsgData> msgDataList = new LinkedList<>();
//    List<String> clientAddressList = new LinkedList<>();

    @Override
    public void preStart() {
        cluster.subscribe(self(), initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    public RoomsManager() {
        receive(ReceiveBuilder
                .match(ClusterEvent.MemberEvent.class, msg -> { //新节点加入
                    log.info("MemberEvent: {}", msg);
                    register(msg.member());
                })
                .match(ClusterEvent.UnreachableMember.class, msg -> {//节点不可用
                    log.info("UnreachableMember: {}", msg);
                    remove(msg.member());
                })
                .match(MsgData.class, msg -> {
                    msgDataList.add(msg);
                })
//                .match(SimpleMsg.RegisterMsg.class, msg -> {
//                    ActorPath path = sender().path();
//                    sendToAll(path + "加入聊天室");
//                    clientAddressList.add(path.toString());
//                })
                .match(SimpleMsg.RequestMsg.class, msg -> {
                    //TODO 向其它节点查询数据
                    sender().tell(msgDataList, self());
                })
                .build());
    }

//    private void sendToAll(String str) {
//        for (String clientAdr : clientAddressList) {
//            context().actorSelection(clientAdr).tell(new SCMessage(str), self());
//        }
//    }

    List<ActorSelection> serverActorSelection = new LinkedList<>();

    /**
     * 注册其它服务端节点的RoomsManagerActor
     */
    void register(Member member) {
        if (member.status().equals(MemberStatus.up())) {
            ActorSelection selection = getSelection(member.address());
            serverActorSelection.add(selection);

        }
    }

    void remove(Member member) {
        ActorSelection selection = getSelection(member.address());
        serverActorSelection.remove(selection);
    }

    private ActorSelection getSelection(Address address) {
        ActorSelection actorSelection = getContext().actorSelection(address + "/user/roomManager");
        return actorSelection;
    }

}
