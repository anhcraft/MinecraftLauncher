package org.anhcraft.minecraftlauncher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.anhcraft.BitStorage.APIs.BSViewer;
import org.anhcraft.spaciouslib.io.DirectoryManager;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Objects;

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
    public static boolean needUpdateVersions = true;

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
        new DirectoryManager(new File(folder, "optifine\\")).mkdirs();
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
        File acc = new File(folder, "account.json");
        if(acc.exists()){
            try {
                account = new Gson().fromJson(new FileManager(acc).readAsString(),
                        Account.class);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            new FileManager(acc).create();
        }
        File verTracker = new File(folder, "lasttime.txt");
        if(verTracker.exists()){
            try {
                long last = Long.parseLong(new FileManager(verTracker).readAsString());
                if(System.currentTimeMillis()-last < 86400000){
                    needUpdateVersions = false;
                } else {
                    new FileManager(verTracker).write(Long.toString(System.currentTimeMillis()).getBytes());
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                new FileManager(verTracker).initFile(Long.toString(System.currentTimeMillis()).getBytes());
            } catch(IOException e) {
                e.printStackTrace();
            }
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
        File settingFile = new File(folder, "settings.json");
        if(!settingFile.exists()){
            new FileManager(settingFile).initFile(new Gson().toJson(s).getBytes(StandardCharsets.UTF_8));
        } else {
            s = new Gson().fromJson(new FileManager(settingFile).readAsString(), Setting.class);
        }
        setting = s;
    }

    public static void saveSetting() throws IOException {
        File settingFile = new File(folder, "settings.json");
        if(settingFile.exists()) {
            new FileManager(settingFile).write(new Gson().toJson(setting));
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
        for(File s : Objects.requireNonNull(folder.listFiles())) {
            if(!s.isFile()){
                if(new File(s, s.getName()+".jar").exists()
                        && new File(s, s.getName()+".json").exists()){
                    try {
                        JsonObject b = new Gson().fromJson(new FileManager(new File(s,
                                s.getName()+".json")).readAsString(), JsonObject.class);
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
        File versionFile = new File(MinecraftLauncher.folder, "custom_versions.json");
        if(!versionFile.exists() || needUpdateVersions) {
            try {
                new FileManager(versionFile).create().write(IOUtils.toByteArray(new URL("https://cdn.jsdelivr.net/gh/anhcraft/MinecraftLauncher@master/cdn/versions.json").openConnection().getInputStream()));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        JsonObject customVersionJson = null;
        try {
            customVersionJson = new Gson()
                    .fromJson(new FileManager(versionFile).readAsString(), JsonObject.class);
        } catch(IOException e) {
            e.printStackTrace();
        }
        assert customVersionJson != null;
        LinkedList<McVersion> mvl = new LinkedList<>();
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
                if(!x.exists()) {
                    JsonObject obj = new Gson().fromJson(IOUtils.toString(new URL(m.jsonUrl).openConnection().getInputStream()), JsonObject.class);
                    obj.addProperty("mainClass", "net.minecraft.launchwrapper.Launch");
                    obj.addProperty("id", v.id);
                    if(obj.has("minecraftArguments")) {
                        obj.addProperty("minecraftArguments", obj.get("minecraftArguments").getAsString() +
                                " --tweakClass optifine.OptiFineTweaker");
                    } else {
                        obj.addProperty("minecraftArguments", "--tweakClass optifine.OptiFineTweaker");
                    }
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
                    new FileManager(x).initFile(new Gson().toJson(obj).getBytes(StandardCharsets.UTF_8));
                }
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
