/**
 * @Author: RogerDTZ
 * @FileName: FadeInTransition.java
 */

package stage.transition;

import component.animation.Animation;
import component.animation.Animator;
import graphics.Shape;
import main.App;

import java.awt.*;

public class FadeInTransition implements Transition {

    private boolean active;
    private Animator alphaAnimator;
    private Shape pc;


    public FadeInTransition(Color color, double fadeTime, double delay) {
        this.pc = new Shape("bg", color, Shape.Type.Rect, App.WinSize);
        this.pc.setAlpha(1);
        this.pc.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.alphaAnimator = new Animator(1);
        this.alphaAnimator.append(Animation.GetTanh(1, 0, fadeTime, false, delay));
        this.alphaAnimator.setActive(false);
        this.active = false;
    }

    public FadeInTransition(Color color, double fadeTime) {
        this(color, fadeTime, 0);
    }

    public FadeInTransition() {
        this(Color.black, 1.0, 0);
    }

    @Override
    public void init() {
        this.active = true;
        this.alphaAnimator.setActive(true);
    }

    @Override
    public void update(double dt) {
        if (!this.active)
            return;
        this.alphaAnimator.update(dt);
        this.pc.setAlpha(this.alphaAnimator.val());
    }

    @Override
    public void render(Graphics2D g2d) {
        if (!this.active)
            return;
        this.pc.render(g2d);
    }

    @Override
    public boolean isFinished() {
        return this.alphaAnimator.isIdle();
    }

}
