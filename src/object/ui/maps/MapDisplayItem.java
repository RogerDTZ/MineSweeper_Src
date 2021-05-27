/**
 * @Author: RogerDTZ
 * @FileName: MapDisplayItem.java
 */

package object.ui.maps;

import datatype.Vector2;
import graphics.Shape;
import graphics.Sprite;
import input.Controller;
import input.InputCallback;
import main.SLManager;
import main.AudioManager;
import object.GameObject;
import graphics.Text;
import util.FontLibrary;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.NoninvertibleTransformException;

public class MapDisplayItem extends GameObject implements InputCallback {

    public final static Vector2 PanelSize = new Vector2(400, 40);
    private static int item_id = 0;


    private boolean touchable;
    private boolean chosen;
    private double chosenTime;

    String map;
    int width, height;
    int mineNum, restNum;

    private GameObject root;
    private GameObject panel;
    private GameObject highlightPanel;
    private Text number;
    private Text fileName;
    private Text gridSize;
    private Text mineInfo;


    public MapDisplayItem(String id, String fileName, String map, int width, int height, int mineNum, int restNum) {
        super(id);
        Controller.registerCallback(this);

        this.touchable = false;

        this.map = map;
        this.width = width;
        this.height = height;
        this.mineNum = mineNum;
        this.restNum = restNum;

        this.initElements(fileName);
    }

    private void initElements(String fileName) {
        this.root = new GameObject(this.id + "_root");
        this.addObject(this.root);

        this.panel = new Shape(this.id + "_panel", new Color(80, 80, 80), Shape.Type.Rect, new Vector2(PanelSize.x * 0.95, PanelSize.y * 0.8));
        this.root.addObject(this.panel);

        this.highlightPanel = new GameObject(this.id + "_highlight_panel", new Sprite("rect_yellow"));
        this.highlightPanel = new Shape(this.id + "_highlight_panel", new Color(227, 181, 11), Shape.Type.Rect, PanelSize);
        this.highlightPanel.setRenderPriority(-1);
        this.highlightPanel.setVisible(false);
        this.root.addObject(this.highlightPanel);

        this.number = new Text(this.id + "_number", "#" + this.id, FontLibrary.GetMapItemFont(15));
        this.number.setColor(new Color(212, 212, 212));
        this.number.setPosition(PanelSize.x * (-0.45) + this.number.getSprite().getUnitSize().x * 0.5, 0);
        this.root.addObject(this.number);

        String name = fileName.length() <= 23 ? fileName : (fileName.substring(0, 23) + "...");
        this.fileName = new Text(this.id + "fileName", name, FontLibrary.GetMapItemFont(15));
        this.fileName.setColor(new Color(212, 212, 212));
        this.fileName.setPosition(PanelSize.x * (-0.5) + 60 + this.fileName.getSprite().getUnitSize().x * 0.5, 0);
        this.root.addObject(this.fileName);

        this.gridSize = new Text(this.id + "_gridSize", "[" + this.width + " X " + this.height + "]", FontLibrary.GetMapItemFont(15));
        this.gridSize.setColor(new Color(212, 212, 212));
        this.gridSize.setPosition(PanelSize.x * 0.23, 0);
        this.root.addObject(this.gridSize);

        this.mineInfo = new Text(this.id + "_mineInfo", (this.mineNum - this.restNum)  + " / " + this.mineNum, FontLibrary.GetMapItemFont(15));
        this.mineInfo.setColor(new Color(212, 212, 212));
        this.mineInfo.setPosition(PanelSize.x * 0.4, 0);
        this.root.addObject(this.mineInfo);
    }

    public void setTouchable(boolean flag) {
        this.touchable = flag;
    }

    public static void ResetID() {
        item_id = 0;
    }

    public static MapDisplayItem Create(String dir) {
        String data = SLManager.ReadFile(dir);
        if (data == null)
            return null;
        int width = -1;
        int height = 0;
        StringBuilder sb = new StringBuilder();
        int mineNum = 0;
        int restNum = 0;
        for (int i = 0, j; i < data.length(); i = j + 1) {
            for (j = i; j < data.length() && data.charAt(j) != '\n'; ++j);
            if (width == -1) {
                width = j - i;
            } else if (width != j - i) {
                return null;
            }
            ++height;
            for (int k = i; k < j; ++k) {
                char c = data.charAt(k);
                if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4')
                    return null;
                if (c == '2' || c == '3' || c == '4') {
                    ++mineNum;
                    if (c == '2')
                        ++restNum;
                }
                sb.append(c);
            }
        }
        return new MapDisplayItem("" + (++item_id), dir.substring(5), sb.toString(), width, height, mineNum, restNum);
    }

    public boolean isHovering() {
        Vector2 pos = Controller.getMousePos();
        try {
            pos.transform(this.getAbsoluteTransform().createInverse());
        } catch (NoninvertibleTransformException ignored) {
        }
        Vector2 rect = new Vector2(this.panel.getObjSize().x * this.transform.scale.x, this.panel.getObjSize().y *  this.transform.scale.y);
        return (- rect.x / 2 <= pos.x && pos.x <= rect.x / 2) && (- rect.y / 2 <= pos.y && pos.y <= rect.y / 2);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.highlightPanel.setVisible(this.touchable && (this.chosen || this.isHovering()));
    }

    public void toggleChosen(boolean flag) {
        if (flag && !this.chosen) {
            this.chosen = true;
            this.chosenTime = System.currentTimeMillis();
        } else if (!flag && this.chosen) {
            this.chosen = false;
            this.chosenTime = -1;
        }
    }

    public double getChosenTime() {
        return this.chosenTime;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        if (e.getButton() == 1 && this.touchable && this.isHovering()) {
            this.toggleChosen(true);
            AudioManager.Play("click");
        }
    }

    @Override
    public void onMouseReleased(MouseEvent e) {

    }

    @Override
    public void onMouseMoved(Vector2 mousePos) {

    }

    @Override
    public void onKeyPressed(KeyEvent e) {

    }

    @Override
    public void onKeyReleased(KeyEvent e) {

    }

    @Override
    public void onKeyTyped(KeyEvent e) {

    }

    @Override
    public void onMouseWheelMoved(MouseWheelEvent e) {

    }
}
