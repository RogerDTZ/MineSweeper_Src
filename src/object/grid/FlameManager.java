/**
 * @Author: RogerDTZ
 * @FileName: FlameManager.java
 */

package object.grid;

import datatype.Vector2;
import object.GameObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class FlameManager extends GameObject {

    private static long idCnt = 0;
    private ArrayList<Flame> flames;


    public FlameManager(String id) {
        super(id);
        this.flames = new ArrayList<Flame>();
    }

    public void spawn(Vector2 position, double delay) {
        Flame flame = new Flame("flame_" + (++idCnt));
        flame.play(position, delay);
        this.flames.add(flame);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        for (Flame flame : this.flames)
            flame.update(dt);
        this.flames.removeIf(Flame::isDead);
    }

    @Override
    public AffineTransform render(Graphics2D g2d, AffineTransform parentTransform, double alpha) {
        AffineTransform at = super.render(g2d, parentTransform, alpha);
        alpha *= this.alpha;

        for (Flame flame : this.flames)
            flame.render(g2d, at, alpha);

        return at;
    }
}
