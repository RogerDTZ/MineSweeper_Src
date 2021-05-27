/**
 * @Author: RogerDTZ
 * @FileName: Player.java
 */

package object;

import datatype.ScoreData;
import network.Client;
import object.GameObject;
import main.AttentionManager;

public class Player extends GameObject {

    private int playerID;
    private String playerName;
    private boolean isLocal;
    private String session_id;
    private String ip;

    private boolean isReady;
    private boolean online;

    private int order;
    private int score;
    private int mistakes;

    private int cost;


    public Player(int playerID, String playerName, String session_id, String ip, Client localClient) {
        super("player_" + playerID);
        this.playerID = playerID;
        this.playerName = playerName;
        this.isLocal = session_id.equals(localClient.getSessionID());
        this.session_id = session_id;
        this.ip = ip;
        this.isReady = false;
        this.setOnline(true);
        this.cost = 0;
    }

    public void reset() {
        this.isReady = false;
        this.cost = 0;
        this.score = 0;
        this.mistakes = 0;
    }

    public boolean isLocal() {
        return this.isLocal;
    }

    public void setReady(boolean flag) {
        this.isReady = flag;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public void setOnline(boolean flag) {
        if (flag && !this.online) {
            this.online = true;
        } else if(!flag && this.online) {
            this.online = false;
            if (!this.isLocal())
                AttentionManager.showWarnMessage(this.getPlayerName() + " has dropped from the game");
        }
    }

    public boolean isOnline() {
        return this.online;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getSessionID() {
        return this.session_id;
    }

    public String getIp() {
        return this.ip;
    }

    public void applyScoreData(ScoreData data) {
        if (data.playerID == this.playerID) {
            this.score += data.score;
            this.mistakes += data.mistake;
            this.cost = Math.max(0, this.cost - data.cost);
        }
    }

    public int getScore() {
        return this.score;
    }

    public int getMistakes() {
        return this.mistakes;
    }

    public void setOrder(int x) {
        this.order = x;
    }

    public int getOrder() {
        return this.order;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getRemainCost() {
        return this.cost;
    }

}
