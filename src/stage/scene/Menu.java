/**
 * @Author: RogerDTZ
 * @FileName: Menu.java
 */

package stage.scene;

import component.animation.Animation;
import datatype.Vector2;
import graphics.Shape;
import object.ui.input.FullScreenInput;
import graphics.Sprite;
import main.App;
import network.Room;
import object.GameObject;
import graphics.Text;
import object.ui.button.MenuButton;
import stage.GameStage;
import stage.GameStageID;
import component.animation.Animator;
import main.AttentionManager;
import util.FontLibrary;

import java.awt.*;

public class Menu extends GameObject implements GameStage {

    public static final double MenuBackgroundCycle = 120;
    public static final double MenuBackgroundScale = 0.05;

    public static final double PopOutDuration = 1.0;
    public static final double PopBackDuration = 0.3;

    public static final double MenuButtonWidth = 500;
    public static final double MenuButtonHeight = 60;
    public static final Vector2 MenuButtonsPivot = new Vector2(App.Width - MenuButtonWidth * 0.43, App.Height / 2.0 + 300);
    public static final double MenuShift = MenuButtonWidth;

    public static final double ConnectBlackBackground = 0.8;

    public static final Vector2 RoomPosition = new Vector2(200, 110);
    public static final double RoomAnimationDuration = 0.3;
    public static final double RoomScaleSize = 1.3;

    public static final double ReadyScale = 1.2;
    public static final double ReadyAnimationDuration = 0.3;

    public static final Vector2 InputBoxSize = new Vector2(400, 48);


    private GameObject background;
    private Animator backgroundAnimator;

    private enum MenuState {
        Main, Create, Connect, Help, Room, Empty
    }
    private MenuState menuState;

    // menu_main
    private GameObject menu_main;
    private MenuButton button_main_start;
    private MenuButton button_main_help;
    private MenuButton button_main_exit;
    private Animator menu_main_animator;
    private Animator menu_main_alphaAnimator;

    // menu_create
    private GameObject menu_create;
    private MenuButton button_create_new;
    private MenuButton button_create_join;
    private MenuButton button_create_return;
    private Animator menu_create_animator;
    private Animator menu_create_alphaAnimator;
    private FullScreenInput hostInput;

    // menu_connect
    private GameObject menu_connect;
    private MenuButton button_connect_cancel;
    private Shape menu_connect_bg;
    private Animator menu_connect_bg_animator;
    private Animator menu_connect_animator;
    private Animator menu_connect_alphaAnimator;
    private Text menu_connect_text;
    private Animator menu_connect_text_scaleAnimator;
    private Animator menu_connect_text_verAnimator;

    // menu_room
    private GameObject menu_room;
    private MenuButton button_room_ready;
    private MenuButton button_room_return;
    private MenuButton button_room_addLocalPlayer;
    private Animator menu_room_animator;
    private Animator menu_room_alphaAnimator;
    private boolean ready;
    private Animator readyAnimator;
    private Animator banAlphaAnimator;
    private FullScreenInput playerNameInput;

    private Room room;
    private Animator room_animator;
    private Animator room_alphaAnimator;

    private boolean exitFlag;


    public Menu() {
        super("scene_menu");
    }

    public Menu(Room room) {
        super("scene_menu");
        this.initExistRoom(room);
    }

