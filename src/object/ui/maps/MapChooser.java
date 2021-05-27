/**
 * @Author: RogerDTZ
 * @FileName: MapChooser.java
 */

package object.ui.maps;

import datatype.MapInfo;
import datatype.Vector2;
import graphics.Shape;
import graphics.Sprite;
import input.Controller;
import input.InputCallback;
import main.SLManager;
import object.GameObject;
import object.ui.button.IconButton;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;

public class MapChooser extends GameObject implements InputCallback {

    public final static Vector2 PanelSize = new Vector2(550, 300);
    public final static Vector2 PanelPos = new Vector2(0, 50);
    public final static int ItemsEachPage = 5;
    public final static Vector2 FirstItemPos = new Vector2(-20, -20);


    private ArrayList<MapDisplayItem> maps;

    private GameObject root;
    private GameObject panel;
    private IconButton reload;

    private int curr;
    private MapDisplayItem choice;


    public MapChooser(String id) {
        super(id);
        Controller.registerCallback(this);

        this.root = new GameObject(this.id + "_root");
        this.addObject(this.root);

        this.initElements();

        this.maps = new ArrayList<>();

        this.refresh();
    }

    private void initElements() {
        this.panel = new GameObject(this.id + "_panel", new Sprite("map_chooser"));
        this.panel.resizeTo(PanelSize);
        this.panel.setPosition(PanelPos);
        this.root.addObject(this.panel);

        this.reload = new IconButton(this.id + "_reload", new Vector2(50, 50), 0.01, new Sprite("reload"), Shape.Type.Rect);
        this.reload.setPosition(-230, -70);
        this.root.addObject(this.reload);
    }

    public void setTouchable(boolean flag) {
        this.reload.setTouchable(flag);
        for (MapDisplayItem item : this.maps)
            item.setTouchable(flag);
    }

    private void refresh() {
        for (MapDisplayItem item : this.maps)
            item.detach();
        this.maps.clear();
        String[] files = SLManager.GetDirList("maps\\");
        MapDisplayItem.ResetID();
        for (String file : files) {
            MapDisplayItem item = MapDisplayItem.Create("maps\\" + file);
            if (item != null) {
                this.maps.add(item);
                this.root.addObject(item);
            }
        }
        this.curr = 0;
        this.updatePage(this.curr);
    }

    private void updatePage(int start) {
        for (int i = 0; i < this.maps.size(); ++i) {
            if (start <= i && i < start + ItemsEachPage) {
                int index = i - start;
                this.maps.get(i).setPosition(FirstItemPos.x, FirstItemPos.y + index * MapDisplayItem.PanelSize.y);
                this.maps.get(i).setVisible(true);
                this.maps.get(i).setTouchable(true);
            } else {
                this.maps.get(i).setVisible(false);
                this.maps.get(i).setTouchable(false);
            }
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.updateLast();

        if (this.reload.clicked())
            this.refresh();
    }

    private void deselect() {
        for (MapDisplayItem item : this.maps)
            item.toggleChosen(false);
        this.choice = null;
    }

    private void updateLast() {
        this.choice = null;
        double val = 0;
        int id = -1;
        for (int i = 0; i < this.maps.size(); ++i) {
            MapDisplayItem item = this.maps.get(i);
            if (item.getChosenTime() > val) {
                val = item.getChosenTime();
                this.choice = item;
                id = i;
            }
        }
        for (int i = 0; i < this.maps.size(); ++i) {
            MapDisplayItem item = this.maps.get(i);
            if (i != id)
                item.toggleChosen(false);
        }
    }

    public MapInfo getChoice() {
        if (this.choice == null)
            return null;
        return new MapInfo(this.choice.width, this.choice.height, this.choice.mineNum, 10, 5, this.choice.map);
    }

    private void roll(int delta) {
        this.curr += delta;
        if (this.curr + ItemsEachPage > this.maps.size())
            this.curr = this.maps.size() - ItemsEachPage;
        if (this.curr < 0)
            this.curr = 0;
        this.updatePage(this.curr);
    }

    public boolean isHovering() {
        Vector2 pos = Controller.getMousePos();
        try {
            pos.transform(this.panel.getAbsoluteTransform().createInverse());
        } catch (NoninvertibleTransformException ignored) {
        }
        Vector2 rect = new Vector2(this.panel.getObjSize().x * this.transform.scale.x, this.panel.getObjSize().y *  this.transform.scale.y);
        return (- rect.x / 2 <= pos.x && pos.x <= rect.x / 2) && (- rect.y / 2 <= pos.y && pos.y <= rect.y / 2);
    }

    @Override
    public void onMousePressed(MouseEvent e) {
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
        if (this.isHovering())
            this.roll(e.getWheelRotation());
    }

}
