/**
 * @Author: RogerDTZ
 * @FileName: GamePlayerBox.java
 */

package object.ui.box;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Shape;
import graphics.Sprite;
import graphics.particle.ParticleSystem;
import graphics.particle.ParticleType;
import object.Player;
import object.GameObject;
import object.grid.Mine;
import graphics.Text;
import util.ColorScheme;
import util.FontLibrary;

import java.awt.*;
import java.util.ArrayList;

public class GamePlayerBox extends GameObject {

    public static final Vector2 PlayerDisplayBox = new Vector2(350, 100);
    public static final Vector2 BarSize = new Vector2(PlayerDisplayBox.x * 0.9, 8);
    public static final Vector2 BarPos = new Vector2(0, PlayerDisplayBox.y / 2 + BarSize.y / 2 + 3);
    public static final double BarVanishDuration = 0.2;
    public static final double BarAppearDuration = 0.2;
    public static final double BarDuration = 0.1;
    public static final double BlinkDuration = 1.0;
    public static final double BasicAlpha = 0.0;

    public static final double PopOutDuration = 0.3;
    public static final double PopBackDuration = 0.3;
    public static final double PopOutTransX = 0;
    public static final double PopOutScale = 1.1;


    private GameObject root;
    private Player player;
    private GameObject box;
    private GameObject localIndicator;
    private Text text_name;
    private Text text_score;
    private Text text_mistake;

    private boolean activeState;
    private Animator amt_scale;
    private Animator amt_trans;

    private int maxStep;
    private int step;
    private ArrayList<Shape> bars;
    private ArrayList<Shape> bars_cover;
    private ArrayList<Animator> bars_scale;
    private ArrayList<Animator> bars_alpha;

    private int score;
    private int mistake;
    private int currentCost;

    private ParticleSystem fire_l, fire_r;


    public GamePlayerBox(Player player, int maxStep, Sprite box) {
        super(player.getID() + "_display");
        this.player = player;
        this.activeState = false;

        this.root = new GameObject(this.id + "_container");
        this.addObject(this.root);

        this.box = new GameObject(this.id + "_box", box);
        this.box.resizeTo(PlayerDisplayBox);
        this.text_name = new Text(this.id + "_name", this.player.getPlayerName(), FontLibrary.GetPlayerNameFont(25));
        this.text_name.setPosition(0, -20);
        this.text_score = new Text(this.id + "_score", this.player.getPlayerName(), FontLibrary.GetPlayerNameFont(20));
        this.text_score.setPosition(-80, 8);
        this.text_mistake = new Text(this.id + "_mistake", this.player.getPlayerName(), FontLibrary.GetPlayerNameFont(20));
        this.text_mistake.setPosition(80, 8);
        this.box.setRenderPriority(0);
        this.text_name.setRenderPriority(1);
        this.text_score.setRenderPriority(1);
        this.text_mistake.setRenderPriority(1);
        if (this.player.isLocal()) {
            this.localIndicator = new GameObject(this.id + "_local", new Sprite("home"));
            this.localIndicator.resizeTo(25, 25);
            this.localIndicator.setPosition(- this.text_name.getObjSize().x / 2 - 15, -20);
            this.localIndicator.setRenderPriority(1);
            this.root.addObject(this.localIndicator);
        }

        this.text_name.setColor(Color.white);
        this.text_score.setColor(Color.white);
        this.text_mistake.setColor(Color.white);

        this.root.addObject(this.box);
        this.root.addObject(this.text_name);
        this.root.addObject(this.text_score);
        this.root.addObject(this.text_mistake);

        this.amt_scale = new Animator(1);
        this.amt_trans = new Animator(0);
        this.addComponent(this.amt_scale);
        this.addComponent(this.amt_trans);

        this.score = -1000;
        this.mistake = -1000;
        this.text_score.setText("Score: 0");
        this.text_mistake.setText("Mistake: 0");

        this.maxStep = maxStep;
        this.bars = new ArrayList<>();
        this.bars_cover = new ArrayList<>();
        this.bars_scale = new ArrayList<>();
        this.bars_alpha = new ArrayList<>();
        for (int i = 0; i < this.maxStep; ++i) {
            Shape pc = new Shape(this.id + "_bar_" + i, new Color(79, 156, 12), Shape.Type.Rect, new Vector2(BarSize.x / this.maxStep * 0.9, BarSize.y));
            pc.setScale(0);
            pc.setPosition(BarPos.x - BarSize.x / 2 + BarSize.x / this.maxStep * i + BarSize.x / this.maxStep / 2, BarPos.y);
            this.bars.add(pc);
            this.root.addObject(pc);
            pc = new Shape(this.id + "_bar_cover_" + i, new Color(255, 0, 161, 255), Shape.Type.Rect, Vector2.scalar(new Vector2(BarSize.x / this.maxStep * 0.9, BarSize.y), 1));
            pc.setScale(0);
            pc.setPosition(BarPos.x - BarSize.x / 2 + BarSize.x / this.maxStep * i + BarSize.x / this.maxStep / 2, BarPos.y);
            this.bars_cover.add(pc);
            this.root.addObject(pc);

            Animator amt = new Animator(0);
            this.bars_scale.add(amt);
            this.addComponent(amt);
            amt = new Animator(BasicAlpha);
            this.bars_alpha.add(amt);
            this.addComponent(amt);
        }

        this.fire_l = new ParticleSystem(this.id + "_fire_l", 1000, ColorScheme.Fire);
        this.fire_r = new ParticleSystem(this.id + "_fire_r", 1000, ColorScheme.Fire);
        this.fire_l.setPara(ParticleType.Straight, 5, 10,40, 80, 1, 1.3,
                0.7, 0.9, 125, 235);
        this.fire_r.setPara(ParticleType.Straight, 5, 10,40, 80, 1, 1.3,
                0.7, 0.9, -55, 55);
        this.fire_l.setRenderPriority(-1);
        this.fire_r.setRenderPriority(-1);
        this.fire_l.setPosition(-150, 0);
        this.fire_r.setPosition(+150, 0);
        this.root.addObject(fire_l);
        this.root.addObject(fire_r);
    }

