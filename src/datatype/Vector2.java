/**
 * @Author: RogerDTZ
 * @FileName: Vector2.java
 */

package datatype;

import javafx.scene.transform.Affine;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Random;

public class Vector2 {

    public double x, y;


    public Vector2() {
        this.x = this.y = 0.0;
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getModulus() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vector2 normalize() {
        double modulus = this.getModulus();
        this.x /= modulus;
        this.y /= modulus;
        return this;
    }

    public Vector2 scalar(double c) {
        this.x *= c;
        this.y *= c;
        return this;
    }

    public Vector2 add(Vector2 rhs) {
        this.x += rhs.x;
        this.y += rhs.y;
        return this;
    }

    public Vector2 transform(AffineTransform at) {
        Point2D.Double src = new Point2D.Double(this.x, this.y);
        Point2D.Double dst = null;
        dst = (Point2D.Double)(at.transform(src, dst));
        this.x = dst.x;
        this.y = dst.y;
        return this;
    }

    public static Vector2 scalar(Vector2 v, double c) {
        return new Vector2(v.x * c, v.y * c);
    }

    public static Vector2 random() {
        Random random = new Random();
        double theta = random.nextDouble() * 2 * Math.PI;
        return new Vector2(Math.cos(theta), Math.sin(theta));
    }

}
