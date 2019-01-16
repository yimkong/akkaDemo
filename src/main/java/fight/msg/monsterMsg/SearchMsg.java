package fight.msg.monsterMsg;

import scala.Serializable;

/**
 * author yg
 * description 搜寻处于挂机状态的敌人
 * date 2019/1/16
 */
public class SearchMsg implements Serializable {
    //怪物id
    private final int id;

    public SearchMsg(int id) {
        this.id = id;
    }
}
