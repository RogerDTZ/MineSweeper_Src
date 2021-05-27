/**
 * @Author: RogerDTZ
 * @FileName: OperationResult.java
 */

package datatype;

import net.sf.json.JSONObject;

public class OperationResult {

    public AnimationData animation;
    public ScoreData scores;


    public OperationResult(AnimationData animation, ScoreData scores) {
        this.animation = animation;
        this.scores = scores;
    }

    public OperationResult(JSONObject json) {
        this.animation = new AnimationData(json.getString("animations"));
        this.scores = new ScoreData(json.getString("scores"));
    }

    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject();
        res.put("animations", this.animation.toJSONObject().toString());
        res.put("scores", this.scores.toJSONObject().toString());
        return res;
    }

}
