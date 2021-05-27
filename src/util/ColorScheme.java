/**
 * @Author: RogerDTZ
 * @FileName: ColorScheme.java
 */

package util;

import java.awt.*;
import java.util.ArrayList;

public class ColorScheme {

    public final static ArrayList<Color> Smoke = new ArrayList<Color>() {{
        add(new Color(47, 47, 47));
        add(new Color(92, 92, 92));
        add(new Color(111, 111, 111));
        add(new Color(130, 130, 130));
    }};

    public final static ArrayList<Color> Cheer = new ArrayList<Color>() {{
        add(new Color(0xFF7F00));
        add(new Color(0x6CD945));
        add(new Color(0xF32D9D));
    }};

    public final static ArrayList<Color> Fire = new ArrayList<Color>() {{
        add(new Color(0xDB2709));
        add(new Color(0xA51F17));
        add(new Color(0xDC9F08));
        add(new Color(0xDB9E47));
    }};

}
