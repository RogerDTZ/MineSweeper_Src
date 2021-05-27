/**
 * @Author: RogerDTZ
 * @FileName: BaseButton.java
 */

package object.ui.button;

import graphics.Sprite;
import input.Controller;
import input.InputCallback;
import object.GameObject;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public abstract class BaseButton extends GameObject implements InputCallback {

    // whether the hover component.animation will step on
    protected boolean active;


    public BaseButton(String id, Sprite sprite) {
        super(id, sprite);
        Controller.registerCallback(this);
        this.active = true;
    }

    /**
     * @return: Whether the mouse is hovering on the button. Write the shape test here.
     */
    public abstract boolean isHovering();

    public abstract void onClicked(int button);

    public void setActive(boolean flag) {
        this.active = flag;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        if (this.isHovering() && this.active) {
            // System.out.println("[RectButton] Being Clicked!");
            this.onClicked(e.getButton());
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e) {

    }

    @Override
    public void onKeyTyped(KeyEvent e) {

    }

    @Override
    public void onKeyPressed(KeyEvent e) {

    }

    @Override
    public void onKeyReleased(KeyEvent e) {

    }

}
