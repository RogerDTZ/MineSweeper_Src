/**
 * @Author: RogerDTZ
 * @FileName: DraggedParticle.java
 */

package graphics.particle.instance;

import datatype.Vector2;
import graphics.particle.Particle;

import java.awt.*;

public class DraggedParticle extends Particle {

    public DraggedParticle(Color color, Type type, Vector2 size, double lifeTime, Vector2 target, double alpha, double delay) {
        super(color, type, size, lifeTime, target, alpha, delay);
    }

    @Override
    public double getTrans(double t) {
        if (t < 0)
            return 0;
        double x = t / this.lifeTime;
        double c = 4;
        double k = 3;
        double m = 0.5;
        return 1 - (c * Math.exp(- k / m * x)) / c;
    }

    @Override
    public double getAlpha(double t) {
        if (t < 0)
            return 0;
        if (t < this.lifeTime * 0.2)
            return t / (this.lifeTime * 0.2);
        else if (t > this.lifeTime * 0.7)
            return (this.lifeTime - t) / (this.lifeTime * 0.3);
        else
            return 1;
    }

}
