/**
 * @Author: RogerDTZ
 * @FileName: MapInfo.java
 */

package datatype;

import net.sf.json.JSONObject;

import java.util.Objects;

public class MapInfo {

    public int width, height;
    public int mineNum;
    public int stepPerRound;
    public int timePerRound;
    public String map;


    public MapInfo(int width, int height, int mineNum, int stepPerRound, int timePerRound, String map) {
        this.width = width;
        this.height = height;
        this.mineNum = mineNum;
        this.stepPerRound = stepPerRound;
        this.timePerRound = timePerRound;
        this.map = map;
    }

    public MapInfo(int width, int height, int mineNum, int stepPerRound, int timePerRound) {
        this(width, height, mineNum, stepPerRound, timePerRound, null);
    }

    public MapInfo(JSONObject json) {
        this(json.getInt("width"), json.getInt("height"), json.getInt("mineNum"), json.getInt("step"),
                json.getInt("time"), json.containsKey("map") ? json.getString("map") : null);
    }

    public boolean softEquals(MapInfo rhs) {
        return this.width == rhs.width && this.height == rhs.height && this.mineNum == rhs.mineNum;
    }

    public void set(MapInfo rhs) {
        this.width = rhs.width;
        this.height = rhs.height;
        this.mineNum = rhs.mineNum;
        this.timePerRound = rhs.timePerRound;
        this.stepPerRound = rhs.stepPerRound;
        this.map = rhs.map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapInfo mapInfo = (MapInfo) o;
        return width == mapInfo.width &&
                height == mapInfo.height &&
                mineNum == mapInfo.mineNum &&
                stepPerRound == mapInfo.stepPerRound &&
                timePerRound == mapInfo.timePerRound &&
                (Objects.equals(map, mapInfo.map));
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, mineNum, stepPerRound, timePerRound);
    }

    public JSONObject toJSONObject() {
        JSONObject res = new JSONObject();
        res.put("width", this.width);
        res.put("height", this.height);
        res.put("mineNum", this.mineNum);
        res.put("step", this.stepPerRound);
        res.put("time", this.timePerRound);
        if (this.map != null)
            res.put("map", this.map);
        return res;
    }

}
