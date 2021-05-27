/**
 * @Author: RogerDTZ
 * @FileName: StraightParticle.java
 */

package graphics.particle.instance;

import datatype.Vector2;
import graphics.particle.Particle;

import java.awt.*;

public class StraightParticle extends Particle {

    public StraightParticle(Color color, Type type, Vector2 size, double lifeTime, Vector2 target, double alpha, double delay) {
        super(color, type, size, lifeTime, target, alpha, delay);
    }

    @Override
    public double getTrans(double t) {
        if (t < 0)
            return 0;
        return t / this.lifeTime;
    }

    @Override
    public double getAlpha(double t) {
        if (t < 0)
            return 0;
        if (t < this.lifeTime * 0.2)
            return t / (this.lifeTime * 0.2);
        else if (t > this.lifeTime * 0.6)
            return (this.lifeTime - t) / (this.lifeTime * 0.4);
        else
            return 1;
    }

}
