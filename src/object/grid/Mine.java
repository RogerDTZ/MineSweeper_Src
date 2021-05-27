/**
 * @Author: RogerDTZ
 * @FileName: Mine.java
 */

package object.grid;

import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Camera;
import graphics.Sprite;
import javafx.util.Pair;
import main.AudioManager;
import object.GameObject;
import object.ui.button.RectButton;

public class Mine extends GameObject {

    public static final double ContentFill = 0.8;

    public static final double RevealDuration = 0.3;
    public static final double FlagShowDuration = 0.5;
    public static final double JumpDuration = 0.1;
    public static final double AimDuration = 0.15;

    private Sprite sprite_cover;

    private int initMineCnt;

    private boolean isMine;
    private int neighbourCnt;
    private boolean hasFlag;
    private boolean isRevealed;
    private boolean isCheating;
    private boolean showState;
    private boolean flagShowState;
    private boolean aimShowState;

    private GameObject root;
    private RectButton button;
    private GameObject content;
    private GameObject surface;
    private GameObject glassTile;
    private GameObject flag;
    private GameObject pit;
    private GameObject aim;
    private Sprite aim_red, aim_yellow;

    private double len;
    private int coord_x;
    private int coord_y;

    private Animator amt_scale;
    private Animator amt_surf_scale;
    private Animator amt_surf_alpha;
    private Animator amt_flag_pos;
    private Animator amt_flag_alpha;
    private Animator amt_mine_scale;
    private Animator amt_mine_alpha;
    private Animator amt_pit_alpha;
    private Animator amt_jump;
    private Animator amt_aim;

    private int buttonClicked;
    private int clicked;


    public Mine(double x, double y, double a, int pos_x, int pos_y, Sprite sprite_cover, Vector2 imgOffset, double delay) {
        super("mine_" + x + "_" + y, null);

        this.setPosition(x, y);

        this.root = new GameObject(this.id + "_root");
        this.addObject(root);

        this.button = new RectButton(new Vector2(a, a), "Mine_" + x + "_" + y, null) {
            @Override
            public void onClicked(int button) {
                buttonClicked = button;
            }
        };
        this.button.setHoverZoom(1.1);
        this.button.setHoverDuration(0.3, 0.2);
        this.root.addObject(this.button);

        this.len = a;
        this.coord_x = pos_x;
        this.coord_y = pos_y;

        this.neighbourCnt = -2;
        this.hasFlag = false;
        this.isRevealed = false;
        this.isCheating = false;
        this.showState = false;
        this.flagShowState = false;

        this.content = new GameObject(this.id + "_content");
        this.surface = new GameObject(this.id + "_surface");
        this.glassTile = new GameObject(this.id + "_glass");
        this.pit = new GameObject(this.id + "_pit");
        this.surface.addObject(this.glassTile);
        this.flag = new GameObject(this.id + "_flag");
        this.root.addObject(this.content);
        this.root.addObject(this.surface);
        this.root.addObject(this.flag);
        this.root.addObject(this.pit);
        this.content.setRenderPriority(1);
        this.surface.setRenderPriority(2);
        this.flag.setRenderPriority(3);
        this.pit.setRenderPriority(1.5);

        this.sprite_cover = sprite_cover;
        this.surface.setSprite(this.sprite_cover.subImage(new Vector2(imgOffset.x + a * pos_x, imgOffset.y + a * pos_y), new Vector2(a, a)));
        this.surface.setAlpha(0);
        this.glassTile.setSprite(new Sprite("grid_glassTile"));
        this.glassTile.setAlpha(0.8);
        this.flag.setSprite(new Sprite("grid_flag"));
        this.flag.setPosition(0, - this.len);
        this.pit.setSprite(new Sprite("pit"));
        this.flag.setAlpha(0);
        this.pit.setAlpha(0);

        this.surface.resizeTo(a, a);
        this.glassTile.resizeTo(a, a);
        this.content.resizeTo(a * ContentFill, a * ContentFill);
        this.flag.resizeTo(a * ContentFill, a * ContentFill);
        this.pit.resizeTo(a, a);

        this.amt_scale = new Animator(0);
        this.amt_scale.append(Animation.GetTanh(0,1.2,0.8,false, 1 + delay));
        this.amt_scale.append(Animation.GetTanh(1.2,1.0,0.7,true, 0));
        this.amt_scale.append(Animation.GetTanh(1.0,1.0,0.3,false, 0));
        this.amt_surf_alpha = new Animator(1);
        this.amt_surf_scale = new Animator(1);
        this.amt_flag_alpha = new Animator(0);
        this.amt_flag_pos = new Animator(- this.len * 0.5);
        this.amt_mine_scale = new Animator(1);
        this.amt_mine_alpha = new Animator(1);
        this.amt_pit_alpha = new Animator(0);
        this.amt_jump = new Animator(0);
        this.addComponent(this.amt_scale);
        this.addComponent(this.amt_surf_alpha);
        this.addComponent(this.amt_surf_scale);
        this.addComponent(this.amt_flag_alpha);
        this.addComponent(this.amt_flag_pos);
        this.addComponent(this.amt_mine_scale);
        this.addComponent(this.amt_mine_alpha);
        this.addComponent(this.amt_pit_alpha);
        this.addComponent(this.amt_jump);

        this.aim_red = new Sprite("aim");
        this.aim_yellow = new Sprite("aim_yellow");
        this.aim = new GameObject(this.id + "_aim", this.aim_red);
        this.aim.resizeTo(a, a);
        this.aim.setAlpha(0);
        this.aim.setRenderPriority(10);
        this.amt_aim = new Animator(0);
        this.addComponent(this.amt_aim);
        this.root.addObject(this.aim);

        this.root.setScale(0);

        this.clicked = this.buttonClicked = 0;
    }

