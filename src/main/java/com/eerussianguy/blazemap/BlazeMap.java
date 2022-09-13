package com.eerussianguy.blazemap;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.eerussianguy.blazemap.api.event.BlazeRegistryEvent;
import com.eerussianguy.blazemap.engine.BlazeMapEngine;
import com.eerussianguy.blazemap.feature.BlazeMapFeatures;
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

            EventHandler.init();
            ForgeEventHandler.init();
            BlazeMapConfig.init();
        }
    }

    public void setup(FMLCommonSetupEvent event) {
        BlazeMapEngine.init();

        BlazeMapFeatures.initMapping();
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.post(new BlazeRegistryEvent.CollectorRegistryEvent());
        bus.post(new BlazeRegistryEvent.LayerRegistryEvent());
        bus.post(new BlazeRegistryEvent.MapTypeRegistryEvent());

        BlazeMapFeatures.initFullMap();
        BlazeMapFeatures.initWaypoints();
    }
}
