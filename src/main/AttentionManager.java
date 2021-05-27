/**
 * @Author: RogerDTZ
 * @FileName: AttentionManager.java
 */

package main;

import graphics.Sprite;
import object.ui.Attention;

import java.awt.*;
import java.util.ArrayList;

public class AttentionManager {

    private static ArrayList<Attention> messages = new ArrayList<>();


    public static void showErrorMessage(String msg) {
        showErrorMessage(msg, 1.5);
    }

    public static void showWarnMessage(String msg) {
        showWarnMessage(msg, 1.5);
    }

    public static void showGoodMessage(String msg) {
        showGoodMessage(msg, 1);
    }

    public static void showErrorMessage(String msg, double last) {
        messages.add(new Attention(msg, new Sprite("attention_red"), last));
    }

    public static void showWarnMessage(String msg, double last) {
        messages.add(new Attention(msg, new Sprite("attention_yellow"), last));
    }

    public static void showGoodMessage(String msg, double last) {
        messages.add(new Attention(msg, new Sprite("attention_green"), last));
    }

    public static void update(double dt) {
        messages.removeIf(Attention::isFinished);
        for (Attention attention : messages)
            attention.update(dt);
    }

    public static void render(Graphics2D g2d) {
        for (Attention attention : messages)
            attention.render(g2d);
    }

}
