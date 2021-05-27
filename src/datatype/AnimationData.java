/**
 * @Author: RogerDTZ
 * @FileName: AnimationData.java
 */

package datatype;

import net.sf.json.JSONObject;

public class AnimationData {

    public int x;
    public int y;
    public double delay;
    public String info;


    public AnimationData(int x, int y, double delay, String info) {
        this.x = x;
        this.y = y;
        this.delay = delay;
        this.info = info;
    }

    public AnimationData(JSONObject json) {
        this(json.getInt("x"), json.getInt("y"), json.getDouble("delay"), json.getString("info"));
    }

    public AnimationData(String str) {
        this(JSONObject.fromObject(str));
    }

    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject();
        res.put("x", this.x);
        res.put("y", this.y);
        res.put("info", this.info);
        res.put("delay", this.delay);
        return res;
    }

}