    @Override
    public void init() {

        this.background = new GameObject("menu_background", new Sprite("menu_background"));
        this.addObject(this.background);
        this.background.resizeTo(App.Width, App.Height);
        this.background.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.backgroundAnimator = new Animator(0);
        this.addComponent(this.backgroundAnimator);
        this.backgroundAnimator.append(Animation.GetLinear(0, 360, MenuBackgroundCycle));
        this.backgroundAnimator.setLoop(true);

        initMainMenu();
        initCreateMenu();
        initConnectMenu();
        initRoomMenu();

        this.menu_main_setActive(false);
        this.menu_create_setActive(false);
        this.menu_connect_setActive(false);
        this.menu_room_setActive(false);
        if (this.room != null) {
            this.menuState = MenuState.Room;
            this.menu_room_setActive(true);
            this.menu_room_popOut(0);
        } else {
            this.menuState = MenuState.Main;
            this.menu_main_setActive(true);
            this.menu_main_popOut(0);
        }

        this.exitFlag = false;
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.background.setScale(1 + MenuBackgroundScale + MenuBackgroundScale* Math.sin(this.backgroundAnimator.val() / 180 * Math.PI));

        this.menu_main_update(dt);
        this.menu_create_update(dt);
        this.menu_connect_update(dt);
        this.menu_room_update(dt);
        this.room_update(dt);

        if (this.menuState == MenuState.Main) { // Main
            if (this.button_main_start.isClicked()) {
                this.changeMenuState(MenuState.Create);
            }
            if (this.button_main_exit.isClicked()) {
                this.exitFlag = true;
            }
        } else if (this.menuState == MenuState.Create) { // Create
            if (this.button_create_new.isClicked()) {
                if (this.room == null || this.room.isDead()) {
                    this.createRoom("localhost", 23333);
                }
            }
            if (this.button_create_join.isClicked()) {
                if (this.room == null || this.room.isDead()) {
                    this.hostInput.setVisible(true);
                    this.hostInput.onClicked(1);
                    this.hostInput.toggleShow(true);
                }
            }
            if (this.button_create_return.isClicked()) {
                this.changeMenuState(MenuState.Main);
            }

            String hostAddress = this.hostInput.getResult();
            if (hostAddress != null) {
                if (hostAddress.equals(""))
                    hostAddress = "0.0.0.0";
                this.createRoom(hostAddress, 23333);
                this.hostInput.clear();
                this.menu_connect_text.setColor(new Color(212, 212, 212));
                this.menu_connect_text.setText("Connecting to " + hostAddress);
                this.changeMenuState(MenuState.Connect);
                this.menu_connect_lockBump(false);
            }
            this.menu_create_setActive(!this.hostInput.isFocus());
        } else if (this.menuState == MenuState.Connect) { // Connect
            if (this.button_connect_cancel.isClicked()) {
                if (this.room != null && !this.room.getShutdownHandled()) {
                    this.room.setShutdownHandled(true);
                    this.room.leave();
                }
                this.changeMenuState(MenuState.Create);
            }
        } else if (this.menuState == MenuState.Room) { // Room
            if (this.button_room_ready.isClicked()) {
                this.roomToggleReady();
            }
            if (this.button_room_addLocalPlayer.isClicked()) {
                this.playerNameInput.setVisible(true);
                this.playerNameInput.onClicked(1);
                this.playerNameInput.toggleShow(true);
            }

            if (this.button_room_return.isClicked()) {
                if (!this.room.getShutdownHandled()) {
                    this.room.setShutdownHandled(true);
                    this.room.leave();
                    this.room_toggleDisplay(false);
                    this.changeMenuState(MenuState.Create);
                }
            }

            String newPlayerName = this.playerNameInput.getResult();
            if (newPlayerName != null) {
                this.room.client_addPlayer(newPlayerName);
                this.playerNameInput.clear();
            }
            if (this.room.readyForGame())
                this.menu_room_setActive(false);
            else
                this.menu_room_setActive(!this.playerNameInput.isFocus());
        }
    }

    private void initMainMenu() {
        this.menu_main = new GameObject("menu_main");
        this.addObject(this.menu_main);
        this.menu_main.setPosition(MenuButtonsPivot);
        this.menu_main.setAlpha(0);

        this.button_main_start = new MenuButton("menu_main_start", new Sprite("menu_button_red"));
        this.button_main_help = new MenuButton("menu_main_help", new Sprite("menu_button_orange"));
        this.button_main_exit = new MenuButton("menu_main_exit", new Sprite("menu_button_blue"));
        this.menu_main.addObject(this.button_main_start);
        this.menu_main.addObject(this.button_main_help);
        this.menu_main.addObject(this.button_main_exit);

        this.button_main_start.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_main_help.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_main_exit.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_main_start.setPosition(0, - 20 - MenuButtonHeight);
        this.button_main_help.setPosition(0, 0);
        this.button_main_exit.setPosition(0, + 20 + MenuButtonHeight);

        Font font = FontLibrary.GetMenuButtonFont(30);
        this.button_main_start.setFont(font);
        this.button_main_help.setFont(font);
        this.button_main_exit.setFont(font);
        this.button_main_start.setText("New Game");
        this.button_main_help.setText("Help");
        this.button_main_exit.setText("Exit");
        this.button_main_start.setTextColor(new Color(212, 212, 212));
        this.button_main_help.setTextColor(new Color(212, 212, 212));
        this.button_main_exit.setTextColor(new Color(212, 212, 212));


        this.menu_main_animator = new Animator(0);
        this.menu_main_alphaAnimator = new Animator(0);
        this.addComponent(this.menu_main_animator);
        this.addComponent(this.menu_main_alphaAnimator);
    }

