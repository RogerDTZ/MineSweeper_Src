/**
 * @Author: RogerDTZ
 * @FileName: RectButton.java
 */

package object.ui.button;

import component.animation.Animation;
import datatype.Vector2;
import graphics.Sprite;
import input.Controller;
import component.animation.Animator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class RectButton extends BaseButton {

    private Vector2 rect;

    private boolean hoverState;

    private Animator amt_scale;

    private double hoverZoom = 1.01;
    private double hoverOnDuration = 0.08;
    private double hoverAwayDuration = 0.04;


    public RectButton(Vector2 rect, String id, Sprite sprite) {
        super(id, sprite);
        this.rect = rect;
        this.resizeTo(this.rect.x, this.rect.y);
        this.active = true;
        this.amt_scale = new Animator(1.0);
        this.addComponent(this.amt_scale);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (!this.active) {
        } else {
            if (this.isHovering()) {
                if (!this.hoverState) {
                    this.hoverState = true;
                    this.onHoverStageChange(true);
                }
            } else {
                if (this.hoverState) {
                    this.hoverState = false;
                    this.onHoverStageChange(false);
                }
            }
        }
        this.setScale(this.amt_scale.val(), this.amt_scale.val());
    }

    @Override
    public AffineTransform render(Graphics2D g2d, AffineTransform parentTransform, double alpha) {
        AffineTransform at = super.render(g2d, parentTransform, alpha);
        return at;
    }

    @Override
    public boolean isHovering() {
        Vector2 pos = Controller.getMousePos();

        try {
            pos.transform(this.getAbsoluteTransform().createInverse());
        } catch (NoninvertibleTransformException ignored) {
        }
        Vector2 rect = new Vector2(this.rect.x * this.transform.scale.x, this.rect.y * this.transform.scale.y);
        return (- rect.x / 2 <= pos.x && pos.x <= rect.x / 2) && (- rect.y / 2 <= pos.y && pos.y <= rect.y / 2);
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
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), hoverZoom, hoverOnDuration, true));
        }
        else {
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), 1.0, hoverAwayDuration, true));
        }
    }

    public void setHoverZoom(double hoverZoom) {
        this.hoverZoom = hoverZoom;
    }

    public void setHoverDuration(double on, double away) {
        this.hoverOnDuration = on;
        this.hoverAwayDuration = away;
    }

    @Override
    public void onClicked(int button) {
    }

    @Override
    public void onMouseMoved(Vector2 mousePos) {
    }

    @Override
    public void onKeyTyped(KeyEvent e) {

    }

    @Override
    public void onMouseWheelMoved(MouseWheelEvent e) {

    }
}
