/**
 * @Author: RogerDTZ
 * @FileName: Client.java
 */

package network;

import net.sf.json.JSONObject;
import util.ByteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

public class Client extends Thread {

    public static final int PACKAGE_HEAD_LENGTH = 2;

    private boolean ready;
    private boolean connectSuccess;
    private boolean exit;

    private Socket socket;
    private String session_id;

    private InputStream is;
    private OutputStream os;

    private String joinFailInformation;

    private Queue<JSONObject> queue;


    public Client(String address, int port) {
        this.ready = false;
        this.connectSuccess = false;
        this.exit = false;
        this.queue = new LinkedList<>();
        Thread connectThread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, port), 5000);
                    initStreams();
                } catch (SocketTimeoutException e) {
                    joinFailInformation = "Connection timeout: " + address;
                    ready = true;
                    connectSuccess = false;
                } catch (SocketException e) {
                    joinFailInformation = "Connection refused: " + address;
                    ready = true;
                    connectSuccess = false;
                } catch (IOException e) {
                }
            }
        };
        connectThread.start();
    }

    private void initStreams() {
        try {
            this.is = this.socket.getInputStream();
            this.os = this.socket.getOutputStream();
        } catch (IOException e) {
        }
        this.start();
    }

    public boolean send(String msg) {
        if (this.getInitState() != 1)
            return false;
        if (this.os == null)
            return false;
        if (this.exit)
            return false;
        byte[] sendBytes = msg.getBytes(StandardCharsets.UTF_8);
        try {
            this.os.write(sendBytes.length >> 8);
            this.os.write(sendBytes.length & 0xff);
            this.os.write(sendBytes);
            this.os.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean send(JSONObject msg) {
        return this.send(msg.toString());
    }

    public void terminate() {
        if (this.exit)
            return;
        this.send("");
        this.exit = true;
        try {
            if (this.socket != null)
                this.socket.close();
        } catch (IOException e) {
        }
        System.out.println("[Client] terminated");
    }

    @Override
    public void run() {
        try {
            byte[] bytes = new byte[0];
            System.out.println("[Client] client start");
            while (!this.exit) {
                if (this.is == null)
                    break;
                // process the head
                if (bytes.length < PACKAGE_HEAD_LENGTH) {
                    byte[] data = new byte[PACKAGE_HEAD_LENGTH - bytes.length];
                    int len = this.is.read(data);
                    if (len == -1)
                        continue;
                    bytes = ByteUtil.merge(bytes, data, 0, len);
                    if (len < data.length)
                        continue;
                }
                // process the body
                int bodyLength = ((bytes[0] & 0xff) << 8) + (bytes[1] & 0xff);
                if (bytes.length < PACKAGE_HEAD_LENGTH + bodyLength) {
                    byte[] data = new byte[PACKAGE_HEAD_LENGTH + bodyLength - bytes.length];
                    int len = this.is.read(data);
                    if (len == -1)
                        continue;
                    bytes = ByteUtil.merge(bytes, data, 0, len);
                    if (len < data.length)
                        continue;
                }
                byte[] body = new byte[bytes.length - PACKAGE_HEAD_LENGTH];
                System.arraycopy(bytes, PACKAGE_HEAD_LENGTH, body, 0, bytes.length - PACKAGE_HEAD_LENGTH);
                bytes = new byte[0];

                String data = new String(body, StandardCharsets.UTF_8);
                if (data.equals("")) {
                    this.exit = true;
                    break;
                }
                JSONObject json = JSONObject.fromObject(data);
                if (json.getString("event_type").equals("join_result")) {
                    this.ready = true;
                    if (json.getBoolean("result")) {
                        this.connectSuccess = true;
                        this.session_id = json.getString("session_id");
                    } else {
                        this.connectSuccess = false;
                        this.joinFailInformation = json.getString("msg");
                        break;
                    }
                } else {
                    this.queue.offer(json);
                }
            }
        } catch (IOException ignored) {
        } finally {
            this.terminate();
            System.out.println("[Client] the server has disconnected");
        }
    }

    public String getServerAddress() {
        if (this.getInitState() != 1)
            return null;
        return this.socket.getInetAddress().getHostAddress();
    }

    public String getSessionID() {
        return this.session_id;
    }

    public JSONObject getMsg() {
        if (this.getInitState() != 1)
            return null;
        return this.queue.poll();
    }

    public int getInitState() {
        if (!this.ready)
            return 0;
        else if (this.connectSuccess)
            return 1;
        return 2;
    }

    public String getJoinFailInformation() {
        return this.joinFailInformation;
    }

    public boolean isDead() {
        return this.exit;
    }

}