    private void menu_main_popOut(double delay) {
        this.menu_main_animator.forceAppend(Animation.GetTanh(0, 0, PopOutDuration, true, delay));
        this.menu_main_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_main_alphaAnimator.val(), 1, PopOutDuration, true, delay));
    }

    private void menu_main_popBack(double delay) {
        this.menu_main_animator.forceAppend(Animation.GetTanh(this.menu_main_animator.val(), MenuShift, PopBackDuration, false, delay));
        this.menu_main_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_main_alphaAnimator.val(), 0, PopBackDuration, false, delay));
    }

    private void menu_main_update(double dt) {
        this.menu_main.setPosition(MenuButtonsPivot.x + this.menu_main_animator.val(), MenuButtonsPivot.y);
        this.menu_main.setAlpha(this.menu_main_alphaAnimator.val());
    }

    private void menu_main_setActive(boolean flag) {
        this.button_main_start.setActive(flag);
        this.button_main_help.setActive(flag);
        this.button_main_exit.setActive(flag);
    }

    private void initCreateMenu() {
        this.menu_create = new GameObject("menu_create");
        this.addObject(this.menu_create);
        this.menu_create.setPosition(MenuButtonsPivot);
        this.menu_create.setAlpha(0);

        this.button_create_new = new MenuButton("menu_create_new", new Sprite("menu_button_red"));
        this.button_create_join = new MenuButton("menu_create_join", new Sprite("menu_button_orange"));
        this.button_create_return = new MenuButton("menu_create_return", new Sprite("menu_button_blue"));
        this.menu_create.addObject(this.button_create_new);
        this.menu_create.addObject(this.button_create_join);
        this.menu_create.addObject(this.button_create_return);

        this.button_create_new.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_create_join.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_create_return.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_create_new.setPosition(0, - 20 - MenuButtonHeight);
        this.button_create_join.setPosition(0, 0);
        this.button_create_return.setPosition(0, + 20 + MenuButtonHeight);

        Font font = FontLibrary.GetMenuButtonFont(30);
        this.button_create_new.setFont(font);
        this.button_create_join.setFont(font);
        this.button_create_return.setFont(font);
        this.button_create_new.setText("Create Room");
        this.button_create_join.setText("Join Room");
        this.button_create_return.setText("Back");
        this.button_create_new.setTextColor(new Color(212, 212, 212));
        this.button_create_join.setTextColor(new Color(212, 212, 212));
        this.button_create_return.setTextColor(new Color(212, 212, 212));

        this.menu_create_animator = new Animator(30 + MenuButtonWidth / 2.0);
        this.menu_create_alphaAnimator = new Animator(0);
        this.addComponent(this.menu_create_animator);
        this.addComponent(this.menu_create_alphaAnimator);

        this.hostInput = new FullScreenInput(
                "menu_create_hostInput",
                new Sprite("input_box"),
                InputBoxSize,
                0, 15,
                FontLibrary.GetInputBoxFont(20),
                FontLibrary.GetInputBoxDefaultFont(20),
                "Input host address ...",
                "0123456789.");
        this.addObject(this.hostInput);
        this.hostInput.setTextColor(new Color(30, 30, 30));
        this.hostInput.setDefaultTextColor(new Color(150, 150, 150));
        this.hostInput.setDarkWhenFocus(true);
        this.hostInput.setAutoToggleShow(true);
        this.hostInput.toggleShow(false);
        this.hostInput.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.hostInput.setActive(false);
        this.hostInput.setRenderPriority(10);
    }

    private void menu_create_popOut(double delay) {
        this.menu_create_animator.forceAppend(Animation.GetTanh(0, 0, PopOutDuration, true));
        this.menu_create_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_create_alphaAnimator.val(), 1, PopOutDuration, true, delay));
    }

    private void menu_create_popBack(double delay) {
        this.menu_create_animator.forceAppend(Animation.GetTanh(this.menu_create_animator.val(), MenuShift, PopBackDuration, false));
        this.menu_create_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_create_alphaAnimator.val(), 0, PopBackDuration, false, delay));
    }

    private void menu_create_update(double dt) {
        this.menu_create.setPosition(MenuButtonsPivot.x + this.menu_create_animator.val(), MenuButtonsPivot.y);
        this.menu_create.setAlpha(this.menu_create_alphaAnimator.val());
    }

    private void menu_create_setActive(boolean flag) {
        this.button_create_new.setActive(flag);
        this.button_create_join.setActive(flag);
        this.button_create_return.setActive(flag);
    }

    private void initConnectMenu() {
        this.menu_connect = new GameObject("menu_connect");
        this.addObject(this.menu_connect);
        this.menu_connect.setPosition(MenuButtonsPivot);
        this.menu_connect.setAlpha(0);

        Font font = FontLibrary.GetMenuButtonFont(30);
        this.button_connect_cancel = new MenuButton("menu_connect_cancel", new Sprite("menu_button_red"));
        this.menu_connect.addObject(this.button_connect_cancel);
        this.button_connect_cancel.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_connect_cancel.setPosition(0, + 20 + MenuButtonHeight);
        this.button_connect_cancel.setTextColor(new Color(212, 212, 212));
        this.button_connect_cancel.setFont(font);
        this.button_connect_cancel.setText("Cancel");

        this.menu_connect_animator = new Animator(30 + MenuButtonWidth / 2.0);
        this.menu_connect_alphaAnimator = new Animator(0);
        this.addComponent(this.menu_connect_animator);
        this.addComponent(this.menu_connect_alphaAnimator);

        this.menu_connect_bg = new Shape("menu_connect_bg", Color.black, Shape.Type.Rect, App.WinSize);
        this.menu_connect.addObject(this.menu_connect_bg);
        this.menu_connect_bg.setAlpha(0);
        this.menu_connect_bg.setRenderPriority(-1.0);
        this.menu_connect_bg.setAbsoluteTransform(true);
        this.menu_connect_bg.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.menu_connect_bg_animator = new Animator(0);
        this.addComponent(this.menu_connect_bg_animator);

        this.menu_connect_text = new Text("menu_connect_text", "", FontLibrary.GetMenuConnectTextFont(40));
        this.menu_connect.addObject(this.menu_connect_text);
        this.menu_connect_text.setColor(new Color(212, 212, 212));
        this.menu_connect_text.setAbsoluteTransform(true);
        this.menu_connect_text.setPosition(App.Width / 2.0, App.Height / 2.0);

        this.menu_connect_text_scaleAnimator = new Animator(1.0);
        this.menu_connect_text_verAnimator = new Animator(0);
        this.addComponent(this.menu_connect_text_scaleAnimator);
        this.addComponent(this.menu_connect_text_verAnimator);
        this.menu_connect_text_scaleAnimator.setLoop(true);
        this.menu_connect_text_scaleAnimator.append(Animation.GetBump(1.0, 1.05, 1.0));
        this.menu_connect_text_verAnimator.setLoop(true);
        this.menu_connect_text_verAnimator.append(Animation.GetBump(0, -20, 1.0));
    }

    private void menu_connect_popOut(double delay) {
        this.menu_connect_animator.forceAppend(Animation.GetTanh(0, 0, PopOutDuration, true));
        this.menu_connect_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_connect_alphaAnimator.val(), 1, PopOutDuration, true, delay));
        this.menu_connect_bg_animator.forceAppend(Animation.GetTanh(this.menu_connect_bg_animator.val(), ConnectBlackBackground, PopOutDuration, true, delay));
    }

    private void menu_connect_popBack(double delay) {
        this.menu_connect_animator.forceAppend(Animation.GetTanh(this.menu_connect_animator.val(), MenuShift, PopBackDuration, false));
        this.menu_connect_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_connect_alphaAnimator.val(), 0, PopBackDuration, false, delay));
        this.menu_connect_bg_animator.forceAppend(Animation.GetTanh(this.menu_connect_bg_animator.val(), 0, PopBackDuration, false, delay));
    }

    private void menu_connect_update(double dt) {
        this.menu_connect_bg.setAlpha(this.menu_connect_bg_animator.val());
        this.menu_connect.setPosition(MenuButtonsPivot.x + this.menu_connect_animator.val(), MenuButtonsPivot.y);
        this.menu_connect.setAlpha(this.menu_connect_alphaAnimator.val());
        this.menu_connect_text.setScale(this.menu_connect_text_scaleAnimator.val());
        this.menu_connect_text.setPosition(App.Width / 2.0, App.Height / 2.0 + this.menu_connect_text_verAnimator.val());
    }

    private void menu_connect_lockBump(boolean flag) {
        if (flag) {
            this.menu_connect_text_scaleAnimator.setActive(false);
            this.menu_connect_text_verAnimator.setActive(false);
            this.menu_connect_text_scaleAnimator.setValue(1.0);
            this.menu_connect_text_verAnimator.setValue(0);
        } else {
            this.menu_connect_text_scaleAnimator.setActive(true);
            this.menu_connect_text_verAnimator.setActive(true);
        }
    }

    private void menu_connect_setActive(boolean flag) {
        this.button_connect_cancel.setActive(flag);
    }

    void initRoomMenu() {
        this.menu_room = new GameObject("menu_room");
        this.addObject(this.menu_room);
        this.menu_room.setPosition(MenuButtonsPivot);
        this.menu_room.setAlpha(0);

        this.button_room_ready = new MenuButton("menu_room_ready", new Sprite("menu_button_green_bright"));
        this.button_room_addLocalPlayer = new MenuButton("menu_room_addLocalPlayer", new Sprite("menu_button_orange"));
        this.button_room_return = new MenuButton("menu_room_return", new Sprite("menu_button_blue"));
        this.menu_room.addObject(this.button_room_ready);
        this.menu_room.addObject(this.button_room_return);
        this.menu_room.addObject(this.button_room_addLocalPlayer);

        this.button_room_ready.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_room_return.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_room_addLocalPlayer.resizeTo(MenuButtonWidth, MenuButtonHeight);
        this.button_room_ready.setPosition(0, - 20 - MenuButtonHeight);
        this.button_room_addLocalPlayer.setPosition(0,0);
        this.button_room_return.setPosition(0, + 20 + MenuButtonHeight);

        Font font = FontLibrary.GetMenuButtonFont(30);
        this.button_room_ready.setFont(font);
        this.button_room_addLocalPlayer.setFont(font);
        this.button_room_return.setFont(font);
        this.button_room_ready.setText("Ready");
        this.button_room_addLocalPlayer.setText("Add Local Player");
        this.button_room_return.setText("Back");
        this.button_room_ready.setTextColor(new Color(212, 212, 212));
        this.button_room_addLocalPlayer.setTextColor(new Color(212, 212, 212));
        this.button_room_return.setTextColor(new Color(212, 212, 212));

        this.menu_room_animator = new Animator(30 + MenuButtonWidth / 2.0);
        this.menu_room_alphaAnimator = new Animator(0);
        this.addComponent(this.menu_room_animator);
        this.addComponent(this.menu_room_alphaAnimator);

        this.ready = false;
        this.readyAnimator = new Animator(1);
        this.banAlphaAnimator = new Animator(1);
        this.addComponent(this.readyAnimator);
        this.addComponent(this.banAlphaAnimator);

        this.playerNameInput = new FullScreenInput(
                "menu_room_playerNameInput",
                new Sprite("input_box"),
                InputBoxSize,
                2, 12,
                FontLibrary.GetInputBoxFont(20),
                FontLibrary.GetInputBoxDefaultFont(20),
                "Input your name...");
        this.addObject(this.playerNameInput);
        this.playerNameInput.setTextColor(new Color(30, 30, 30));
        this.playerNameInput.setDefaultTextColor(new Color(150, 150, 150));
        this.playerNameInput.setDarkWhenFocus(true);
        this.playerNameInput.setAutoToggleShow(true);
        this.playerNameInput.toggleShow(false);
        this.playerNameInput.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.playerNameInput.setActive(false);
        this.playerNameInput.setRenderPriority(10);
    }

    private void menu_room_popOut(double delay) {
        this.menu_room_animator.forceAppend(Animation.GetTanh(0, 0, PopOutDuration, true));
        this.menu_room_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_room_alphaAnimator.val(), 1, PopOutDuration, true, delay));
    }

    private void menu_room_popBack(double delay) {
        this.menu_room_animator.forceAppend(Animation.GetTanh(this.menu_room_animator.val(), MenuShift, PopBackDuration, false));
        this.menu_room_alphaAnimator.forceAppend(Animation.GetTanh(this.menu_room_alphaAnimator.val(), 0, PopBackDuration, false, delay));
    }

    private void menu_room_update(double dt) {
        this.menu_room.setPosition(MenuButtonsPivot.x + this.menu_room_animator.val(), MenuButtonsPivot.y);
        this.menu_room.setAlpha(this.menu_room_alphaAnimator.val());

        this.button_room_ready.setScale(this.readyAnimator.val());

        this.button_room_addLocalPlayer.setAlpha(this.banAlphaAnimator.val());
        this.button_room_return.setAlpha(this.banAlphaAnimator.val());
    }

    public void menu_room_setActive(boolean flag) {
        this.button_room_ready.setActive(flag);
        this.button_room_addLocalPlayer.setActive(flag && !this.ready);
        this.button_room_return.setActive(flag && !this.ready);
        if (!flag) {
            this.button_room_addLocalPlayer.toggleHoverZoom(false);
            this.button_room_return.toggleHoverZoom(false);
        }
    }

    private void roomToggleReady() {
        if (!this.ready) {
            if (this.room.getLocalPlayerNum() > 0) {
                this.ready = true;
                this.room.client_toggleReady(true);
                this.button_room_ready.getBase().setSprite(new Sprite("menu_button_green"));
                this.readyAnimator.forceAppend(Animation.GetTanh(this.readyAnimator.val(), ReadyScale, ReadyAnimationDuration, true));
                this.banAlphaAnimator.forceAppend(Animation.GetTanh(this.banAlphaAnimator.val(), 0, ReadyAnimationDuration, true));
            } else {
                AttentionManager.showWarnMessage("Please add at least 1 local player to get ready!");
            }
        } else {
            this.ready = false;
            this.room.client_toggleReady(false);
            this.button_room_ready.getBase().setSprite(new Sprite("menu_button_white"));
            this.button_room_ready.getBase().getSprite().setColor(new Color(255, 255, 255, 20));
            this.readyAnimator.forceAppend(Animation.GetTanh(this.readyAnimator.val(), 1, ReadyAnimationDuration, true));
            this.banAlphaAnimator.forceAppend(Animation.GetTanh(this.banAlphaAnimator.val(), 1, ReadyAnimationDuration, true));
        }
    }

    private void createRoom(String address, int port) {
        if (this.findObject("room") != null)
            this.removeObject("room");
        if (this.room_animator != null)
            this.room_animator.destroy();
        if (this.room_alphaAnimator != null)
            this.room_alphaAnimator.destroy();

        this.room = new Room(address, port);
        this.addObject(this.room);
        this.room.setPosition(RoomPosition);
        this.room.setAlpha(0);
        this.room.setAutoClean(true);
        this.room_animator = new Animator(RoomScaleSize);
        this.room_alphaAnimator = new Animator(0);
        this.addComponent(this.room_animator);
        this.addComponent(this.room_alphaAnimator);
    }

    private void initExistRoom(Room room) {
        this.room = room;
        this.room.detach();
        this.addObject(this.room);
        this.room.setRenderPriority(5);
        this.room.setPosition(RoomPosition);
        this.room.setAutoClean(true);
        this.room_animator = new Animator(RoomScaleSize);
        this.room_alphaAnimator = new Animator(0);
        this.addComponent(this.room_animator);
        this.addComponent(this.room_alphaAnimator);

        this.room_toggleDisplay(true);
        this.room.setMapInfoInputTouchable(true);
        this.room.setButtonsAvailable(true);

        if (this.room.isDead()) {
            if (!this.room.getShutdownHandled()) {
                this.room.setShutdownHandled(true);
                this.room_toggleDisplay(false);
            }
        }
    }

    private void room_update(double dt) {
        if (this.room != null) {
            if (this.room.isServer() && !this.room.readyForGame())
                this.room.server_setAcceptJoin(true);

            if (this.room.isServer() && this.room.serverReadyEvent()) {
                int state = this.room.getServerInitState();
                if (state == 1) {
                    room_toggleDisplay(true);
                    AttentionManager.showGoodMessage("Successfully create a room");
                    this.changeMenuState(MenuState.Room);
                } else {
                    this.room.leave();
                    AttentionManager.showErrorMessage("Fail to create room (multiple rooms in this computer)");
                    return;
                }
            }

            if (!this.room.isServer() && this.room.clientReadyEvent()) {
                int state = this.room.getClientInitState();
                if (state == 1) {
                    room_toggleDisplay(true);
                    this.room.client_getExistPlayers();
                    this.room.client_updateMapInfo();
                    this.menu_connect_text.setText("Success!");
                    AttentionManager.showGoodMessage("Successfully connect to the host!");
                    this.changeMenuState(MenuState.Room);
                } else {
                    this.menu_connect_text.setText(this.room.getJoinFailInfo());
                    this.menu_connect_lockBump(true);
                    this.room.leave();
                    AttentionManager.showErrorMessage("Join fail");
                }
            }

            if (this.room.isDead()) {
                if (!this.room.getShutdownHandled()) {
                    this.room.setShutdownHandled(true);
                    this.room_toggleDisplay(false);
                    if (this.room.getClientInitState() == 1)
                        this.changeMenuState(MenuState.Create);
                }
            }

            this.room.setScale(this.room_animator.val());
            this.room.setAlpha(this.room_alphaAnimator.val());
        }
    }

    private void room_toggleDisplay(boolean flag) {
        if (flag) {
            this.room_animator.forceAppend(Animation.GetTanh(this.room_animator.val(), 1, RoomAnimationDuration, true));
            this.room_alphaAnimator.forceAppend(Animation.GetTanh(this.room_alphaAnimator.val(), 1, RoomAnimationDuration, true));
        } else {
            this.room_animator.forceAppend(Animation.GetTanh(this.room_animator.val(), RoomScaleSize, RoomAnimationDuration, false));
            this.room_alphaAnimator.forceAppend(Animation.GetTanh(this.room_alphaAnimator.val(), 0, RoomAnimationDuration, false));
        }
    }

    private void changeMenuState(MenuState nextState) {
        switch (this.menuState) {
            case Main:
                this.menu_main_popBack(0);
                this.menu_main_setActive(false);
                break;
            case Create:
                this.menu_create_popBack(0);
                this.menu_create_setActive(false);
                break;
            case Connect:
                this.menu_connect_popBack(0);
                this.menu_connect_setActive(false);
                break;
            case Room:
                if (this.ready)
                    this.roomToggleReady();
                this.menu_room_popBack(0);
                this.menu_room_setActive(false);
                break;
            case Help:
                break;
            case Empty:
                break;
        }
        switch(nextState) {
            case Main:
                this.menu_main_popOut(PopBackDuration);
                this.menu_main_setActive(true);
                break;
            case Create:
                this.menu_create_popOut(PopBackDuration);
                this.menu_create_setActive(true);
                break;
            case Connect:
                this.menu_connect_popOut(PopBackDuration);
                this.menu_connect_setActive(true);
                break;
            case Room:
                this.menu_room_popOut(PopBackDuration);
                this.menu_room_setActive(true);
                break;
            case Help:
                break;
            case Empty:
                break;
        }
        this.menuState = nextState;
    }

    public boolean isExiting() {
        return exitFlag;
    }

    public Room getRoom() {
        return this.room;
    }

    @Override
    public GameStageID getGameStageID() {
        return GameStageID.Menu;
    }

}

