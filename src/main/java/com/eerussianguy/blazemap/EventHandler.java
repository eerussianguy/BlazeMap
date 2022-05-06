package com.eerussianguy.blazemap;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


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
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
    }

}
