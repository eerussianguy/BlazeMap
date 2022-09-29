package com.eerussianguy.blazemap;

import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ForgeEventHandler {


    public static void init() {
        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(ForgeEventHandler::registerClientCommands);

    }

    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(BlazeMapCommands.create());
    }

}
