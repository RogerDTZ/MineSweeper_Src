/**
 * @Author: RogerDTZ
 * @FileName: Server.java
 */

package network;

import net.sf.json.JSONObject;
import util.Random;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Server extends Thread {

    public static final int SessionIDLength = 16;

    private boolean ready;
    private boolean createSuccess;
    private boolean active;

    private final int port;
    private ServerSocket serverSocket;
    private ArrayList<ServerThread> serverThreads;
    private Map<String, ServerThread> sessionIDMap;

    private Queue<JSONObject> queue;

    private boolean exit;


    public Server(int port) {
        this.ready = false;
        this.createSuccess = false;
        this.active = false;
        this.exit = false;
        this.port = port;
        this.queue = new LinkedList<JSONObject>();
        this.serverThreads = new ArrayList<>();
        this.sessionIDMap = new HashMap<>();
        try {
            this.serverSocket = new ServerSocket(this.port);
            this.start();
            this.ready = true;
            this.createSuccess = true;
        } catch (IOException e) {
            this.ready = true;
            this.createSuccess = false;
        }
    }

    public String getServerAddress() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setActive(boolean flag) {
        this.active = flag;
    }

    public void clear(){
        for (ServerThread serverThread : this.serverThreads)
            serverThread.terminate();
        this.serverThreads.clear();
        this.sessionIDMap.clear();
    }

    public void terminate() {
        this.clear();
        this.exit = true;
        try {
            if (this.serverSocket != null)
                this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanDeadSession() {
        while (true) {
            boolean has = false;
            for (ServerThread serverThread : this.serverThreads) {
                if (serverThread.isDead()) {
                    this.sessionIDMap.remove(serverThread.getSessionID());
                    this.serverThreads.remove(serverThread);
                    has = true;
                    break;
                }
            }
            if (!has)
                break;
        }
    }

    public int getClientsCount() {
        return this.serverThreads.size();
    }

    @Override
    public void run() {
        System.out.println("Server listener started, waiting for clients...");
        while (!this.exit) {
            Socket client = null;
            try {
                client = this.serverSocket.accept();
            } catch (IOException ignored) {
            }
            ServerThread serverThread = new ServerThread(client, this.queue);

            JSONObject msg = new JSONObject();
            msg.put("event_type", "join_result");

            /*
            if (!this.active) {
                msg.put("result", false);
                msg.put("msg", "Sorry, the game has started!");
                serverThread.send(msg.toString());
                continue;
            }
             */

            String session_id = null;
            while (true) {
                session_id = Random.randomString(SessionIDLength);
                if (!this.sessionIDMap.containsKey(session_id))
                    break;
            }
            serverThread.setSessionID(session_id);
            this.sessionIDMap.put(session_id, serverThread);
            this.serverThreads.add(serverThread);
            msg.put("result", true);
            msg.put("session_id", session_id);
            serverThread.send(msg.toString());

            msg = new JSONObject();
            msg.put("event_type", "room_client_join");
            msg.put("session_id", session_id);
            this.queue.offer(msg);

            if (client != null)
                System.out.println("Connect with a new client " + client.getInetAddress().getHostAddress());
            System.out.printf("Totally %d clients\n", this.getClientsCount());
        }
    }

    public boolean send(String session_id, JSONObject msg) {
        if (this.getInitState() != 1)
            return false;

        if (this.sessionIDMap.containsKey(session_id))
            return this.sessionIDMap.get(session_id).send(msg.toString());
        return false;
    }

    public boolean sendAll(JSONObject msg) {
        if (this.getInitState() != 1)
            return false;
        String msgString = msg.toString();
        for (ServerThread serverThread : this.serverThreads)
            serverThread.send(msgString);
        return true;
    }

    public JSONObject getMsg() {
        return this.queue.poll();
    }

    public ServerThread getSession(String session_id) {
        return this.sessionIDMap.get(session_id);
    }

    public int getInitState() {
        if (!this.ready)
            return 0;
        else if (this.createSuccess)
            return 1;
        return 2;
    }

}