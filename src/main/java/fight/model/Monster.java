package fight.model;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import fight.msg.Attack;
import fight.msg.Disconnected;
import fight.msg.FightReport;
import fight.msg.monsterMsg.AttackPlayer;
import fight.msg.monsterMsg.Patrolling;
import fight.msg.monsterMsg.PlayerFound;
import fight.msg.monsterMsg.SearchMsg;
import fight.msg.playerMsg.PlayerEscape;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * author yg
 * description 怪物,热切换
 * date 2019/1/15
 */
public class Monster extends AbstractActor {
    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
    public static final long reviveTime = 3000;
    public static final int attackInterval = 1000;

    //巡逻
    private PartialFunction<Object, BoxedUnit> patrol;
    //战斗
    private PartialFunction<Object, BoxedUnit> fighting;
    //复活
    private PartialFunction<Object, BoxedUnit> reviving;

    private ModelInfo modelInfo;
    private ActorSelection fighterSup;

    private Cancellable fightScheduler;

    @Override
    public void preStart() {//开启调度器
        schedulePatrol();
    }

    //通过上层监督者寻找攻击对象
    private void schedulePatrol() {
        ActorRef self = self();
        ActorSystem system = context().system();
        system.scheduler().scheduleOnce(Duration.apply(1000, TimeUnit.MILLISECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        self.tell(new Patrolling(), ActorRef.noSender());
                    }
                }, system.dispatcher());
    }

    public Monster(String dbPath, ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
        fighterSup = context().actorSelection(dbPath);

        patrol = ReceiveBuilder.
                match(Patrolling.class, x -> {
                    log.debug("怪物[{}]巡逻中", modelInfo);
                    CompletionStage<Object> completionStage = toJava(ask(fighterSup, new SearchMsg(modelInfo.getId()), Timeout.apply(500, TimeUnit.MILLISECONDS)));
                    completionStage.handle((msg, t) -> {
                        if (msg != null) {
                            if (msg instanceof PlayerFound) {//发现敌人并攻击
                                log.debug("怪物发现敌人[{}]进入战斗状态", modelInfo);
                                context().become(fighting);
                                self().forward(msg, context());
                                beginAttack(((PlayerFound) msg).getPlayerRef());
                            } else {
                                schedulePatrol();
                            }
                        } else {
                            schedulePatrol();
                        }
                        return null;
                    });
                })
                .match(Attack.class, x -> {
                    log.debug("怪物巡逻状态[{}]被攻击", modelInfo);
                    context().become(fighting);
                    boolean ifDead = beAttacked(modelInfo, x);
                    if (ifDead) {
                        log.debug("怪物[{}]死亡", modelInfo);
                        context().become(reviving);
                        sender().tell(new FightReport(), self());
                        reviveSchedule();
                    } else {//开启攻击模式
                        self().forward(x, context());
                        beginAttack(sender().path().toString());
                    }
                })
                .matchAny(x -> {
                    log.debug("怪物[{}]巡逻中接收到未处理消息[{}]", modelInfo, x);
                })
                .build();

        fighting = ReceiveBuilder.
                match(Attack.class, x -> {
                    beAttacked(modelInfo, x);
                    log.debug("怪物[{}]被攻击", modelInfo);
                })
                .match(PlayerEscape.class, x -> {
                    outOfFight();
                    log.debug("玩家逃离,怪物[{}]继续巡逻", modelInfo);
                })
                .match(Disconnected.class, x -> {
                    outOfFight();
                    log.debug("玩家掉线,怪物[{}]继续巡逻", modelInfo);
                })
                .match(PlayerFound.class, x -> {
                    log.debug("怪物[{}]战斗中发现敌人[{}]", modelInfo, x.getPlayerRef());
                })
                .matchAny(x -> {
                    log.debug("怪物[{}]战斗中接收到未处理消息[{}]", modelInfo, x);
                })
                .build();

        reviving = ReceiveBuilder.
                match(String.class, x -> x.equals("revive"), x -> {
                    this.modelInfo = ModelInfo.valueOf(modelInfo);
                    log.debug("怪物[{}]复活", modelInfo);
                    goPatrol();
                })
                .matchAny(x -> {
                    log.debug("怪物[{}]正在复活接收到未处理消息[{}]", modelInfo, x);
                })
                .build();
        receive(patrol);
    }

    private void outOfFight() {
        this.modelInfo = ModelInfo.valueOf(modelInfo);
        fightScheduler.cancel();
        goPatrol();
    }

    //脱离战斗状态继续巡逻
    private void goPatrol() {
        context().become(patrol);
        schedulePatrol();
    }

    private void reviveSchedule() {
        context().system().scheduler().scheduleOnce(Timeout.apply(reviveTime, TimeUnit.MILLISECONDS).duration(),
                self(), "revive", context().system().dispatcher(), ActorRef.noSender());
    }

    //返回是否死亡
    public static boolean beAttacked(ModelInfo modelInfo, Attack x) {
        int damage = x.getDamage();
        int curBlood = modelInfo.getCurBlood() - damage;
        modelInfo.setCurBlood(curBlood);
        return curBlood <= 0;
    }

    private void beginAttack(String playerRef) {
        ActorSystem system = context().system();
        ActorSelection actorSelection = context().actorSelection(playerRef);
        fightScheduler = system.scheduler().schedule(Timeout.zero().duration(), Timeout.apply(attackInterval, TimeUnit.MILLISECONDS).duration(), new Runnable() {
            @Override
            public void run() {
                actorSelection.tell(new AttackPlayer(modelInfo.getAttack()), self());
            }
        }, system.dispatcher());

    }
}
