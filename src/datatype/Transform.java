/**
 * @Author: RogerDTZ
 * @FileName: Transform.java
 */

package datatype;

import javafx.scene.transform.Affine;

import java.awt.geom.AffineTransform;

public class Transform {

    public Vector2 position;
    public double rotation;
    public Vector2 scale;


    public Transform(Vector2 position, double rotation, Vector2 scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Transform() {
        this(new Vector2(0.0, 0.0), 0, new Vector2(1.0, 1.0));
    }

    public AffineTransform concatenate(AffineTransform inputAt) {
        AffineTransform at = (AffineTransform) inputAt.clone();
        at.translate(this.position.x, this.position.y);
        at.rotate(-Math.toRadians(this.rotation));
        at.scale(this.scale.x, this.scale.y);
        return at;
    }

    public void translate(Vector2 replacement) {
        this.position.add(replacement);
    }

    public void rotate(double angle) {
        this.rotation += angle;
    }

}
