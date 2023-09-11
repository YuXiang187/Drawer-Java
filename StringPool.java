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
    Random random;

    public StringPool() {
        es = new EncryptString();
        random = new Random();
        initPool = new ArrayList<>(Arrays.asList(readFile("list.es").split(",")));
        if (Files.exists(Paths.get("pool.es"))) {
            pool = new ArrayList<>(Arrays.asList(readFile("pool.es").split(",")));
        } else {
            resetPool();
        }
        if (pool.get(0).isEmpty()) {
            pool = new ArrayList<>(initPool);
        }
        Timer timer = new Timer();
        // 每5分钟保存一次
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveFile();
            }
        }, 0, 300000);
    }

    protected String getRandomString() {
        if (pool.isEmpty()) {
            resetPool();
        }
        return pool.get(random.nextInt(pool.size()));
    }

    protected void resetPool() {
        pool = new ArrayList<>(initPool);
    }

    protected void removeString(String str) {
        pool.remove(str);
    }

    private String readFile(String fileName) {
        StringBuilder contentBuilder = new StringBuilder();
        String text = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            contentBuilder.append(reader.readLine());
            text = es.decrypt(contentBuilder.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, fileName + "文件读取错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        return text;
    }

    protected void saveFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pool.es"))) {
            writer.write(es.encrypt(String.join(",", pool)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
