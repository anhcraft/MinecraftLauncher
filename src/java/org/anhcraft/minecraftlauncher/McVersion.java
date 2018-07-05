package org.anhcraft.minecraftlauncher;

public class McVersion {
    public String id;
    public Type type;
    public String jsonUrl;
    public boolean isOptifine = false;
    public String optifineLibUrl = "";

    public enum Type {
        SNAPSHOT,
        RELEASE,
        OLD_ALPHA,
        OLD_BETA,
        MODIFIED
    }
}