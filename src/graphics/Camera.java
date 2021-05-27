/**
 * @Author: RogerDTZ
 * @FileName: Camera.java
 */

package graphics;

import component.animation.Animation;
import component.animation.Animator;
import object.GameObject;
import util.Random;

import java.awt.geom.AffineTransform;

public class Camera {

    public static final double PeekTime = 0.05;
    public static final double Rotate = 1.5;
    public static final double Translate = 30;
    public static final double Scale = 0;

    private static GameObject obj = new GameObject("camera_obj") ;

    private static Animator rotate = new Animator(0);
    private static Animator px = new Animator(0);
    private static Animator py = new Animator(0);
    private static Animator scale = new Animator(1);


    public static void update(double dt) {
        rotate.update(dt);
        px.update(dt);
        py.update(dt);
        scale.update(dt);

        obj.setRotation(rotate.val());
        obj.setPosition(px.val(), py.val());
        obj.setScale(scale.val());
    }

    public static AffineTransform getView() {
        return obj.getAbsoluteTransform();
    }

    public static void shake(double duration, double intensity, double delay) {
        rotate.forceAppend(Animation.GetLinear(0, 0, delay));
        px.forceAppend(Animation.GetLinear(0, 0, delay));
        py.forceAppend(Animation.GetLinear(0, 0, delay));
        scale.forceAppend(Animation.GetLinear(1, 1, delay));

        int cnt = (int)Math.round(duration / PeekTime);
        double rotate_v = 0, x_v = 0, y_v = 0, scale_v = 1;
        for (int i = 0; i <= cnt; ++i) {
            double v = intensity * Math.cos(1.0 * i / cnt * Math.PI / 2);
            double next_rotate = Random.nextDouble(-Rotate * v, Rotate * v);
            double next_x = Random.nextDouble(-Translate * v, Translate * v);
            double next_y = Random.nextDouble(-Translate * v, Translate * v);
            double next_scale = Random.nextDouble(1 - Scale * v, 1 + Scale * v);
            rotate.append(Animation.GetLinear(rotate_v, next_rotate, PeekTime));
            px.append(Animation.GetLinear(x_v, next_x, PeekTime));
            py.append(Animation.GetLinear(y_v, next_y, PeekTime));
            scale.append(Animation.GetLinear(scale_v, next_scale, PeekTime));
            rotate_v = next_rotate;
            x_v = next_x;
            y_v = next_y;
            scale_v = next_scale;
        }

    }

}
