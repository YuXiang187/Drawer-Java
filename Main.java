import com.formdev.flatlaf.FlatLightLaf;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Main extends JDialog implements NativeKeyListener {
    int poolIndex;
    boolean isRun = false;
    boolean isShow = false;
    boolean isControl = true;

    JLabel mainLabel;
    Thread thread;
    Timer timer;
    TrayIcon trayIcon;
    List<String> initPool;
    List<String> pool;
    MenuItem runItem;
    MenuItem switchItem;

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == 29) {
            if (isControl) {
                run();
            }
        }
    }

    public Main() {
        try {
            EncryptString es = new EncryptString();
            initPool = new ArrayList<>(Arrays.asList(es.decrypt(readFile()).split(",")));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "list.es文件读取错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        pool = new ArrayList<>(initPool);

        setTitle("YuXiang Drawer");
        setSize(450, 250);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/run.png"))).getImage());
        setResizable(false);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!isRun) {
                    setVisible(false);
                }
            }
        });

        trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/trayrun.png"))).getImage());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainLabel = new JLabel("未运行");
        mainLabel.setFont(new Font("微软雅黑", Font.BOLD, 80));
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isRun) {
                    setVisible(false);
                }
            }
        });

        mainPanel.add(mainLabel, BorderLayout.CENTER);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        validate();

        if (SystemTray.isSupported()) {
            trayIcon.setToolTip("YuXiang Drawer");

            PopupMenu popupMenu = new PopupMenu();

            runItem = new MenuItem("运行(Ctrl)");
            runItem.addActionListener(e -> run());
            popupMenu.add(runItem);

            switchItem = new MenuItem("热键(开)");
            switchItem.addActionListener(e -> control());
            popupMenu.add(switchItem);

            MenuItem exitItem = new MenuItem("退出");
            exitItem.addActionListener(e -> System.exit(0));
            popupMenu.add(exitItem);

            trayIcon.setPopupMenu(popupMenu);
            SystemTray systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add(trayIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "您当前的操作系统不支持系统托盘，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void control() {
        if (isControl) {
            isControl = false;
            runItem.setLabel("运行");
            switchItem.setLabel("热键(关)");
        } else {
            isControl = true;
            runItem.setLabel("运行(Ctrl)");
            switchItem.setLabel("热键(开)");
        }
    }

    private void run() {
        if (!isShow) {
            isShow = true;
            setVisible(true);
            if (isRun) {
                stop();
            } else {
                start();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isRun) {
                        stop();
                    } else {
                        start();
                    }
                    isShow = false;
                    timer.cancel();
                }
            }, 650);
        }
    }

    private void start() {
        mainLabel.setForeground(Color.GRAY);
        trayIcon.setImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/traystop.png"))).getImage());
        setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/stop.png"))).getImage());
        if (pool.size() == 0) {
            pool = new ArrayList<>(initPool);
        }
        isRun = true;
        thread = new Thread(() -> {
            Random random = new Random();
            while (isRun) {
                poolIndex = random.nextInt(pool.size());
                mainLabel.setText(pool.get(poolIndex));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        thread.start();
    }

    private void stop() {
        mainLabel.setForeground(Color.BLACK);
        trayIcon.setImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/trayrun.png"))).getImage());
        setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/run.png"))).getImage());
        isRun = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        pool.remove(poolIndex);
    }

    private String readFile() {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("list.es"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "list.es文件读取错误，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        return contentBuilder.toString();
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            JOptionPane.showMessageDialog(null, "系统按键注册失败，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        GlobalScreen.addNativeKeyListener(new Main());
    }
}
