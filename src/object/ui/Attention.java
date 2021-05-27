/**
 * @Author: RogerDTZ
 * @FileName: Attention.java
 */

package object.ui;

import component.animation.Animation;
import component.animation.Animator;
import graphics.Sprite;
import graphics.Text;
import main.App;
import main.AudioManager;
import object.GameObject;
import util.FontLibrary;

import java.awt.*;

public class Attention extends GameObject {

    public static final double RestPos = -30;
    public static final double OutPos = 40;
    public static final double AnimationDuration = 0.5;


    private Text text;
    private Animator amt_verticalPos;


    public Attention(String msg, Sprite sprite, double lastTime) {
        super("attention", sprite);

        this.text = new Text("attention_text", msg, FontLibrary.GetAttentionFont(25));
        this.text.setColor(new Color(212, 212, 212));
        this.resizeTo(this.text.getObjSize().x * 1.5, this.text.getObjSize().y + 14);
        this.addObject(text);

        this.amt_verticalPos = new Animator(RestPos);
        this.addComponent(this.amt_verticalPos);
        this.amt_verticalPos.append(Animation.GetTanh(RestPos, OutPos, AnimationDuration, true));
        this.amt_verticalPos.append(Animation.GetTanh(OutPos, RestPos, AnimationDuration, true, lastTime));

        this.setPosition(App.Width / 2.0, RestPos);

        AudioManager.Play("msg");
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.setPosition(App.Width / 2.0, this.amt_verticalPos.val());
    }

    public boolean isFinished() {
        return this.amt_verticalPos.isIdle();
    }

}