    public void toggleOnFire(boolean flag) {

        if (flag) {
            this.fire_l.startEmit(50);
            this.fire_r.startEmit(50);
        } else {
            this.fire_l.stopEmit();
            this.fire_r.stopEmit();
        }
    }

    public void initBars(double delay) {
        this.step = this.maxStep;

        for (int i = 0; i < this.maxStep; ++i) {
            Animator animator = this.bars_scale.get(i);
            animator.append(Animation.GetTanh(0, 1.05, BarDuration, true), delay);
            animator.append(Animation.GetTanh(1.05, 1, BarDuration * 0.7, false));
            animator = this.bars_alpha.get(i);
            animator.setLoop(false);
            animator.forceAppend(Animation.GetLinear(BasicAlpha, BasicAlpha, 0, delay));
            this.bars_cover.get(i).setScale(0);
            if (this.step > 0)
                delay += BarAppearDuration / (this.step - 1);
        }
    }

    public void clearBars(double delay) {
        this.decrease(this.step, delay);
    }

    public void toggleActive(boolean flag, double delay) {
        if (flag && !this.activeState) {
            this.activeState = true;
            this.amt_trans.append(Animation.GetTanh(0, PopOutTransX, PopOutDuration, true, delay));
            this.amt_scale.append(Animation.GetTanh(1, PopOutScale, PopOutDuration, true, delay));
            this.initBars(delay);
        } else if (!flag && this.activeState) {
            this.activeState = false;
            this.amt_trans.append(Animation.GetTanh(PopOutTransX, 0, PopBackDuration, true, delay));
            this.amt_scale.append(Animation.GetTanh(PopOutScale, 1, PopBackDuration, true, delay));
            this.clearBars(delay);
        }
    }

    public void decrease(int x, double delay) {
        for (int i = 0; i < x && this.step - 1 - i >= 0; ++i) {
            this.bars_scale.get(this.step - 1 - i).forceAppend(
                    Animation.GetTanh(this.bars_scale.get(this.step - 1 - i).val(), 0, BarVanishDuration, true, delay)
            );
        }
        this.step = Math.max(0, this.step - x);
    }

    public void setOnline(boolean flag) {
        if (!flag) {
            this.box.setSprite(new Sprite("player_box_offline"));
            this.toggleActive(false, 0);
        } else {
            this.box.setSprite(new Sprite("player_box_" + this.player.getOrder()));
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        this.root.setPosition(this.amt_trans.val(), 0);
        this.root.setScale(this.amt_scale.val());

        for (int i = 0; i < this.maxStep; ++i) {
            this.bars.get(i).setScale(this.bars_scale.get(i).val());
            if (this.step - this.currentCost <= i)
                this.bars_cover.get(i).setScale(this.bars_scale.get(i).val());
            this.bars_cover.get(i).setAlpha(this.bars_alpha.get(i).val());
        }

        this.updateInformation();
    }

    private void updateInformation() {
        if (this.score != this.player.getScore()) {
            int target = this.player.getScore();
            if (this.score < target) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep((int)Math.round(Mine.FlagShowDuration * 1000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        text_score.setText("Score: " + player.getScore());
                        score = player.getScore();
                    }
                };
                thread.start();
            } else {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        text_score.setText("Score: " + target);
                        score = target;
                    }
                };
                thread.start();
            }
        }

        if (this.mistake != this.player.getMistakes()) {
            int target = this.player.getMistakes();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep((int)Math.round(Mine.FlagShowDuration * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    text_mistake.setText("Mistake: " + target);
                    mistake = target;
                }
            };
            thread.start();
        }
    }

    public void toggleBlink(boolean flag, double delay) {
        for (int i = 0; i < this.currentCost && i < this.step; ++i) {
            Animator animator = this.bars_alpha.get(this.step - 1 - i);
            if (flag) {
                animator.setLoop(true);
                animator.sleep(delay);
                animator.forceAppend(Animation.GetSmooth(BasicAlpha, 1, BlinkDuration * 0.5));
                animator.append(Animation.GetSmooth(1, BasicAlpha, BlinkDuration * 0.5));
            } else {
                animator.setLoop(false);
                animator.forceAppend(Animation.GetLinear(BasicAlpha, BasicAlpha, 0));
            }
        }
    }

    public void setCurrentCost(int x) {
        this.toggleBlink(false, 0);
        this.currentCost = x;
        this.toggleBlink(true, 0);
    }

}
