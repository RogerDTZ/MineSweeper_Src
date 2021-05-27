/**
 * @Author: RogerDTZ
 * @FileName: SLManager.java
 */

package main;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SLManager {

    private static String lastFile = "";
    private static int same_id = 1;

    private static class FileWriteRequest {

        String dir;
        String name;
        String data;

        public FileWriteRequest(String dir, String name, String data) {
            this.dir = dir;
            this.name = name;
            this.data = data;
        }

    }

    private static ArrayList<FileWriteRequest> writeQueue = new ArrayList<>();


    public static String GetDefaultFileName() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = sdf.format(c.getTime());
        if (fileName.equals(lastFile)) {
            fileName = fileName + "_" + (++same_id);
        } else {
            lastFile = fileName;
            same_id = 1;
        }
        return fileName + ".txt";
    }

    public static void Append(String dir, String name, String data) {
        writeQueue.add(new FileWriteRequest(dir, name, data));
    }

    public static void FlushWriteQueue() {
        int cnt = 0;
        for (FileWriteRequest request : writeQueue) {
            WriteFile(request.dir, request.name, request.data);
            ++cnt;
        }
        if (cnt > 0) {
            if (cnt == 1)
                AttentionManager.showGoodMessage(cnt + " map has been exported");
            else
                AttentionManager.showGoodMessage(cnt + " maps has been exported");
        }
        writeQueue.clear();
    }

    public static void WriteFile(String dir, String name, String data) {
        try {
            File directory = new File(dir);
            if (!directory.exists())
                directory.mkdirs();
            File file = new File(dir + "\\" + name);
            if (!file.exists())
                file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(data);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String ReadFile(String dir) {
        File file = new File(dir);
        if (!file.exists())
            return null;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line;
            try {
                if ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                } else {
                    break;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return sb.toString();
    }

    public static String[] GetDirList(String path) {
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdir();
        return dir.list();
    }

}
