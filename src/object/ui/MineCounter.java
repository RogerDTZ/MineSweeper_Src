/**
 * @Author: Kaia
 * @FileName: MineCounter.java
 */

package object.ui;

import datatype.Vector2;
import graphics.Sprite;
import object.GameObject;

public class MineCounter extends GameObject {

    public final static Vector2 PanelSize = new Vector2(280, 66);
    public final static Vector2 MineSize = new Vector2(130, 130);
    public final static Vector2 DigitSize = new Vector2(27, 50);


    private GameObject root;
    private GameObject panel;
    private GameObject mine;
    private GameObject digit_0;
    private GameObject digit_1;
    private GameObject digit_2;

    private int number;


    public MineCounter(String id) {
        super(id);

        this.root = new GameObject(this.id + "_root");
        this.addObject(this.root);

        this.panel = new GameObject(this.id + "_panel", new Sprite("mine_counter"));
        this.panel.setScale(1.1);
        this.mine = new GameObject(this.id + "_mine", new Sprite("grid_mine"));
        this.digit_0 = new GameObject(this.id + "_0");
        this.digit_1 = new GameObject(this.id + "_1");
        this.digit_2 = new GameObject(this.id + "_2");
        this.root.addObject(this.panel);
        this.root.addObject(this.mine);
        this.root.addObject(this.digit_0);
        this.root.addObject(this.digit_1);
        this.root.addObject(this.digit_2);

        this.panel.setRenderPriority(1);
        this.mine.setRenderPriority(2);
        this.digit_0.setRenderPriority(2);
        this.digit_1.setRenderPriority(2);
        this.digit_2.setRenderPriority(2);

        this.panel.resizeTo(PanelSize);

        this.mine.resizeTo(MineSize);
        this.mine.setPosition(-PanelSize.x / 2 + 50 + MineSize.x / 2, - (MineSize.y - PanelSize.y) / 2);

        double pos_x = -PanelSize.x / 2 + 10 + MineSize.x + 15 + DigitSize.x / 2;
        this.digit_0.setPosition(pos_x, 0);
        this.digit_1.setPosition(pos_x + DigitSize.x + 8 , 0);
        this.digit_2.setPosition(pos_x + DigitSize.x * 2 + 16, 0);

        this.number = -1;
        this.digit_0.setSprite(new Sprite("number_0"));
        this.digit_1.setSprite(new Sprite("number_0"));
        this.digit_2.setSprite(new Sprite("number_0"));
        this.digit_0.resizeTo(DigitSize);
        this.digit_1.resizeTo(DigitSize);
        this.digit_2.resizeTo(DigitSize);
    }

    public void setNumber(int x, double delay) {
        if (this.number != x) {
            this.number = x;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    int t = x;
                    try {
                        Thread.sleep(Math.round(delay * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    digit_0.setSprite(new Sprite("number_" + t / 100));
                    digit_1.setSprite(new Sprite("number_" + (t / 10) % 10));
                    digit_2.setSprite(new Sprite("number_" + t % 10));
                }
            };
            thread.start();
        }
    }

    public int getNumber() {
        return this.number;
    }

}
