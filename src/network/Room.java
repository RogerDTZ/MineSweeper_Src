/**
 * @Author: RogerDTZ
 * @FileName: Room.java
 */

package network;

import datatype.MapInfo;
import datatype.Vector2;
import main.App;
import object.Player;
import net.sf.json.JSONObject;
import object.GameObject;
import graphics.Text;
import object.ui.input.MapInfoInput;
import object.ui.box.RoomPlayerBox;
import main.AttentionManager;
import util.FontLibrary;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Room extends GameObject {

    public final static int RoomCapacity = 5;
    public final static Vector2 RoomPlayerDisplayPos = new Vector2(300, 125);
    public final static Vector2 RoomPlayerDisplaySize = new Vector2(400, 125);
    public final static double RoomPlayerDisplayMargin = 30;
    public final static double RoomPlayerAnimationDuration = 0.5;

    public static final Vector2 MapInfoPos = new Vector2(App.Width - 600, App.Height / 2.0 - 400);

    private boolean isServer;

    private int playerIDCnt;
    private GameObject players;
    private GameObject playerDisplays;
    private Text serverAddress;

    public final static int SleepWhenNoMsg = 20;
    private Thread serverSyncThread;
    private Thread clientSyncThread;
    private boolean syncEnded;
    private boolean shutdownHandled;

    private Server server;
    private Client client;
    private boolean serverReadyEvent;
    private boolean clientReadyEvent;

    Queue<JSONObject> serverMsgQueue;
    Queue<JSONObject> clientMsgQueue;

    private boolean autoClean;
    private boolean inGame;

    private MapInfoInput mapInfoInput;
    private MapInfo mapInfo;


    public Room(String address, int port) {
        super("room");

        this.serverAddress = new Text("server_address", "", FontLibrary.GetMenuButtonFont(30));
        this.serverAddress.setPosition(MapInfoPos.x + 100, MapInfoPos.y + 250);
        this.serverAddress.setColor(new Color(212, 212, 212));
        this.addObject(this.serverAddress);

        if (address.equals("localhost")) {
            this.isServer = true;
            this.server = new Server(port);
            this.server.setActive(true);
            initServerSyncThread();
            this.client = new Client("localhost", port);
            initClientSyncThread();
            this.serverAddress.setText("Server: " + this.server.getServerAddress());
        } else {
            this.isServer = false;
            this.client = new Client(address, port);
            initClientSyncThread();
        }

        this.mapInfoInput = new MapInfoInput("mapInfoInput", this.isServer);
        this.mapInfoInput.setPosition(MapInfoPos);
        this.mapInfo = this.mapInfoInput.getInfo();
        this.addObject(this.mapInfoInput);

        this.serverReadyEvent = false;
        this.clientReadyEvent = false;

        this.players = new GameObject("room_players");
        this.addObject(players);

        this.playerDisplays = new GameObject("room_playerDisplays");
        this.addObject(this.playerDisplays);

        this.serverMsgQueue = new LinkedList<>();
        this.clientMsgQueue = new LinkedList<>();

        this.inGame = false;

        // exit handler
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                leave();
            }
        });

    }

    public MapInfo getMapInfo() {
        return this.mapInfoInput.getInfo();
    }

    public void server_setAcceptJoin(boolean flag) {
        if (!this.isServer)
            return;
        this.server.setActive(flag);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if (this.isServer) {
            if (this.autoClean)
                this.server.cleanDeadSession();
            this.updateMapInfo();
        } else {
            if (this.client.getInitState() == 1) {
                this.serverAddress.setText("Server: " + this.client.getServerAddress());
            }
        }

        this.updateRoomPlayerDisplays(dt);
    }

    private void updateMapInfo() {
        MapInfo info = this.mapInfoInput.getInfo();
        if (!this.mapInfo.equals(info)) {
            this.mapInfo.set(info);
            this.server_updateMapInfo();
        }
    }

    private void server_updateMapInfo() {
        if (!isServer)
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "update_mapInfo");
        msg.put("info", this.mapInfo.toJSONObject());
        this.server.sendAll(msg);
    }

    private void server_updateMapInfo(String session_id) {
        if (!isServer)
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "update_mapInfo");
        msg.put("info", this.mapInfo.toJSONObject());
        this.server.send(session_id, msg);
    }

    public void client_updateMapInfo() {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "update_mapInfo");
        msg.put("session_id", this.client.getSessionID());
        this.client.send(msg);
    }

    private void serverCallback_setMapInfo(MapInfo info) {
        this.mapInfoInput.setInfo(info);
    }

    private void updateRoomPlayerDisplays(double dt) {
        while (true) {
            boolean has = false;
            for (GameObject obj : this.playerDisplays.getSubObjects()) {
                RoomPlayerBox rp = (RoomPlayerBox)obj;
                if (rp.isEnded()) {
                    has = true;
                    this.playerDisplays.removeObject(obj.getID());
                    break;
                }
            }
            if (!has)
                break;
        }
        for (GameObject obj : this.playerDisplays.getSubObjects()) {
            RoomPlayerBox rp = (RoomPlayerBox) obj;
            if (rp.applyToDelete())
                this.client_removePlayer(rp.getPlayer().getPlayerID());
        }
    }

    public String getJoinFailInfo() {
        if (this.client.getInitState() == 2)
            return this.client.getJoinFailInformation();
        return null;
    }

    public int getServerInitState() {
        return this.server.getInitState();
    }

    public boolean serverReadyEvent() {
        if (!this.serverReadyEvent && this.server != null && this.server.getInitState() != 0) {
            this.serverReadyEvent = true;
            return true;
        }
        return false;
    }

    public int getClientInitState() {
        return this.client.getInitState();
    }

    public boolean clientReadyEvent() {
        if (!this.clientReadyEvent && this.client != null && this.client.getInitState() != 0) {
            this.clientReadyEvent = true;
            return true;
        }
        return false;
    }

    private void initServerSyncThread() {
        this.serverSyncThread = new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject msg;
                while (!syncEnded) {
                    msg = server.getMsg();
                    if (msg != null) {
                        serverSync(msg);
                    } else {
                        try {
                            Thread.sleep(SleepWhenNoMsg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        this.serverSyncThread.start();
    }

    private void initClientSyncThread() {
        this.clientSyncThread = new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject msg;
                while (!syncEnded) {
                    msg = client.getMsg();
                    if (msg != null) {
                        clientSync(msg);
                    } else {
                        try {
                            Thread.sleep(SleepWhenNoMsg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        this.clientSyncThread.start();
    }

    private void serverSync(JSONObject msg) {
        switch(msg.getString("event_type")) {
            case "room_player_join_application":
                this.server_addPlayer(msg.getString("session_id"), msg.getString("playerName"));
                break;
            case "room_player_toggle_ready":
                this.server_toggleReady(msg.getString("session_id"), msg.getBoolean("flag"));
                break;
            case "room_getExistPlayers":
                this.server_getExistPlayers(msg.getString("session_id"));
                break;
            case "room_client_join":
                if (this.inGame) {
                    this.server_announceEnterGame(msg.getString("session_id"));
                    JSONObject json = new JSONObject();
                    json.put("event_type", "init_spectator");
                    json.put("session_id", msg.getString("session_id"));
                    this.serverMsgQueue.offer(json);
                }
                break;
            case "room_client_leave":
                if (!this.inGame)
                    this.server_removePlayersBySessionID(msg.getString("session_id"));
                else
                    this.server_setPlayerOnline(msg.getString("session_id"), false);
                break;
            case "room_remove_player":
                this.server_removePlayer(msg.getInt("playerID"));
                break;
            case "update_mapInfo":
                this.server_updateMapInfo(msg.getString("session_id"));
                break;
            default:
                this.serverMsgQueue.offer(msg);
        }
    }

    private void clientSync(JSONObject msg) {
        if (this.isServer)
            return;
        switch(msg.getString("event_type")) {
            case "update_mapInfo":
                this.serverCallback_setMapInfo(new MapInfo(msg.getJSONObject("info")));
                break;
            case "room_player_join_success":
                this.serverCallback_addPlayer(
                        msg.getInt("playerID"),
                        msg.getString("playerName"),
                        msg.getString("session_id"),
                        msg.getString("ip"),
                        msg.getBoolean("need_announcement")
                        );
                break;
            case "room_player_join_fail":
                AttentionManager.showWarnMessage("Sorry, the room is full!");
                break;
            case "room_remove_player":
                this.serverCallback_removePlayer(msg.getInt("playerID"));
                break;
            case "room_server_shutdown":
                this.client_onServerShutdown();
                break;
            case "room_reset_roomState":
                this.serverCallback_resetRoomState();
                break;
            case "room_player_toggle_ready":
                this.serverCallback_toggleReady(msg.getInt("playerID"), msg.getBoolean("flag"));
                break;
            case "enter_game":
                this.serverCallback_enterGame();
                break;
            default:
                this.clientMsgQueue.offer(msg);
        }
    }

    public JSONObject getMsgForServer() {
        if (!this.isServer)
            return null;
        return this.serverMsgQueue.poll();
    }

    public JSONObject getMsgForClient() {
        return this.clientMsgQueue.poll();
    }

    public void leave() {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_client_leave");
        msg.put("session_id", this.client.getSessionID());
        if (this.client != null) {
            this.client.send(msg);
            this.client.terminate();
        }
        if (this.isServer) {
            this.server_announce_shutdown();
            this.server.terminate();
        }
        this.syncEnded = true;
    }

    public void client_addPlayer(String playerName) {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_player_join_application");
        msg.put("session_id", this.client.getSessionID());
        msg.put("playerName", playerName);
        this.client.send(msg);
    }

    private void server_addPlayer(String session_id, String playerName) {
        if (!isServer)
            return;
        int playerNum = this.players.getSubObjectsNum();
        if (playerNum == RoomCapacity) {
            JSONObject msg = new JSONObject();
            msg.put("event_type", "room_player_join_fail");
            msg.put("message", "房间已满!");
            this.server.send(session_id, msg);
        } else {
            // [server]
            Player player = this.serverCallback_addPlayer(this.playerIDCnt++, playerName, session_id, this.client.getSessionID().equals(session_id) ? "Host" : "Client", true);
            // [client]
            JSONObject msg = new JSONObject();
            msg.put("event_type", "room_player_join_success");
            msg.put("playerID", player.getPlayerID());
            msg.put("playerName", player.getPlayerName());
            msg.put("session_id", session_id);
            msg.put("ip", this.client.getSessionID().equals(session_id) ? "Host" : "Client");
            msg.put("need_announcement", true);
            this.server.sendAll(msg);
            // [end]
        }
    }

    private Player serverCallback_addPlayer(int playerID, String playerName, String session_id, String ip, boolean needAnnouncement) {
        for (GameObject obj : this.players.getSubObjects())
            if(((Player)obj).getPlayerID() == playerID)
                return null;
        Player player = new Player(playerID, playerName, session_id, ip, this.client);
        this.players.addObject(player);
        this.addPlayerDisplay(player);
        if (!player.isLocal() && needAnnouncement)
            AttentionManager.showGoodMessage(playerName + " has joined the game");
        return player;
    }

    private void client_removePlayer(int playerID) {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_remove_player");
        msg.put("playerID", playerID);
        this.client.send(msg);
    }

    private void server_removePlayer(int playerID) {
        if (!this.isServer)
            return;
        this.serverCallback_removePlayer(playerID);
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_remove_player");
        msg.put("playerID", playerID);
        this.server.sendAll(msg);
    }

    public void client_toggleReady(boolean flag) {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_player_toggle_ready");
        msg.put("session_id", this.client.getSessionID());
        msg.put("flag", flag);
        this.client.send(msg);
    }

    private void server_toggleReady(String session_id, boolean flag) {
        if (!isServer)
            return;
        for (GameObject object : this.players.getSubObjects()) {
            Player player = (Player)object;
            if (player.getSessionID().equals(session_id)) {
                // [Server]
                this.serverCallback_toggleReady(player.getPlayerID(), flag);
                // [Client]
                JSONObject msg = new JSONObject();
                msg.put("event_type", "room_player_toggle_ready");
                msg.put("playerID", player.getPlayerID());
                msg.put("flag", flag);
                this.server.sendAll(msg);
                // [end]
            }
        }
    }

    private void serverCallback_toggleReady(int playerID, boolean flag) {
        for (GameObject object : this.players.getSubObjects()) {
            Player player = (Player) object;
            if (player.getPlayerID() == playerID) {
                player.setReady(flag);
                ((RoomPlayerBox)this.playerDisplays.findObject("roomPlayerDisplay_" + player.getPlayerID())).toggleReadyState(flag);
                break;
            }
        }
    }

    public void client_getExistPlayers() {
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_getExistPlayers");
        msg.put("session_id", this.client.getSessionID());
        this.client.send(msg);
    }

    private void server_getExistPlayers(String session_id) {
        if (!isServer)
            return;
        for (GameObject object : this.players.getSubObjects()) {
            Player player = (Player)object;
            JSONObject msg = new JSONObject();
            msg.put("event_type", "room_player_join_success");
            msg.put("playerID", player.getPlayerID());
            msg.put("playerName", player.getPlayerName());
            msg.put("session_id", player.getSessionID());
            msg.put("ip", player.getIp());
            msg.put("need_announcement", false);
            this.server.send(session_id, msg);
        }
    }

    public void server_announceEnterGame() {
        if (!this.isServer)
            return;
        // [Server]
        this.serverCallback_enterGame();
        // [Client]
        JSONObject msg = new JSONObject();
        msg.put("event_type", "enter_game");
        this.server.sendAll(msg);
        // [end]
    }

    public void server_announceEnterGame(String session_id) {
        if (!this.isServer)
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "enter_game");
        this.server.send(session_id, msg);
    }

    public void server_announceReset() {
        if (!this.isServer)
            return;
        // [Server]
        this.serverCallback_resetRoomState();
        for (GameObject object : this.players.getSubObjects()) {
            Player player = (Player) object;
            this.server_toggleReady(player.getSessionID(), player.isReady());
        }
        // [Client]
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_reset_roomState");
        this.server.sendAll(msg);
        // [end]
    }

    private void serverCallback_enterGame() {
        this.inGame = true;
        for (GameObject object : this.players.getSubObjects()) {
            Player player = (Player) object;
            player.reset();
        }
    }

    private void serverCallback_resetRoomState() {
        this.inGame = false;
        for (GameObject object : this.players.getSubObjects()) {
            Player player = (Player) object;
            ((RoomPlayerBox) this.playerDisplays.findObject("roomPlayerDisplay_" + player.getPlayerID())).toggleReadyState(player.isReady());
        }
    }

    private void addPlayerDisplay(Player player) {
        RoomPlayerBox rp = new RoomPlayerBox("roomPlayerDisplay_" + player.getPlayerID(), RoomPlayerDisplaySize, player, player.isLocal() || this.isServer);
        rp.setPlayerName(player.getPlayerName());
        rp.setPlayerAddress(player.isLocal() ? "Local Player" : player.getIp());
        rp.toggleReadyState(false);
        rp.setTargetAlpha(1.0, RoomPlayerAnimationDuration);

        int cnt = this.playerDisplays.getSubObjectsNum();
        double target = cnt * RoomPlayerDisplayPos.y + (cnt + 1) * RoomPlayerDisplayMargin;
        rp.setPosition( RoomPlayerDisplayPos.x / 2, 0);
        rp.setTargetLocation(target + RoomPlayerDisplayPos.y, target, RoomPlayerAnimationDuration);

        this.playerDisplays.addObject(rp);
    }

    private void removePlayerDisplay(Player player) {
        String playerDisplayID = "roomPlayerDisplay_" + player.getPlayerID();
        int cnt = 0;
        int k;
        RoomPlayerBox rp;
        for (k = 0; k < this.playerDisplays.getSubObjectsNum(); ++k) {
            if (this.playerDisplays.getSubObjects().get(k).getID().equals(playerDisplayID))
                break;
            rp = (RoomPlayerBox)this.playerDisplays.getSubObjects().get(k);
            if (!rp.isDestroyed())
                ++cnt;
        }
        rp = (RoomPlayerBox)this.playerDisplays.getSubObjects().get(k);
        rp.setTargetAlpha(0, RoomPlayerAnimationDuration * 0.3);
        rp.destroy();
        for (int i = k + 1; i < this.playerDisplays.getSubObjectsNum(); ++i) {
            rp = (RoomPlayerBox)this.playerDisplays.getSubObjects().get(i);
            if (!rp.isDestroyed()) {
                double target = cnt * RoomPlayerDisplayPos.y + (cnt + 1) * RoomPlayerDisplayMargin;
                rp.setTargetLocation(target, RoomPlayerAnimationDuration);
                ++cnt;
            }
        }
    }

    private void server_announce_shutdown() {
        if (!isServer)
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "room_server_shutdown");
        this.server.sendAll(msg);
    }

    private void client_onServerShutdown() {
        AttentionManager.showWarnMessage("The room has been dismissed by the host");
        this.leave();
    }

    private void server_removePlayersBySessionID(String session_id) {
        if (!this.isServer)
            return;
        while(true) {
            boolean has = false;
            for (GameObject player : this.players.getSubObjects()) {
                if (((Player)player).getSessionID().equals(session_id)) {
                    // [Server]
                    this.serverCallback_removePlayer(((Player) player).getPlayerID());
                    // [Client]
                    JSONObject msg = new JSONObject();
                    msg.put("event_type", "room_remove_player");
                    msg.put("playerID", ((Player)player).getPlayerID());
                    this.server.sendAll(msg);
                    // [end]
                    has = true;
                    break;
                }
            }
            if (!has)
                break;
        }
    }

    private void serverCallback_removePlayer(int playerID) {
        for (GameObject player : this.players.getSubObjects()) {
            if (((Player)player).getPlayerID() == playerID) {
                this.players.removeObject(player.getID());
                this.removePlayerDisplay((Player) player);
                if (!((Player)player).isLocal()) {
                    AttentionManager.showWarnMessage(((Player) player).getPlayerName() + " has leave the game");
                }
                return;
            }
        }
    }

    private void server_setPlayerOnline(String session_id, boolean flag) {
        if (!this.isServer)
            return;
        JSONObject msg = new JSONObject();
        msg.put("event_type", "set_player_online");
        msg.put("session_id", session_id);
        msg.put("flag", flag);
        this.serverMsgQueue.offer(msg);
    }

    public boolean readyForGame() {
        if (this.isServer) {
            if (this.inGame)
                return true;
            if (this.players.getSubObjectsNum() > 0) {
                for (GameObject object : this.players.getSubObjects()) {
                    if (!((Player) object).isReady())
                        return false;
                }
                return true;
            }
            return false;
        } else {
            return this.inGame;
        }
    }

    public boolean isDead() {
        return (this.isServer && this.getServerInitState() == 2) || this.getClientInitState() == 2 || this.syncEnded;
    }

    public void setShutdownHandled(boolean flag) {
        this.shutdownHandled = flag;
    }

    public boolean isServer() {
        return this.isServer;
    }

    public Server getServer() {
        return this.server;
    }

    public Client getClient() {
        return this.client;
    }

    public boolean getShutdownHandled() {
        return this.shutdownHandled;
    }

    public void setAutoClean(boolean flag) {
        if (this.isServer)
            this.autoClean = flag;
    }

    public ArrayList<Player> getPlayers() {
        ArrayList<Player> res = new ArrayList<>();
        for (GameObject obj : this.players.getSubObjects())
            res.add((Player)obj);
        return res;
    }

    public int getLocalPlayerNum() {
        int res = 0;
        for (Player player : this.getPlayers()) {
            if (player.isLocal())
                ++res;
        }
        return res;
    }

    public void setButtonsAvailable(boolean flag) {
        for (GameObject obj : this.playerDisplays.getSubObjects()) {
            RoomPlayerBox rp = (RoomPlayerBox)obj;
            rp.toggleActive(flag);
        }
    }

    public void setMapInfoInputTouchable(boolean flag) {
        this.mapInfoInput.setTouchable(flag);
    }

}
