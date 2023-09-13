import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class StringPool {
    EncryptString es;
    List<String> initPool;
    List<String> pool;
    Random random;

    public StringPool() {
        es = new EncryptString();
        random = new Random();
        initPool = new ArrayList<>(Arrays.asList(read("list.es").split(",")));
        if (Files.exists(Paths.get("pool.es"))) {
            pool = new ArrayList<>(Arrays.asList(read("pool.es").split(",")));
        } else {
            reset();
        }
        if (pool.get(0).isEmpty()) {
            pool = new ArrayList<>(initPool);
        }
    }

    protected String get() {
        if (pool.isEmpty()) {
            reset();
        }
        return pool.get(random.nextInt(pool.size()));
    }

    protected void reset() {
        pool = new ArrayList<>(initPool);
    }

    protected void remove(String str) {
        pool.remove(str);
    }

    private String read(String fileName) {
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

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pool.es"))) {
            writer.write(es.encrypt(String.join(",", pool)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
