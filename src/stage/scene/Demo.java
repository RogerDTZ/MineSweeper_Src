/**
 * @Author: RogerDTZ
 * @FileName: Demo.java
 */

package stage.scene;

import graphics.Sprite;
import main.App;
import object.GameObject;
import stage.GameStage;
import stage.GameStageID;

public class Demo extends GameObject implements GameStage {

    private GameObject root;
    private GameObject mine;
    private GameObject popo;
    private double totalTime;


    public Demo() {
        super("scene_demo");
    }

    @Override
    public void init() {
        this.root = new GameObject("root");
        this.addObject(this.root);
        this.mine = new GameObject("mine", new Sprite("grid_mine"));
        this.root.addObject(this.mine);
        this.popo = new GameObject("popo", new Sprite("popo"));
        this.mine.addObject(this.popo);

        this.root.setPosition(App.WinSize.x / 2, App.WinSize.y / 2);
        this.popo.resizeTo(100, 100);
        this.mine.resizeTo(200, 200);

        this.totalTime = 0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.totalTime += dt;
        this.mine.setPosition(100 * Math.cos(this.totalTime), 100 * Math.sin(this.totalTime));
        this.popo.setPosition(50 * Math.cos(this.totalTime), 50 * Math.sin(this.totalTime));
    }

    @Override
    public GameStageID getGameStageID() {
        return null;
    }

}
