/**
 * @Author: RogerDTZ
 * @FileName: Controller.java
 */

package input;

import datatype.Vector2;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Controller implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    private static boolean pressed[] = new boolean[1024];
    private static ArrayList<InputCallback> callbackObjects = new ArrayList<>();
    private static Vector2 mousePos = new Vector2(0, 0);
    private static Lock lock = new ReentrantLock();


    public static boolean isKeyDown(int code) {
        return pressed[code];
    }

    public static boolean isKeyDown(char key) {
        int code = getKeyCode(key);
        return pressed[code];
    }

    public static void registerCallback(InputCallback obj) {
        lock.lock();
        callbackObjects.add(obj);
        lock.unlock();
    }

    public static void removeCallback(InputCallback obj) {
        callbackObjects.remove(obj);
    }

    public static Vector2 getMousePos() {
        return new Vector2(mousePos.x, mousePos.y);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onKeyTyped(e);
        lock.unlock();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressed[e.getKeyCode()] = true;
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onKeyPressed(e);
        lock.unlock();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressed[e.getKeyCode()] = false;
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onKeyReleased(e);
        lock.unlock();
    }

    private static int getKeyCode(char key) {
        if (Character.isLetter(key) && 'a' <= key && key <= 'z')
            key = (char)(key - 'a' + 'A');
        switch (key) {
            case 'A': return KeyEvent.VK_A;
            case 'B': return KeyEvent.VK_B;
            case 'C': return KeyEvent.VK_C;
            case 'D': return KeyEvent.VK_D;
            case 'E': return KeyEvent.VK_E;
            case 'F': return KeyEvent.VK_F;
            case 'G': return KeyEvent.VK_G;
            case 'H': return KeyEvent.VK_H;
            case 'I': return KeyEvent.VK_I;
            case 'J': return KeyEvent.VK_J;
            case 'K': return KeyEvent.VK_K;
            case 'L': return KeyEvent.VK_L;
            case 'M': return KeyEvent.VK_M;
            case 'N': return KeyEvent.VK_N;
            case 'O': return KeyEvent.VK_O;
            case 'P': return KeyEvent.VK_P;
            case 'Q': return KeyEvent.VK_Q;
            case 'R': return KeyEvent.VK_R;
            case 'S': return KeyEvent.VK_S;
            case 'T': return KeyEvent.VK_T;
            case 'U': return KeyEvent.VK_U;
            case 'V': return KeyEvent.VK_V;
            case 'W': return KeyEvent.VK_W;
            case 'X': return KeyEvent.VK_X;
            case 'Y': return KeyEvent.VK_Y;
            case 'Z': return KeyEvent.VK_Z;
            case '0': return KeyEvent.VK_0;
            case '1': return KeyEvent.VK_1;
            case '2': return KeyEvent.VK_2;
            case '3': return KeyEvent.VK_3;
            case '4': return KeyEvent.VK_4;
            case '5': return KeyEvent.VK_5;
            case '6': return KeyEvent.VK_6;
            case '7': return KeyEvent.VK_7;
            case '8': return KeyEvent.VK_8;
            case '9': return KeyEvent.VK_9;
            default: return -1;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //System.out.println("Mouse Pressed at (" + e.getX() + ", " + e.getY() + ")");
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onMousePressed(e);
        lock.unlock();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //System.out.println("Mouse Released at (" + e.getX() + ", " + e.getY() + ")");
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onMouseReleased(e);
        lock.unlock();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePos.x = e.getX() - 8;
        mousePos.y = e.getY() - 31;
        // System.out.printf("Mouse Dragging: (%.0f, %.0f)\n", mousePos.x, mousePos.y);
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onMouseMoved(mousePos);
        lock.unlock();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.x = e.getX() - 8;
        mousePos.y = e.getY() - 31;
        // System.out.printf("Mouse Moving: (%.0f, %.0f)\n", mousePos.x, mousePos.y);
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onMouseMoved(mousePos);
        lock.unlock();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        lock.lock();
        for (InputCallback obj : callbackObjects)
            obj.onMouseWheelMoved(e);
        lock.unlock();
    }

}
