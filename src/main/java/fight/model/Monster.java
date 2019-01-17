package fight.model;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import fight.exception.FatalException;
import fight.exception.MyRuntimeException;
import fight.msg.*;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.util.Random;
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
    Random random = new Random();

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
        System.err.println(self().path().toString() + "启动");
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
                    //巡逻时小概率致命异常
                    double nextDouble = random.nextDouble();
                    if (nextDouble < 0.1) {
                        System.err.println(self().path().toString() + "抛出致命异常");
                        throw new FatalException(self().path().toString());
                    }
                    CompletionStage<Object> completionStage = toJava(ask(fighterSup, new SearchMsg(self().path().toString()), Timeout.apply(500, TimeUnit.MILLISECONDS)));
                    completionStage.handle((msg, t) -> {
                        if (msg != null) {
                            if (msg instanceof PlayerFound) {//发现敌人并攻击
                                log.debug("怪物[{}]发现敌人[{}]进入战斗状态", modelInfo, ((PlayerFound) msg).getPlayerRef());
                                context().become(fighting);
                                self().forward(msg, context());
                                beginAttack(((PlayerFound) msg).getPlayerRef());
                                return null;
                            }
                        }
                        schedulePatrol();
                        return null;
                    });
                })
                .match(Attack.class, x -> {
                    log.debug("怪物巡逻状态[{}]被[{}]攻击", modelInfo, sender().path().toString());
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
                    log.debug("怪物[{}]巡逻中接收到消息[{}]不做处理", modelInfo, x);
                })
                .build();

        fighting = ReceiveBuilder.
                match(Attack.class, x -> {
                    beAttacked(modelInfo, x);
                    log.debug("怪物[{}]被[{}]攻击", modelInfo, sender().path().toString());
                    //小概率出现运行时异常
                    double v = random.nextDouble();
                    if (v < 0.3) {
                        System.err.println(self().path().toString() + "抛出运行时异常");
                        throw new MyRuntimeException(self().path().toString());
                    }
                })
                .match(PlayerEscape.class, x -> {
                    outOfFight();
                    log.debug("玩家[{}]逃离,怪物[{}]继续巡逻", sender().path().toString(), modelInfo);
                })
                .match(Disconnected.class, x -> {
                    outOfFight();
                    log.debug("玩家[{}]掉线,怪物[{}]继续巡逻", sender().path().toString(), modelInfo);
                })
                .match(FightReport.class, x -> {
                    log.debug("玩家[{}]死亡,怪物[{}]战斗结束", sender().path().toString(), modelInfo);
                    outOfFight();
                })
                .matchAny(x -> {
                    log.debug("怪物[{}]战斗中接收到[{}]不做处理", modelInfo, x);
                })
                .build();

        reviving = ReceiveBuilder.
                match(String.class, x -> x.equals("revive"), x -> {
                    this.modelInfo = ModelInfo.valueOf(modelInfo);
                    log.debug("怪物[{}]复活", modelInfo);
                    goPatrol();
                })
                .matchAny(x -> {
                    log.debug("怪物[{}]正在复活接收到消息[{}]不做处理", modelInfo, x);
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
                actorSelection.tell(new Attack(modelInfo.getAttack()), self());
            }
        }, system.dispatcher());

    }
}
