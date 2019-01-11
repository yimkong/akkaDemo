package redPack.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.alibaba.fastjson.JSONObject;
import redPack.msg.RedPackResult;

import java.util.*;

/**
 * author yg
 * description
 * date 2019/1/10
 */
public class Checker extends AbstractActor {
    private List<Integer> list = new ArrayList<>(); //抢到的红包

    private Map<Long, List<Integer>> record = new HashMap<>();

    private Set<String> threadNames = new HashSet<>();//有几个线程执行actor

    public Checker() {
        receive(ReceiveBuilder.match(RedPackResult.class, msg -> {
            long playerId = msg.getPlayerId();
            int id = msg.getRedPackId();
            if (list.contains(id)) {
                throw new Exception("并发异常");
            }
            record(playerId, id);
        }).build());
    }

    private void record(long playerId, int id) {
        list.add(id);
        List<Integer> integers = record.computeIfAbsent(playerId, x -> new ArrayList<>());
        integers.add(id);
        threadNames.add(Thread.currentThread().getName());
    }

    @Override
    public void postStop() throws Exception {
        int total = 0;
        for (Map.Entry<Long, List<Integer>> longListEntry : record.entrySet()) {
            Long key = longListEntry.getKey();
            int size = longListEntry.getValue().size();
            System.err.println("玩家" + key + "抢到红包" + size + "个");
            total += size;
        }
        System.err.println("总共" + total + "个红包");
        System.err.println("总共" + threadNames.size() + "条线程:" + JSONObject.toJSON(threadNames));
        super.postStop();
    }

}
