/**
 * @Author: Kaia
 * @FileName: End.java
 */

package stage.scene;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Sprite;
import main.App;
import object.Player;
import network.Room;
import object.GameObject;
import stage.GameStage;
import stage.GameStageID;
import object.ui.box.BoardPlayerBox;
import object.ui.button.MenuButton;
import util.FontLibrary;
import component.Timer;
import graphics.Shape;

import java.awt.*;
import java.util.ArrayList;

public class End extends GameObject implements GameStage {

    public static final double EachTime = 0.8;
    public static final double MenuButtonWidth = 400;
    public static final double MenuButtonHeight = 60;
    public static final Vector2 MenuButtonsPivot = new Vector2(App.Width - MenuButtonWidth * 0.43, App.Height / 2.0 + 400);
    public static final double MenuButtonPopOut = 0.5;
    public static final double BarWidth=700;


    private Room room;

    private MenuButton button_return;
    private Animator amt_button_return_alpha;
    private Timer tmr_button_return_unlock;

    private ArrayList<Player> ranked_players;
    private ArrayList<Player> players;
    private GameObject displays;

    private GameObject bg;
    private Shape canvas;

    private boolean returnToMenu;


    public End(Room room) {
        super("scene_end");

        this.room = room;

        this.canvas = new Shape("canvas", Color.black, Shape.Type.Rect, App.WinSize);
        this.addObject(this.canvas);
        this.canvas.setPosition(App.Width / 2.0, App.Height / 2.0);

        this.bg = new GameObject("bg", new Sprite("game_background"));
        this.addObject(this.bg);
        this.bg.resizeTo(App.WinSize);
        this.bg.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.bg.setScale(1.3);
        this.bg.setAlpha(0.5);

        this.button_return = new MenuButton("button_return", new Sprite("menu_button_orange"));
        this.addObject(this.button_return);
        this.button_return.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_return.setPosition(MenuButtonsPivot);
        this.button_return.setAlpha(0);
        Font font = FontLibrary.GetMenuButtonFont(30);
        this.button_return.setFont(font);
        this.button_return.setText("Return to Menu");
        this.button_return.setTextColor(new Color(212, 212, 212));
        this.button_return.setActive(false);

        double appear = this.room.getPlayers().size() * EachTime + 1;
        this.amt_button_return_alpha = new Animator(0);
        this.addComponent(this.amt_button_return_alpha);
        this.amt_button_return_alpha.append(Animation.GetTanh(0, 1, MenuButtonPopOut, true, appear));

        this.tmr_button_return_unlock = new Timer(appear);
        this.addComponent(this.tmr_button_return_unlock);

        this.returnToMenu = false;

        if (this.room.isServer())
            this.room.server_announceReset();

        this.players = this.room.getPlayers();
        this.ranked_players = new ArrayList<>();
        this.rankPlayers();

        this.displays = new GameObject("displays");
        this.addObject(this.displays);
        this.displays.setPosition(App.Width * 0.5, 300);

        for(int i=0;i<players.size();i++){
            BoardPlayerBox boardPlayerBox =new BoardPlayerBox("player_" + i,ranked_players.get(i), BarWidth * (1 + 1.0 * (players.size()-i) / (players.size())),
                    1 + (players.size()-i)*EachTime, new Sprite("board_" + i));
            boardPlayerBox.setPosition(0, 0 + i * BoardPlayerBox.Height + (i - 1) * 30);
            this.displays.addObject(boardPlayerBox);
        }

    }



    public void rankPlayers(){
        this.ranked_players=players;
        this.ranked_players.sort((o1, o2) -> {
            if ((o1.getScore() > o2.getScore())||o1.getScore()==o2.getScore()&&o1.getMistakes()<o2.getMistakes()) {
                return -1;
            }
           if(o1.getScore()<o2.getScore()||o1.getScore()==o2.getScore()&&o1.getMistakes()>o2.getMistakes()){
               return 1;
           }
           return 0;
        });
    }


    @Override
    public void init() {
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.button_return.update(dt);
        this.button_return.setAlpha(this.amt_button_return_alpha.val());

        if (this.tmr_button_return_unlock.done())
            this.button_return.setActive(true);

        if (this.button_return.isClicked())
            this.returnToMenu = true;
    }

    public boolean returnToMenu() {
        if (this.returnToMenu) {
            this.returnToMenu = false;
            return true;
        }
        return false;
    }

    public Room getRoom() {
        return this.room;
    }

    @Override
    public GameStageID getGameStageID() {
        return GameStageID.End;
    }

}
