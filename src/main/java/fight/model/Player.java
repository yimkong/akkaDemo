package fight.model;

import akka.actor.AbstractFSM;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import fight.msg.Attack;
import fight.msg.Disconnected;
import fight.msg.FightReport;
import fight.msg.playerMsg.*;

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

    public Player(String dbPath, ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
        fighterSup = context().actorSelection(dbPath);
    }

    {
        startWith(DISCONNECTED, new EventQueue());
        when(DISCONNECTED,
                matchEvent(LoginMsg.class, (msg, container) -> {
                    this.modelInfo = ModelInfo.valueOf(modelInfo);
                    log.debug("玩家[{}]登录成功!", modelInfo);
                    return goTo(HANG);
                })
                        .event(Attack.class, (msg, container) -> {
                            log.debug("玩家[{}]离线被攻击!", modelInfo);
                            sender().tell(new Disconnected(), self());
                            return stay();
                        })
                        .event(GetStatus.class, (x, t) -> {
                            sender().tell(stateName().toString(), self());
                            return stay();
                        })
                        .event(OfflineMsg.class, (msg, container) -> stay())
        );
        when(HANG, matchEvent(OfflineMsg.class, (msg, container) -> {
                    log.debug("玩家[{}]从挂机离线!", modelInfo);
                    return goTo(DISCONNECTED);
                })
                        .event(Attack.class, (msg, container) -> {
                            log.debug("玩家[{}]从挂机进入战斗!", modelInfo);
                            boolean ifDead = beAttackAndFightBack(modelInfo, msg);
                            if (ifDead) {
                                sender().tell(new FightReport(), self());
                                return stay();
                            }
                            return goTo(FIGHTING);
                        })
                        .event(GetStatus.class, (x, t) -> {
                            sender().tell(stateName().toString(), self());
                            return stay();
                        })
        );
        when(FIGHTING, matchEvent(OfflineMsg.class, (msg, container) -> {
                    log.debug("玩家[{}]从战斗离线!", modelInfo);
                    return goTo(DISCONNECTED);
                })
                        .event(Attack.class, (msg, container) -> {
                            log.debug("玩家[{}]被攻击自动反击!", modelInfo);
                            boolean ifDead = beAttackAndFightBack(modelInfo, msg);
                            if (ifDead) {
                                sender().tell(new FightReport(), self());
                                this.modelInfo = ModelInfo.valueOf(modelInfo);
                                return stay();
                            }
                            //设置小概率逃跑
                            Random random = new Random();
                            double i = random.nextDouble();
                            if (i < 0.2) {
                                sender().tell(new PlayerEscape(), self());
                                goTo(HANG);
                            }
                            return stay();
                        })
                        .event(FightReport.class, (msg, container) -> {
                            log.debug("玩家[{}]结束战斗!", modelInfo);
                            this.modelInfo = ModelInfo.valueOf(modelInfo);
                            return goTo(HANG);
                        })
                        .event(GetStatus.class, (x, t) -> {
                            sender().tell(stateName().toString(), self());
                            return stay();
                        })
        );

        initialize();
    }

    private boolean beAttackAndFightBack(ModelInfo modelInfo, Attack msg) {
        boolean ifDead = Monster.beAttacked(modelInfo, msg);
        if (ifDead) {
            log.debug("玩家[{}]死亡", modelInfo);
            return true;
        }
        sender().tell(new Attack(modelInfo.getAttack()), self());
        return false;
    }
}
