/**
 * @Author: RogerDTZ
 * @FileName: InputBox.java
 */

package object.ui.input;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Sprite;
import input.InputCallback;
import main.AttentionManager;
import graphics.Text;
import main.AudioManager;
import object.ui.button.RectButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class InputBox extends RectButton implements InputCallback {

    public static final double FocusDuration = 0.3;
    public static final double FocusScale = 1.1;


    private Font textFont;
    private Font defaultTextFont;
    private Color textColor;
    private Color defaultTextColor;
    private String hint;

    private boolean isFocus;
    private StringBuilder sb;
    private Text text;
    private boolean submitted;
    private int lengthMin;
    private int lengthLimit;

    private Animator amt_scale;

    private int min;
    private int max;

    private boolean[] legalChar;

    private boolean lock;

    private boolean silent;

    private boolean touchable;


    public InputBox(String id, Sprite sprite, Vector2 size, int lengthMin, int lengthLimit, Font textFont, Font defaultTextFont, String hint, String charSet) {
        super(size, id, sprite);
        this.isFocus = false;
        this.sb = new StringBuilder();

        this.textFont = textFont;
        this.defaultTextFont = defaultTextFont;
        this.text = new Text(this.id + "_text", "", defaultTextFont);
        this.hint = hint;
        this.text.setText(hint);
        this.initCharSet(charSet);
        this.setTextColor(Color.white);
        this.addObject(text);

        this.lengthMin = lengthMin;
        this.lengthLimit = lengthLimit;

        this.amt_scale = new Animator(1.0);
        this.addComponent(this.amt_scale);

        this.setHoverZoom(1.01);

        this.min = 0;
        this.max = Integer.MAX_VALUE;

        this.lock = false;
        this.silent = false;
        this.touchable = true;
    }

    public InputBox(String id, Sprite sprite, Vector2 size, int lengthMin, int lengthLimit, Font textFont, Font defaultTextFont, String hint) {
        this(id, sprite, size, lengthMin, lengthLimit, textFont, defaultTextFont, hint, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-");
    }

    public void setSilent(boolean flag) {
        this.silent = flag;
    }

    public void setRange(int min, int max) {
        this.setRange(min, max, true);
    }

    public void setRange(int min, int max, boolean apply) {
        this.min = min;
        this.max = max;
        if (apply)
            this.clamp();
    }

    private void initCharSet(String charSet) {
        this.legalChar = new boolean[512];
        for (char c : charSet.toCharArray())
            this.legalChar[c] = true;
    }

    private void clamp() {
        try {
            int x = Integer.parseInt(this.sb.toString());
            int y = x;
            if (y < this.min) {
                y = this.min;
                if (!this.silent)
                    AttentionManager.showWarnMessage("Should be at least " + this.min + "!");
            }
            if (y > this.max) {
                y = this.max;
                if (!this.silent)
                    AttentionManager.showWarnMessage("Should be at most " + this.max + "!");
            }
            if (y != x) {
                this.clear();
                this.sb.append(y);
                this.submit();
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setDefaultTextColor(Color color) {
        this.defaultTextColor = color;
    }

    public void clear() {
        this.sb.delete(0, this.sb.length());
    }

    public String getResult() {
        if (this.submitted) {
            this.submitted = false;
            return this.sb.toString();
        }
        return null;
    }

    public boolean isFocus() {
        return this.isFocus;
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (this.sb.length() > 0) {
            this.text.setFont(this.textFont);
            this.text.setColor(this.textColor);
            this.text.setText(this.sb.toString());
        } else {
            this.text.setFont(this.defaultTextFont);
            this.text.setColor(this.defaultTextColor);
            this.text.setText(this.hint);
        }

        this.setScale(this.amt_scale.val());
    }

    public void toggleFocus(boolean flag) {
        if (this.lock && flag) {
            AttentionManager.showWarnMessage("Sorry, you cannot edit this item");
            return;
        }
        if (flag && !this.isFocus) {
            this.isFocus = true;
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), FocusScale, FocusDuration, true));
        } else if (!flag && this.isFocus) {
            this.isFocus = false;
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), 1, FocusDuration, true));
        }
    }

    @Override
    public void onClicked(int button) {
        if (button == 1 && this.touchable) {
            this.toggleFocus(true);
        }
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        if (this.isHovering() && this.active) {
            this.onClicked(e.getButton());
        }
        if (!this.isHovering()) {
            this.toggleFocus(false);
            this.submit();
        }
    }

    public void submit() {
        this.clamp();
        if (this.sb.length() >= this.lengthMin && this.sb.length() <= this.lengthLimit) {
            this.submitted = true;
            this.toggleFocus(false);
        } else {
            AttentionManager.showWarnMessage("Too short!");
        }
    }

    @Override
    public void onKeyTyped(KeyEvent e) {
        super.onKeyPressed(e);
        if (this.isFocus) {
            if ((int)e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                if (this.sb.length() > 0)
                    this.sb.deleteCharAt(this.sb.length() - 1);
            } else if ((int)e.getKeyChar() == KeyEvent.VK_ENTER) {
                AudioManager.PlayWithVolume("type", 0.5, 0);
                this.submit();
            } else {
                AudioManager.PlayWithVolume("type", 0.5, 0);
                if (this.sb.length() < this.lengthLimit && this.legalChar[e.getKeyChar()])
                    this.sb.append(e.getKeyChar());
            }
        }
    }

    @Override
    public void onMouseWheelMoved(MouseWheelEvent e) {

    }

    public void setInput(String str) {
        this.clear();
        this.sb.append(str);
    }

    public void lock() {
        this.lock = true;
    }

    public void setTouchable(boolean flag) {
        this.touchable = flag;
    }


}
