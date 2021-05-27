/**
 * @Author: RogerDTZ
 * @FileName: SingleChooser.java
 */

package object.ui.input;

import datatype.Vector2;
import graphics.Sprite;
import object.GameObject;
import graphics.Text;
import object.ui.button.ChooseButton;
import util.FontLibrary;

import java.awt.*;
import java.util.ArrayList;

public class SingleChooser extends GameObject {

    private boolean available;
    private int choice;

    private GameObject root;

    private GameObject panel;
    private ArrayList<ChooseButton> buttons;
    private ArrayList<Double> lastActive;


    public SingleChooser(String id, Vector2 size, double buttonSize, double buttonMargin, int buttonNum, ArrayList<String> labels, int fontSize, ArrayList<String> colors) {
        super(id);

        this.available = true;

        this.root = new GameObject(this.id + "_root");
        this.addObject(this.root);

        this.panel = new GameObject(this.id + "_panel", new Sprite("chooser"));
        this.panel.resizeTo(size);
        this.root.addObject(this.panel);

        this.buttons = new ArrayList<>();
        this.lastActive = new ArrayList<>();

        double left = -0.5 * (buttonSize * buttonNum + buttonMargin * (buttonNum - 1));
        for (int i = 0; i < buttonNum; ++i) {
            Vector2 pos = new Vector2(left + i * (buttonSize + buttonMargin) + 0.5 * buttonSize, - 0.3 * buttonSize);

            ChooseButton button = new ChooseButton(this.id + "_choice_" + i, buttonSize, new Sprite("hexagon_" + colors.get(i)));
            button.rotate(90);
            button.setPosition(pos);
            this.buttons.add(button);
            this.root.addObject(button);

            Text text = new Text(this.id + "_text_" + i, labels.get(i), FontLibrary.GetLabelFont(fontSize));
            text.setColor(new Color(212, 212, 212));
            text.setPosition(pos.x, pos.y + buttonSize * 0.5 + text.getSprite().getUnitSize().y * 0.5);
            this.root.addObject(text);

            this.lastActive.add(-1.0);
        }

        this.choice = 0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        this.updateLastActive();
    }

    private void updateLastActive() {
        this.choice = 0;

        double val = 0;
        for (int i = 0; i < this.buttons.size(); ++i) {
            ChooseButton button = this.buttons.get(i);
            if ((this.lastActive.get(i) >= 0) != button.isActive()) {
                if (button.isActive())
                    this.lastActive.set(i, (double)System.currentTimeMillis());
                else
                    this.lastActive.set(i, -1.0);
            }
            if (this.lastActive.get(i) > val) {
                val = this.lastActive.get(i);
                this.choice = i + 1;
            }
        }

        for (int i = 0; i < this.buttons.size(); ++i)
            if (this.choice != i + 1)
                this.buttons.get(i).toggleActive(false);
    }

    public void setAvailable(boolean flag, double delay) {
        if (flag && !this.available) {
            this.available = true;
            for (ChooseButton button : this.buttons)
                button.toggleFreeze(false, delay);
        } else if (!flag && this.available) {
            this.available = false;
            for (ChooseButton button : this.buttons)
                button.toggleFreeze(true, delay);
            this.choice = 0;
        }
    }

    public void ban(int id) {
        this.buttons.get(id).toggleActive(false);
        this.buttons.get(id).toggleFreeze(true, 0);
        this.lastActive.set(id, -1.0);
    }

    public int getChoice() {
        return this.choice;
    }

    public void choose(int id) {
        if (id == 0) {
            for (ChooseButton button : this.buttons)
                button.toggleActive(false);
            return;
        }
        this.buttons.get(id - 1).toggleActive(true);
    }

    public void setTouchable(boolean flag) {
        for (ChooseButton button : this.buttons)
            button.setTouchable(flag);
    }

}
