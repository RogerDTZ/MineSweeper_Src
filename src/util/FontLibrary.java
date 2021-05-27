/**
 * @Author: RogerDTZ
 * @FileName: FontLibrary.java
 */

package util;

import java.awt.*;

public class FontLibrary {

    public static Font GetPlayerNameFont(int fontSize) {
        return new Font("Impact", Font.PLAIN, fontSize);
    }

    public static Font GetPlayerIpFont(int fontSize) {
        return new Font("Consolas", Font.PLAIN, fontSize);
    }

    public static Font GetMenuButtonFont(int fontSize) {
        return new Font("Cooper Std", Font.BOLD, fontSize);
    }

    public static Font GetInputBoxDefaultFont(int fontSize) {
        return new Font("Consolas", Font.ITALIC, fontSize);
    }

    public static Font GetInputBoxFont(int fontSize) {
        return new Font("Consolas", Font.BOLD, fontSize);
    }

    public static Font GetMenuConnectTextFont(int fontSize) {
        return new Font("Consolas", Font.BOLD, fontSize);
    }

    public static Font GetAttentionFont(int fontSize) {
        return new Font("Consolas", Font.PLAIN, fontSize);
    }

    public static Font GetLabelFont(int fontSize) {
        return new Font("Impact", Font.PLAIN, fontSize);
    }

    public static Font GetTitleFont(int fontSize) {
        return new Font("Impact", Font.BOLD, fontSize);
    }

    public static Font GetPauseMenuButtonFont(int fontSize) {
        return new Font("Cooper Std", Font.BOLD, fontSize);
    }

    public static Font GetMapItemFont(int fontSize) {
        return new Font("Consolas", Font.PLAIN, fontSize);
    }

}
