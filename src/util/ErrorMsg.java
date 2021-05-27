/**
 * @Author: RogerDTZ
 * @FileName: ErrorMsg.java
 */

package util;

import javax.swing.*;
import java.awt.*;

public class ErrorMsg {

    public static void error(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

}
