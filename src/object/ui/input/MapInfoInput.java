/**
 * @Author: RogerDTZ
 * @FileName: MapInfoInput.java
 */

package object.ui.input;

import component.animation.Animation;
import component.animation.Animator;
import datatype.MapInfo;
import datatype.Vector2;
import graphics.Sprite;
import object.GameObject;
import graphics.Text;
import object.ui.maps.MapChooser;
import util.FontLibrary;

import java.awt.*;
import java.util.ArrayList;

public class MapInfoInput extends GameObject {

    private enum Preset {

        Easy(9, 9, 10, 10, 3),
        Middle(16, 16, 40, 15, 5),
        Hard(30, 16, 99, 20, 8);

        Preset(int width, int height, int mineNum, int timePerRound, int stepPerRound) {
            this.width = width;
            this.height = height;
            this.mineNum = mineNum;
            this.timePerRound = timePerRound;
            this.stepPerRound = stepPerRound;
        }

        int width;
        int height;
        int mineNum;
        int timePerRound;
        int stepPerRound;

        public MapInfo get() {
            return new MapInfo(this.width, this.height, this.mineNum, this.stepPerRound, this.timePerRound);
        }

    }

    public static final Vector2 BoxSize = new Vector2(100, 48);
    public static final Vector2 PresetPanelSize = new Vector2(350, 90);
    public static final double PresetButtonSize = 40.0;
    public static final double PresetButtonMargin = 40.0;

    public static final Vector2 MapChooserPos = new Vector2(-500, 0);
    public static final double MapChooserPopBack = 600;
    public static final double PopDuration = 0.3;


    private boolean touchable;
    private boolean changeable;

    private GameObject root;

    private InputBox gridInput_width;
    private InputBox gridInput_height;
    private InputBox gridInput_mineNum;
    private InputBox gridInput_time;
    private InputBox gridInput_step;

    private GameObject panel;
    private Text title;
    private GameObject icon_grid;
    private GameObject icon_mine;
    private GameObject icon_time;
    private GameObject icon_cost;

    private MapChooser mapChooser;
    private boolean mapChooserShowState;

    private Animator amt_alpha;
    private Animator amt_popOut;

    private SingleChooser gridInput_preset;
    private int lastChoice;

    private MapInfo info;
    private MapInfo fileInfo;


    public MapInfoInput(String id, boolean changeable) {
        super(id, null);

        this.root = new GameObject(this.id + "_root");
        this.addObject(this.root);

        this.initElements();

        this.root.setAlpha(0);
        this.amt_alpha = new Animator(0);
        this.addComponent(this.amt_alpha);
        this.amt_alpha.append(Animation.GetTanh(0, 1, 0.3, true, 0.1));

        this.lastChoice = 1;
        this.gridInput_preset.choose(1);
        this.info = Preset.Easy.get();
        this.updateBox();

        this.changeable = changeable;
        if (!this.changeable)
            this.lock();
    }

