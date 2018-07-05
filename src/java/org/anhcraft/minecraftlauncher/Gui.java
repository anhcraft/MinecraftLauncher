package org.anhcraft.minecraftlauncher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.anhcraft.spaciouslib.utils.RegEx;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Gui extends Thread {
    public static Frame frame;
    public static Font f;
    private static int backgroundSlides;
    private static JPanel cont;
    private static List<BufferedImage> images;
    private static JLabel slideshow;
    private static String versionSelected = null;
    private static JTextArea console = new JTextArea();
    public static JProgressBar bar;

    @Override
    public void run(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            f = Font.createFont(Font.TRUETYPE_FONT, MinecraftLauncher.class.
                    getResourceAsStream("/OpenSans.ttf"))
                    .deriveFont(Font.PLAIN, 20);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(f);
        } catch(Exception ignored){}
        backgroundSlides = 0;
        List<String> a = MinecraftLauncher.config.getStringList("launcher.slideshow");
        File backgroundFolder = new File(MinecraftLauncher.folder, "background/");
        if(!backgroundFolder.exists()){
            backgroundFolder.mkdirs();
        }
        Progress progress = new Progress();
        progress.start();
        int extend_progress = 3;
        images = new ArrayList<>();
        for(String img : a){
            File image = new File(backgroundFolder, DigestUtils.md5Hex(img));
            try {
                if(!image.exists()) {
                    try {
                        image.createNewFile();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    ImageIO.setUseCache(false);
                    ImageIO.write(ImageIO.read(new URL(img)),  FilenameUtils.getExtension(img), image);
                }
                images.add(ImageIO.read(image));
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                Progress.setProgress(images.size(), a.size()+extend_progress);
            }
        }
        File versionFile = new File(MinecraftLauncher.folder, "versions.json");
        try {
            FileUtils.copyURLToFile(new URL(MinecraftLauncher.config.getString(
                    "start_gui.version_list")), versionFile);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            Progress.setProgress(images.size()+1, a.size()+extend_progress);
            try {
                MinecraftLauncher.versionJson = new Gson().fromJson(
                        FileUtils.readFileToString(versionFile, StandardCharsets.UTF_8), JsonObject.class);
                MinecraftLauncher.handleVersions();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                Progress.setProgress(images.size()+2, a.size()+extend_progress);
            }
        }
        MinecraftLauncher.initCustomVersion();
        Progress.setProgress(images.size()+3, a.size()+extend_progress);
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        Progress.close();
        main();
    }

    public static synchronized void setBackgroundImage() {
        Timer t = new Timer(MinecraftLauncher.config.getInteger(
                "launcher.slideshow_interval")*1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(slideshow == null){
                    ((Timer) e.getSource()).stop();
                    return;
                }
                if(0 < images.size()) {
                    if(backgroundSlides == images.size()) {
                        backgroundSlides = 0;
                    }
                    try {
                        slideshow.setIcon(new ImageIcon(images.get(backgroundSlides)
                                .getScaledInstance(600, 300, Image.SCALE_SMOOTH)));
                    } catch(Exception x) {
                        x.printStackTrace();
                    }
                    backgroundSlides++;
                }
            }
        });
        t.setInitialDelay(0);
        t.start();
    }

    public static synchronized void main() {
        if(frame != null){
            frame.removeAll();
        }
        console = new JTextArea();
        cont = new JPanel();
        cont.setBackground(Color.WHITE);
        cont.setLayout(new FlowLayout());
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        JLabel title = new JLabel("", JLabel.CENTER);
        title.setIcon(new ImageIcon(MinecraftLauncher.logo));
        title.setBorder(new EmptyBorder(10, 0, 0, 0));
        cont.add(title);

        JPanel slide = new JPanel();
        slide.setBackground(Color.WHITE);
        slide.setPreferredSize(new Dimension(d.width, 350));
        slideshow = new JLabel("", JLabel.CENTER);
        slideshow.setHorizontalAlignment(SwingConstants.CENTER);
        slide.add(slideshow);
        cont.add(slide);

        JPanel leftButton = new JPanel();
        leftButton.setBackground(Color.WHITE);
        leftButton.setBorder(new EmptyBorder(0, 0, 0, 200));
        JLabel btn = new JLabel();
        try {
            btn.setIcon(new ImageIcon(ImageIO.read(MinecraftLauncher
                    .class.getResourceAsStream("/btn_play.png")).
                    getScaledInstance(180, 125, java.awt.Image.SCALE_SMOOTH)));
        } catch(IOException e) {
            e.printStackTrace();
        }
        btn.setPreferredSize(new Dimension(180, 125));
        btn.setFont(f.deriveFont(Font.PLAIN, 20));
        btn.setLocation(5, 5);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                
            }

            @Override
            public void mousePressed(MouseEvent e) {
                slideshow = null;
                playGui();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        leftButton.add(btn);
        cont.add(leftButton);

        JLabel settings = new JLabel();
        try {
            settings.setIcon(new ImageIcon(ImageIO.read(MinecraftLauncher
                    .class.getResourceAsStream("/btn_setting.png")).
                    getScaledInstance(180, 125, java.awt.Image.SCALE_SMOOTH)));
        } catch(IOException e) {
            e.printStackTrace();
        }
        settings.setBackground(Color.DARK_GRAY);
        settings.setForeground(Color.WHITE);
        settings.setPreferredSize(new Dimension(180, 125));
        settings.setFont(f.deriveFont(Font.PLAIN, 20));
        settings.setVerticalAlignment(SwingConstants.CENTER);
        settings.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                slideshow = null;
                settingsGui();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        cont.add(settings);
/*
        JLabel copyright = new JLabel("(c) anhcraft");
        copyright.setFont(f.deriveFont(Font.PLAIN, 15));
        copyright.setPreferredSize(new Dimension(d.width, 60));
        copyright.setBorder(new EmptyBorder(30, 50, 0, 0));
        cont.add(copyright);*/

        if(frame == null){
            frame = new Frame();
        }
        frame.setTitle(MinecraftLauncher.config.getString("launcher.title"));
        Gui.setBackgroundImage();
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(d);
        frame.setFont(f);
        frame.add(cont);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void playGui() {
        if(frame != null){
            frame.removeAll();
        }console = new JTextArea();
        FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
        flow.setVgap(0);
        cont = new JPanel();
        cont.setBackground(Color.WHITE);
        cont.setLayout(flow);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        JPanel leftSide = new JPanel();
        leftSide.setLayout(flow);
        JLabel anime = new JLabel();
        try {
            anime.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(
                    MinecraftLauncher.class.getResource("/startgui_image.gif"))));
        } catch(Exception e) {
            e.printStackTrace();
        }
        anime.setPreferredSize(new Dimension(d.width/10*3, d.height-80));
        anime.setMinimumSize(new Dimension(d.width/10*3, d.height-80));
        anime.setMaximumSize(new Dimension(d.width/10*3, d.height-80));
        leftSide.add(anime);
        leftSide.setPreferredSize(new Dimension(d.width/10*3, d.height-80));
        leftSide.setBackground(Color.WHITE);
        cont.add(leftSide);

        /////////////////////////////////////////////////////////////////////////////////////////////
        JPanel rightSide = new JPanel();
        rightSide.setLayout(flow);
        rightSide.setBorder(new EmptyBorder(30, 0, 0, 0));
        int rightSideWidth = d.width/10*6;
        //-------------- VERSION ------------------//
        JLabel versionTitle = new JLabel(MinecraftLauncher.config.getString("start_gui.version"));
        versionTitle.setPreferredSize(new Dimension(rightSideWidth, 30));
        versionTitle.setFont(f.deriveFont(Font.PLAIN, 18));
        versionTitle.setForeground(new Color(57, 143, 249));
        versionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        rightSide.add(versionTitle);

        // filter
        JPanel versionFilter = new JPanel();
        versionFilter.setLayout(flow);
        versionFilter.setPreferredSize(new Dimension(rightSideWidth/10*3, 300));
        versionFilter.setBackground(Color.WHITE);
        versionFilter.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.lightGray, 1),
                new EmptyBorder(20, 20, 0, 20)));
        // release
        JCheckBox versionFilterRelease = new JCheckBox(MinecraftLauncher.config.getString(
                "start_gui.version_release"), MinecraftLauncher.config.getBoolean(
                "start_gui.version_default_filter.release"));
        versionFilterRelease.setPreferredSize(new Dimension(rightSideWidth/10*2, 30));
        versionFilterRelease.setFont(f.deriveFont(Font.PLAIN, 14));
        versionFilterRelease.setForeground(new Color(66, 156, 65));
        versionFilterRelease.setBackground(Color.WHITE);
        versionFilter.add(versionFilterRelease);
        // snapshot
        JCheckBox versionFilterSnapshot = new JCheckBox(MinecraftLauncher.config.getString(
                "start_gui.version_snapshot"), MinecraftLauncher.config.getBoolean(
                "start_gui.version_default_filter.snapshot"));
        versionFilterSnapshot.setPreferredSize(new Dimension(rightSideWidth/10*2, 30));
        versionFilterSnapshot.setFont(f.deriveFont(Font.PLAIN, 14));
        versionFilterSnapshot.setForeground(new Color(242, 186, 84));
        versionFilterSnapshot.setBackground(Color.WHITE);
        versionFilter.add(versionFilterSnapshot);
        // old beta
        JCheckBox versionFilterOldBeta = new JCheckBox(MinecraftLauncher.config.getString(
                "start_gui.version_old_beta"), MinecraftLauncher.config.getBoolean(
                "start_gui.version_default_filter.old_beta"));
        versionFilterOldBeta.setPreferredSize(new Dimension(rightSideWidth/10*2, 30));
        versionFilterOldBeta.setFont(f.deriveFont(Font.PLAIN, 14));
        versionFilterOldBeta.setForeground(new Color(81, 195, 240));
        versionFilterOldBeta.setBackground(Color.WHITE);
        versionFilter.add(versionFilterOldBeta);
        // old alpha
        JCheckBox versionFilterOldAlpha = new JCheckBox(MinecraftLauncher.config.getString(
                "start_gui.version_old_alpha"), MinecraftLauncher.config.getBoolean(
                "start_gui.version_default_filter.old_alpha"));
        versionFilterOldAlpha.setPreferredSize(new Dimension(rightSideWidth/10*2, 30));
        versionFilterOldAlpha.setFont(f.deriveFont(Font.PLAIN, 14));
        versionFilterOldAlpha.setForeground(new Color(177, 143, 237));
        versionFilterOldAlpha.setBackground(Color.WHITE);
        versionFilter.add(versionFilterOldAlpha);

        JPanel versionBox = new JPanel();
        // versions box
        List<JLabel> versionSubs = filterVersions(versionFilterRelease.isSelected(), versionFilterSnapshot.isSelected(),
                versionFilterOldAlpha.isSelected(), versionFilterOldBeta.isSelected(), rightSideWidth/10*4);
        for(JLabel x : versionSubs){
            versionBox.add(x);
        }
        versionBox.setLayout(flow);
        versionBox.setPreferredSize(new Dimension(rightSideWidth/10*6,
                30*versionSubs.size()));
        versionBox.setBackground(Color.WHITE);
        versionBox.setBorder(new EmptyBorder(0, 0, 0, 0));
        JScrollPane scrollPaneVersionBox = new JScrollPane(versionBox);

        JButton filterBtn = new JButton(MinecraftLauncher.config.getString("start_gui.version_filter"));
        filterBtn.setPreferredSize(new Dimension(70, 25));
        filterBtn.setFont(f.deriveFont(Font.PLAIN, 14));
        filterBtn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                scrollPaneVersionBox.getVerticalScrollBar().setValue(0);
                versionBox.removeAll();
                List<JLabel> versionSubs = filterVersions(versionFilterRelease.isSelected(),
                        versionFilterSnapshot.isSelected(),
                        versionFilterOldAlpha.isSelected(), versionFilterOldBeta.isSelected(),
                        rightSideWidth/10*4);
                for(JLabel x : versionSubs){
                    versionBox.add(x);
                }
                versionBox.setPreferredSize(new Dimension(rightSideWidth/10*6,
                        30*versionSubs.size()));
                frame.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        versionFilter.add(filterBtn);
        rightSide.add(versionFilter);

        scrollPaneVersionBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneVersionBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneVersionBox.setPreferredSize(new Dimension(rightSideWidth/10*6, 300));
        scrollPaneVersionBox.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
        rightSide.add(scrollPaneVersionBox, BorderLayout.CENTER);

        // ACCOUNT
        JLabel accTitle = new JLabel(MinecraftLauncher.config.getString("start_gui.account"));
        accTitle.setPreferredSize(new Dimension(rightSideWidth, 60));
        accTitle.setFont(f.deriveFont(Font.PLAIN, 18));
        accTitle.setForeground(new Color(57, 143, 249));
        accTitle.setBorder(new EmptyBorder(30, 0, 5, 0));
        rightSide.add(accTitle);

        String user = "";
        String token = null;
        Boolean pre = false;
        if(MinecraftLauncher.account != null){
            user = MinecraftLauncher.account.user;
            if(MinecraftLauncher.account.token != null){
                pre = true;
                token = MinecraftLauncher.account.token;
            }
        }

        JPanel accPass = new JPanel();
        JLabel accUserLabel = new JLabel(MinecraftLauncher.config.getString("start_gui.account_input_user"));
        if(pre){
            accUserLabel.setText("Email");
        }
        accPass.setVisible(pre);
        JCheckBox accPre = new JCheckBox(MinecraftLauncher.config.getString("start_gui.account_premium"));
        accPre.setFont(f.deriveFont(Font.PLAIN, 16));
        accPre.setSelected(pre);
        accPre.setPreferredSize(new Dimension(rightSideWidth, 40));
        accPre.setBackground(Color.WHITE);
        accPre.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(accPre.isSelected()){
                    accPass.setVisible(false);
                    accUserLabel.setText(MinecraftLauncher.config.getString("start_gui.account_input_user"));
                } else {
                    accPass.setVisible(true);
                    accUserLabel.setText("Email");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        rightSide.add(accPre);

        JPanel accUser = new JPanel();
        accUser.setLayout(flow);
        accUser.setBackground(Color.WHITE);
        accUser.setPreferredSize(new Dimension(rightSideWidth, 40));
        accUserLabel.setFont(f.deriveFont(Font.PLAIN, 16));
        accUserLabel.setPreferredSize(new Dimension(rightSideWidth/10*2, 30));
        accUser.add(accUserLabel);
        JTextField accUserField = new JTextField(user);
        accUserField.setFont(f.deriveFont(Font.PLAIN, 16));
        accUserField.setPreferredSize(new Dimension(rightSideWidth/10*5, 30));
        accUser.add(accUserField);
        rightSide.add(accUser);

        accPass.setLayout(flow);
        accPass.setBorder(new EmptyBorder(3, 0, 0, 0));
        accPass.setBackground(Color.WHITE);
        accPass.setPreferredSize(new Dimension(rightSideWidth, 47));
        JLabel accPassLabel = new JLabel(MinecraftLauncher.config.getString("start_gui.account_input_pass"));
        accPassLabel.setFont(f.deriveFont(Font.PLAIN, 16));
        accPassLabel.setPreferredSize(new Dimension(rightSideWidth/10*2, 30));
        accPass.add(accPassLabel);
        JPasswordField accPassField = new JPasswordField();
        accPassField.setEchoChar('•');
        accPassField.setFont(f.deriveFont(Font.PLAIN, 16));
        accPassField.setPreferredSize(new Dimension(rightSideWidth/10*5, 30));
        accPass.add(accPassField);
        if(token != null){
            JOptionPane.showMessageDialog(frame, "Hệ thống đã lưu lại lần đăng nhập trước, " +
                            "vì thế bạn không cần phải điền mật khẩu!\n " +
                            "Nếu muốn, bạn có thể ghi mật khẩu mới vào.",
                    MinecraftLauncher.config.getString("launcher.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
        rightSide.add(accPass);

        JLabel btn = new JLabel();
        try {
            btn.setIcon(new ImageIcon(ImageIO.read(MinecraftLauncher
                    .class.getResourceAsStream("/btn_play.png")).
                    getScaledInstance(200, 135, java.awt.Image.SCALE_SMOOTH)));
        } catch(IOException e) {
            e.printStackTrace();
        }
        btn.setPreferredSize(new Dimension(200, 135));
        btn.setFont(f.deriveFont(Font.PLAIN, 20));
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(versionSelected == null){
                    JOptionPane.showMessageDialog(frame, "Bạn phải chọn một phiên bản!",
                            MinecraftLauncher.config.getString("launcher.title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(accUserField.getText().length() < 3){
                    JOptionPane.showMessageDialog(frame,
                            "Bạn phải nhập tài khoản/email từ 3 kí tự trở lên!",
                            MinecraftLauncher.config.getString("launcher.title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(accPre.isSelected()
                        && new String(accPassField.getPassword()).length() < 3){
                    if(MinecraftLauncher.account == null || MinecraftLauncher.account.token == null) {
                        JOptionPane.showMessageDialog(frame,
                                "Bạn phải nhập mật khẩu từ 3 kí tự trở lên!",
                                MinecraftLauncher.config.getString("launcher.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                if(MinecraftLauncher.account == null){
                    console(accUserField.getText(), String.valueOf(accPassField.getPassword()),
                            accPre.isSelected(), versionSelected, null);
                } else {
                    console(accUserField.getText(), String.valueOf(accPassField.getPassword()),
                            accPre.isSelected(), versionSelected, MinecraftLauncher.account.token);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        rightSide.add(btn);
        //////////////////////////////////////////////////////////////
        rightSide.setPreferredSize(new Dimension(rightSideWidth, d.height-80));
        rightSide.setBackground(Color.WHITE);
        cont.add(rightSide);

        if(frame == null){
            frame = new Frame();
        }
        frame.setTitle(MinecraftLauncher.config.getString("start_gui.title"));
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(d);
        frame.setFont(f);
        frame.add(cont);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                main();
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void console(String text, String s, boolean selected, String versionSelected, String token) {
        String user = text;
        AuthToken t = null;
        if(selected){
            if(0 < s.length()) {
                try {
                    t = LaunchGame.getAuthToken(text, s);
                    if(!t.isSuccess) {
                        JOptionPane.showMessageDialog(frame,
                                "Sai tài khoản hoặc mật khẩu!",
                                MinecraftLauncher.config.getString("launcher.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    user = t.user;
                } catch(Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                if(token == null){
                    return;
                }
                try {
                    if(!LaunchGame.validateAuthToken(token)){
                        JOptionPane.showMessageDialog(frame,
                                "Token của bạn đã quá hạn :( Xin hãy nhập lại mật khẩu và vào game lại!",
                                MinecraftLauncher.config.getString("launcher.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                t = new AuthToken();
                t.isSuccess = true;
                t.token = token;
                t.user = MinecraftLauncher.account.realuser;
                user = MinecraftLauncher.account.realuser;
            }
        }
        //if(MinecraftLauncher.setting.show_console) {
            if(frame != null) {
                frame.removeAll();
            }
            cont = new JPanel();
            cont.setBackground(Color.WHITE);
            cont.setLayout(new FlowLayout());
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

            console.setPreferredSize(new Dimension(d.width / 2, d.height/10*8));
            console.setFont(f.deriveFont(Font.PLAIN, 12));
            console.setEditable(false);
            console.setRows(50);
            console.setForeground(Color.WHITE);
            console.setBackground(Color.DARK_GRAY);
            console.setBorder(new EmptyBorder(10, 20, 10, 20));
            JScrollPane scroll = new JScrollPane(console);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.setPreferredSize(new Dimension(d.width / 2, 300));
            cont.add(scroll);

            bar = new JProgressBar();
            bar.setMaximum(100);
            bar.setMinimum(0);
            bar.setBackground(new Color(57, 143, 249));
            bar.setForeground(new Color(255, 255, 255));
            bar.setPreferredSize(new Dimension(d.width/2, 50));
            bar.setVisible(false);
            cont.add(bar);

            if(frame == null) {
                frame = new Frame();
            }
            frame.setTitle(MinecraftLauncher.config.getString("launcher.title"));
            frame.pack();
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setSize(new Dimension(d.width / 2, d.height / 2));
            frame.setFont(f);
            frame.add(cont);
            frame.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    main();
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            frame.setResizable(false);
            frame.setAlwaysOnTop(true);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
       /* } else {
            main();
        }*/

        log("Đang khởi động...");
        MinecraftLauncher.account = new Account();
        if(t != null) {
            if(t.token != null) {
                MinecraftLauncher.account.token = t.token;
            }
        }
        MinecraftLauncher.account.realuser = user;
        MinecraftLauncher.account.user = text;
        File acc = new File(MinecraftLauncher.folder, "accounts.json");
        try {
            FileUtils.write(acc, new Gson().toJson(MinecraftLauncher.account), StandardCharsets.UTF_8, false);
        } catch(IOException e) {
            e.printStackTrace();
        }
        if(selected && t.token != null){
            new LaunchGame(user, s, true, versionSelected, t, token).start();
        } else {
            new LaunchGame(user, s, selected, versionSelected, t, token).start();
        }
    }

    public static int lines = 0;

    public static synchronized void log(String s) {
        if(frame != null && console != null){
            console.append(s+"\n");
            lines++;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            console.setPreferredSize(new Dimension(d.width/2, lines*30));
        }
    }

    public static synchronized void progress(int value, int max){
        if(frame != null && bar != null) {
            bar.setValue(value);
            bar.setMaximum(max);
            bar.setString(value+"/"+max);
        }
    }

    public static synchronized void togglebar(){
        if(frame != null && bar != null) {
            bar.setVisible(!bar.isVisible());
        }
    }

    private static List<JLabel> filterVersions(boolean r, boolean s, boolean a, boolean b, int width) {
        List<JLabel> versionSubs = new ArrayList<>();
        try {
            for(McVersion v : MinecraftLauncher.versions) {
                if(v.type == McVersion.Type.OLD_ALPHA && !a){
                    continue;
                }
                if(v.type == McVersion.Type.OLD_BETA && !b){
                    continue;
                }
                if(v.type == McVersion.Type.RELEASE && !r){
                    continue;
                }
                if(v.type == McVersion.Type.SNAPSHOT && !s){
                    continue;
                }
                JLabel versionSub = new JLabel(v.id);
                versionSub.setIcon(new ImageIcon(ImageIO.read(MinecraftLauncher
                        .class.getResourceAsStream("/mojang_icon.png")).
                        getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
                versionSub.setBackground(Color.WHITE);
                versionSub.setBorder(new EmptyBorder(10, 10, 10, 10));
                versionSub.setPreferredSize(new Dimension(width, 30));
                versionSub.setFont(f.deriveFont(Font.PLAIN, 16));
                versionSub.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        JLabel label = (JLabel) e.getSource();
                        versionSelected = label.getText();
                        for(JLabel x : versionSubs) {
                            if(x.getText().equals(label.getText())) {
                                label.setBorder(new CompoundBorder(BorderFactory.createLineBorder(
                                        new Color(66, 156, 65), 1),
                                        new EmptyBorder(9, 9, 9, 9)));
                            } else {
                                x.setBorder(new EmptyBorder(10, 10, 10, 10));
                            }
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {

                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                    }
                });
                versionSubs.add(versionSub);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return versionSubs;
    }

    private synchronized static void settingsGui() {
        if(frame != null){
            frame.removeAll();
        }
        console = new JTextArea();
        cont = new JPanel();
        cont.setBackground(Color.WHITE);
        cont.setLayout(new FlowLayout());
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        JLabel logo = new JLabel("", JLabel.CENTER);
        logo.setIcon(new ImageIcon(MinecraftLauncher.logo));
        logo.setBorder(new EmptyBorder(10, 0, 30, 0));
        cont.add(logo);

        JLabel title = new JLabel(MinecraftLauncher.config.getString("settings.header"), JLabel.CENTER);
        title.setFont(f.deriveFont(Font.BOLD, 30));
        title.setBorder(new EmptyBorder(30, 0, 60, 0));
        title.setPreferredSize(new Dimension(d.width, 50));
        title.setBackground(Color.WHITE);
        cont.add(title);

        JCheckBox consoleShowable =
                new JCheckBox(MinecraftLauncher.config.getString("settings_show_console.label"));
        consoleShowable.setSelected(MinecraftLauncher.setting.show_console);
        consoleShowable.setPreferredSize(new Dimension(d.width, 30));
        consoleShowable.setHorizontalAlignment(SwingConstants.CENTER);
        consoleShowable.setBackground(Color.WHITE);
        consoleShowable.setFont(f.deriveFont(Font.PLAIN, 16));
        if(MinecraftLauncher.config.getBoolean("settings_show_console.show")) {
            cont.add(consoleShowable);
        }

        JPanel resolution = new JPanel();
        resolution.setPreferredSize(new Dimension(d.width, 50));

        JLabel resolutionLabel = new JLabel(MinecraftLauncher.config.getString("settings_resolution.label"));
        resolutionLabel.setPreferredSize(new Dimension(100, 50));
        resolutionLabel.setBackground(Color.WHITE);
        resolutionLabel.setFont(f.deriveFont(Font.PLAIN, 16));
        resolution.add(resolutionLabel);

        String[] res = MinecraftLauncher.setting.resolution.split("x");
        JTextField resolutionValueX = new JTextField(res[0]);
        resolutionValueX.setPreferredSize(new Dimension(50, 25));
        resolutionValueX.setBackground(Color.WHITE);
        resolutionValueX.setFont(f.deriveFont(Font.PLAIN, 14));
        resolutionValueX.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                StringBuilder q = new StringBuilder();
                String[] t = resolutionValueX.getText().split("");
                for(String x : t){
                    if(RegEx.INTEGER.matches(x)){
                        q.append(x);
                    }
                }
                if(5 < q.length()){
                    q = q.delete(5, q.length());
                }
                int x = 0;
                if(0 < q.length()){
                    x = Integer.parseInt(q.toString());
                }
                if(Toolkit.getDefaultToolkit().getScreenSize().width < x){
                    x = Toolkit.getDefaultToolkit().getScreenSize().width;
                }
                resolutionValueX.setText(Integer.toString(x));
            }
        });
        resolution.add(resolutionValueX);

        JLabel resolutionValueText = new JLabel("x");
        resolutionValueText.setPreferredSize(new Dimension(10, 25));
        resolutionValueText.setBackground(Color.WHITE);
        resolutionValueText.setFont(f.deriveFont(Font.PLAIN, 16));
        resolution.add(resolutionValueText);

        JTextField resolutionValueY = new JTextField(res[1]);
        resolutionValueY.setPreferredSize(new Dimension(50, 25));
        resolutionValueY.setBackground(Color.WHITE);
        resolutionValueY.setFont(f.deriveFont(Font.PLAIN, 14));
        resolutionValueY.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                StringBuilder q = new StringBuilder();
                String[] t = resolutionValueY.getText().split("");
                for(String x : t){
                    if(RegEx.INTEGER.matches(x)){
                        q.append(x);
                    }
                }
                if(5 < q.length()){
                    q = q.delete(5, q.length());
                }
                int y = 0;
                if(0 < q.length()){
                    y = Integer.parseInt(q.toString());
                }
                if(Toolkit.getDefaultToolkit().getScreenSize().height < y){
                    y = Toolkit.getDefaultToolkit().getScreenSize().height;
                }
                resolutionValueY.setText(Integer.toString(y));
            }
        });
        resolution.add(resolutionValueY);
        resolution.setBackground(Color.WHITE);
        if(MinecraftLauncher.config.getBoolean("settings_resolution.show")) {
            cont.add(resolution);
        }

        JPanel arguments = new JPanel();
        arguments.setPreferredSize(new Dimension(d.width, 50));
        JLabel argumentsLabel = new JLabel(MinecraftLauncher.config.getString("settings_arguments.label"));
        argumentsLabel.setPreferredSize(new Dimension(100, 25));
        argumentsLabel.setBackground(Color.WHITE);
        argumentsLabel.setFont(f.deriveFont(Font.PLAIN, 16));
        arguments.add(argumentsLabel);
        JTextField argumentsValue = new JTextField(MinecraftLauncher.setting.arguments);
        argumentsValue.setPreferredSize(new Dimension(400, 25));
        argumentsValue.setBackground(Color.WHITE);
        argumentsValue.setFont(f.deriveFont(Font.PLAIN, 14));
        arguments.add(argumentsValue);
        arguments.setBackground(Color.WHITE);
        if(MinecraftLauncher.config.getBoolean("settings_arguments.show")) {
            cont.add(arguments);
        }

        int ram = (int) (Runtime.getRuntime().maxMemory() / (1024L * 1024L));
        JPanel memory_allocation = new JPanel();
        memory_allocation.setPreferredSize(new Dimension(d.width, 50));
        memory_allocation.setBackground(Color.WHITE);
        int r = MinecraftLauncher.setting.memory_allocation;
        if(ram < r){
            r = ram;
        }
        JLabel memory_allocationCurrent = new JLabel(Integer.toString(r));

        JLabel memory_allocationLabel = new JLabel(MinecraftLauncher.config.getString(
                "settings_memory_allocation.label"));
        memory_allocationLabel.setPreferredSize(new Dimension(150, 25));
        memory_allocationLabel.setBackground(Color.WHITE);
        memory_allocationLabel.setFont(f.deriveFont(Font.PLAIN, 16));
        memory_allocation.add(memory_allocationLabel);

        JSlider memory_allocationValue = new JSlider(JSlider.HORIZONTAL, 0, ram,
                r);
        memory_allocationValue.setPreferredSize(new Dimension(200, 25));
        memory_allocationValue.setBackground(Color.WHITE);
        memory_allocationValue.setFont(f.deriveFont(Font.PLAIN, 14));
        memory_allocationValue.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                memory_allocationCurrent.setText(Integer.toString(memory_allocationValue.getValue())+"MB");
            }
        });
        memory_allocation.add(memory_allocationValue);

        memory_allocationCurrent.setPreferredSize(new Dimension(100, 25));
        memory_allocationCurrent.setBackground(Color.WHITE);
        memory_allocationCurrent.setFont(f.deriveFont(Font.PLAIN, 14));
        memory_allocation.add(memory_allocationCurrent);
        if(MinecraftLauncher.config.getBoolean("settings_memory_allocation.show")) {
            cont.add(memory_allocation);
        }

        JButton save = new JButton(MinecraftLauncher.config.getString("settings.accept"));
        save.setFocusPainted(false);
        save.setSelected(false);
        save.setPreferredSize(new Dimension(100, 30));
        save.setFont(f.deriveFont(Font.PLAIN, 14));
        save.setBorder(new EmptyBorder(0, 0, 0, 0));
        save.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                Setting x = MinecraftLauncher.setting;
                x.resolution = resolutionValueX.getText()+"x"+resolutionValueY.getText();
                x.show_console = consoleShowable.isSelected();
                x.memory_allocation = memory_allocationValue.getValue();
                x.arguments = argumentsValue.getText();
                MinecraftLauncher.setting = x;
                try {
                    MinecraftLauncher.saveSetting();
                } catch(IOException e1) {
                    e1.printStackTrace();
                }
                Gui.main();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        cont.add(save);

        JButton close = new JButton(MinecraftLauncher.config.getString("settings.deny"));
        close.setFocusPainted(false);
        close.setSelected(false);
        close.setPreferredSize(new Dimension(100, 30));
        close.setFont(f.deriveFont(Font.PLAIN, 14));
        close.setBorder(new EmptyBorder(0, 0, 0, 0));
        close.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                main();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        cont.add(close);

        if(frame == null) {
            frame = new Frame();
        }
        frame.setTitle(MinecraftLauncher.config.getString("settings.title"));
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(d);
        frame.setFont(f);
        frame.add(cont);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
