/**
 * @Author: RogerDTZ
 * @FileName: Particle.java
 */

package graphics.particle;

import datatype.Vector2;
import graphics.Shape;

import java.awt.*;

public abstract class Particle extends Shape {

    private static long idCnt = 0;

    protected double lifeTime;
    protected double delay;
    protected double time;
    protected Vector2 target;

    private double baseAlpha;


    public Particle(Color color, Type type, Vector2 size, double lifeTime, Vector2 target, double alpha, double delay) {
        super("particle_" + (++idCnt), color, type, size);
        this.lifeTime = lifeTime;
        this.target = target;
        this.delay = delay;
        this.time = -this.delay;
        this.baseAlpha = alpha;

        this.setAlpha(0);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        this.time = Math.min(this.lifeTime, this.time + dt);
        this.setPosition(Vector2.scalar(this.target, this.getTrans(this.time)));
        this.setAlpha(this.baseAlpha * this.getAlpha(this.time));
    }

    public boolean isDead() {
        return this.time >= this.lifeTime;
    }

    public abstract double getTrans(double t);
    public abstract double getAlpha(double t);

}
