/**
 * @Author: RogerDTZ
 * @FileName: Game.java
 */

package stage.scene;

import datatype.*;
import graphics.Camera;
import graphics.Shape;
import graphics.Sprite;
import input.Controller;
import main.*;
import object.Player;
import object.ui.box.PlayerBoxManager;
import network.Room;
import net.sf.json.JSONObject;
import object.GameObject;
import object.grid.Mine;
import object.grid.MineManager;
import stage.GameStage;
import stage.GameStageID;
import object.ui.*;
import object.ui.button.IconButton;
import object.ui.input.SingleChooser;
import util.Random;
import component.Timer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Game extends GameObject implements GameStage {

    public static final Vector2 GameFieldSize = new Vector2(1100, 800);
    public static final Vector2 RoundTimerPosition = new Vector2(450, 100);
    public static final Vector2 MineCounterPosition = new Vector2(100, 100);
    public static final Vector2 ModeChooserPosition = new Vector2(275, 210);
    public static final Vector2 PlayerDisplayPosition = new Vector2(275, 280);
    public static final Vector2 ButtonSize = new Vector2(50, 50);

    public static final double RoundSwitchDuration = 2;

    private Shape canvas;
    private GameObject background;
    private String backgroundName;
    private Vector2 gridGraphicsSize;
    private Vector2 imgOffset;

    private Room room;
    private MapInfo mapInfo;

    private ArrayList<Integer> playerOrder;
    private ArrayList<Player> players;
    private Map<Integer, Player> idMap;
    int currentIndex;

    private MineManager manager;
    private PlayerBoxManager displayManager;
    private MineCounter mineCounter;
    private TimeClock roundTimer;
    private SingleChooser chooser;
    private FullScreenMessage yourTurn;
    private PauseMenu pauseMenu;
    private IconButton button_reset;
    private IconButton button_respawn;

    private Skill currSkill;
    private Skill lastSkill;

    private boolean fieldLock;
    private Timer fieldUnlockTimer;

    private boolean gameOver;
    private boolean gameOverState;

    private int ptr_x, ptr_y;


    public Game(Room room) {
        super("scene_game");

        this.room = room;

        this.canvas = new Shape("black_canvas", Color.black, Shape.Type.Rect, Vector2.scalar(App.WinSize, 2));
        this.addObject(this.canvas);
        this.canvas.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.canvas.setRenderPriority(-10);

        this.background = null;
        this.manager = null;
        this.displayManager = null;

        this.mineCounter = new MineCounter("mineCounter");
        this.addObject(this.mineCounter);
        this.mineCounter.setPosition(MineCounterPosition);
        this.mineCounter.setNumber(0, 0);

        this.roundTimer = new TimeClock("roundTimer", 0);
        this.addObject(this.roundTimer);
        this.roundTimer.setPosition(RoundTimerPosition);

        this.chooser = new SingleChooser(
                "modeChooser",
                new Vector2(250, 100),
                50, 35,
                Skill.values().length - 1,
                new ArrayList<String>() {{ add("GodPick"); add("Rude"); }},
                15,
                new ArrayList<String>() {{ add("yellow"); add("pink"); }}
        );
        this.addObject(this.chooser);
        this.chooser.setPosition(ModeChooserPosition);
        this.chooser.setAvailable(false, 0);

        this.yourTurn = new FullScreenMessage("yourTurn", new Sprite("your_turn"));
        this.addObject(this.yourTurn);
        this.yourTurn.setRenderPriority(9);

        this.pauseMenu = new PauseMenu("pauseMenu");
        this.addObject(this.pauseMenu);
        this.pauseMenu.setRenderPriority(10);

        this.fieldUnlockTimer = new Timer(0);
        this.addComponent(this.fieldUnlockTimer);

        this.currSkill = Skill.None;

        this.ptr_x = this.ptr_y = -1;

        this.gameOver = false;
        this.gameOverState = false;
    }

    public void init() {
        this.room.setButtonsAvailable(false);
        this.room.setMapInfoInputTouchable(false);
        if (this.room.isServer()) {
            this.room.server_setAcceptJoin(false);
            this.room.setAutoClean(false);
            this.room.server_announceEnterGame();

            this.mapInfo = this.room.getMapInfo();
            String background = "mine_background_" + Random.nextInt(ResourceManager.MineBackgroundNum);
            this.backgroundName = background;
            double len;
            if (1.0 * mapInfo.width / mapInfo.height >= GameFieldSize.x / GameFieldSize.y) {
                len = GameFieldSize.x / mapInfo.width;
            } else {
                len = GameFieldSize.y / mapInfo.height;
            }

            // [Server]
            Vector2 size = new Vector2(len * mapInfo.width, len * mapInfo.height);
            this.gridGraphicsSize = size;
            Sprite img = new Sprite(background, size);
            Vector2 imgSize = img.getUnitSize();
            Vector2 imgOffset = new Vector2(Random.nextDouble(0, imgSize.x - size.x), Random.nextDouble(0, imgSize.y - size.y));
            this.imgOffset = imgOffset;
            this.serverCallback_initMap(size, mapInfo.width, mapInfo.height, mapInfo.mineNum, mapInfo.stepPerRound, mapInfo.timePerRound, background, imgOffset);
            // [Client]
            JSONObject msg = new JSONObject();
            msg.put("event_type", "init_map");
            msg.put("size_x", size.x);
            msg.put("size_y", size.y);
            msg.put("grid_width", mapInfo.width);
            msg.put("grid_height", mapInfo.height);
            msg.put("mine_num", mapInfo.mineNum);
            msg.put("step_perRound", mapInfo.stepPerRound);
            msg.put("time_perRound", mapInfo.timePerRound);
            msg.put("background", background);
            msg.put("offset_x", imgOffset.x);
            msg.put("offset_y", imgOffset.y);
            room.getServer().sendAll(msg);
            // [end]

            // [Server]
            this.playerOrder = new ArrayList<>();
            for (Player player : this.room.getPlayers())
                this.playerOrder.add(player.getPlayerID());
            Collections.shuffle(this.playerOrder);
            JSONObject order = new JSONObject();
            order.put("num", this.playerOrder.size());
            for (int i = 0; i < this.playerOrder.size(); ++i)
                order.put("ele_" + i, this.playerOrder.get(i));
            this.serverCallback_initPlayers(order.toString());
            // [Client]
            msg = new JSONObject();
            msg.put("event_type", "init_players");
            msg.put("order", order.toString());
            room.getServer().sendAll(msg);
            // [end]

            if (this.mapInfo.map != null)
                this.server_initMines(this.mapInfo.map);

            this.currentIndex = -1;

            this.fieldLock = true;
            this.fieldUnlockTimer.init(2);
            this.server_updateFieldLock();

            if (this.players.size() == 1) {
                this.roundTimer.setVisible(false);
                this.roundTimer.setMute(true);
                this.mapInfo.timePerRound = Integer.MAX_VALUE;
            }

            this.button_reset = new IconButton("button_reset", ButtonSize, 0.1, new Sprite("reset"), Shape.Type.Rect);
            this.button_respawn = new IconButton("button_respawn", ButtonSize, 0.1, new Sprite("respawn"), Shape.Type.Rect);
            this.addObject(this.button_reset);
            this.addObject(this.button_respawn);
            this.button_reset.setPosition(10 + ButtonSize.x * 0.5, App.Height - 10 - ButtonSize.y * 0.5);
            this.button_respawn.setPosition(20 + ButtonSize.x * 1.5, App.Height - 10 - ButtonSize.y * 0.5);
            this.button_reset.setHoverZoom(1.1);
            this.button_respawn.setHoverZoom(1.1);
        }
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        this.room.update(dt);
        this.serverSync();
        this.clientSync();

        if (this.room.isServer()) {
            if (this.fieldUnlockTimer.done() && this.fieldLock) {
                this.fieldLock = false;
                this.server_updateFieldLock();
            }

            if (this.manager.hasInitMines()) {
                int rest = this.manager.server_getRemainingMineNum();
                this.mineCounter.setNumber(rest, Mine.FlagShowDuration);
                this.server_updateMineCounter();

                int gap = this.getWinGap();
                if ((rest == 0 || rest < gap) && !this.gameOverState) {
                    this.gameOver = true;
                    this.gameOverState = true;
                    this.yourTurn.setVisible(false);
                    this.server_updateGameOver();

                    this.fieldLock = true;
                    this.fieldUnlockTimer.init(99999);
                    this.fieldUnlockTimer.setActive(false);
                    this.server_updateFieldLock();

                    this.roundTimer.setActive(false);
                    this.server_setTimerActive(false);

                    AudioManager.Play("applause", 0.5);
                }
            }

            if (this.currentIndex < 0 || !this.getCurrentPlayer().isOnline() || this.roundTimer.isFinished())
                this.server_nextPlayer();
        }

        if (this.manager != null) {
            Player player = this.getCurrentPlayer();
            if (player != null) {
                this.manager.setIsCurrentLocal(player.isLocal());
                Operation op;
                while ((op = this.manager.client_getOperation()) != null) {
                    if (player.isLocal()) {
                        if (!this.fieldLock) {
                            op.playerID = player.getPlayerID();
                            this.client_sendOperation(op);
                        }
                    } else {
                        AttentionManager.showWarnMessage("It's NOT your turn now!");
                    }
                }
            }
            if (player.isLocal())
                this.setCutoff(player.getRemainCost());
            if (Controller.isKeyDown('c'))
                this.manager.setCheat(true);
            else
                this.manager.setCheat(false);
        }

        if (this.displayManager != null) {
            this.updateFirePlayer();
        }

        if (this.chooser != null && this.players != null) {
            Skill skill = Skill.Get(this.chooser.getChoice());
            if (this.getCurrentPlayer().isLocal()) {
                if (skill != this.currSkill) {
                    if (skill == Skill.None) {
                        this.displayManager.setCurrentCost(this.getCurrentPlayer().getOrder(), skill.cost);
                    } else if (skill == Skill.GodPick) {
                        this.displayManager.setCurrentCost(this.getCurrentPlayer().getOrder(), skill.cost);
                    } else if (skill == Skill.Rude) {
                        this.displayManager.setCurrentCost(this.getCurrentPlayer().getOrder(), skill.cost);
                    }
                }
                this.currSkill = skill;
                if (this.manager != null)
                    this.manager.setCurrSkill(this.currSkill);
            }
        }

        // PauseMenu [begin]

        this.chooser.setTouchable(!this.pauseMenu.getShowState());
        if (this.manager != null)
            this.manager.setTouchable(!this.pauseMenu.getShowState());

        if (this.pauseMenu.wantToSave()) {
            if (this.manager != null && this.manager.getInitState() == 2) {
                String mapData = this.manager.exportMap(true);
                SLManager.Append("maps", SLManager.GetDefaultFileName(), mapData);
                AttentionManager.showGoodMessage("The map has been add to the export queue");
            } else {
                AttentionManager.showErrorMessage("Sorry, the map is not generated");
            }
        }

        if (this.pauseMenu.wantToExit())
            this.room.leave();

        // PauseMenu [end]

        if (this.button_reset != null) {
            if (this.button_reset.clicked()) {
                this.server_coverMines();
                this.server_announce("The map has been reset by the host", "good");
            }
        }

        if (this.button_respawn != null) {
            if (this.button_respawn.clicked()) {
                this.server_resetMines();
                this.server_announce("The map has been regenerated by the host", "good");
            }
        }

        this.client_syncPtr();
    }

    @Override
    public AffineTransform render(Graphics2D g2d) {
        return super.render(g2d, Camera.getView(), 1);
    }

    private void updateFirePlayer() {
        Player fp = null;
        for (int i = 0; i < this.players.size(); ++i) {
            Player player = this.players.get(i);
            if (fp == null || (player.getScore() > fp.getScore() || (player.getScore() == fp.getScore() && player.getMistakes() < fp.getMistakes())))
                fp = player;
        }
        if (fp.getScore() == 0) {
            for (int i = 0; i < this.players.size(); ++i)
                this.displayManager.setOnFire(i, false);
        } else {
            for (int i = 0; i < this.players.size(); ++i) {
                Player player = this.players.get(i);
                this.displayManager.setOnFire(i, player.getScore() == fp.getScore() && player.getMistakes() == fp.getMistakes());
            }
        }
    }

    private Sprite getBackgroundTexture(Vector2 pos, Vector2 size) {
        Shape black = new Shape("game_black", Color.black, Shape.Type.Rect, App.WinSize);
        black.setScale(2);
        black.setPosition(App.Width / 2.0, App.Height / 2.0);

        GameObject bg = new GameObject("background", new Sprite("game_background"));
        bg.setPosition(App.Width / 2.0, App.Height / 2.0);
        bg.resizeTo(App.Width, App.Height);
        bg.setScale(1.3);
        bg.setAlpha(0.3);

        GameObject border = new GameObject("border", new Sprite("grid_border"));
        border.resizeTo(size.x + 140, size.y + 170);
        border.setPosition(pos);

        BufferedImage img = new BufferedImage((int)Math.round(App.Width * 1.3), (int)Math.round(App.Height * 1.3), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        black.move(new Vector2(App.Width * 0.15, App.Height * 0.15));
        bg.move(new Vector2(App.Width * 0.15, App.Height * 0.15));
        border.move(new Vector2(App.Width * 0.15, App.Height * 0.15));
        black.render(g2d);
        bg.render(g2d);
        border.render(g2d);

        g2d.dispose();

        return new Sprite(img);
    }

    private Player getCurrentPlayer() {
        if (this.players == null || this.currentIndex == -1)
            return null;
        return this.players.get(this.currentIndex);
    }

    private int getWinGap() {
        int max = -1, max2 = -1;
        if (this.players.size() == 1)
            return -1;
        for (int i = 0; i < this.players.size(); ++i)
            if (max == -1 || this.players.get(i).getScore() > this.players.get(max).getScore()) {
                max = i;
            }
        for (int i = 0; i < this.players.size(); ++i)
            if (i != max && (max2 == -1 || this.players.get(i).getScore() > this.players.get(max2).getScore())) {
                max2 = i;
            }
        return this.players.get(max).getScore() - this.players.get(max2).getScore();
    }

    private void serverSync() {
        if (!this.room.isServer())
            return;
        JSONObject msg;
        while ((msg = this.room.getMsgForServer()) != null) {
            switch(msg.getString("event_type")) {
                case "operate":
                    this.server_handleOperation(new Operation(JSONObject.fromObject(msg.getString("value"))));
                    break;
                case "set_player_online":
                    this.server_setPlayerOnline(msg.getString("session_id"), msg.getBoolean("flag"));
                    break;
                case "init_spectator":
                    this.server_initSpectator(msg.getString("session_id"));
                    break;
                case "update_cursor":
                    this.server_setCursor(msg.getString("session_id"), msg.getInt("x"), msg.getInt("y"), msg.getString("skill"));
                    break;
            }
        }
    }

    private void clientSync() {
        if (this.room.isServer())
            return;
        JSONObject msg;
        while ((msg = this.room.getMsgForClient()) != null) {
            switch(msg.getString("event_type")) {
                case "init_map":
                    this.serverCallback_initMap(
                            new Vector2(msg.getDouble("size_x"), msg.getDouble("size_y")),
                            msg.getInt("grid_width"),
                            msg.getInt("grid_height"),
                            msg.getInt("mine_num"),
                            msg.getInt("step_perRound"),
                            msg.getInt("time_perRound"),
                            msg.getString("background"),
                            new Vector2(msg.getDouble("offset_x"), msg.getDouble("offset_y"))
                    );
                    break;
                case "init_mines":
                    this.manager.serverCallback_initMines(msg.getString("value"));
                    break;
                case "reset_mines":
                    this.manager.serverCallback_resetMines();
                    break;
                case "cover_mines":
                    this.manager.serverCallback_coverMines();
                    break;
                case "init_players":
                    this.serverCallback_initPlayers(msg.getString("order"));
                    break;
                case "set_player_online":
                    this.serverCallback_setPlayerOnline(msg.getString("session_id"), msg.getBoolean("flag"));
                    break;
                case "set_currentIndexAndStep":
                    this.serverCallback_setCurrentIndexAndStep(msg.getInt("currentIndex"), msg.getInt("step"));
                    break;
                case "set_fieldLock":
                    this.serverCallback_setFieldLock(msg.getBoolean("flag"));
                    break;
                case "set_roundTimer":
                    this.serverCallback_setRoundTimer(msg.getDouble("value"), msg.getDouble("delay"));
                    break;
                case "set_roundTimer_active":
                    this.serverCallback_setRoundTimerActive(msg.getBoolean("flag"));
                    break;
                case "set_mineCounter":
                    this.serverCallback_setMineCounter(msg.getInt("value"));
                    break;
                case "set_gameOver":
                    this.serverCallback_setGameOver(msg.getBoolean("flag"));
                    break;
                case "operation_result":
                    this.serverCallback_applyOperationResult(new ScoreData(msg.getString("score_data")), new AnimationData(msg.getString("animation_data")));
                    break;
                case "announce":
                    this.serverCallback_announce(msg.getString("msg"), msg.getString("type"));
                    break;
                case "set_cursor":
                    this.serverCallback_setCursor(msg.getInt("x"), msg.getInt("y"), Skill.Get(msg.getString("skill")));
                    break;
            }
        }
    }

    private void server_announce(String message, String type) {
        // [Server]
        this.serverCallback_announce(message, type);
        // [Client]
        JSONObject msg = new JSONObject();
        msg.put("event_type", "announce");
        msg.put("msg", message);
        msg.put("type", type);
        this.room.getServer().sendAll(msg);
        // [end]
    }

    private void server_initSpectator(String session_id) {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "init_map");
        msg.put("size_x", this.gridGraphicsSize.x);
        msg.put("size_y", this.gridGraphicsSize.y);
        msg.put("grid_width", mapInfo.width);
        msg.put("grid_height", mapInfo.height);
        msg.put("mine_num", mapInfo.mineNum);
        msg.put("step_perRound", mapInfo.stepPerRound);
        msg.put("time_perRound", mapInfo.timePerRound);
        msg.put("background", backgroundName);
        msg.put("offset_x", imgOffset.x);
        msg.put("offset_y", imgOffset.y);
        room.getServer().send(session_id, msg);

        JSONObject order = new JSONObject();
        order.put("num", this.playerOrder.size());
        for (int i = 0; i < this.playerOrder.size(); ++i)
            order.put("ele_" + i, this.playerOrder.get(i));
        msg = new JSONObject();
        msg.put("event_type", "init_players");
        msg.put("order", order.toString());
        room.getServer().send(session_id, msg);

        this.server_updateCurrentIndexAndStep(session_id);
        this.server_updateMineCounter(session_id);
        this.server_setRoundTimer(session_id, Math.max(this.roundTimer.getTime() - 4, 0), 2);
        if (this.manager.getInitState() == 2)
            this.server_initMines(session_id, this.manager.exportMap(false));
    }

    private void server_updateCurrentIndexAndStep() {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_currentIndexAndStep");
        msg.put("currentIndex", this.currentIndex);
        msg.put("step", this.mapInfo.stepPerRound);
        this.room.getServer().sendAll(msg);
    }

    private void server_updateCurrentIndexAndStep(String session_id) {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_currentIndexAndStep");
        msg.put("currentIndex", this.currentIndex);
        msg.put("step", this.mapInfo.stepPerRound);
        this.room.getServer().send(session_id, msg);
    }

    private void server_updateFieldLock() {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_fieldLock");
        msg.put("flag", this.fieldLock);
        this.room.getServer().sendAll(msg);
    }

    private void server_setRoundTimer(double time, double delay) {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_roundTimer");
        msg.put("value", time);
        msg.put("delay", delay);
        this.room.getServer().sendAll(msg);
    }

    private void server_setRoundTimer(String session_id, double time, double delay) {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_roundTimer");
        msg.put("value", time);
        msg.put("delay", delay);
        this.room.getServer().send(session_id, msg);
    }

    private void server_setTimerActive(boolean flag) {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_roundTimer_active");
        msg.put("flag", flag);
        this.room.getServer().sendAll(msg);
    }

    private void server_updateMineCounter() {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_mineCounter");
        msg.put("value", this.mineCounter.getNumber());
        this.room.getServer().sendAll(msg);
    }

    private void server_updateMineCounter(String session_id) {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_mineCounter");
        msg.put("value", this.mineCounter.getNumber());
        this.room.getServer().send(session_id, msg);
    }

    private void server_updateGameOver() {
        if (!this.room.isServer())
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_gameOver");
        msg.put("flag", this.gameOver);
        this.room.getServer().sendAll(msg);
    }

    private void server_setPlayerOnline(String session_id, boolean flag) {
        if (!this.room.isServer())
            return;
        // [Server]
        this.serverCallback_setPlayerOnline(session_id, flag);
        // [Client]
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_player_online");
        msg.put("session_id", session_id);
        msg.put("flag", flag);
        this.room.getServer().sendAll(msg);
        // [end]
    }

    private void serverCallback_setPlayerOnline(String session_id, boolean flag) {
        for (Player player : this.players) {
            if (player.getSessionID().equals(session_id)) {
                player.setOnline(flag);
                this.displayManager.setOnline(player.getOrder(), flag);
            }
        }
    }

    private void serverCallback_initMap(Vector2 size, int gridWidth, int gridHeight, int mineNum, int stepPerRound, int timePerRound, String background, Vector2 imgOffset) {
        if (!this.room.isServer())
            this.mapInfo = new MapInfo(gridWidth, gridHeight, mineNum, stepPerRound, timePerRound);

        this.manager = new MineManager(size, gridWidth, gridHeight, new Sprite(background, size), imgOffset);
        this.addObject(this.manager);
        this.manager.setPosition(730 + size.x / 2, App.Height / 2.0 - 20);

        this.background = new GameObject("background", this.getBackgroundTexture(
                new Vector2(730 + size.x / 2, App.Height / 2.0 - 20),
                size));
        this.addObject(this.background);
        this.background.setPosition(App.Width / 2.0, App.Height / 2.0);
        this.background.setRenderPriority(-1);

        this.mineCounter.setNumber(mineNum, 0);
    }

    private void serverCallback_initPlayers(String orderString) {
        this.playerOrder = new ArrayList<>();
        JSONObject order = JSONObject.fromObject(orderString);
        int num = order.getInt("num");
        for (int i = 0; i < num; ++i)
            this.playerOrder.add(order.getInt("ele_" + i));

        this.idMap = new HashMap<>();
        this.players = new ArrayList<>();

        for (int i = 0; i < this.playerOrder.size(); ++i) {
            int playerID = this.playerOrder.get(i);
            Player player = null;
            for (Player p : this.room.getPlayers())
                if (p.getPlayerID() == playerID) {
                    player = p;
                    break;
                }
            if (player == null) {
                AttentionManager.showErrorMessage("[ERROR] cannot find player with id " + playerID);
                continue;
            }
            player.setOrder(i);
            this.players.add(player);
            this.idMap.put(player.getPlayerID(), player);
        }

        this.displayManager = new PlayerBoxManager(this.players, this.mapInfo.stepPerRound);
        this.addObject(this.displayManager);
        this.displayManager.setPosition(PlayerDisplayPosition);
        for (int i = 0; i < this.players.size(); ++i)
            this.displayManager.setCurrentCost(i, 1);
    }

    private void serverCallback_setCurrentIndexAndStep(int currentIndex, int step) {
        if (this.currentIndex >= 0)
            this.displayManager.setActive(this.currentIndex, false, 0);
        if (this.getCurrentPlayer() != null && this.getCurrentPlayer().isLocal())
            this.chooser.setAvailable(false, 0);
        this.currentIndex = currentIndex;
        if (this.getCurrentPlayer() != null)
            this.getCurrentPlayer().setCost(step);
        if (this.currentIndex >= 0)
            this.displayManager.setActive(this.currentIndex, true, RoundSwitchDuration);
        if (this.getCurrentPlayer() != null && this.getCurrentPlayer().isLocal()) {
            this.yourTurn.play();
            AudioManager.PlayWithVolume("your_turn", 0.3, 1);
            this.chooser.setAvailable(true, RoundSwitchDuration);
            this.displayManager.setBlink(this.getCurrentPlayer().getOrder(), true, 0);
        }
    }

    private void serverCallback_setFieldLock(boolean flag) {
        this.fieldLock = flag;
    }

    private void serverCallback_setRoundTimer(double time, double delay) {
        this.roundTimer.setTime(time, delay);
    }

    private void serverCallback_setRoundTimerActive(boolean flag) {
        this.roundTimer.setActive(flag);
    }

    private void serverCallback_setMineCounter(int x) {
        this.mineCounter.setNumber(x, Mine.FlagShowDuration);
    }

    private void serverCallback_setGameOver(boolean flag) {
        this.gameOverState = flag;
        this.gameOver = flag;
        if (flag) {
            this.yourTurn.setVisible(false);
            AudioManager.Play("applause", 0.5);
        }
    }

    private void serverCallback_announce(String msg, String type) {
        if (type.equals("good"))
            AttentionManager.showGoodMessage(msg);
        else if (type.equals("warn"))
            AttentionManager.showWarnMessage(msg);
        else
            AttentionManager.showErrorMessage(msg);
    }

    private void server_initMines(String mineData) {
        // [Server]
        this.manager.serverCallback_initMines(mineData);
        // [Client]
        JSONObject msg = new JSONObject();
        msg.put("event_type", "init_mines");
        msg.put("value", mineData);
        this.room.getServer().sendAll(msg);
        // [end]
    }

    private void server_resetMines() {
        // [Server]
        this.manager.serverCallback_resetMines();
        // [Client]
        JSONObject msg = new JSONObject();
        msg.put("event_type", "reset_mines");
        this.room.getServer().sendAll(msg);
        // [end]
    }

    private void server_coverMines() {
        // [Server]
        this.manager.serverCallback_coverMines();
        this.mineCounter.setNumber(this.mapInfo.mineNum, 0);
        this.server_updateMineCounter();
        // [Client]
        JSONObject msg = new JSONObject();
        msg.put("event_type", "cover_mines");
        this.room.getServer().sendAll(msg);
        // [end]
    }

    private void server_initMines(String session_id, String mineData) {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "init_mines");
        msg.put("value", mineData);
        this.room.getServer().send(session_id, msg);
    }

    private void server_setCursor(String session_id, int x, int y, String skill) {
        if (this.getCurrentPlayer() != null && this.getCurrentPlayer().getSessionID().equals(session_id)) {
            this.serverCallback_setCursor(x, y, Skill.Get(skill));
            JSONObject msg = new JSONObject();
            msg.put("event_type", "set_cursor");
            msg.put("x", x);
            msg.put("y", y);
            msg.put("skill", skill);
            this.room.getServer().sendAll(msg);
        }
    }

    private void server_handleOperation(Operation op) {
        if (!this.room.isServer())
            return;
        if (this.gameOverState)
            return;

        // first initialization
        if (this.manager.getInitState() == 1) {
            String mineData = this.manager.server_generate(this.mapInfo.mineNum, op.x, op.y);
            this.server_initMines(mineData);
        }

        Player player = this.getCurrentPlayer();
        if (player.getPlayerID() != op.playerID)
            return;

        ArrayList<OperationResult> results = this.manager.server_getOperationResult(player, op);

        double lockTime = 0;

        for (OperationResult result : results) {
            lockTime = Math.max(lockTime, result.scores.getAnimTime());

            // [Server]
            this.manager.client_playAnimation(result.animation);
            if (this.players.size() == 1)
                result.scores.cost = 0;
            player.applyScoreData(result.scores);
            this.displayManager.client_playAnimation(result.scores);
            if (this.getCurrentPlayer().isLocal())
                this.setCutoff(this.getCurrentPlayer().getRemainCost());
            // [Client]
            JSONObject msg = new JSONObject();
            msg.put("event_type", "operation_result");
            msg.put("score_data", result.scores.toJSONObject().toString());
            msg.put("animation_data", result.animation.toJSONObject().toString());
            this.room.getServer().sendAll(msg);
            // [end]
        }

        if (lockTime > 0) {
            this.fieldLock = true;
            this.fieldUnlockTimer.init(lockTime);
            this.server_updateFieldLock();
        }

        if (this.getCurrentPlayer() != null && this.getCurrentPlayer().getRemainCost() == 0 && this.players.size() > 1)
            this.server_nextPlayer();
    }

    private void server_nextPlayer() {
        if (this.currentIndex >= 0) {
            this.displayManager.setActive(this.currentIndex, false, 0);
            if (this.getCurrentPlayer().isLocal()) {
                this.displayManager.setBlink(this.currentIndex, false, 0);
                this.chooser.setAvailable(false, 0);
            }
        }

        while (true) {
            this.currentIndex = (this.currentIndex + 1) % this.getRoom().getPlayers().size();
            Player nextPlayer = this.getCurrentPlayer();
            if (nextPlayer.isOnline())
                break;
        }

        this.getCurrentPlayer().setCost(this.mapInfo.stepPerRound);
        if (this.getCurrentPlayer().isLocal()) {
            this.yourTurn.play();
            AudioManager.PlayWithVolume("your_turn", 0.3, 0.7);
            this.chooser.setAvailable(true, RoundSwitchDuration);
            this.setCutoff(this.mapInfo.stepPerRound);
            this.displayManager.setCurrentCost(this.getCurrentPlayer().getOrder(), 1);
            this.currSkill = Skill.None;
        }
        this.server_updateCurrentIndexAndStep();

        this.displayManager.setActive(this.currentIndex, true, RoundSwitchDuration);
        if (this.getCurrentPlayer().isLocal())
            this.displayManager.setBlink(this.currentIndex, true, RoundSwitchDuration);

        this.roundTimer.setTime(this.mapInfo.timePerRound, RoundSwitchDuration);
        this.server_setRoundTimer(this.mapInfo.timePerRound, RoundSwitchDuration);

        this.fieldLock = true;
        this.fieldUnlockTimer.init(RoundSwitchDuration);
        this.server_updateFieldLock();
    }

    private void client_sendOperation(Operation op) {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "operate");
        msg.put("value", op.toJSONObject().toString());
        this.room.getClient().send(msg);
    }

    private void serverCallback_applyOperationResult(ScoreData scoreData, AnimationData animationData) {
        if (this.players.size() == 1)
            scoreData.cost = 0;

        Player player = this.idMap.get(scoreData.playerID);
        player.applyScoreData(scoreData);

        this.manager.client_playAnimation(animationData);
        this.displayManager.client_playAnimation(scoreData);
        if (player.isLocal())
            this.setCutoff(player.getRemainCost());
    }

    private void serverCallback_setCursor(int x, int y, Skill skill) {
        this.manager.serverCallback_setRemoteCursor(x, y, skill);
    }

    private void setCutoff(int x) {
        for (int i = 1; i < Skill.values().length; ++i)
            if (x < Skill.Get(i).cost)
                this.chooser.ban(i - 1);
    }

    public Room getRoom() {
        return this.room;
    }

    public boolean isGameOver() {
        if (this.gameOver) {
            this.gameOver = false;
            return true;
        }
        return false;
    }

    private void client_syncPtr() {
        int nx = this.manager.getCursor().getKey(), ny = this.manager.getCursor().getValue();
        if (!(nx == this.ptr_x && ny == this.ptr_y && this.currSkill == this.lastSkill)) {
            this.ptr_x = nx;
            this.ptr_y = ny;
            this.lastSkill = this.currSkill;
            JSONObject msg = new JSONObject();
            msg.put("event_type", "update_cursor");
            msg.put("session_id", this.room.getClient().getSessionID());
            msg.put("x", this.ptr_x);
            msg.put("y", this.ptr_y);
            msg.put("skill", this.currSkill.toString());
            this.room.getClient().send(msg);
        }
    }

    @Override
    public GameStageID getGameStageID() {
        return GameStageID.Game;
    }

}
