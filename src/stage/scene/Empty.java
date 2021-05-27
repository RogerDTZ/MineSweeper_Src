/**
 * @Author: RogerDTZ
 * @FileName: Empty.java
 */

package stage.scene;

import main.App;
import object.GameObject;
import stage.GameStage;
import stage.GameStageID;
import graphics.Shape;

import java.awt.*;

public class Empty extends GameObject implements GameStage {

    private double totalTime;
    private Shape dark;


    public Empty() {
        super("scene_empty");

        this.dark = new Shape(this.id + "_dark", Color.black, Shape.Type.Rect, App.WinSize);
        this.addObject(this.dark);
        this.dark.setPosition(App.WinSize.x / 2, App.WinSize.y / 2);
    }

    @Override
    public void init() {
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.totalTime += dt;
    }

    @Override
    public GameStageID getGameStageID() {
        return GameStageID.Empty;
    }

    public double getTotalTime() {
        return this.totalTime;
    }

}
