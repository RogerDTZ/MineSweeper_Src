/**
 * @Author: RogerDTZ
 * @FileName: ParticleSystem.java
 */

package graphics.particle;

import datatype.Vector2;
import graphics.Shape;
import object.GameObject;
import graphics.particle.instance.DraggedParticle;
import graphics.particle.instance.StraightParticle;
import util.Random;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParticleSystem extends GameObject {

    private ArrayList<Color> colors;
    private ArrayList<Particle> particles;
    private int limit;
    private int emitNum;
    private double emitTimer;
    private double emitGap;

    private Lock lock;

    private ParticleType type;
    private double size_min, size_max;
    private double dis_min, dis_max;
    private double life_min, life_max;
    private double alpha_min, alpha_max;
    private double angle_min, angle_max;


    public ParticleSystem(String id, int limit, ArrayList<Color> colors) {
        super(id);

        this.limit = limit;
        this.colors = colors;
        this.particles = new ArrayList<>();

        this.setPara(ParticleType.Straight, 1, 10, 1, 10, 0, 1, 0, 1, 0, 360);

        this.lock = new ReentrantLock();
    }

    public void setPara(ParticleType type, double size_min, double size_max, double dis_min, double dis_max,
                        double life_min, double life_max, double alpha_min, double alpha_max,
                        double angle_min, double angle_max) {
        this.type = type;
        this.size_min = size_min;
        this.size_max = size_max;
        this.dis_min = dis_min;
        this.dis_max = dis_max;
        this.life_min = life_min;
        this.life_max = life_max;
        this.alpha_min = alpha_min;
        this.alpha_max = alpha_max;
        this.angle_min = angle_min;
        this.angle_max = angle_max;
    }

    public void startEmit(int num) {
        this.emitNum = num;
        this.emitGap = 1.0 / this.emitNum;
    }

    public void stopEmit() {
        this.emitNum = 0;
    }

    public void spawn(int num, double delay) {
        for (int i = 0; i < num; ++i)
            this.spawn(delay);
    }

    public void spawn(double delay) {
        if (this.particles.size() + 1 > this.limit)
            return;
        double size = Random.nextDouble(size_min, size_max);
        double dis = Random.nextDouble(dis_min, dis_max);
        double life = Random.nextDouble(life_min, life_max);
        double alpha = Random.nextDouble(alpha_min, alpha_max);
        double angle = Random.nextDouble(angle_min, angle_max) / 180 * Math.PI;
        Vector2 v = (new Vector2(Math.cos(angle), Math.sin(angle))).scalar(dis);
        Particle p = null;
        if (this.type == ParticleType.Straight) {
            p = new StraightParticle(
                    this.colors.get(Random.nextInt(this.colors.size())),
                    Shape.Type.Circle,
                    new Vector2(size, size),
                    life,
                    v,
                    alpha,
                    delay
            );
        } else if (this.type == ParticleType.Dragged) {
            p = new DraggedParticle(
                    this.colors.get(Random.nextInt(this.colors.size())),
                    Shape.Type.Circle,
                    new Vector2(size, size),
                    life,
                    v,
                    alpha,
                    delay
            );
        }
        this.particles.add(p);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        for (Particle p : this.particles)
            p.update(dt);
        this.particles.removeIf(Particle::isDead);

        if (this.emitNum > 0) {
            double sum = 0;
            while (dt > 0) {
                if (this.emitTimer < dt) {
                    dt -= this.emitTimer;
                    sum += this.emitTimer;
                    this.spawn(sum);
                    this.emitTimer = this.emitGap;
                } else {
                    this.emitTimer -= dt;
                    break;
                }
            }
        }
    }

    @Override
    public AffineTransform render(Graphics2D g2d, AffineTransform parentTransform, double alpha) {
        AffineTransform at = super.render(g2d, parentTransform, alpha);
        alpha *= this.alpha;

        try {
            for (Particle p : this.particles)
                p.render(g2d, at, alpha);
        } catch (ConcurrentModificationException ignored) {
        }

        return at;
    }

    public int getParticleNum() {
        return this.particles.size();
    }

}