    private void initElements() {
        this.gridInput_width = new InputBox(
                "gridInput_width",
                new Sprite("rect_black"),
                BoxSize,
                1, 3,
                FontLibrary.GetInputBoxFont(20),
                FontLibrary.GetInputBoxDefaultFont(20),
                "Width",
                "0123456789");
        this.gridInput_height = new InputBox(
                "gridInput_height",
                new Sprite("rect_black"),
                BoxSize,
                1, 3,
                FontLibrary.GetInputBoxFont(20),
                FontLibrary.GetInputBoxDefaultFont(20),
                "Height",
                "0123456789");
        this.gridInput_mineNum = new InputBox(
                "gridInput_mine",
                new Sprite("rect_black"),
                BoxSize,
                1, 4,
                FontLibrary.GetInputBoxFont(20),
                FontLibrary.GetInputBoxDefaultFont(20),
                "Mine",
                "0123456789");
        this.gridInput_time = new InputBox(
                "gridInput_time",
                new Sprite("rect_black"),
                BoxSize,
                1, 4,
                FontLibrary.GetInputBoxFont(20),
                FontLibrary.GetInputBoxDefaultFont(20),
                "Time",
                "0123456789");
        this.gridInput_step = new InputBox(
                "gridInput_step",
                new Sprite("rect_black"),
                BoxSize,
                1, 4,
                FontLibrary.GetInputBoxFont(20),
                FontLibrary.GetInputBoxDefaultFont(20),
                "Step",
                "0123456789");
        this.gridInput_preset = new SingleChooser(
                "gridInput_preset",
                PresetPanelSize,
                PresetButtonSize,
                PresetButtonMargin,
                4,
                new ArrayList<String>(){{ add("Easy"); add("Middle"); add("Hard"); add("Load"); }},
                20,
                new ArrayList<String>(){{ add("green"); add("yellow"); add("red"); add("blue"); }}
        );
        this.gridInput_width.setPosition(- 60, -80);
        this.gridInput_height.setPosition(60, -80);
        this.gridInput_mineNum.setPosition(270, -80);
        this.gridInput_time.setPosition(-60, -10);
        this.gridInput_step.setPosition(270, -10);
        this.gridInput_preset.setPosition(0, 100);

        this.gridInput_width.setDefaultTextColor(new Color(150, 150, 150));
        this.gridInput_height.setDefaultTextColor(new Color(150, 150, 150));
        this.gridInput_mineNum.setDefaultTextColor(new Color(150, 150, 150));
        this.gridInput_time.setDefaultTextColor(new Color(150, 150, 150));
        this.gridInput_step.setDefaultTextColor(new Color(150, 150, 150));

        this.gridInput_width.setRange(3, 30);
        this.gridInput_height.setRange(3, 24);
        this.gridInput_mineNum.setRange(1, 360);
        this.gridInput_time.setRange(5, 999);
        this.gridInput_step.setRange(1, 10);

        this.root.addObject(this.gridInput_width);
        this.root.addObject(this.gridInput_height);
        this.root.addObject(this.gridInput_mineNum);
        this.root.addObject(this.gridInput_time);
        this.root.addObject(this.gridInput_step);
        this.root.addObject(this.gridInput_preset);

        this.icon_grid = new GameObject("icon_grid", new Sprite("grid_img"));
        this.icon_grid.resizeTo(BoxSize.y * 1.2, BoxSize.y * 1.2);
        this.icon_grid.setPosition(-60 - 20 - BoxSize.x * 0.5 - BoxSize.y * 1.2 * 0.5, -80);
        this.root.addObject(this.icon_grid);

        this.icon_mine = new GameObject("icon_mine", new Sprite("grid_mine"));
        this.icon_mine.resizeTo(BoxSize.y * 1.2, BoxSize.y * 1.2);
        this.icon_mine.setPosition(270 - 20 - BoxSize.x * 0.5 - BoxSize.y * 1.2 * 0.5, -80);
        this.root.addObject(this.icon_mine);

        this.icon_cost = new GameObject("icon_cost", new Sprite("waterdrop"));
        this.icon_cost.resizeTo(BoxSize.y * 1.2, BoxSize.y * 1.2);
        this.icon_cost.setPosition(270 - 20 - BoxSize.x * 0.5 - BoxSize.y * 1.2 * 0.5, -10);
        this.root.addObject(this.icon_cost);


        this.icon_time = new GameObject("icon_time", new Sprite("clock_img"));
        this.icon_time.resizeTo(BoxSize.y * 1.2, BoxSize.y * 1.2);
        this.icon_time.setPosition(-60 - 20 - BoxSize.x * 0.5 - BoxSize.y * 1.2 * 0.5, -10);
        this.root.addObject(this.icon_time);

        this.title = new Text("title", "Map Settings", FontLibrary.GetTitleFont(50));
        this.title.setPosition(0, -150);
        this.title.setColor(new Color(212, 212, 212));
        this.root.addObject(this.title);

        this.panel = new GameObject("panel", new Sprite("map_info_input"));
        this.panel.resizeTo(800, 450);
        this.panel.setPosition(100, 0);
        this.panel.setRenderPriority(-1);
        this.root.addObject(this.panel);

        this.mapChooser = new MapChooser(this.id + "_mapChooser");
        this.mapChooser.setPosition(MapChooserPos.x + MapChooserPopBack, MapChooserPos.y);
        this.mapChooser.setRenderPriority(-2);
        this.mapChooser.setTouchable(false);
        this.root.addObject(this.mapChooser);

        this.amt_popOut = new Animator(+ MapChooserPopBack);
        this.addComponent(this.amt_popOut);

        this.fileInfo = new MapInfo(-1, -1, 0, 0, 0);
    }

    public void setTouchable(boolean flag) {
        this.touchable = flag;
        this.gridInput_width.setTouchable(flag);
        this.gridInput_height.setTouchable(flag);
        this.gridInput_mineNum.setTouchable(flag);
        this.gridInput_time.setTouchable(flag);
        this.gridInput_step.setTouchable(flag);
        this.mapChooser.setTouchable(flag && this.mapChooserShowState);
    }

