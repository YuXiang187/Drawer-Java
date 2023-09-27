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
    private static boolean isControl = true;
    private static Point mouseDownCompCoords = null;
    private static Dimension screenSize;

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
        if (nativeEvent.getKeyCode() == 29 && isControl) run();
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
        floatWindow.setVisible(false);

        trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/trayicon/trayrun.png"))).getImage());

        if (SystemTray.isSupported()) {
            trayIcon.setToolTip("YuXiang Drawer");

            PopupMenu popupMenu = mainPopupMenu();

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && isControl) run();
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

        MenuItem floatItem = new MenuItem("浮窗(关)");
        floatItem.addActionListener(e -> {
            if (floatWindow.isVisible()) {
                floatItem.setLabel("浮窗(关)");
                floatWindow.setVisible(false);
            } else {
                floatItem.setLabel("浮窗(开)");
                floatWindow.setLocation(screenSize.width - 150, screenSize.height - 130);
                floatWindow.setVisible(true);
            }
        });
        popupMenu.add(floatItem);

        popupMenu.addSeparator();

        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        popupMenu.add(exitItem);
        return popupMenu;
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
