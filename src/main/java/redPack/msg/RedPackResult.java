package redPack.msg;

/**
 * author yg
 * description
 * date 2019/1/10
 */
public class RedPackResult {
    private final long playerId;
    private final int redPackId;

    public RedPackResult(long playerId, int redPackId) {
        this.playerId = playerId;
        this.redPackId = redPackId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public int getRedPackId() {
        return redPackId;
    }
}