    public void setMine(int mineCnt) {
        /*
        if (this.neighbourCnt != -2)
            return;
         */
        this.initMineCnt = mineCnt;
        this.isMine = false;
        this.hasFlag = false;
        this.isRevealed = false;
        this.neighbourCnt = -2;
        this.toggleDisplay(false, 0);
        this.toggleFlag(false, 0);
        this.toggleAim(false, 0);

        this.pit.setAlpha(0);
        this.amt_pit_alpha.reset(0);

        if (mineCnt == -1 || mineCnt == -2 || mineCnt == -3) {
            this.neighbourCnt = -1;
            this.isMine = true;
            if (mineCnt == -1) { // uncovered
                this.content.setSprite(new Sprite("grid_mine"));
            } else if (mineCnt == -2) { // flag
                this.hasFlag = true;
                this.content.setSprite(new Sprite("grid_mine"));
                this.toggleFlag(true, 1);
            } else { // exploded
                this.isRevealed = true;
                this.toggleDisplay(true, 1);
                this.amt_pit_alpha.forceAppend(Animation.GetLinear(1, 1, 0));
            }
        } else {
            boolean reveal = mineCnt >= 10;
            if (mineCnt >= 10)
                mineCnt -= 10;
            this.neighbourCnt = mineCnt;
            this.isMine = false;
            this.content.setSprite((this.neighbourCnt > 0 ? new Sprite("grid_number_" + this.neighbourCnt) : null));
            if (reveal)
                this.reveal(1);
        }

        this.content.resizeTo(this.len * ContentFill, this.len * ContentFill);
    }

    public void reveal(double delay) {
        if (this.isRevealed)
            return;
        this.isRevealed = true;
        this.toggleDisplay(true, delay);
        if (this.isMine) {
            this.amt_mine_scale.append(Animation.GetTanh(1, 1.7, 0.7, false, delay + MineManager.ZoomDuration + 0.1));
            this.amt_mine_alpha.append(Animation.GetLinear(1, 0, 0.2, delay + MineManager.ZoomDuration + 0.7));
            this.amt_pit_alpha.append(Animation.GetTanh(0, 1, 0.2, true, delay + MineManager.ZoomDuration + 0.7));
            Camera.shake(0.4, 1, delay + MineManager.ZoomDuration + 0.3);
            /* decompose
            this.mine_scale_animator.append(Animation.GetTanh(1, 1.7, 0.7, false, delay + MineManager.ZoomDuration + 0.1 + 1));
            this.mine_alpha_animator.append(Animation.GetLinear(1, 0, 0.2, delay + MineManager.ZoomDuration + 0.7 + 1));
            this.pit_alpha_animator.append(Animation.GetTanh(0, 1, 0.2, true, delay + MineManager.ZoomDuration + 0.7 + 1));
            Camera.shake(0.4, 1, delay + MineManager.ZoomDuration + 0.3 + 1);
             */
        }
    }

    public void cover() {
        this.setMine(this.initMineCnt < 0 ? -1 : (this.initMineCnt >= 10 ? this.initMineCnt - 10 : this.initMineCnt));
    }

