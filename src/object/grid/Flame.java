/**
 * @Author: RogerDTZ
 * @FileName: Flame.java
 */

package object.grid;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Sprite;
import main.AudioManager;
import graphics.particle.ParticleSystem;
import graphics.particle.ParticleType;
import object.GameObject;
import util.ColorScheme;
import util.Random;

public class Flame extends GameObject {

    public final static Vector2 FireSize = new Vector2(200, 200);
    public final static double Stage1 = 0.12;
    public final static double Stage2 = 0.2;

    private GameObject fire;
    private ParticleSystem smoke;

    private Animator scaleAnimator;
    private Animator alphaAnimator;

    private double time;


    public Flame(String id) {
        super(id);

        this.fire = new GameObject(this.id + "_fire", new Sprite("flame"));
        this.fire.resizeTo(FireSize);
        this.fire.setScale(0);
        this.fire.setAlpha(0);
        this.fire.setRenderPriority(2);
        this.addObject(this.fire);

        this.smoke = new ParticleSystem(this.id + "_smoke", 100, ColorScheme.Smoke);
        this.smoke.setRenderPriority(1);
        this.addObject(this.smoke);

        this.scaleAnimator = new Animator(0.7);
        this.alphaAnimator = new Animator(0);
        this.addComponent(this.scaleAnimator);
        this.addComponent(this.alphaAnimator);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        this.time += dt;
        this.fire.setAlpha(this.alphaAnimator.val());
        this.fire.setScale(this.scaleAnimator.val());
    }

    public void play(Vector2 position, double delay) {
        this.setPosition(position);

        this.fire.setScale(0);
        this.fire.setAlpha(0);
        this.fire.rotate(Random.nextDouble(0, 360));
        if (this.scaleAnimator != null)
            this.scaleAnimator.destroy();
        if (this.alphaAnimator != null)
            this.alphaAnimator.destroy();
        this.scaleAnimator = new Animator(0.7);
        this.alphaAnimator = new Animator(0);
        this.addComponent(this.scaleAnimator);
        this.addComponent(this.alphaAnimator);
        this.scaleAnimator.append(Animation.GetLinear(0.7, 1.3, Stage1 + Stage2, delay));
        this.alphaAnimator.append(Animation.GetLinear(0, 1, Stage1 * 0.8, delay));
        this.alphaAnimator.append(Animation.GetLinear(1, 1, Stage1 * 0.2));
        this.alphaAnimator.append(Animation.GetLinear(1, 0, Stage2));

        this.smoke.setPara(ParticleType.Straight,15, 25, 100, 200, 0.5, 0.8, 0.7, 0.9, 0, 360);
        this.smoke.spawn(50, delay - 0.1);

        AudioManager.PlayWithVolume("explode", 0.3, delay - 0.2);
    }

    public boolean isDead() {
        return this.time >= Stage1 + Stage2 + 1;
    }

}
