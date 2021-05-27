/**
 * @Author: RogerDTZ
 * @FileName: App.java
 */

package main;

import datatype.Vector2;
import input.Controller;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

public class App extends JFrame {

    public static final int Width = 1880;
    public static final int Height = 1000;
    public static final Vector2 WinSize = new Vector2(Width, Height);

    public static GameManager gameManager;
    public static Controller controller;


    public App() {
        this.setTitle("MineSweeper");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void init() {
        this.add(gameManager.getGameStageContainer());
        this.pack();
        this.setLocationRelativeTo(null);
        this.addKeyListener(controller);
        this.addMouseListener(controller);
        this.addMouseMotionListener(controller);
        this.addMouseWheelListener(controller);
        SwingUtilities.invokeLater(JFXPanel::new);
    }

    public static void main(String[] args) {
        ResourceManager.init();
        App app = new App();
        gameManager = new GameManager();
        controller = new Controller();
        gameManager.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                SLManager.FlushWriteQueue();
            }
        });
        app.init();
        app.setVisible(true);
    }

}
