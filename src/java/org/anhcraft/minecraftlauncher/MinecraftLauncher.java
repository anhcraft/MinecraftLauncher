package org.anhcraft.minecraftlauncher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.anhcraft.BitStorage.APIs.BSViewer;
import org.anhcraft.spaciouslib.io.DirectoryManager;
import org.anhcraft.spaciouslib.io.FileManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class MinecraftLauncher {
    public static BSViewer config;
    public static File folder;
    public static File folderMinecraft;
    public static File configFile;
    public static BufferedImage logo;
    public static Setting setting;
    public static JsonObject versionJson;
    public static LinkedList<McVersion> versions = new LinkedList<>();
    public static Account account;

    public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }

    public static void main(String[] args){
        folder = new File(System.getProperty("user.home")+"\\AppData\\Roaming\\.mclauncher\\");
        new DirectoryManager(folder).mkdirs();
        folderMinecraft = new File(System.getProperty("user.home")+"\\AppData\\Roaming\\.minecraft\\");
        new DirectoryManager(folderMinecraft).mkdirs();
        String[] genFolders = {"assets","libraries","logs","mods",
                "saves","shaderpacks","server-resource-packs","screenshots","resourcepacks","versions"};
        for(String genFolder : genFolders){
            new DirectoryManager(new File(folderMinecraft, genFolder+"\\")).mkdir();
        }
        configFile = new File(folder, "config.txt");
        try {
            new FileManager(configFile).initFile(IOUtils.toByteArray(MinecraftLauncher.class.getResourceAsStream("/config.txt")));
        } catch(IOException e) {
            e.printStackTrace();
        }
        try {
            logo = ImageIO.read(MinecraftLauncher.class.getResourceAsStream("/logo.png"));
        } catch(IOException e) {
            e.printStackTrace();
        }
        config = new BSViewer(configFile);
        try {
            initSetting();
        } catch(IOException e) {
            e.printStackTrace();
        }
        File acc = new File(folder, "accounts.json");
        if(acc.exists()){
            try {
                account = new Gson().fromJson(FileUtils.readFileToString(acc, StandardCharsets.UTF_8),
                        Account.class);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            new FileManager(acc).create();
        }
        new Gui().start();
    }

    public static void initSetting() throws IOException {
        Setting s = new Setting();
        s.arguments = config.getString("settings_arguments.default");
        s.memory_allocation = config.getInteger("settings_memory_allocation.default");
        s.player_selected = "";
        s.show_console = config.getBoolean("settings_show_console.default");
        s.resolution = config.getString("settings_resolution.default");
        File settingFile = new File(folder, "setting.json");
        if(!settingFile.exists()){
            new FileManager(settingFile).create();
            FileUtils.write(settingFile, new Gson().toJson(s), StandardCharsets.UTF_8, false);
        } else {
            s = new Gson().fromJson(FileUtils.readFileToString(settingFile, StandardCharsets.UTF_8), Setting.class);
        }
        setting = s;
    }

    public static void saveSetting() throws IOException {
        File settingFile = new File(folder, "setting.json");
        if(settingFile.exists()) {
            FileUtils.write(settingFile, new Gson().toJson(setting), StandardCharsets.UTF_8, false);
        }
    }

    public static void handleVersions() {
        bv:
        for(JsonElement jsonElement : versionJson.getAsJsonArray("versions")) {
            McVersion v = new McVersion();
            JsonObject b = jsonElement.getAsJsonObject();
            v.id = b.get("id").getAsString();
            v.type = McVersion.Type.valueOf(b.get("type").getAsString().toUpperCase());
            v.jsonUrl = b.get("url").getAsString();
            for(McVersion x : versions){
                if(x.id.equals(v.id)){
                    continue bv;
                }
            }
            versions.add(v);
        }
        File folder = new File(folderMinecraft, "versions\\");
        new DirectoryManager(folder).mkdirs();
        if(folder.listFiles() == null){
            return;
        }
        f:
        for(File s : folder.listFiles()) {
            if(!s.isFile()){
                if(new File(s, s.getName()+".jar").exists()
                        && new File(s, s.getName()+".json").exists()){
                    try {
                        JsonObject b = new Gson().fromJson(FileUtils.readFileToString(new File(s,
                                s.getName()+".json"), StandardCharsets.UTF_8), JsonObject.class);
                        McVersion v = new McVersion();
                        v.id = b.get("id").getAsString();
                        v.type = McVersion.Type.valueOf(b.get("type").getAsString().toUpperCase());
                        v.jsonUrl = new File(s, s.getName()+".json").getAbsolutePath();
                        for(McVersion x : versions){
                            if(x.id.equals(v.id)){
                                continue f;
                            }
                        }
                        versions.add(v);
                    } catch(IOException ignored) {
                    }
                }
            }
        }
    }

    public static void initCustomVersion(){
        String url = "https://cdn.rawgit.com/anhcraft/MinecraftLauncher/master/cdn/versions.json";
        File versionFile = new File(MinecraftLauncher.folder, "custom_versions.json");
        try {
            FileUtils.copyURLToFile(new URL(url), versionFile);
        } catch(Exception e) {
            e.printStackTrace();
        }
        JsonObject customVersionJson = null;
        try {
            customVersionJson = new Gson()
                    .fromJson(FileUtils.readFileToString(versionFile, StandardCharsets.UTF_8), JsonObject.class);
        } catch(IOException e) {
            e.printStackTrace();
        }
        assert customVersionJson != null;
        List<McVersion> mvl = new ArrayList<>();
        bv:
        for(JsonElement jsonElement : customVersionJson.getAsJsonArray("optifine")) {
            McVersion v = new McVersion();
            JsonObject b = jsonElement.getAsJsonObject();
            v.id = b.get("version").getAsString();
            v.type = McVersion.Type.MODIFIED;
            McVersion m = getMCVersion(b.get("mcversion").getAsString());
            if(m == null){
                continue;
            }
            File x = new File(folder, "optifine\\"+v.id+".json");
            try {
                FileUtils.copyURLToFile(new URL(m.jsonUrl), x);
                JsonObject obj = new Gson().fromJson(FileUtils.readFileToString(x, StandardCharsets.UTF_8), JsonObject.class);
                obj.addProperty("mainClass", "net.minecraft.launchwrapper.Launch");
                obj.addProperty("id", v.id);
                obj.addProperty("minecraftArguments", obj.get("minecraftArguments").getAsString()+
                        " --tweakClass optifine.OptiFineTweaker");
                obj.addProperty("inheritsFrom", m.id);
                JsonArray t = new JsonArray();
                JsonObject tx1 = new JsonObject();
                tx1.addProperty("name", "optifine:OptiFine:"+v.id.replaceFirst("-OptiFine",
                        ""));
                t.add(tx1);
                JsonObject tx2 = new JsonObject();
                tx2.addProperty("name", "net.minecraft:launchwrapper:1.12");
                t.add(tx2);
                obj.add("libraries", t);
                FileUtils.write(x, new Gson().toJson(obj), StandardCharsets.UTF_8, false);
                v.jsonUrl = x.getAbsolutePath();
                v.isOptifine = true;
                v.optifineLibUrl = b.get("download").getAsString();
                for(McVersion d : versions){
                    if(d.id.equals(v.id)){
                        continue bv;
                    }
                }
                mvl.add(v);
            } catch(Exception ignored) {
            }
        }

        versions.addAll(0, mvl);
    }

    private static McVersion getMCVersion(String id){
        for(McVersion x : versions){
            if(x.id.equals(id)){
                return x;
            }
        }
        return null;
    }
}
