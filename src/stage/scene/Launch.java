/**
 * @Author: RogerDTZ
 * @FileName: Launch.java
 */

package stage.scene;

import component.animation.Animation;
import datatype.Vector2;
import graphics.Sprite;
import main.App;
import object.GameObject;
import stage.GameStage;
import stage.GameStageID;
import component.animation.Animator;

public class Launch extends GameObject implements GameStage {

    private GameObject background;
    private Animator amt_background_scale;

    private double totalTime;


    public Launch() {
        super("scene_launch");
    }

    @Override
    public void init() {
        this.background = new GameObject("launch_background", new Sprite("launch_background"));
        this.addObject(this.background);
        this.background.resizeTo(App.Width, App.Height);
        this.background.setPosition(App.Width / 2.0, App.Height / 2.0);

        this.amt_background_scale = new Animator(1.0);
        this.addComponent(this.amt_background_scale);
        this.amt_background_scale.append(Animation.GetLinear(1.0, 1.10, 30));
        this.totalTime = 0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.totalTime += dt;
        double scale = this.amt_background_scale.val();
        this.background.setScale(new Vector2(scale, scale));
    }

    @Override
    public GameStageID getGameStageID() {
        return GameStageID.Launch;
    }

    public double getTotalTime() {
        return this.totalTime;
    }

}
