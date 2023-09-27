import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    Properties properties;
    File configFile;

    public Config() {
        properties = new Properties();
        configFile = new File("config.properties");

        try {
            if (configFile.createNewFile()) {
                set("isHotKey", true);
                set("isFloatWindow", false);
            }
            FileInputStream input = new FileInputStream(configFile);
            properties.load(input);
            input.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "配置文件config.properties读取错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    protected void set(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
        try {
            FileOutputStream output = new FileOutputStream(configFile);
            properties.store(output, "Drawer");
            output.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "配置文件config.properties写入错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

    }

    protected boolean get(String key) {
        try {
            FileInputStream input = new FileInputStream(configFile);
            properties.load(input);
            input.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "配置文件config.properties读取错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        return Boolean.parseBoolean(properties.getProperty(key));
    }
}
