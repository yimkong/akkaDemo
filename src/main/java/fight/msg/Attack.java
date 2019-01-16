package fight.msg;

import java.io.Serializable;

/**
 * author yg
 * description
 * date 2019/1/16
 */
public class Attack implements Serializable {
    private final int damage;

    public Attack(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }
}
