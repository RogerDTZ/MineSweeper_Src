/**
 * @Author: RogerDTZ
 * @FileName: PlayerBoxManager.java
 */

package object.ui.box;

import datatype.ScoreData;
import graphics.Sprite;
import object.Player;
import object.GameObject;

import java.util.ArrayList;

public class PlayerBoxManager extends GameObject {

    public static final double DisplayMargin = 30;


    private ArrayList<Player> players;
    private ArrayList<GamePlayerBox> pds;


    public PlayerBoxManager(ArrayList<Player> players, int maxStep) {
        super("playerDisplay_manager");

        this.players = players;
        this.pds = new ArrayList<>();
        for (int i = 0; i < this.players.size(); ++i) {
            GamePlayerBox pd = new GamePlayerBox(this.players.get(i), maxStep, new Sprite("player_box_" + i));
            this.pds.add(pd);
            pd.setPosition(0, GamePlayerBox.PlayerDisplayBox.y * 0.5 + i *  (GamePlayerBox.PlayerDisplayBox.y + DisplayMargin));
            this.addObject(pd);
        }
    }

    public void setActive(int order, boolean flag, double delay) {
        this.pds.get(order).toggleActive(flag, delay);
    }

    public void setCurrentCost(int order, int cost) {
        this.pds.get(order).setCurrentCost(cost);
    }

    public void setBlink(int order, boolean flag, double delay) {
        this.pds.get(order).toggleBlink(flag, delay);
    }

    public void setOnline(int order, boolean flag) {
        this.pds.get(order).setOnline(flag);
    }

    public void setOnFire(int order, boolean flag) {
        this.pds.get(order).toggleOnFire(flag);
    }

    public void client_playAnimation(ScoreData sd) {
        if (sd.cost == 0)
            return;
        for (int i = 0; i < this.players.size(); ++i) {
            if (this.players.get(i).getPlayerID() == sd.playerID) {
                this.pds.get(i).decrease(sd.cost, 0);
                this.pds.get(i).toggleBlink(true, 0);
                break;
            }
        }
    }

}
