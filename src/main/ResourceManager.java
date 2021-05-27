/**
 * @Author: RogerDTZ
 * @FileName: ResourceManager.java
 */

package main;

import javafx.util.Pair;
import util.ErrorMsg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {

    public static final int MineBackgroundNum = 4;

    private static ArrayList<Pair<Pair<String, String>, Color>> basicList = new ArrayList<>();
    private static ArrayList<Pair<Pair<String, String>, Color>> fullList = new ArrayList<>();
    private static ArrayList<Pair<String, String>> soundList = new ArrayList<>();
    private static Map<String, BufferedImage> imgs = new HashMap<>();
    private static boolean basicLoaded;
    private static boolean fullyLoaded;


    private static class LoadingThread implements Runnable {

        private static void load(ArrayList<Pair<Pair<String, String>, Color>> basicList, ArrayList<Pair<Pair<String, String>, Color>> fullList) {
            for (Pair<Pair<String, String>, Color> pair : basicList)
                imgs.put(pair.getKey().getKey(), loadImage("resources/" + pair.getKey().getValue(), pair.getValue()));
            for (Pair<String, String> pair : soundList) {
                AudioManager.load(pair.getKey(), "resources/" + pair.getValue());
            }
            basicLoaded = true;
            for (Pair<Pair<String, String>, Color> pair : fullList) {
                imgs.put(pair.getKey().getKey(), loadImage("resources/" + pair.getKey().getValue(), pair.getValue()));
                System.out.println("Finish loading " + pair.getKey().getKey());
            }
            fullyLoaded = true;
        }

        @Override
        public void run() {
            basicLoaded = fullyLoaded = false;
            load(basicList, fullList);
        }

    }

    private static void addList(ArrayList<Pair<Pair<String, String>, Color>> list, String textureName, String imgPath) {
        addList(list, textureName, imgPath, Color.white);
    }

    private static void addList(ArrayList<Pair<Pair<String, String>, Color>> list, String textureName, String imgPath, Color color) {
        list.add(new Pair<>(new Pair<>(textureName, imgPath), color));
    }

    private static void addSoundList(ArrayList<Pair<String, String>> list, String soundName, String soundPath) {
        list.add(new Pair<>(soundName, soundPath));
    }

    public static void init() {
        addList(basicList, "launch_background", "launch_background.jpg");
        addList(basicList, "menu_background", "menu_background.jpg");
        addList(basicList, "attention_red", "attention.png", new Color(210, 10, 60));
        addList(basicList, "attention_green", "attention.png", new Color(72, 146, 34, 255));
        addList(basicList, "attention_yellow", "attention.png", new Color(175, 160, 19, 255));
        addList(basicList, "popo", "popo.png");

        addList(fullList,  "menu_button", "menu_button.png");
        addList(fullList,  "menu_button_red", "menu_button.png", new Color(210,10,60));
        addList(fullList,  "menu_button_orange", "menu_button.png", new Color(220, 117, 48));
        addList(fullList,  "menu_button_blue", "menu_button.png", new Color(22, 115, 149));
        addList(fullList,  "menu_button_green_bright", "menu_button.png", new Color(115, 177, 91));
        addList(fullList,  "menu_button_green", "menu_button.png", new Color(100, 151, 39));
        addList(fullList,  "menu_button_white", "menu_button.png", new Color(255, 255, 255));
        addList(fullList, "player_label", "player_label.png");
        addList(fullList, "room_ready_yes", "yes.png");
        addList(fullList, "room_ready_no", "waiting.png");
        addList(fullList, "map_info_input", "box.png", new Color(16, 49, 87));
        addList(fullList, "map_chooser", "box.png", new Color(109, 0, 30));
        addList(fullList, "input_box", "input_box.png");
        addList(fullList, "your_turn", "your_turn.png");
        addList(fullList, "exit", "exit.png", new Color(232, 77, 14));
        addList(fullList, "reload", "reload.png");
        addList(fullList, "respawn", "dice.png", new Color(255, 255, 255));
        addList(fullList, "reset", "reload.png");
        addList(fullList, "aim", "aim.png", new Color(255, 65, 65));
        addList(fullList, "aim_yellow", "aim.png", new Color(255, 192, 0));

        addList(fullList, "rect_black", "rect.png", new Color(45, 45, 45));
        addList(fullList, "rect_dark", "rect.png", new Color(59, 59, 59));
        addList(fullList, "rect_gray", "rect.png", new Color(64, 64, 64));
        addList(fullList, "rect_yellow", "rect.png", new Color(219, 177, 3));

        addList(fullList, "player_box_0", "box.png", new Color(241, 59, 59));
        addList(fullList, "player_box_1", "box.png", new Color(13, 163, 103));
        addList(fullList, "player_box_2", "box.png", new Color(67, 106, 238));
        addList(fullList, "player_box_3", "box.png", new Color(210, 77, 6));
        addList(fullList, "player_box_4", "box.png", new Color(187, 26, 171));

        addList(fullList, "board_0", "board.png", new Color(241, 59, 59));
        addList(fullList, "board_1", "board.png", new Color(13, 163, 103));
        addList(fullList, "board_2", "board.png", new Color(67, 106, 238));
        addList(fullList, "board_3", "board.png", new Color(210, 77, 6));
        addList(fullList, "board_4", "board.png", new Color(187, 26, 171));

        addList(fullList, "player_box_offline", "box.png", new Color(118, 118, 118));

        addList(fullList, "number_0", "number/0.png");
        addList(fullList, "number_1", "number/1.png");
        addList(fullList, "number_2", "number/2.png");
        addList(fullList, "number_3", "number/3.png");
        addList(fullList, "number_4", "number/4.png");
        addList(fullList, "number_5", "number/5.png");
        addList(fullList, "number_6", "number/6.png");
        addList(fullList, "number_7", "number/7.png");
        addList(fullList, "number_8", "number/8.png");
        addList(fullList, "number_9", "number/9.png");
        addList(fullList, "clock_container", "box.png", new Color(72, 113, 210));
        addList(fullList, "clock_img", "clock.png", new Color(255, 255, 255));
        addList(fullList, "grid_img", "grid.png", new Color(220, 140, 70));
        addList(fullList, "waterdrop", "waterdrop.png", new Color(255, 255, 255));
        addList(fullList, "mine_counter", "box.png", new Color(49, 139, 139));
        addList(fullList, "chooser", "box.png", new Color(88, 88, 88));
        addList(fullList, "home", "home.png", new Color(188, 226, 22));

        addList(fullList, "hexagon_gray", "hexagon.png", new Color(83, 83, 83));
        addList(fullList, "hexagon_pink", "hexagon.png", new Color(184, 69, 167));
        addList(fullList, "hexagon_yellow", "hexagon.png", new Color(205, 192, 55));
        addList(fullList, "hexagon_green", "hexagon.png", new Color(101, 193, 56));
        addList(fullList, "hexagon_blue", "hexagon.png", new Color(41, 102, 180));
        addList(fullList, "hexagon_red", "hexagon.png", new Color(205, 60, 55));

        addList(fullList, "game_background", "game_background.jpg");
        addList(fullList, "grid_mine", "grid/mine.png");
        addList(fullList, "grid_flag", "grid/flag.png");
        addList(fullList, "grid_border", "grid/grid_border.png");
        addList(fullList, "grid_glassTile", "grid/tile.png");
        addList(fullList, "pit", "grid/pit.png");
        addList(fullList, "flame", "grid/explosion.png");
        addList(fullList, "grid_number_1", "number/1_center.png", new Color(57, 109, 236));
        addList(fullList, "grid_number_2", "number/2.png", new Color(52, 187, 144));
        addList(fullList, "grid_number_3", "number/3.png", new Color(150, 206, 47));
        addList(fullList, "grid_number_4", "number/4.png", new Color(243, 220, 40));
        addList(fullList, "grid_number_5", "number/5.png", new Color(241, 168, 41));
        addList(fullList, "grid_number_6", "number/6.png", new Color(203, 103, 16));
        addList(fullList, "grid_number_7", "number/7.png", new Color(208, 73, 30));
        addList(fullList, "grid_number_8", "number/8.png", new Color(229, 0, 0));
        for (int i = 0; i < MineBackgroundNum; ++i)
            addList(fullList, "mine_background_" + i, "grid/background/" + i + ".jpg");

        addSoundList(soundList, "msg", "audio/msg.wav");
        addSoundList(soundList, "like", "audio/like.mp3");
        addSoundList(soundList, "intro", "audio/intro.wav");
        addSoundList(soundList, "your_turn", "audio/your_turn.wav");
        addSoundList(soundList, "applause", "audio/applause.wav");
        addSoundList(soundList, "click", "audio/click.wav");
        addSoundList(soundList, "activate", "audio/activate.mp3");
        addSoundList(soundList, "type", "audio/type.mp3");
        addSoundList(soundList, "tick", "audio/tick.mp3");
        addSoundList(soundList, "explode", "audio/explode.mp3");
        addSoundList(soundList, "error", "audio/error.mp3");
        addSoundList(soundList, "wood", "audio/wood.mp3");
        addSoundList(soundList, "bgm", "audio/bgm_moving_on.mp3");

        Thread loadingThread = new Thread(new LoadingThread());
        loadingThread.start();
    }

    public static int getLoadState() {
        if (!basicLoaded && !fullyLoaded)
            return 0;
        if (basicLoaded && !fullyLoaded)
            return 1;
        return 2;
    }

    private static BufferedImage loadImage(String imgPath) {
        return loadImage(imgPath, Color.white);
    }

    private static BufferedImage loadImage(String imgPath, Color color) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new FileInputStream(imgPath));
        } catch (IOException e) {
            ErrorMsg.error(String.format("File error: %s", e.getMessage()));
        }
        BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = res.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        float[] scale = {color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f};
        RescaleOp rop = new RescaleOp(scale, new float[4], null);
        return rop.filter(res, null);
    }

    public static BufferedImage getImage(String name) {
        return imgs.get(name);
    }

}
