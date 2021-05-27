/**
 * @Author: RogerDTZ
 * @FileName: Sprite.java
 */

package graphics;

import datatype.Vector2;
import main.ResourceManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

public class Sprite {

    private int width, height;
    private Vector2 baseScale;
    private Vector2 unitSize;
    private Color color;
    private BufferedImage img;

    protected boolean visible;


    public Sprite(String imgName) {
        this.color = new Color(255, 255, 255, 255);
        this.baseScale = new Vector2(1.0, 1.0);
        this.visible = true;
        this.setImage(imgName);
    }

    public Sprite(BufferedImage img) {
        this.color = new Color(255, 255, 255, 255);
        this.baseScale = new Vector2(1.0, 1.0);
        this.visible = true;
        this.setImage(img);
    }

    public Sprite(String imgName, Vector2 minSize) {
        this(imgName);
        if (this.width < minSize.x || this.height < minSize.y) {
            double scale = Math.max(minSize.x / this.width, minSize.y / this.height);
            this.resizeTo(width * scale, height * scale);
        }
    }

    public Sprite clone() {
        return new Sprite(this.img);
    }

    public void draw(Graphics2D g2d, AffineTransform at, Vector2 scale, double setAlpha) {
        if (!this.visible)
            return;
        AffineTransform localAt = (AffineTransform) at.clone();
        localAt.scale(this.baseScale.x * scale.x, this.baseScale.y * scale.y);
        localAt.translate(- this.width / 2.0, - this.height / 2.0);
        g2d.setComposite(AlphaComposite.SrcOver.derive((float)(this.color.getAlpha() * setAlpha / 255.0)));
        g2d.drawImage(this.img, localAt, null);
    }

    public void setImage(BufferedImage img) {
        this.img = img;
        this.width = this.img.getWidth(null);
        this.height = this.img.getHeight(null);
        this.unitSize = new Vector2(this.width, this.height);
    }

    public void setImage(String imgName) {
        this.setImage(ResourceManager.getImage(imgName));
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void resizeTo(Vector2 rect) {
        this.unitSize = rect;
        this.baseScale.x = rect.x / this.width;
        this.baseScale.y = rect.y / this.height;
    }

    public void resizeTo(double x, double y) {
        this.resizeTo(new Vector2(x, y));
    }

    public void setVisible(boolean flag) {
        this.visible = flag;
    }

    public Vector2 getUnitSize() {
        return new Vector2(this.unitSize.x, this.unitSize.y);
    }

    public Sprite subImage(Vector2 pos, Vector2 size) {
        Sprite res = new Sprite(this.img.getSubimage(
                (int)Math.round(pos.x / this.baseScale.x),
                (int)Math.round(pos.y / this.baseScale.y),
                (int)Math.round(size.x / this.baseScale.x),
                (int)Math.round(size.y / this.baseScale.y)));
        res.resizeTo(size);
        return res;
    }

}
