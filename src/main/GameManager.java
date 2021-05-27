/**
 * @Author: RogerDTZ
 * @FileName: GameManager.java
 */

package main;

import input.Controller;
import stage.*;
import stage.scene.*;
import stage.scene.Menu;
import stage.transition.FadeInTransition;
import stage.transition.FadeOutTransition;

import java.awt.*;

public class GameManager implements Runnable {

    public static final int FPS = 144;

    private StageContainer container;


    public GameManager() {
        this.container = new StageContainer();
    }

    public void start() {
        Thread gameManagerThread = new Thread(this);
        gameManagerThread.start();
    }

    private void updateStage() {
        GameStage stage = this.container.getCurrStage();
        if (stage.getGameStageID() == GameStageID.Empty) {
            if (((Empty)stage).getTotalTime() >= 0.5 && ResourceManager.getLoadState() >= 1) {
                this.container.enterStage(new Launch(), null, new FadeInTransition(Color.black, 1));
                AudioManager.PlayWithVolume("intro", 0.1, 0);
            }
        } else if (stage.getGameStageID() == GameStageID.Launch) {
            if (((Launch)stage).getTotalTime() >= 2 && ResourceManager.getLoadState() == 2) {
                this.container.enterStage(new Menu(), new FadeOutTransition(Color.black, 1), new FadeInTransition(Color.black, 1));
                AudioManager.initBGM();
                AudioManager.playBGM();
            }
            //this.container.enterStage(new Demo(), null, new FadeInTransition(Color.black, 1));
        } else if (stage.getGameStageID() == GameStageID.Menu) {
            if (((Menu)stage).isExiting())
                System.exit(0);
            if (((Menu)stage).getRoom() != null && ((Menu)stage).getRoom().readyForGame()) {
                this.container.enterStage(new Game(((Menu) stage).getRoom()), new FadeOutTransition(Color.black, 1, 1), new FadeInTransition(Color.black, 2, 0));
            }
        } else if (stage.getGameStageID() == GameStageID.Game) {
            if (((Game)stage).getRoom().isDead()) {
                this.container.enterStage(new Menu(), new FadeOutTransition(Color.black, 1), new FadeInTransition(Color.black, 1));
                SLManager.FlushWriteQueue();
            }
            if (((Game) stage).isGameOver()) {
                this.container.enterStage(new End(((Game) stage).getRoom()),
                        new FadeOutTransition(new Color(234, 234, 234), 1.5, 2),
                        new FadeInTransition(new Color(234, 234, 234), 2));
            }
        } else if (stage.getGameStageID() == GameStageID.End) {
            if (((End)stage).returnToMenu()) {
                this.container.enterStage(new Menu(((End) stage).getRoom()), new FadeOutTransition(Color.black, 1), new FadeInTransition(Color.black, 1.0));
                SLManager.FlushWriteQueue();
            }
        }
    }

    public void update(double dt) {
        this.updateStage();
    }

    @Override
    public void run() {
        long stdWait = (long) Math.floor(1000.0 / FPS);
        long lastTime = System.currentTimeMillis();
        while (true) {
            long currTime = System.currentTimeMillis();
            long deltaTime = currTime - lastTime;
            if (deltaTime < stdWait) {
                try {
                    Thread.sleep(stdWait - deltaTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            deltaTime = System.currentTimeMillis() - lastTime;
            double dt = (double)deltaTime / 1000;
            lastTime = System.currentTimeMillis();

            if (Controller.isKeyDown('m'))
                dt = 0.002;

            this.update(dt);
            this.container.update(dt);
            this.container.repaint();
        }
    }

    public StageContainer getGameStageContainer() {
        return this.container;
    }

}
