package org.anhcraft.minecraftlauncher;

import javax.swing.*;
import java.awt.*;

public class Progress extends Thread {
    private static JFrame frame;
    private static JProgressBar bar;
    private static Thread thread;

    public Progress(){
        try {
            frame = new JFrame(MinecraftLauncher.config.getString("progress.title"));
            bar = new JProgressBar();
            thread = this;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        bar.setMaximum(100);
        bar.setMinimum(0);
        bar.setBackground(new Color(57, 143, 249));
        bar.setForeground(new Color(255, 255, 255));
        bar.setSize(new Dimension(500, 50));
        frame.add(bar);
        frame.setSize(new Dimension(500, 50));
        frame.setResizable(false);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setFont(Gui.f);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }

    public static synchronized void setProgress(int value, int max){
        int x = 100 / max * value;
        bar.setValue(x);
        bar.setString(x + "/100");
        frame.repaint();
    }

    public static synchronized void close(){
        frame.setVisible(false);
        frame.dispose();
        thread.interrupt();
    }
}
