package com.eerussianguy.blazemap.engine;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.event.BlazeRegistryEvent;

public class RegistryController {
    private static boolean frozenRegistries = false;

    public static synchronized void ensureRegistriesReady() {
        if(!frozenRegistries) {
            IEventBus bus = MinecraftForge.EVENT_BUS;
            bus.post(new BlazeRegistryEvent.MasterDataRegistryEvent());
            BlazeMapAPI.MASTER_DATA.freeze();

            bus.post(new BlazeRegistryEvent.CollectorRegistryEvent());
            BlazeMapAPI.COLLECTORS.freeze();

            bus.post(new BlazeRegistryEvent.TransformerRegistryEvent());
            BlazeMapAPI.TRANSFORMERS.freeze();

            bus.post(new BlazeRegistryEvent.ProcessorRegistryEvent());
            BlazeMapAPI.PROCESSORS.freeze();

            if(FMLEnvironment.dist == Dist.CLIENT) {
                ensureClientRegistriesReady();
            }
            frozenRegistries = true;
        }
    }

    private static void ensureClientRegistriesReady() {
        IEventBus bus = MinecraftForge.EVENT_BUS;

        bus.post(new BlazeRegistryEvent.LayerRegistryEvent());
        BlazeMapAPI.LAYERS.freeze();

        bus.post(new BlazeRegistryEvent.MapTypeRegistryEvent());
        BlazeMapAPI.MAPTYPES.freeze();

        bus.post(new BlazeRegistryEvent.ObjectRendererRegistryEvent());
        BlazeMapAPI.OBJECT_RENDERERS.freeze();
    }
}
