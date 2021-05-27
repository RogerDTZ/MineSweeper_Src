/**
 * @Author: RogerDTZ
 * @FileName: IconButton.java
 */

package object.ui.button;

import datatype.Vector2;
import graphics.Shape;
import graphics.Sprite;
import main.AudioManager;

import java.awt.*;

public class IconButton extends RectButton {

    private boolean isClicked;
    private Shape panel;
    private boolean touchable;


    public IconButton(String id, Vector2 size, double scalar, Sprite sprite, Shape.Type type) {
        super(size, id, sprite);

        this.panel = new Shape("panel",
                new Color(70, 70, 70),
                type, Vector2.scalar(size, scalar));

        this.panel.setRenderPriority(-1);
        this.panel.setPosition(-5, -5);
        this.addObject(this.panel);
        this.touchable = true;
    }

    public void setTouchable(boolean flag) {
        this.touchable = flag;
    }

    @Override
    public void onClicked(int button) {
        if (button == 1 && this.touchable) {
            this.isClicked = true;
            AudioManager.Play("click");
        }
    }

    public boolean clicked() {
        if (this.isClicked) {
            this.isClicked = false;
            return true;
        }
        return false;
    }

}
