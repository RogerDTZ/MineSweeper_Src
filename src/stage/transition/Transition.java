/**
 * @Author: RogerDTZ
 * @FileName: Transition.java
 */

package stage.transition;

import java.awt.*;

public interface Transition {

    void init();

    void update(double dt);

    void render(Graphics2D g2d);

    boolean isFinished();

}
