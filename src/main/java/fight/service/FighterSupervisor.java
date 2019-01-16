package fight.service;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import fight.model.ModelInfo;
import fight.model.Monster;
import fight.model.Player;
import fight.model.State;
import fight.msg.monsterMsg.PlayerFound;
import fight.msg.monsterMsg.SearchMsg;
import fight.msg.playerMsg.GetStatus;
import fight.msg.playerMsg.LoginMsg;
import fight.msg.playerMsg.OfflineMsg;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * author yg
 * description 监督者,监督玩家和怪物
 * date 2019/1/15
 */
public class FighterSupervisor extends AbstractActor {
    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);

    //2个玩家、5只怪物
    private final int pNum = 2;
    private final int mNum = 5;

    List<ActorRef> pList = new LinkedList<>();
    List<ActorRef> mList = new LinkedList<>();

    public FighterSupervisor() {
        initData();
    }

    private void initData() {
        int id = 1;
        String thisRef = context().self().path().toString();
        for (int i = 0; i < pNum; i++) {
            ActorRef actorRef = context().actorOf(Props.create(Player.class, thisRef, getModelInfo(id)));
            id++;
            pList.add(actorRef);
        }
        for (int i = 0; i < mNum; i++) {
            ActorRef actorRef = context().actorOf(Props.create(Monster.class, thisRef, getModelInfo(id)));
            id++;
            mList.add(actorRef);
        }

        List<Object> objList = new LinkedList<>();
        objList.add(new LoginMsg());
        objList.add(new OfflineMsg());
        Random r = new Random();

        context().system().scheduler().schedule(Timeout.zero().duration(), Timeout.apply(5000, TimeUnit.MILLISECONDS).duration(), new Runnable() {
            @Override
            public void run() {
                Collections.shuffle(pList);
                for (ActorRef actorRef : pList) {
                    int i = r.nextInt(objList.size());
                    Object obj = objList.get(i);
                    log.debug("给玩家[{}]发送[{}]指令", actorRef, obj.getClass());
                    actorRef.tell(obj, ActorRef.noSender());
                }
            }
        }, context().system().dispatcher());
    }

    private Object getModelInfo(int id) {
        return new ModelInfo(id, 1000, new Random().nextInt(50), 1000);
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder
                .match(SearchMsg.class, msg -> {
                    for (ActorRef actorRef : pList) {
                        CompletionStage completionStage = toJava(ask(actorRef, new GetStatus(), Timeout.apply(500, TimeUnit.MILLISECONDS)));
                        completionStage.handle((m, t) -> {
                            if (m instanceof String) {
                                if (!m.equals(State.HANG)) {
                                    return null;
                                }
                                sender().tell(new PlayerFound(actorRef.path().toString()), self());
                            }
                            return null;
                        });
                    }
                })
                .matchAny(x -> {
                    System.err.println("wrong");
                }).build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.create("1 minute"),
                akka.japi.pf.DeciderBuilder

                        .matchAny(e -> SupervisorStrategy.escalate()).build());
    }
}