    public void mark(double delay) {
        if (this.hasFlag)
            return;
        if (this.isMine) {
            this.hasFlag = true;
            this.toggleFlag(true, delay);
            AudioManager.Play("like", delay + FlagShowDuration - 0.2);
        } else {
            this.toggleFlag(true, 0);
            this.toggleFlag(false, FlagShowDuration);
            Camera.shake(0.2, 0.15, FlagShowDuration);
            AudioManager.PlayWithVolume("error", 0.7, FlagShowDuration - 0.1);
        }
    }

    private void toggleDisplay(boolean show, double delay) {
        if (show && !this.showState) {
            this.showState = true;
            this.amt_surf_alpha.forceAppend(Animation.GetTanh(this.amt_surf_alpha.val(), 0, RevealDuration, true, delay));
            this.amt_surf_scale.forceAppend(Animation.GetTanh(this.amt_surf_scale.val(), 2, RevealDuration, true, delay));
        } else if (!show && this.showState){
            this.showState = false;
            this.amt_surf_alpha.forceAppend(Animation.GetTanh(this.amt_surf_alpha.val(), 1, RevealDuration, true, delay));
            this.amt_surf_scale.forceAppend(Animation.GetTanh(this.amt_surf_scale.val(), 1, RevealDuration, true, delay));
        }
    }

    private void toggleFlag(boolean show, double delay) {
        if (this.isRevealed)
            return;
        if (show && !this.flagShowState) {
            this.flagShowState = true;
            this.amt_flag_alpha.append(Animation.GetTanh(0, 1, FlagShowDuration, true, delay));
            this.amt_flag_pos.append(Animation.GetTanh(- this.len * 0.5, 0, FlagShowDuration, true, delay));
        } else if (!show && this.flagShowState) {
            this.flagShowState = false;
            this.amt_flag_alpha.append(Animation.GetTanh(1, 0, FlagShowDuration, false, delay));
            this.amt_flag_pos.append(Animation.GetTanh(01, - this.len * 0.5, FlagShowDuration, false, delay));
        }
    }

    public void toggleCheating(boolean flag) {
        this.isCheating = flag;
        this.toggleDisplay(this.isCheating || this.isRevealed, 0);
    }

    public int isClicked() {
        if (this.clicked != 0) {
            int res = this.clicked;
            this.clicked = 0;
            return res;
        } else {
            return 0;
        }
    }

    public void jump(double y, double delay) {
        this.amt_jump.forceAppend(Animation.GetTanh(0, -y, JumpDuration, true, delay));
        //this.jump_animator.append(Animation.GetTanh(-y, -y, 0.05, true));
        this.amt_jump.append(Animation.GetTanh(-y, 0, JumpDuration, false));
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (this.buttonClicked != 0) {
            this.clicked = this.buttonClicked;
            this.buttonClicked = 0;
        }

        this.root.setPosition(0, this.amt_jump.val());

        if (!this.amt_scale.isIdle())
            this.root.setScale(this.amt_scale.val());

        this.surface.setAlpha(this.amt_surf_alpha.val());
        this.surface.setScale(this.amt_surf_scale.val());

        this.flag.setAlpha(this.amt_flag_alpha.val());
        this.flag.setPosition(0, this.amt_flag_pos.val());

        this.content.setScale(this.amt_mine_scale.val());
        this.content.setAlpha(this.amt_mine_alpha.val());

        this.pit.setAlpha(this.amt_pit_alpha.val());

        this.aim.setAlpha(this.amt_aim.val());
    }

    public void toggleAim(boolean flag, int color) {
        if (flag && !this.aimShowState && !this.isRevealed) {
            this.aimShowState = true;
            if (color == 0)
                this.aim.setSprite(this.aim_red);
            else
                this.aim.setSprite(this.aim_yellow);
            this.amt_aim.forceAppend(Animation.GetTanh(this.amt_aim.val(), 0.9, AimDuration, true));
        } else if (!flag && this.aimShowState) {
            this.aimShowState = false;
            this.amt_aim.forceAppend(Animation.GetTanh(this.amt_aim.val(), 0, AimDuration, false));
        }
    }

    public boolean isMine() {
        return this.isMine;
    }

    public boolean hasFlag() {
        return this.hasFlag;
    }

    public boolean isRevealed() {
        return this.isRevealed;
    }

    public int getNeighbourCnt() {
        return this.neighbourCnt;
    }

    public Pair<Integer, Integer> getCoord() {
        return new Pair<>(this.coord_x, this.coord_y);
    }

    public boolean isHovering() {
        return this.button.isHovering();
    }

}
