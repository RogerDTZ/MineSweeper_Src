/**
 * @Author: RogerDTZ
 * @FileName: PauseMenu.java
 */

package object.ui;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Shape;
import graphics.Sprite;
import input.Controller;
import input.InputCallback;
import main.App;
import object.GameObject;
import object.ui.button.PauseButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class PauseMenu extends GameObject implements InputCallback {

    public static final Vector2 ButtonSize = new Vector2(300, 70);
    public static double ShowDuration = 0.2;


    private GameObject root;
    private Shape dark;
    private PauseButton resume;
    private PauseButton save;
    private PauseButton exit;

    private Animator amt_alpha;

    private boolean showState;
    private boolean wantToSave;
    private boolean wantToExit;


    public PauseMenu(String id) {
        super(id);

        Controller.registerCallback(this);

        this.setRenderPriority(10);

        this.root = new GameObject(this.id + "_root");
        this.root.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.addObject(this.root);

        this.initElement();

        this.amt_alpha = new Animator(0);
        this.addComponent(this.amt_alpha);
        this.root.setAlpha(0);

        this.showState = false;
        this.wantToSave = false;
        this.wantToExit = false;
    }

    private void initElement() {
        this.dark = new Shape(this.id + "_dark", Color.black, Shape.Type.Rect, App.WinSize);
        this.dark.setAlpha(0.9);
        this.dark.setRenderPriority(0);
        this.root.addObject(this.dark);

        this.resume = new PauseButton(this.id + "_resume", ButtonSize, "Resume", 30, new Sprite("rect_dark"));
        this.resume.setPosition(0, - 180);
        this.resume.setHoverZoom(1.1);
        this.resume.setTouchable(false);
        this.root.addObject(this.resume);

        this.save = new PauseButton(this.id + "_save", ButtonSize, "Save", 30, new Sprite("rect_dark"));
        this.save.setPosition(0, - 100);
        this.save.setHoverZoom(1.1);
        this.save.setTouchable(false);
        this.root.addObject(this.save);

        this.exit = new PauseButton(this.id + "_exit", ButtonSize, "Exit", 30, new Sprite("rect_dark"));
        this.exit.setPosition(0, - 20);
        this.exit.setHoverZoom(1.1);
        this.exit.setTouchable(false);
        this.root.addObject(this.exit);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.root.setAlpha(this.amt_alpha.val());

        if (this.resume.clicked()) {
            this.toggleShow(false);
        }

        if (this.save.clicked()) {
            this.wantToSave = true;
            this.toggleShow(false);
        }

        if (this.exit.clicked()) {
            this.wantToExit = true;
            this.toggleShow(false);
        }
    }


    public void toggleShow(boolean flag) {
        if (flag && !this.showState) {
            this.showState = true;
            this.amt_alpha.forceAppend(Animation.GetTanh(this.amt_alpha.val(), 1, ShowDuration, true));
            this.resume.setTouchable(true);
            this.save.setTouchable(true);
            this.exit.setTouchable(true);
        } else if (!flag && this.showState) {
            this.showState = false;
            this.amt_alpha.forceAppend(Animation.GetTanh(this.amt_alpha.val(), 0, ShowDuration, true));
            this.resume.setTouchable(false);
            this.save.setTouchable(false);
            this.exit.setTouchable(false);
        }
    }

    @Override
    public void onMousePressed(MouseEvent e) {

    }

    @Override
    public void onMouseReleased(MouseEvent e) {

    }

    @Override
    public void onMouseMoved(Vector2 mousePos) {

    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.toggleShow(!this.showState);
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {

    }

    @Override
    public void onKeyTyped(KeyEvent e) {

    }

    @Override
    public void onMouseWheelMoved(MouseWheelEvent e) {

    }

    public boolean getShowState() {
        return this.showState;
    }

    public boolean wantToSave() {
        if (this.wantToSave) {
            this.wantToSave = false;
            return true;
        }
        return false;
    }

    public boolean wantToExit() {
        return this.wantToExit;
    }

}
