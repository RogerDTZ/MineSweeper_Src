/**
 * @Author: RogerDTZ
 * @FileName: FullScreenInput.java
 */

package object.ui.input;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Shape;
import graphics.Sprite;
import input.InputCallback;
import main.App;
import main.AttentionManager;
import graphics.Text;
import main.AudioManager;
import object.ui.button.RectButton;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class FullScreenInput extends RectButton implements InputCallback {

    public static final double AppearScale = 1.5;
    public static final double ShowDuration = 0.2;
    public static final double BackgroundAlpha = 0.8;
    public static final double BackgroundAnimationDuration = 0.2;


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

    private boolean darkWhenFocused;
    private Shape darkBg;
    private Animator amt_bg_alpha;

    private boolean autoToggleShow;
    private boolean showState;
    private Animator amt_alpha;
    private Animator amt_scale;

    private boolean[] legalChar;


    public FullScreenInput(String id, Sprite sprite, Vector2 size, int lengthMin, int lengthLimit, Font textFont, Font defaultTextFont, String hint, String charSet) {
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

        this.darkWhenFocused = false;
        this.darkBg = new Shape(this.id + "_darkBg", Color.black, Shape.Type.Rect, App.WinSize);
        this.darkBg.setVisible(false);
        this.darkBg.setRenderPriority(-1.0);
        this.darkBg.setAbsoluteTransform(true);
        this.darkBg.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.addObject(this.darkBg);
        this.amt_bg_alpha = new Animator(0);
        this.addComponent(this.amt_bg_alpha);

        this.setHoverZoom(1.005);

        this.autoToggleShow = false;
        this.showState = true;
    }

    public FullScreenInput(String id, Sprite sprite, Vector2 size, int lengthMin, int lengthLimit, Font textFont, Font defaultTextFont, String hint) {
        this(id, sprite, size, lengthMin, lengthLimit, textFont, defaultTextFont, hint, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-");
    }

    private void initCharSet(String charSet) {
        this.legalChar = new boolean[512];
        for (char c : charSet.toCharArray())
            this.legalChar[c] = true;
    }

    public void setDarkWhenFocus(boolean flag) {
        if (!this.darkWhenFocused && flag) {
            this.darkWhenFocused = true;
            this.darkBg.setVisible(true);
        } else if (this.darkWhenFocused && !flag) {
            this.darkWhenFocused = false;
            this.darkBg.setVisible(false);
        }
    }

    public void setAutoToggleShow(boolean flag) {
        if (!this.autoToggleShow && flag) {
            this.autoToggleShow = true;
            this.amt_alpha = new Animator(0);
            this.amt_scale = new Animator(AppearScale);
            this.addComponent(this.amt_alpha);
            this.addComponent(this.amt_scale);
        } else if (this.autoToggleShow && !flag) {
            this.autoToggleShow = false;
            this.amt_alpha.destroy();
            this.amt_scale.destroy();
            this.amt_alpha = null;
            this.amt_scale = null;
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

    private void toggleFocus(boolean flag) {
        if (flag && !this.isFocus) {
            this.isFocus = true;
            this.setActive(false);
            if (this.darkWhenFocused)
                this.toggleDark(true);
            if (this.autoToggleShow)
                this.toggleShow(true);
        } else if (!flag && this.isFocus){
            this.isFocus = false;
            this.setActive(true);
            if (this.darkWhenFocused)
                this.toggleDark(false);
            if (this.autoToggleShow)
                this.toggleShow(false);
        }
    }

    private void toggleDark(boolean flag) {
        if (!this.darkWhenFocused)
            return;
        if (flag) {
            this.amt_bg_alpha.forceAppend(Animation.GetTanh(this.amt_bg_alpha.val(), BackgroundAlpha, BackgroundAnimationDuration, true));
        } else {
            this.amt_bg_alpha.forceAppend(Animation.GetTanh(this.amt_bg_alpha.val(), 0, BackgroundAnimationDuration, false));
        }
    }

    public void toggleShow(boolean flag) {
        if (!this.autoToggleShow)
            return;
        if (flag && !this.showState) {
            this.showState = true;
            this.setActive(true);
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), 1, ShowDuration * 1.5, true));
            this.amt_alpha.forceAppend(Animation.GetTanh(this.amt_alpha.val(), 1, ShowDuration, false));
        } else if (!flag && this.showState) {
            this.showState = false;
            this.setActive(false);
            this.amt_scale.forceAppend(Animation.GetTanh(this.amt_scale.val(), AppearScale, ShowDuration * 1.5, false));
            this.amt_alpha.forceAppend(Animation.GetTanh(this.amt_alpha.val(), 0, ShowDuration * 1.5, true));
        }
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
        if (this.darkWhenFocused) {
            this.darkBg.setAlpha(this.amt_bg_alpha.val());
        }
        if (this.autoToggleShow) {
            this.setScale(this.amt_scale.val());
            this.setAlpha(this.amt_alpha.val());
        }
    }

    @Override
    public void onClicked(int button) {
        if (button == 1) {
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
        }
    }

    @Override
    public void onKeyTyped(KeyEvent e) {
        super.onKeyPressed(e);
        if (this.isFocus) {
            if ((int)e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                if (this.sb.length() > 0)
                    this.sb.deleteCharAt(this.sb.length() - 1);
            } else if ((int)e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                this.toggleFocus(false);
            } else if ((int)e.getKeyChar() == KeyEvent.VK_ENTER) {
                AudioManager.PlayWithVolume("type", 0.5, 0);
                if (this.sb.length() >= this.lengthMin) {
                    this.submitted = true;
                    this.toggleFocus(false);
                } else {
                    AttentionManager.showWarnMessage("Too short!");
                }
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

}
