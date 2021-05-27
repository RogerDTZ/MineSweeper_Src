/**
 * @Author: RogerDTZ
 * @FileName: FullScreenMessage.java
 */

package object.ui;

import component.animation.Animation;
import component.animation.Animator;
import graphics.Shape;
import graphics.Sprite;
import main.App;
import object.GameObject;

import java.awt.*;

public class FullScreenMessage extends GameObject {

    public final static double EnterScale = 1.3;
    public final static double ExitScale = 0.4;


    private Shape dark;
    private GameObject img;

    private Animator amt_alpha;
    private Animator amt_scale;


    public FullScreenMessage(String id, Sprite sprite) {
        super(id);

        this.setAlpha(0);
        this.amt_alpha = new Animator(0);
        this.amt_scale = new Animator(ExitScale);
        this.addComponent(this.amt_alpha);
        this.addComponent(this.amt_scale);

        this.dark = new Shape(this.id + "_dark", Color.black, Shape.Type.Rect, App.WinSize);
        this.dark.setAlpha(0.8);
        this.dark.setRenderPriority(1);
        this.dark.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.addObject(this.dark);

        this.img = new GameObject(this.id + "_img", sprite);
        this.img.setRenderPriority(2);
        this.img.setScale(ExitScale);
        this.img.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.addObject(this.img);
    }

    public void play() {
        this.amt_alpha.forceAppend(Animation.GetTanh(0, 1, 0.5, true, 0.5));
        this.amt_alpha.append(Animation.GetTanh(1, 0, 0.5, true, 1));
        this.amt_scale.forceAppend(Animation.GetTanh(EnterScale, 1, 0.5, true, 0.5));
        this.amt_scale.append(Animation.GetTanh(1, ExitScale, 0.5, true, 1));
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.setAlpha(this.amt_alpha.val());
        this.img.setScale(this.amt_scale.val());
    }


}
