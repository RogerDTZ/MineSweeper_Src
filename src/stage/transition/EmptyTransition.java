/**
 * @Author: RogerDTZ
 * @FileName: EmptyTransition.java
 */

package stage.transition;

import java.awt.*;

public class EmptyTransition implements Transition {

    private boolean active;


    public EmptyTransition() {
        this.active = false;
    }

    public void init() {
        active = true;
    }

    public void update(double dt) {

    }

    public void render(Graphics2D g2d) {

    }

    public boolean isFinished() {
        return this.active;
    }
}
