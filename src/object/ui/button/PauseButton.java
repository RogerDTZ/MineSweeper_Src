/**
 * @Author: RogerDTZ
 * @FileName: PauseButton.java
 */

package object.ui.button;

import datatype.Vector2;
import graphics.Sprite;
import graphics.Text;
import main.AudioManager;
import util.FontLibrary;

import java.awt.*;

public class PauseButton extends RectButton {

    private Text text;
    private boolean isClicked;


    public PauseButton(String id, Vector2 size, String text, int textFont, Sprite sprite) {
        super(size, id, sprite);

        this.text = new Text(this.id + "_text", text, FontLibrary.GetPauseMenuButtonFont(textFont));
        this.text.setColor(new Color(212, 212, 212));
        this.addObject(this.text);
    }

    public void setTouchable(boolean flag) {
        this.setActive(flag);
    }


    @Override
    public void onClicked(int button) {
        if (button == 1 && this.isHovering() && this.active) {
            isClicked = true;
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
