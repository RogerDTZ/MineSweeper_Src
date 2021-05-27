/**
 * @Author: RogerDTZ
 * @FileName: ServerThread.java
 */

package network;

import net.sf.json.JSONObject;
import util.ByteUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Queue;

public class ServerThread extends Thread {

    public static final int PACKAGE_HEAD_LENGTH = 2;

    private boolean exit;

    private Socket socket;
    private String session_id;

    private InputStream is;
    private OutputStream os;

    private Queue<JSONObject> queue;


    public ServerThread(Socket socket, Queue<JSONObject> queue) {
        this.queue = queue;
        try {
            if (socket == null)
                throw new IOException();
            this.socket = socket;
            this.exit = false;
            this.is = this.socket.getInputStream();
            this.os = this.socket.getOutputStream();
        } catch (IOException e) {
        }
        this.start();
    }

    public void setSessionID(String session_id) {
        this.session_id = session_id;
    }

    public String getSessionID() {
        return this.session_id;
    }

    public String getClientIP() {
        return this.socket.getInetAddress().getHostAddress();
    }

    public boolean send(String msg) {
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
        System.out.println("[Server] terminated");
    }

    @Override
    public void run() {
        try {
            byte[] bytes = new byte[0];
            System.out.println("[Server] server thread start");
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
                this.queue.offer(JSONObject.fromObject(data));
            }
        } catch (IOException ignored) {
        } finally {
            this.terminate();
            System.out.println("[Server] the client has disconnected");
        }
    }

    public boolean isDead() {
        return this.exit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerThread that = (ServerThread) o;
        return Objects.equals(session_id, that.session_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session_id);
    }

}