    private void toggleMapChooser(boolean flag) {
        if (flag && !this.mapChooserShowState) {
            this.mapChooserShowState = true;
            this.amt_popOut.forceAppend(Animation.GetTanh(this.amt_popOut.val(), 0, PopDuration, true));
            this.mapChooser.setTouchable(true);
        } else if (!flag && this.mapChooserShowState) {
            this.mapChooserShowState = false;
            this.amt_popOut.forceAppend(Animation.GetTanh(this.amt_popOut.val(), MapChooserPopBack, PopDuration, true));
            this.mapChooser.setTouchable(false);
        }
    }

    private void lock() {
        this.gridInput_width.lock();
        this.gridInput_height.lock();
        this.gridInput_mineNum.lock();
        this.gridInput_time.lock();
        this.gridInput_step.lock();
        this.gridInput_preset.setVisible(false);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.root.setAlpha(this.amt_alpha.val());

        this.mapChooser.setPosition(MapChooserPos.x + this.amt_popOut.val(), MapChooserPos.y);

        this.updateInfo();
        if (this.changeable) {
            this.updateChoice();
            this.limitMine();
        }
    }

    private void limitMine() {
        this.gridInput_mineNum.setRange(1, (this.info.width * this.info.height) / 2, true);
    }

    private void updateBox() {
        if (this.lastChoice == 0)
            return;
        this.gridInput_width.setInput("" + this.info.width);
        this.gridInput_height.setInput("" + this.info.height);
        this.gridInput_mineNum.setInput("" + this.info.mineNum);
        this.gridInput_step.setInput("" + this.info.stepPerRound);
        this.gridInput_time.setInput("" + this.info.timePerRound);
    }

    private void updateChoice() {
        if (this.lastChoice == 4) {
            if (this.mapChooser.getChoice() != null && !this.mapChooser.getChoice().equals(this.fileInfo)) {
                this.fileInfo.set(this.mapChooser.getChoice());
                this.info.set(this.fileInfo);
                this.updateBox();
            }
        }
        boolean existSame = false;
        if (this.lastChoice == 0) {
            existSame = true;
        } if (1 <= this.lastChoice && this.lastChoice <= 3) {
            for (Preset preset : Preset.values())
                if (this.info.equals(preset.get())) {
                    existSame = true;
                    break;
                }
        } else if (this.lastChoice == 4) {
            if (this.mapChooser.getChoice() == null || this.info.softEquals(this.mapChooser.getChoice()))
                existSame = true;
        }

        if (!existSame)
            this.gridInput_preset.choose(0);

        if (this.lastChoice != this.gridInput_preset.getChoice()) {
            if (this.lastChoice == 4) {
                this.toggleMapChooser(false);
                this.mapChooser.setTouchable(false);
            }
            this.lastChoice = this.gridInput_preset.getChoice();
            switch (this.lastChoice) {
                case 0:
                    break;
                case 1:
                    this.info = Preset.Easy.get();
                    break;
                case 2:
                    this.info = Preset.Middle.get();
                    break;
                case 3:
                    this.info = Preset.Hard.get();
                    break;
                case 4:
                    this.toggleMapChooser(true);
                    this.mapChooser.setTouchable(true);
                    if (this.mapChooser.getChoice() != null) {
                        this.info.set(this.mapChooser.getChoice());
                    }
                    break;
            }
            this.updateBox();
        }
    }

    private void updateInfo() {
        String result;

        result = this.gridInput_width.getResult();
        if (result != null)
            this.info.width = Integer.parseInt(result);

        result = this.gridInput_height.getResult();
        if (result != null)
            this.info.height = Integer.parseInt(result);

        result = this.gridInput_mineNum.getResult();
        if (result != null)
            this.info.mineNum = Integer.parseInt(result);

        result = this.gridInput_time.getResult();
        if (result != null)
            this.info.timePerRound = Integer.parseInt(result);

        result = this.gridInput_step.getResult();
        if (result != null)
            this.info.stepPerRound = Integer.parseInt(result);
    }

    public MapInfo getInfo() {
        return this.info;
    }

    public void setInfo(MapInfo info) {
        this.gridInput_width.setInput("" + info.width);
        this.gridInput_height.setInput("" + info.height);
        this.gridInput_mineNum.setInput("" + info.mineNum);
        this.gridInput_time.setInput("" + info.timePerRound);
        this.gridInput_step.setInput("" + info.stepPerRound);

        this.gridInput_width.submit();
        this.gridInput_height.submit();
        this.gridInput_mineNum.submit();
        this.gridInput_time.submit();
        this.gridInput_step.submit();
    }

}
