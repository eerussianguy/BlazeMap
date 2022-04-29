package com.eerussianguy.blazemap;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.eerussianguy.blazemap.collector.Collectors;
import com.eerussianguy.blazemap.layer.Layers;
import com.eerussianguy.blazemap.maptype.MapTypes;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import static com.eerussianguy.blazemap.BlazeMap.MOD_ID;

@Mod(MOD_ID)
public class BlazeMap
{
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "blazemap";

    public BlazeMap()
    {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "Nothing", (remote, isServer) -> true));

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            EventHandler.init();
            ForgeEventHandler.init();
        }

        BConfig.init();
    }

    public void setup(FMLCommonSetupEvent event)
    {
        Collectors.init();
        Layers.init();
        MapTypes.init();
    }

}
