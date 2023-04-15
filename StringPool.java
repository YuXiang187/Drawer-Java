import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Timer;

public class StringPool {
    EncryptString es;
    List<String> initPool;
    List<String> pool;

    public StringPool() {
        try {
            es = new EncryptString();
            initPool = new ArrayList<>(Arrays.asList(es.decrypt(readFile("list.es")).split(",")));
            if (Files.exists(Paths.get("pool.es"))) {
                pool = new ArrayList<>(Arrays.asList(es.decrypt(readFile("pool.es")).split(",")));
            } else {
                reset();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "list.es或pool.es文件读取错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveFile();
            }
        }, 0, 600000);
    }

    public String get(int index) {
        return pool.get(index);
    }

    public void remove(int index) {
        pool.remove(index);
    }

    public int length() {
        return pool.size();
    }

    public void reset() {
        pool = new ArrayList<>(initPool);
    }

    private String readFile(String fileName) {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "list.es或pool.es文件读取错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        return contentBuilder.toString();
    }

    public void saveFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pool.es"))) {
            writer.write(es.encrypt(String.join(",", pool)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
