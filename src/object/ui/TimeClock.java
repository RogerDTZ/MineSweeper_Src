/**
 * @Author: Kaia
 * @FileName: TimeClock.java
 */

package object.ui;

import datatype.Vector2;
import graphics.Sprite;
import main.AudioManager;
import object.GameObject;
import graphics.Shape;

import java.awt.*;

public class TimeClock extends GameObject {

    public static final Vector2 PanelSize = new Vector2(280, 66);
    public static final Vector2 ClockSize = new Vector2(110, 110);
    public static final Vector2 DigitSize = new Vector2(27, 50);


    private GameObject root;

    private GameObject panel;
    private GameObject clock;
    private Shape dot_1;
    private Shape dot_2;
    private GameObject min_1;
    private GameObject min_2;
    private GameObject sec_1;
    private GameObject sec_2;
    private boolean ifEnd=false;

    private double time;
    private double delay;
    private double delaySetTime;

    private int lastSec2;

    private boolean mute;

    private boolean active;


    public TimeClock(String id, double time) {
        super(id);

        this.active = true;

        this.time = time;
        this.delaySetTime = -1;

        this.root = new GameObject("counter_root");
        this.addObject(root);

        this.panel = new GameObject(this.id + "_panel", new Sprite("clock_container"));
        this.panel.resizeTo(PanelSize);
        this.panel.setScale(1.1);
        this.dot_1 = new Shape("dot_1", Color.white, Shape.Type.Rect, new Vector2(5, 5));
        this.dot_2 = new Shape("dot_2", Color.white, Shape.Type.Rect, new Vector2(5, 5));
        this.min_1 = new GameObject(this.id + "_min_1", new Sprite("number_0"));
        this.min_2 = new GameObject(this.id + "_min_2", new Sprite("number_0"));
        this.sec_1 = new GameObject(this.id + "_sec_1", new Sprite("number_0"));
        this.sec_2 = new GameObject(this.id + "_sec_2", new Sprite("number_0"));
        this.panel.setPosition(0, 0);
        double offset = 40;
        this.dot_1.setPosition(offset, -8);
        this.dot_2.setPosition(offset, +8);
        this.min_1.setPosition(offset - 12 - DigitSize.x - 10 - DigitSize.x / 2, 0);
        this.min_2.setPosition(offset - 12 - DigitSize.x / 2, 0);
        this.sec_1.setPosition(offset + 12 + DigitSize.x / 2, 0);
        this.sec_2.setPosition(offset + 12 + DigitSize.x + 10 + DigitSize.x / 2, 0);
        this.min_1.resizeTo(DigitSize);
        this.min_2.resizeTo(DigitSize);
        this.sec_1.resizeTo(DigitSize);
        this.sec_2.resizeTo(DigitSize);
        this.clock = new GameObject(this.id + "_clock", new Sprite("clock_img"));
        this.clock.resizeTo(ClockSize);
        this.clock.setPosition(offset - 12 - DigitSize.x - 10 - DigitSize.x - 10 - ClockSize.x / 2, -(ClockSize.y - PanelSize.y) / 2);
        this.root.addObject(this.panel);
        this.root.addObject(this.clock);
        this.root.addObject(this.dot_1);
        this.root.addObject(this.dot_2);
        this.root.addObject(this.min_1);
        this.root.addObject(this.min_2);
        this.root.addObject(this.sec_1);
        this.root.addObject(this.sec_2);

        this.lastSec2 = -1;
    }

    public void setMute(boolean flag) {
        this.mute = flag;
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (!this.active)
            return;
        if (this.delay > 0) {
            this.delay -= dt;
            if (this.delay <= 0) {
                this.time = this.delaySetTime;
                this.delaySetTime = -1;
                this.time += this.delay;
            }
        } else {
            this.time -= dt;
        }

        int curTime = Math.max(0, (int) Math.ceil(this.time));
        int min1=(curTime/60)/10;
        int min2=(curTime/60)%10;
        int sec1=(curTime%60)/10;
        int sec2=(curTime%60)%10;
        if (sec2 != this.lastSec2) {
            if (!this.mute && this.lastSec2 != -1)
                AudioManager.PlayWithVolume("tick", 0.4, 0);
            this.lastSec2 = sec2;
        }
        if (min1 > 9) {
            this.min_1.setSprite(new Sprite("number_0"));
            this.min_2.setSprite(new Sprite("number_0"));
            this.sec_1.setSprite(new Sprite("number_0"));
            this.sec_2.setSprite(new Sprite("number_0"));
        } else {
            this.min_1.setSprite(new Sprite("number_" + min1));
            this.min_2.setSprite(new Sprite("number_" + min2));
            this.sec_1.setSprite(new Sprite("number_" + sec1));
            this.sec_2.setSprite(new Sprite("number_" + sec2));
        }
        if(this.time <= 0 && this.delaySetTime < 0){
            ifEnd=true;
        } else {
            ifEnd = false;
        }
    }

    public void setTime(double time, double delay) {
        this.delay = delay;
        this.delaySetTime = time;
    }

    public boolean isFinished() {
        if(this.ifEnd) {
            this.ifEnd=false;
            return true;
        } else {
            return false;
        }
    }

    public void setActive(boolean flag) {
        this.active = flag;
    }

    public double getTime() {
        return this.time;
    }

}
