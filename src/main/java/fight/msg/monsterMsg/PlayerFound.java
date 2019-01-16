package fight.msg.monsterMsg;

import scala.Serializable;

/**
 * author yg
 * description
 * date 2019/1/16
 */
public class PlayerFound implements Serializable {
    private final String playerRef;

    public PlayerFound(String playerRef) {
        this.playerRef = playerRef;
    }

    public String getPlayerRef() {
        return playerRef;
    }
}
