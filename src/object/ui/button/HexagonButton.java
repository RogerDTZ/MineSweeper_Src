/**
 * @Author: Kaia
 * @FileName: HexagonButton.java
 */

package object.ui.button;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Sprite;
import input.Controller;

import java.awt.event.MouseWheelEvent;
import java.awt.geom.NoninvertibleTransformException;

public class HexagonButton extends BaseButton {

    private double size;
    private boolean hoverState;

    private Animator amt_hover;

    private double hoverZoom = 1.02;
    private double hoverOnDuration = 0.10;
    private double hoverAwayDuration = 0.10;

    private boolean isClicked;


    public HexagonButton(String id, double height, Sprite sprite) {
        super(id, sprite);
        this.size = Math.sqrt(3) / 3 * height;

        this.resizeTo(height, height);
        this.amt_hover = new Animator(1.0);
        this.addComponent(this.amt_hover);
    }

    public void update(double dt) {
        super.update(dt);

        if (!this.active) {
        } else {
            if (this.isHovering()) {
                if (!this.hoverState) {
                    this.hoverState = true;
                    this.onHoverStageChange(true);
                }
            }
            else {
                if (this.hoverState) {
                    this.hoverState = false;
                    this.onHoverStageChange(false);
                }
            }
        }
        this.setScale(this.amt_hover.val(), this.amt_hover.val());
    }

    public void onHoverStageChange(boolean flag) {
        if (flag) {
            this.toggleHoverZoom(true);
        } else {
            this.toggleHoverZoom(false);
        }
    }

    public void toggleHoverZoom(boolean flag) {
        if (flag) {
            this.amt_hover.forceAppend(Animation.GetTanh(this.amt_hover.val(), hoverZoom, hoverOnDuration, true));
        } else {
            this.amt_hover.forceAppend(Animation.GetTanh(this.amt_hover.val(), 1.0, hoverAwayDuration, true));
        }
    }

    @Override
    public boolean isHovering() {
        Vector2 pos = Controller.getMousePos();
        try {
            pos.transform(this.getAbsoluteTransform().createInverse());
        } catch (NoninvertibleTransformException ignored) {
        }
        return pos.x * pos.x + pos.y * pos.y <= this.size * this.size;
        /*
        double k = Math.sqrt(3) / 3;
        if (pos.y <= k * (pos.x + 2 * this.size) && pos.y <= k * (-1) * (pos.x - 2 * this.size)
                && pos.y >= (-1) * k * (pos.x + 2 * this.size) && pos.y >= k * (pos.x - 2 * this.size)) {
            if (pos.y <= this.size * Math.sqrt(3) / 2 && pos.y >=(-1) * this.size * Math.sqrt(3) / 2) {
                return true;
            }
        }
        return false;
         */
    }

    public boolean clicked() {
        if (this.isClicked) {
            this.isClicked = false;
            return true;
        }
        return false;
    }

    @Override
    public void onClicked(int button) {
        if (button == 1 && this.isHovering() && this.active) {
            isClicked = true;
        }
    }

    @Override
    public void onMouseMoved(Vector2 mousePos) {
    }

    @Override
    public void onMouseWheelMoved(MouseWheelEvent e) {

    }

}
