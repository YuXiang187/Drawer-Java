import com.formdev.flatlaf.FlatLightLaf;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.Objects;
import java.util.TimerTask;

public class Main extends JDialog implements NativeKeyListener {
    boolean isRun = false;
    boolean isControl = true;

    JLabel mainLabel;
    Timer randomTimer;
    Timer closeTimer;
    TrayIcon trayIcon;
    JProgressBar closeProgressBar;
    StringPool pool;

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == 29) {
            if (isControl) {
                run();
            }
        }
    }

    public Main() {
        pool = new StringPool();

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
                setVisible(false);
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        closeProgressBar = new JProgressBar();
        closeProgressBar.setMinimum(0);
        closeProgressBar.setMaximum(100);
        closeProgressBar.setStringPainted(false);
        closeProgressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }
        });

        mainLabel = new JLabel();
        mainLabel.setFont(new Font("微软雅黑", Font.BOLD, 80));
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }
        });

        mainPanel.add(mainLabel, BorderLayout.CENTER);
        mainPanel.add(closeProgressBar, BorderLayout.SOUTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        validate();

        trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/trayrun.png"))).getImage());

        if (SystemTray.isSupported()) {
            trayIcon.setToolTip("YuXiang Drawer");

            PopupMenu popupMenu = getPopupMenu();

            trayIcon.setPopupMenu(popupMenu);
            SystemTray systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add(trayIcon);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "您当前的操作系统不支持系统托盘，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "您当前的操作系统不支持系统托盘，无法启动本程序。", "YuXiang Drawer", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private PopupMenu getPopupMenu() {
        PopupMenu popupMenu = new PopupMenu();

        MenuItem runItem = new MenuItem("运行(Ctrl)");
        runItem.addActionListener(e -> run());
        popupMenu.add(runItem);

        MenuItem switchItem = new MenuItem("热键(开)");
        switchItem.addActionListener(e -> {
            isControl = !isControl;
            if (isControl) {
                runItem.setLabel("运行(Ctrl)");
                switchItem.setLabel("热键(开)");
            } else {
                runItem.setLabel("运行");
                switchItem.setLabel("热键(关)");
            }
        });
        popupMenu.add(switchItem);

        popupMenu.addSeparator();

        MenuItem resetItem = new MenuItem("重置");
        resetItem.addActionListener(e -> {
            if (!isRun) {
                int isReset = JOptionPane.showConfirmDialog(Main.this, "是否重置pool.es文件？", "YuXiang Drawer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (isReset == 0) {
                    pool.resetPool();
                    pool.saveFile();
                }
            }
        });
        popupMenu.add(resetItem);

        MenuItem saveItem = getMenuItem();
        popupMenu.add(saveItem);

        popupMenu.addSeparator();

        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        popupMenu.add(exitItem);
        return popupMenu;
    }

    private MenuItem getMenuItem() {
        MenuItem saveItem = new MenuItem("保存");
        saveItem.addActionListener(e -> {
            if (!isRun) {
                pool.saveFile();
                trayIcon.setImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/ok.png"))).getImage());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        trayIcon.setImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/trayrun.png"))).getImage());
                    }
                }, 1000);
            }
        });
        return saveItem;
    }

    private void run() {
        if (!isRun) {
            isRun = true;
            setVisible(true);
            mainLabel.setForeground(Color.GRAY);
            closeProgressBar.setValue(closeProgressBar.getMaximum());
            setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/stop.png"))).getImage());
            trayIcon.setImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/traystop.png"))).getImage());
            randomTimer = new Timer();
            randomTimer.scheduleAtFixedRate(new TimerTask() {
                int countdown = 8;

                @Override
                public void run() {
                    countdown--;
                    mainLabel.setText(pool.getRandomString());
                    if (countdown <= 0) {
                        randomTimer.cancel();
                        mainLabel.setForeground(Color.BLACK);
                        pool.removeString(mainLabel.getText());
                        setIconImage(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/icon/run.png"))).getImage());
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
