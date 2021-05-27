/**
 * @Author: RogerDTZ
 * @FileName: Operation.java
 */

package datatype;

import net.sf.json.JSONObject;

public class Operation {

    public enum OperationType {
        Normal, Mark, GodPick, Rude
    }

    public int playerID;
    public int x, y;
    public OperationType type;


    public Operation(int playerID, int x, int y, OperationType type) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Operation(JSONObject json) {
        this(json.getInt("playerID"), json.getInt("x"), json.getInt("y"), getFromString(json.getString("type")));
    }

    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject();
        res.put("playerID", this.playerID);
        res.put("x", this.x);
        res.put("y", this.y);
        res.put("type", this.type);
        return res;
    }

    private static OperationType getFromString(String str) {
        switch (str) {
            case "Normal":
                return OperationType.Normal;
            case "Mark":
                return OperationType.Mark;
            case "GodPick":
                return OperationType.GodPick;
            case "Rude":
                return OperationType.Rude;
        }
        return null;
    }

}