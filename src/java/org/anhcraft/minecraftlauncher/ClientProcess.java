package org.anhcraft.minecraftlauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientProcess extends Thread {
    private String cmd;
    private File f;

    public ClientProcess(String cmd, String s) {
        this.cmd = cmd;
        this.f = new File(s+"\\");
    }

    @Override
    public void run(){
        try {
            Process p = Runtime.getRuntime().exec(
                    cmd,
                    null,
                    f);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = input.readLine()) != null){
                Gui.log(line);
            }
        } catch(IOException ignored) {
        }
    }
}
