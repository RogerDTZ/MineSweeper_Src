/**
 * @Author: RogerDTZ
 * @FileName: Shape.java
 */

package graphics;

import datatype.Vector2;
import object.GameObject;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Shape extends GameObject {

    public enum Type {
        Rect, Circle
    }

    private Color color;
    private Type type;


    public Shape(String id, Color color, Type type, Vector2 size) {
        super(id, null);
        this.color = color;
        this.type = type;
        this.resizeTo(size);
        this.updateSprite();
    }

    public void setColor(Color color) {
        this.color = color;
        this.updateSprite();
    }

    public void setType(Type type) {
        this.type = type;
        this.updateSprite();
    }

    private void updateSprite() {
        int w = (int)Math.round(this.objSize.x);
        int h = (int)Math.round(this.objSize.y);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(this.color);
        switch(this.type) {
            case Rect:
                g.fillRect(0, 0, w, h);
                break;
            case Circle:
                g.fillOval(0, 0, w, h);
        }
        g.dispose();
        this.sprite = new Sprite(img);
    }

}
