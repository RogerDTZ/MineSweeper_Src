/**
 * @Author: RogerDTZ
 * @FileName: RoomPlayerBox.java
 */

package object.ui.box;

import graphics.Shape;
import component.animation.Animation;
import component.animation.Animator;
import datatype.Vector2;
import graphics.Sprite;
import object.Player;
import object.GameObject;
import graphics.Text;
import object.ui.button.IconButton;
import util.FontLibrary;

import java.awt.*;

public class RoomPlayerBox extends GameObject {

    private Player player;
    private Text playerName;
    private Text address;
    private GameObject readyState;

    private Animator amt_a;
    private Animator amt_v;

    private IconButton cancel;

    private boolean isDestroyed;
    private boolean applyToDelete;

    private boolean active;


    public RoomPlayerBox(String id, Vector2 size, Player player, boolean authority) {
        super(id, new Sprite("player_label"));
        this.resizeTo(size);

        this.player = player;

        this.playerName = new Text(id + "_playerName", "", FontLibrary.GetPlayerNameFont(25));
        this.playerName.setColor(Color.white);
        this.playerName.rotate(1);
        this.setPlayerName("");
        this.addObject(this.playerName);

        this.address = new Text(id + "_playerAddress", "", FontLibrary.GetPlayerIpFont(15));
        this.address.setColor(Color.white);
        this.address.rotate(1);
        this.setPlayerAddress("");
        this.addObject(this.address);

        this.readyState = new GameObject(id + "_ready", new Sprite("room_ready_no"));
        this.readyState.resizeTo(this.sprite.getUnitSize().y * 0.3, this.sprite.getUnitSize().y * 0.3);
        this.readyState.setPosition(this.sprite.getUnitSize().x / 2 - 10 - this.readyState.getSprite().getUnitSize().x / 2, -15);
        this.addObject(this.readyState);

        this.amt_a = new Animator(0);
        this.amt_v = new Animator(0);
        this.addComponent(this.amt_a);
        this.addComponent(this.amt_v);
        this.setAlpha(0);

        if (authority) {
            this.cancel = new IconButton("delete", new Vector2(50, 50), 1.3, new Sprite("exit"), Shape.Type.Circle);
            this.cancel.setPosition(+ this.sprite.getUnitSize().x / 2 + 50, -15);
            this.cancel.setHoverZoom(1.1);
            this.addObject(this.cancel);
        }

        this.active = true;
    }

    public void toggleActive(boolean flag) {
        if (flag && !this.active) {
            this.active = true;
            if (this.cancel != null) {
                this.cancel.setVisible(true);
                this.cancel.setTouchable(true);
            }
        } else if (!flag && this.active) {
            this.active = false;
            if (this.cancel != null) {
                this.cancel.setVisible(false);
                this.cancel.setTouchable(false);
            }
        }
    }

    public void setPlayerName(String name) {
        this.playerName.setText(name);
        this.playerName.setPosition(- this.sprite.getUnitSize().x / 2 + 30 + this.playerName.getSprite().getUnitSize().x / 2, -3);
    }

    public void setPlayerAddress(String address) {
        this.address.setText(address);
        this.address.setPosition(this.address.getSprite().getUnitSize().x * 0.5, -3);
    }

    public void toggleReadyState(boolean flag) {
        this.readyState.setSprite(new Sprite(flag ? "room_ready_yes" : "room_ready_no"));
    }

    public void setTargetLocation(double start, double target, double duration) {
        this.amt_v.forceAppend(Animation.GetTanh(start, target, duration, true));
    }

    public void setTargetLocation(double target, double duration) {
        this.amt_v.forceAppend(Animation.GetTanh(this.amt_v.val(), target, duration, true));
    }

    public void setTargetAlpha(double start, double alpha, double duration) {
        this.amt_a.forceAppend(Animation.GetTanh(start, alpha, duration, alpha > this.amt_a.val()));
    }

    public void setTargetAlpha(double alpha, double duration) {
        this.amt_a.forceAppend(Animation.GetTanh(this.amt_a.val(), alpha, duration, alpha > this.amt_a.val()));
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.setAlpha(this.amt_a.val());
        this.setPosition(this.transform.position.x, this.amt_v.val());

        if (this.cancel != null && this.cancel.clicked()) {
            this.applyToDelete = true;
        }
    }

    public void destroy() {
        this.isDestroyed = true;
    }

    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    public boolean isEnded() {
        return this.amt_a.isIdle() && this.amt_a.val() <= 0;
    }

    public boolean applyToDelete() {
        return this.active && this.applyToDelete;
    }

    public Player getPlayer() {
        return this.player;
    }

}
