package SimpleTest.fsm;

import akka.actor.AbstractFSM;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import fight.model.ModelInfo;
import fight.model.Monster;
import fight.model.State;
import fight.msg.*;

import java.util.LinkedList;
import java.util.Random;

import static fight.model.State.*;

class EventQueue extends LinkedList<PlayerCommand> {
}

/**
 * author yg
 * description 玩家actor,FSM有限自动机
 * date 2019/1/15
 */
public class Player extends AbstractFSM<State, EventQueue> {
    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private ActorSelection fighterSup;
    private ModelInfo modelInfo;

    public Player() {
    }

    {
        startWith(DISCONNECTED, new EventQueue());
        when(DISCONNECTED,
                matchEvent(LoginMsg.class, (msg, container) -> {
                    container.add(msg);
                    int size = container.size();
                    log.debug("[{}]条信息!", size);
                    if (size >= 20) {
                        container.removeAll(container);
                        container = new EventQueue();
                    }
                    return stay();
                })
        );
        when(HANG, matchEvent(OfflineMsg.class, (msg, container) -> {
                    log.debug("玩家[{}]从挂机离线!", modelInfo);
                    return goTo(DISCONNECTED);
                })
        );
        when(FIGHTING, matchEvent(OfflineMsg.class, (msg, container) -> {
                    log.debug("玩家[{}]从战斗离线!", modelInfo);
                    return goTo(DISCONNECTED);
                })
        );

        initialize();
    }

}
