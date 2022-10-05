package com.eerussianguy.blazemap;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.engine.client.BlazeMapClientEngine;
import com.eerussianguy.blazemap.engine.server.BlazeMapServerEngine;
import com.eerussianguy.blazemap.feature.BlazeMapFeaturesClient;
import com.eerussianguy.blazemap.feature.BlazeMapFeaturesCommon;
import com.eerussianguy.blazemap.network.BlazeNetwork;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import static com.eerussianguy.blazemap.BlazeMap.MOD_ID;

@Mod(MOD_ID)
public class BlazeMap {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "blazemap";
    public static final String MOD_NAME = "Blaze Map";

    public BlazeMap() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "Nothing", (remote, isServer) -> true));

        if(FMLEnvironment.dist == Dist.CLIENT) {
            final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(this::setup);

            FMLEventHandler.init();
            ForgeEventHandler.init();
            BlazeMapConfig.init();
        }
        else {
            // These are forbidden in the dedicated server.
            // The others are frozen by BlazeMapServer when the time comes.
            BlazeMapAPI.LAYERS.freeze();
            BlazeMapAPI.MAPTYPES.freeze();
            BlazeMapAPI.OBJECT_RENDERERS.freeze();
        }
    }

    public void setup(FMLCommonSetupEvent event) {
        BlazeNetwork.init();
        BlazeMapClientEngine.init();

        // Server engine must always start after the client engine due to
        // a dependency used to conserve resources in the integrated server
        if(FMLEnvironment.dist == Dist.CLIENT) {
            BlazeMapServerEngine.initForIntegrated();
        }
        else {
            BlazeMapServerEngine.initForDedicated();
        }

        BlazeMapFeaturesCommon.initMapping();

        if(FMLEnvironment.dist == Dist.CLIENT) {
            BlazeMapFeaturesClient.initMapping();
            BlazeMapFeaturesClient.initMaps();
            BlazeMapFeaturesClient.initWaypoints();
        }
    }
}
