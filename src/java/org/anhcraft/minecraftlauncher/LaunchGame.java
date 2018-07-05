package org.anhcraft.minecraftlauncher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LaunchGame extends Thread {
    private String token;
    private String version;
    private String pass;
    private String user;
    private boolean pre;

    public LaunchGame(String user, String pass, boolean pre,
                      String versionSelected, AuthToken t, String token) {
        this.user = user;
        this.pass = pass;
        this.pre = pre;
        this.version = versionSelected;
        if(pre && t.token != null) {
            this.token = t.token;
        }
    }

    @Override
    public void run(){
        launch(1);
    }

    private synchronized void launch(int i) {
        if(5 < i){
            Gui.log("Launcher đã bị lỗi quá nhìu :( Xin hãy liên hệ " +
                    "https://fb.com/anhcraft/ để tác giả sửa lỗi :D");
            try {
                Thread.sleep(3000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            Gui.main();
            return;
        }
        try {
            McVersion x = getVersion(version);
            if(x == null){
                return;
            }
            Gui.log("Đã tìm thấy phiên bản "+x.id+"!");
            JsonObject o = initVersionFiles(x);
            Gui.log("Đang tải các tệp tin cần thiết...");
            initAssets(o);
            Gui.log("Đang tải các thư viện cần thiết...");
            List<String> lib = downloadFiles(x, o);
            Gui.log("Đang khởi tạo trò chơi...");
            StringBuilder cmd = new StringBuilder("java");
            cmd.append(" -Xms").append(MinecraftLauncher.setting.memory_allocation)
                    .append("M -Xmx").append(MinecraftLauncher.setting.memory_allocation)
                    .append("M -XX:HeapDumpPath=MojangTricksIntelDriversFor" +
                            "Performance_javaw.exe_minecraft.exe.heapdump");
            cmd.append(" -Dfile.encoding=UTF-8 -Djava.library.path=\"").append(MinecraftLauncher.folderMinecraft
                    .getAbsolutePath()).append("\\versions\\").append(x.id).append("\\natives\"");
            cmd.append(" -cp \"");
            if(o.has("inheritsFrom")) {
                if(!o.get("id").getAsString().equals(o.get("inheritsFrom").getAsString())) {
                    lib.add(downLaunchWarpper(o.get("inheritsFrom").getAsString()));
                    Thread.sleep(2000);
                }
            }
            else if(o.has("jar")) {
                if(!o.get("id").getAsString().equals(o.get("jar").getAsString())) {
                    lib.add(downLaunchWarpper(o.get("jar").getAsString()));
                    Thread.sleep(2000);
                }
            }
            if(x.id.toLowerCase().contains("OptiFine".toLowerCase()) || x.isOptifine){
                String n = x.id.replace("-OptiFine","");
                File opf = new File(MinecraftLauncher.folderMinecraft, "libraries\\" +
                        "optifine\\OptiFine\\"+n+"\\");
                if(!opf.exists()){
                    opf.mkdirs();
                }
                File opt = new File(MinecraftLauncher.folderMinecraft, "libraries\\" +
                        "optifine\\OptiFine\\"+n+"\\OptiFine-"+n+".jar");
                if(!opt.exists()) {
                    Gui.log("Đang cài thư viện cần thiết cho Optifine...");
                    FileUtils.copyURLToFile(new URL(x.optifineLibUrl), opt);
                }
                Thread.sleep(2000);
                lib.add(opt.getAbsolutePath());
            }
            for(String a : lib){
                cmd.append(a).append(";");
            }
            cmd.append(new File(MinecraftLauncher.folderMinecraft,
                    "versions/" + x.id + "/" + o.get("id").getAsString() + ".jar")
                    .getAbsolutePath()).append("\" -Dfml.ignoreInvalidMinecraftCertificates=true" +
                    " -Dfml.ignorePatchDiscrepancies=true -XX:+UseConcMarkSweepGC " +
                    "-XX:-UseAdaptiveSizePolicy ").append(o.get("mainClass").getAsString())
                    .append(
                            " --version ").append(x.id)
                    .append(" --gameDir ").append(MinecraftLauncher.folderMinecraft.getAbsolutePath())
                    .append(" --assetsDir ").append(MinecraftLauncher.folderMinecraft.getAbsolutePath());
            if(o.has("assets")){
                cmd.append("\\assets\\ --assetIndex ").append(o.get("assets").getAsString());
            }
            if(x.id.toLowerCase().contains("optifine") || x.isOptifine){
                cmd.append(" --tweakClass optifine.OptiFineTweaker");
            }
            cmd.append(" --username ").append(user).append(" --userProperties [] ");
            if(pre && token != null){
                String s = IOUtils.toString(new URL(
                                "https://api.mojang.com/users/profiles/minecraft/"+user),
                        StandardCharsets.UTF_8);
                Thread.sleep(2000);
                cmd.append(" --userType mojang --accessToken ").append(token).append(" --uuid ").append(
                        new Gson().fromJson(s, JsonObject.class).get("id").getAsString());
            } else {
                cmd.append(" --userType legacy --accessToken null --uuid 00000000-0000-0000-0000-000000000000");
            }
            String[] z = MinecraftLauncher.setting.resolution.split("x");
            cmd.append(" --width ").append(z[0])
                    .append(" --height ").append(z[1]);
            cmd.append(" ").append(MinecraftLauncher.setting.arguments);
            Gui.log(cmd.toString());
            new ClientProcess(cmd.toString(), MinecraftLauncher.folderMinecraft
                    .getAbsolutePath()+"\\versions\\"+x.id).start();
        } catch(Exception e) {
            Gui.log("Đang thử lại lần " +i+"...");
            try {
                Thread.sleep(3000);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
            launch(i+1);
        }
    }
    private String downLaunchWarpper(String s) throws Exception {
        String path = "net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar";
        File f = new File(MinecraftLauncher.folderMinecraft, "libraries/" + a(path));
        File e = new File(MinecraftLauncher.folderMinecraft, "libraries/" + path);
        if(!f.exists()) {
            f.mkdirs();
            Gui.log("Đang tải net.minecraft.launchwrapper... 1/1");
            try {
                Thread.sleep(10);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
            FileUtils.copyURLToFile(new URL("https://libraries.minecraft.net/"+path), e);
        }
        return e.getAbsolutePath();
    }

    private List<String> downloadFiles(McVersion x, JsonObject o) throws Exception {
        if(o.has("inheritsFrom")){
            if(!o.get("id").getAsString().equals(o.get("inheritsFrom").getAsString())){
                McVersion v = getVersion(o.get("inheritsFrom").getAsString());
                if(v == null){
                    return new ArrayList<>();
                }
                Gui.log("Phát hiện đây là phiên bản đã chỉnh sửa! Đang chuyển sang bản gốc: "+v.id);
                return downloadFiles(x, initVersionFiles(v));
            }
        } else if(o.has("jar")){
            if(!o.get("id").getAsString().equals(o.get("jar").getAsString())){
                McVersion v = getVersion(o.get("jar").getAsString());
                if(v == null){
                    return new ArrayList<>();
                }
                Gui.log("Phát hiện đây là phiên bản đã chỉnh sửa! Đang chuyển sang bản gốc: "+v.id);
                return downloadFiles(x, initVersionFiles(v));
            }
        }
        Gui.togglebar();
        List<String> s = new ArrayList<>();
        int i = 0;
        int b = o.getAsJsonArray("libraries").size();
        for(JsonElement jsonElement : o.getAsJsonArray("libraries")) {
            i++;
            Gui.progress(i, b);
            JsonObject lib = jsonElement.getAsJsonObject();
            if(!lib.has("downloads")){
                continue;
            }
            JsonObject downloads = lib.getAsJsonObject("downloads");
            if(lib.has("natives")) {
                JsonObject g = downloads.get("classifiers").getAsJsonObject();
                if(!g.has("natives-windows")){
                    continue;
                }
                String a = g
                        .get("natives-windows").getAsJsonObject().get("url").getAsString();
                File n = new File(MinecraftLauncher.folderMinecraft, "versions/" + x.id + "/natives/");
                if(!n.exists()) {
                    n.mkdirs();
                }
                File f = File.createTempFile("mclauncher-", Long.toString(System.currentTimeMillis()));
                FileUtils.copyURLToFile(new URL(a), f);
                try(ZipFile zipFile = new ZipFile(f)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while(entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        File entryDestination = new File(n, entry.getName());
                        if(entry.isDirectory()) {
                            entryDestination.mkdirs();
                        } else {
                            entryDestination.getParentFile().mkdirs();
                            InputStream in = zipFile.getInputStream(entry);
                            OutputStream out = new FileOutputStream(entryDestination);
                            IOUtils.copy(in, out);
                            IOUtils.closeQuietly(in);
                            out.close();
                        }
                    }
                }
                continue;
            }
            if(!downloads.has("artifact")) {
                continue;
            }
            JsonObject artifact = downloads.getAsJsonObject("artifact");
            String path = artifact.get("path").getAsString();
            File f = new File(MinecraftLauncher.folderMinecraft, "libraries/" + a(path));
            File e = new File(MinecraftLauncher.folderMinecraft, "libraries/" + path);
            if(!f.exists()) {
                f.mkdirs();
                Gui.log("Đang tải thư viện " + lib.get("name").getAsString() + "... "+(i+"/"+b));
                Thread.sleep(10);
                FileUtils.copyURLToFile(new URL(artifact.get("url").getAsString()), e);
            }
            s.add(e.getAbsolutePath());
        }
        Gui.togglebar();
        return s;
    }

    private String a(String q){
        String[] x = q.split("/");
        if(x.length == 0){
            return q;
        }
        x[x.length-1] = "";
        StringBuilder t = new StringBuilder();
        for(String c : x){
            t.append(c).append("/");
        }
        return t.substring(0, t.length()-1);
    }

    private void initAssets(JsonObject o) throws Exception {
        if(o.has("inheritsFrom")) {
            if(!o.get("id").getAsString().equals(o.get("inheritsFrom").getAsString())) {
                McVersion v = getVersion(o.get("inheritsFrom").getAsString());
                if(v == null){
                    return;
                }
                Gui.log("Phát hiện đây là phiên bản đã chỉnh sửa! Đang chuyển sang bản gốc: "+v.id);
                initAssets(initVersionFiles(v));
            }
        } else if(o.has("jar")){
            if(!o.get("id").getAsString().equals(o.get("jar").getAsString())) {
                McVersion v = getVersion(o.get("jar").getAsString());
                if(v == null){
                    return;
                }
                Gui.log("Phát hiện đây là phiên bản đã chỉnh sửa! Đang chuyển sang bản gốc: "+v.id);
                initAssets(initVersionFiles(v));
            }
        }
        Gui.togglebar();
        File a = new File(MinecraftLauncher.folderMinecraft,
                "assets/indexes/"+o.get("assets").getAsString()+".json");
        if(!a.exists()){
            FileUtils.copyURLToFile(new URL(o.get("assetIndex").getAsJsonObject().get("url").getAsString()), a);
        }
        JsonObject x = new Gson().fromJson(FileUtils.readFileToString(a, StandardCharsets.UTF_8),
                JsonObject.class).getAsJsonObject("objects");
        int i = 0;
        for(Map.Entry<String, JsonElement> q : x.entrySet()){
            i++;
            Gui.progress(i, x.entrySet().size());
            String name = q.getKey();
            String hash = q.getValue().getAsJsonObject().get("hash").getAsString();
            String r = hash.substring(0, 2)+"/"+hash;
            File c = new File(MinecraftLauncher.folderMinecraft,
                    "assets/objects/"+r);
            if(!c.exists()){
                Gui.log("Đang tải tệp tin "+name+"... "+(i+"/"+x.entrySet().size()));
                Thread.sleep(10);
                FileUtils.copyURLToFile(new URL("http://resources.download.minecraft.net/"+r), c);
            }
        }
        Gui.togglebar();
    }

    private JsonObject initVersionFiles(McVersion versionSelected) throws Exception {
        File versionFolder = new File(MinecraftLauncher.folderMinecraft,
                "versions/"+versionSelected.id+"/");
        if(!versionFolder.exists()){
            versionFolder.mkdirs();
            Gui.log("Đã tạo thư mục phiên bản!");
        }
        File e = new File(MinecraftLauncher.folderMinecraft,
                "versions/"+versionSelected.id+"/natives/");
        if(!e.exists()){
            e.mkdirs();
        }
        File versionFile = new File(versionFolder,versionSelected.id+".json");
        if(!versionFile.exists()){
            if(versionSelected.isOptifine){
                versionFile = new File(versionSelected.jsonUrl);
            } else {
                FileUtils.copyURLToFile(new URL(versionSelected.jsonUrl), versionFile);
            }
            Gui.log("Đã tải tệp dữ liệu phiên bản!");
        }
        JsonObject g = new Gson().fromJson(FileUtils.readFileToString(versionFile, StandardCharsets.UTF_8),
                JsonObject.class);
        File versionJar = new File(versionFolder,g.get("id").getAsString()+".jar");
        if(!versionJar.exists()){
            FileUtils.copyURLToFile(new URL(g.get("downloads").getAsJsonObject()
                        .get("client").getAsJsonObject().get("url").getAsString()), versionJar);
            Gui.log("Đã tải tệp tin chạy phiên bản!");
        }
        return g;
    }

    public static AuthToken getAuthToken(String user, String pass) throws Exception {
        Gui.log("[Client -> Mojang] Đang xác thực tài khoản...");
        byte[] payloadBytes = ("{\"agent\":{\"name\":\"Minecraft\",\"version\":1}," +
                "\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}").getBytes("UTF-8");
        URL url = new URL("https://authserver.mojang.com/authenticate");
        HttpsURLConnection con = (HttpsURLConnection) (url.openConnection());
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "runscope/0.1");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Accept-Charset", "UTF-8");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Content-Length", String.valueOf(payloadBytes.length));
        con.setRequestMethod("POST");
        con.setInstanceFollowRedirects(false);
        con.setUseCaches(false);
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);

        OutputStream out = con.getOutputStream();
        out.write(payloadBytes, 0 , payloadBytes.length);
        out.close();

        AuthToken e = new AuthToken();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while((line = in.readLine()) != null){
                output.append(line);
            }
            in.close();

            JsonObject g = new Gson().fromJson(output.toString(), JsonObject.class);
            if(g.has("accessToken")) {
                e.token = g.get("accessToken").getAsString();
                e.isSuccess = true;
                e.user = g.getAsJsonObject("selectedProfile").get("name").getAsString();
            } else {
                e.isSuccess = false;
            }
        } else {
            e.isSuccess = false;
        }

        return e;
    }

    public static boolean validateAuthToken(String token) throws Exception {
        Gui.log("[Client -> Mojang] Đang kiểm tra token...");
        byte[] payloadBytes =  ("{\"accessToken\":\"" + token + "\"}").getBytes("UTF-8");
        URL url = new URL("https://authserver.mojang.com/validate");
        HttpsURLConnection con = (HttpsURLConnection) (url.openConnection());
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "runscope/0.1");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Accept-Charset", "UTF-8");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Content-Length", String.valueOf(payloadBytes.length));
        con.setRequestMethod("POST");
        con.setInstanceFollowRedirects(false);
        con.setUseCaches(false);
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);
        OutputStream out = con.getOutputStream();
        out.write(payloadBytes, 0 , payloadBytes.length);
        out.close();
        return con.getResponseCode() == 204;
    }

    public static McVersion getVersion(String version) {
        McVersion x = null;
        for(McVersion v : MinecraftLauncher.versions) {
            if(v.id.equals(version)) {
                x = v;
                break;
            }
        }
        if(x == null) {
            return null;
        }
        return x;
    }
}
