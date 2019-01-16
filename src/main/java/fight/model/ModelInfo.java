package fight.model;

/**
 * author yg
 * description
 * date 2019/1/15
 */
public class ModelInfo {
    private int id;
    private int curBlood;
    private int attack;
    private int maxBlood;

    public ModelInfo(int id, int curBlood, int attack, int maxBlood) {
        this.id = id;
        this.curBlood = curBlood;
        this.attack = attack;
        this.maxBlood = maxBlood;
    }

    public static ModelInfo valueOf(ModelInfo model) {
        ModelInfo modelInfo = new ModelInfo(model.id, model.maxBlood, model.attack, model.maxBlood);
        return modelInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurBlood() {
        return curBlood;
    }

    public void setCurBlood(int curBlood) {
        this.curBlood = curBlood;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getMaxBlood() {
        return maxBlood;
    }

    public void setMaxBlood(int maxBlood) {
        this.maxBlood = maxBlood;
    }

    @Override
    public String toString() {
        return "ModelInfo{" +
                "id=" + id +
                ", curBlood=" + curBlood +
                ", attack=" + attack +
                ", maxBlood=" + maxBlood +
                '}';
    }
}
