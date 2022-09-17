package com.eerussianguy.blazemap;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.eerussianguy.blazemap.feature.Overlays;


public class EventHandler {

    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(EventHandler::clientSetup);
        bus.addListener(EventHandler::onConfigReload);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        Overlays.reload();
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        Overlays.reload();
    }
}
