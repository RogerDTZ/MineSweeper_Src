/**
 * @Author: Kaia
 * @FileName: MenuButton.java
 */

package object.ui.button;

import component.animation.Animation;
import datatype.Vector2;
import graphics.Sprite;
import graphics.Text;
import input.Controller;
import component.animation.Animator;
import main.AudioManager;
import object.GameObject;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class MenuButton extends BaseButton {

    public static final double PopOutDuration = 0.08;
    public static final double PopInDuration = 0.12;


    private GameObject root;
    private Text text;

    private GameObject base;
    private GameObject surface;

    private boolean hoverState;
    private Animator amt_h;
    private Animator amt_v;
    private Animator amt_a;

    private boolean clicked;


    public MenuButton(String id, Sprite sprite) {
        super(id, null);

        this.root = new GameObject(this.id + "_root");
        this.addObject(this.root);

        this.base = new GameObject(this.id + "_base", new Sprite("menu_button_white"));
        this.base.setColor(new Color(255, 255, 255, 20));
        this.base.resizeTo(sprite.getUnitSize());
        this.root.addObject(this.base);

        this.surface = new GameObject(this.id + "_surface", sprite);
        this.surface.setColor(Color.white);
        this.root.addObject(this.surface);


        this.text = new Text(this.id + "_text", "", new Font("黑体", Font.BOLD, 50));
        this.root.addObject(this.text);


        this.amt_h = new Animator(0);
        this.amt_v = new Animator(1.0);
        this.amt_a = new Animator(0);
        this.addComponent(this.amt_h);
        this.addComponent(this.amt_v);
        this.addComponent(this.amt_a);
    }

    public void setFont(Font font) {
        this.text.setFont(font);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setTextColor(Color color) {
        this.text.setColor(color);
    }

    public boolean isClicked() {
        if (this.clicked) {
            this.clicked = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (this.active) {
            if (isHovering()) {
                this.toggleHoverZoom(true);
            } else {
                this.toggleHoverZoom(false);
            }
        }
        this.root.setPosition(this.amt_h.val(), 0);
        this.setScale(this.amt_v.val(), this.amt_v.val());
        this.surface.setAlpha(this.amt_a.val());
    }

    @Override
    public AffineTransform render(Graphics2D g2d, AffineTransform parentTransform, double alpha) {
        AffineTransform at = super.render(g2d, parentTransform, alpha);
        return at;
    }

    @Override
    public boolean isHovering() {
        Vector2 size = this.base.getSprite().getUnitSize();
        Vector2 pos = Controller.getMousePos();
        try {
            pos.transform(this.getAbsoluteTransform().createInverse());
        } catch (NoninvertibleTransformException ignored) {
        }
        if (pos.x <= size.x * (-0.5 + 0.105)) {
            if ((size.x * (-0.5) <= pos.x && pos.x <= size.x * (-0.5 + 0.105)) && (pos.y >= size.y * (-0.5) && pos.y <= size.y * 0.5) && pos.y <= -((-size.y / (0.105 * size.x)) * (pos.x - (size.x * -0.5)) + size.y * 0.5)) {
                return true;
            }
        } else {
            if ((size.x * (-0.5 + 0.105) <= pos.x && pos.x <= size.x * 0.5) && (pos.y >= size.y * (-0.5) && pos.y <= size.y * 0.5)) {
                return true;
            }
        }
        return false;
    }

    public void toggleHoverZoom(boolean flag) {
        if (flag && !this.hoverState) {
            this.hoverState = true;
            this.amt_h.forceAppend(Animation.GetTanh(this.amt_h.val(), -this.base.getSprite().getUnitSize().x * 0.03, PopOutDuration, true));
            this.amt_v.forceAppend(Animation.GetTanh(this.amt_v.val(), 1.05, PopOutDuration, true));
            this.amt_a.forceAppend(Animation.GetTanh(this.amt_a.val(), 1, PopOutDuration, true));
        } else if (!flag && this.hoverState){
            this.hoverState = false;
            this.amt_h.forceAppend(Animation.GetTanh(this.amt_h.val(), 0, PopInDuration, false));
            this.amt_v.forceAppend(Animation.GetTanh(this.amt_v.val(), 1.0, PopOutDuration, false));
            this.amt_a.forceAppend(Animation.GetTanh(this.amt_a.val(), 0, PopInDuration, false));
        }
    }

    @Override
    public void resizeTo(Vector2 rect) {
        super.resizeTo(rect);
        this.base.resizeTo(rect);
        this.surface.resizeTo(rect);
    }

    @Override
    public void resizeTo(double x, double y) {
        super.resizeTo(x, y);
        this.base.resizeTo(x, y);
        this.surface.resizeTo(x, y);
    }

    @Override
    public void onClicked(int button) {
        if (button == 1 && this.isHovering()) {
            this.clicked = true;
            AudioManager.Play("click");
        }
    }

    @Override
    public void onMouseMoved(Vector2 mousePos) {

    }

    @Override
    public void onMouseWheelMoved(MouseWheelEvent e) {

    }

    public GameObject getBase() {
        return this.base;
    }

}
