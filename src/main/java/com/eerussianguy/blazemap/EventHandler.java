package com.eerussianguy.blazemap;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.eerussianguy.blazemap.feature.BlazeMapFeatures;
import com.eerussianguy.blazemap.feature.maps.Overlays;


public class EventHandler {
    private static final Supplier<Boolean> OPTIFINE_LOADED = Suppliers.memoize(() ->
    {
        try {
            Class.forName("net.optifine.Config");
            return true;
        }
        catch(ClassNotFoundException ignored) {
            return false;
        }
    });

    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(EventHandler::clientSetup);
        bus.addListener(EventHandler::onConfigReload);
        bus.addListener(EventHandler::onTextureStitch);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        Overlays.reload();
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        Overlays.reload();
    }

    private static void onTextureStitch(TextureStitchEvent.Post event) {
        BlazeMapFeatures.initMiniMap();
        BlazeMapFeatures.initFullMap();
        BlazeMapFeatures.initWaypoints();
    }
}
