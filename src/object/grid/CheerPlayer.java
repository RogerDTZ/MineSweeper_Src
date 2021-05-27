/**
 * @Author: RogerDTZ
 * @FileName: CheerPlayer.java
 */

package object.grid;

import datatype.Vector2;
import graphics.particle.ParticleSystem;
import graphics.particle.ParticleType;
import object.GameObject;
import util.ColorScheme;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class CheerPlayer extends GameObject {

    private ArrayList<ParticleSystem> players;


    public CheerPlayer(String id) {
        super(id);
        this.players = new ArrayList<>();
    }

    public void play(Vector2 pos, double delay) {
        ParticleSystem ps = new ParticleSystem("cheer_particles", 200, ColorScheme.Cheer);
        ps.setPosition(pos);
        ps.setPara(ParticleType.Dragged, 5, 9, 30, 60, 0.8, 1.2, 0.7, 0.9, 0, 360);
        ps.spawn(100, delay);
        this.players.add(ps);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        for (ParticleSystem ps : this.players)
            ps.update(dt);
        this.players.removeIf(x -> x.getParticleNum() == 0);
    }

    @Override
    public AffineTransform render(Graphics2D g2d, AffineTransform parentTransform, double alpha) {
        AffineTransform at =  super.render(g2d, parentTransform, alpha);
        alpha *= this.alpha;

        try {
            for (ParticleSystem ps : this.players)
                ps.render(g2d, parentTransform, alpha);
        } catch (ConcurrentModificationException ignored) {
        }

        return at;
    }
}
