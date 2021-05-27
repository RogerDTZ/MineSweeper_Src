/**
 * @Author: RogerDTZ
 * @FileName: StageContainer.java
 */

package stage;

import graphics.Camera;
import main.App;
import stage.scene.Empty;
import stage.transition.EmptyTransition;
import stage.transition.Transition;
import main.AttentionManager;

import java.awt.*;
import javax.swing.*;

public class StageContainer extends JPanel {

    private GameStage currStage;
    private GameStage prevStage;
    private Transition enterTransition;
    private Transition leaveTransition;


    public StageContainer() {
        this.setPreferredSize(new Dimension(App.Width, App.Height));
        // still use the system cursor
        // this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor"));
        this.prevStage = null;
        this.currStage = new Empty();
    }

    public void update(double dt) {
        Camera.update(dt);
        AttentionManager.update(dt);

        if (this.leaveTransition != null) {
            if (this.leaveTransition.isFinished()) {
                this.leaveTransition = null;
                this.prevStage = null;
                if (this.enterTransition != null)
                    this.enterTransition.init();
            } else {
                this.leaveTransition.update(dt);
            }
        }
        if (this.enterTransition != null) {
            if (this.enterTransition.isFinished()) {
                this.enterTransition = null;
            } else {
                this.enterTransition.update(dt);
            }
        }

        if (this.prevStage != null)
            this.prevStage.update(dt);
        else
            this.currStage.update(dt);
    }

    public void render(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (this.prevStage != null)
            this.prevStage.render(g2d);
        else
            this.currStage.render(g2d);

        if (this.leaveTransition != null)
            this.leaveTransition.render(g2d);
        if (this.enterTransition != null)
            this.enterTransition.render(g2d);

        AttentionManager.render(g2d);
    }

    public void enterStage(GameStage nextStage, Transition leaveTransition, Transition enterTransition) {
        if (nextStage == null)
            nextStage = new Empty();
        nextStage.init();
        this.prevStage = this.currStage;
        this.currStage = nextStage;
        if (leaveTransition == null)
            leaveTransition = new EmptyTransition();
        if (enterTransition == null)
            enterTransition = new EmptyTransition();
        this.leaveTransition = leaveTransition;
        this.leaveTransition.init();
        this.enterTransition = enterTransition;
    }

    public GameStage getCurrStage() {
        return this.currStage;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        this.render((Graphics2D)g);
    }

}
