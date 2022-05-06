package com.eerussianguy.blazemap;

import java.io.File;

import net.minecraft.client.Minecraft;

public class ClientUtils {
    public static final Minecraft MC = Minecraft.getInstance();
    private static File baseDir;

    public static String getServerID() {
        if(MC.hasSingleplayerServer()) {
            return "SP@" + MC.getSingleplayerServer().getWorldData().getLevelName();
        }
        else {
            return "MP@" + MC.getCurrentServer().ip.replace(':', '+');
        }
    }

    public static File getBaseDir() {
        if(baseDir == null) baseDir = new File(MC.gameDirectory, BlazeMap.MOD_ID);
        return baseDir;
    }

    public static void runOnMainThread(Runnable r) {
        MC.tell(r);
    }
}
