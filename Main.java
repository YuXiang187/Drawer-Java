import com.formdev.flatlaf.FlatLightLaf;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends JDialog implements NativeKeyListener {
    private static boolean isRun = false;
    private static boolean isHotKey;
    private static Point mouseDownCompCoords;
    private static Dimension screenSize;
    private final Config config;

    JLabel mainLabel;
    Timer randomTimer;
    Timer closeTimer;
    TrayIcon trayIcon;
    JProgressBar closeProgressBar;
    StringPool pool;
    JWindow floatWindow;
    JLabel runLabel;

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        if (nativeEvent.getKeyCode() == 29 && isHotKey) run();
    }

    public Main() {
        pool = new StringPool();
        config = new Config();
        isHotKey = config.get();

        setTitle("YuXiang Drawer");
        setSize(450, 250);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/run.png"))).getImage());
        setResizable(false);
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        closeProgressBar = new JProgressBar();
        closeProgressBar.setMinimum(0);
        closeProgressBar.setMaximum(100);
        closeProgressBar.setStringPainted(false);

        mainLabel = new JLabel();
        mainLabel.setFont(new Font("微软雅黑", Font.BOLD, 80));
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);

        mainPanel.add(mainLabel, BorderLayout.CENTER);
        mainPanel.add(closeProgressBar, BorderLayout.SOUTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        validate();

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        floatWindow = new JWindow();
        floatWindow.setSize(50, 50);
        floatWindow.setAlwaysOnTop(true);
        floatWindow.setLocation(screenSize.width - 150, screenSize.height - 130);

        runLabel = new JLabel(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/run.png"))));
        runLabel.setBorder(new LineBorder(Color.GRAY));
        runLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                run();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseDownCompCoords = e.getPoint();
            }
        });
        runLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                floatWindow.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        });

        floatWindow.add(runLabel);
        floatWindow.setVisible(!config.get());

        trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/trayrun.png"))).getImage());

        if (SystemTray.isSupported()) {
            trayIcon.setToolTip("YuXiang Drawer");

            PopupMenu popupMenu = mainPopupMenu();

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) run();
                }
            });
            trayIcon.setPopupMenu(popupMenu);

            SystemTray systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add(trayIcon);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "您当前的操作系统不支持系统托盘，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } else {
            JOptionPane.showMessageDialog(null, "您当前的操作系统不支持系统托盘，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private PopupMenu mainPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();

        MenuItem hotKeyItem = new MenuItem("热键");
        MenuItem floatWindowItem = new MenuItem("浮窗");
        MenuItem stopItem = new MenuItem("暂停");
        hotKeyItem.addActionListener(e -> {
            isHotKey = true;
            config.set(true);
            isHotKey(hotKeyItem, floatWindowItem, stopItem);
            trayIcon.displayMessage("YuXiang Drawer - 热键模式", "在热键模式下，您可以通过按下键盘上的Ctrl键来随机抽取名称。", TrayIcon.MessageType.INFO);
        });
        floatWindowItem.addActionListener(e -> {
            isHotKey = false;
            config.set(false);
            isHotKey(hotKeyItem, floatWindowItem, stopItem);
            trayIcon.displayMessage("YuXiang Drawer - 浮窗模式", "在浮窗模式下，您可以通过点击悬浮弹窗上的按钮来随机抽取名称。", TrayIcon.MessageType.INFO);
        });
        stopItem.addActionListener(e -> {
            hotKeyItem.setEnabled(true);
            floatWindowItem.setEnabled(true);
            stopItem.setEnabled(false);
            isHotKey = false;
            floatWindow.setVisible(false);
        });
        popupMenu.add(hotKeyItem);
        popupMenu.add(floatWindowItem);
        popupMenu.add(stopItem);

        isHotKey(hotKeyItem, floatWindowItem, stopItem);

        popupMenu.addSeparator();

        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(null, "是否退出程序？", "YuXiang Drawer", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        popupMenu.add(exitItem);
        return popupMenu;
    }

    private void isHotKey(MenuItem hotKeyItem, MenuItem floatWindowItem, MenuItem stopItem) {
        if (isHotKey) {
            hotKeyItem.setEnabled(false);
            floatWindowItem.setEnabled(true);
            stopItem.setEnabled(true);
            floatWindow.setVisible(false);
        } else {
            hotKeyItem.setEnabled(true);
            floatWindowItem.setEnabled(false);
            stopItem.setEnabled(true);
            floatWindow.setLocation(screenSize.width - 150, screenSize.height - 140);
            floatWindow.setVisible(true);
        }
    }

    private void run() {
        if (!isRun) {
            isRun = true;
            setVisible(true);
            mainLabel.setForeground(Color.GRAY);
            closeProgressBar.setValue(closeProgressBar.getMaximum());
            setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/stop.png"))).getImage());
            runLabel.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/stop.png"))));
            trayIcon.setImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/traystop.png"))).getImage());
            randomTimer = new Timer();
            randomTimer.scheduleAtFixedRate(new TimerTask() {
                int countdown = 8;

                @Override
                public void run() {
                    countdown--;
                    mainLabel.setText(pool.get());
                    if (countdown <= 0) {
                        randomTimer.cancel();
                        mainLabel.setForeground(Color.BLACK);
                        pool.remove(mainLabel.getText());
                        pool.save();
                        setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/run.png"))).getImage());
                        runLabel.setIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/run.png"))));
                        trayIcon.setImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/trayrun.png"))).getImage());
                        isRun = false;
                        close();
                    }
                }
            }, 0, 90);
        }
    }

    private void close() {
        closeTimer = new Timer();
        closeTimer.scheduleAtFixedRate(new TimerTask() {
            int countdown = 100;

            @Override
            public void run() {
                countdown--;
                closeProgressBar.setValue(countdown);
                if (isRun) {
                    closeTimer.cancel();
                    closeProgressBar.setValue(closeProgressBar.getMaximum());
                }
                if (countdown <= 0) {
                    closeTimer.cancel();
                    setVisible(false);
                }
            }
        }, 0, 18);
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            JOptionPane.showMessageDialog(null, "系统热键注册失败，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        GlobalScreen.addNativeKeyListener(new Main());
    }
}
