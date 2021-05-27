/**
 * @Author: RogerDTZ
 * @FileName: ScoreData.java
 */

package datatype;

import net.sf.json.JSONObject;

public class ScoreData {

    public int playerID;
    public int score;
    public int mistake;
    public int cost;


    public ScoreData(int playerID, int score, int mistake, int cost) {
        this.playerID = playerID;
        this.score = score;
        this.mistake = mistake;
        this.cost = cost;
    }

    public ScoreData(JSONObject json) {
        this(json.getInt("playerID"), json.getInt("score"), json.getInt("mistake"), json.getInt("cost"));
    }

    public ScoreData(String str) {
        this(JSONObject.fromObject(str));
    }

    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject();
        res.put("playerID", this.playerID);
        res.put("score", this.score);
        res.put("mistake", this.mistake);
        res.put("cost", this.cost);
        return res;
    }

    public double getAnimTime() {
        if (this.score < 0) // mine;
            return 1;
        else if (this.mistake > 0)
            return 0.6;
        return 0;
    }

}
