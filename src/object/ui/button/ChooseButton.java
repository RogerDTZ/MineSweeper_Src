/**
 * @Author: RogerDTZ
 * @FileName: ChooseButton.java
 */

package object.ui.button;

import component.animation.Animation;
import component.animation.Animator;
import graphics.Sprite;
import main.AudioManager;
import object.GameObject;

public class ChooseButton extends GameObject {

    public final static double ActiveScale = 1.8;
    public final static double FreezeScale = 0.8;
    public final static double ToggleDuration = 0.3;


    private boolean activeState;
    private boolean freezeState;
    private GameObject root;
    private HexagonButton button;
    private GameObject cover;
    private Animator amt_scale;
    private Animator amt_cover;


    public ChooseButton(String id, double height, Sprite sprite) {
        super(id);

        this.root = new GameObject(this.id + "_root");
        this.addObject(this.root);

        this.button = new HexagonButton(this.id + "_button", height, sprite);
        this.root.addObject(this.button);

        this.cover = new GameObject(this.id + "_cover", new Sprite("hexagon_gray"));
        this.cover.resizeTo(height * 1.05, height * 1.05);
        this.cover.setRenderPriority(1);
        this.root.addObject(this.cover);

        this.freezeState = false;
        this.activeState = false;
        this.amt_scale = new Animator(1);
        this.amt_cover = new Animator(0);
        this.addComponent(this.amt_scale);
        this.addComponent(this.amt_cover);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (this.button.clicked())
            this.toggleActive(!this.activeState);
        this.root.setScale(this.amt_scale.val());
        this.cover.setAlpha(this.amt_cover.val());
    }

    public void toggleActive(boolean flag) {
        if (this.freezeState)
            return;
        if (flag && !this.activeState) {
            this.activeState = true;
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), ActiveScale, ToggleDuration, true));
            AudioManager.PlayWithVolume("activate", 0.2, 0);
        } else if (!flag && this.activeState) {
            this.activeState = false;

            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), 1, ToggleDuration, true));
        }
    }

    public void toggleFreeze(boolean flag, double delay) {
        if (flag && !this.freezeState) {
            this.freezeState = true;
            this.toggleActive(false);
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), FreezeScale, ToggleDuration, true));
            this.amt_cover.forceAppend(Animation.GetTanh(this.amt_cover.val(), 1, ToggleDuration, true));
            this.button.setActive(false);
        } else if (!flag && this.freezeState) {
            this.freezeState = false;
            this.amt_scale.append(Animation.GetLinear(FreezeScale, FreezeScale, delay));
            this.amt_scale.append(Animation.GetTanh(FreezeScale, 1, ToggleDuration, true));
            this.amt_cover.append(Animation.GetLinear(1, 1, delay));
            this.amt_cover.append(Animation.GetTanh(1, 0, ToggleDuration, true));
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(Math.round(delay * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    button.setActive(true);
                }
            };
            thread.start();
        }
    }

    public boolean isActive() {
        return this.activeState;
    }

    public void setTouchable(boolean flag) {
        this.button.setActive(flag);
    }

}
