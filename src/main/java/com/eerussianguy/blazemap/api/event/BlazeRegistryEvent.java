package com.eerussianguy.blazemap.api.event;

import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.MapType;
import com.eerussianguy.blazemap.api.pipeline.*;

/**
 * Fired on the ModEventBus when it is a suitable time to register objects.
 *
 * The use of the event to perform registrations is not mandatory, you can just call the registries whenever you want
 * from your mod setup methods, however at some point the registries are frozen, and registering new objects
 * becomes impossible. This event is always fired well before that.
 *
 * The registries start in an open state, so you do not have to wait for the event to register your objects.
 * However, in the dedicated server there are no Layers or Map Types, so those 2 registries start and stay closed.
 *
 * @author LordFokas
 */
public abstract class BlazeRegistryEvent<T> extends Event {
    public final BlazeRegistry<T> registry;

    protected BlazeRegistryEvent(BlazeRegistry<T> registry) {
        this.registry = registry;
    }

    public static class MasterDataRegistryEvent extends BlazeRegistryEvent<DataType<MasterDatum>> {
        public MasterDataRegistryEvent() {
            super(BlazeMapAPI.MASTER_DATA);
        }
    }

    public static class CollectorRegistryEvent extends BlazeRegistryEvent<Collector<MasterDatum>> {
        public CollectorRegistryEvent() {
            super(BlazeMapAPI.COLLECTORS);
        }
    }

    public static class TransformerRegistryEvent extends BlazeRegistryEvent<Transformer<MasterDatum>> {
        public TransformerRegistryEvent() {
            super(BlazeMapAPI.TRANSFORMERS);
        }
    }

    public static class ProcessorRegistryEvent extends BlazeRegistryEvent<Processor> {
        public ProcessorRegistryEvent() {
            super(BlazeMapAPI.PROCESSORS);
        }
    }

    public static class LayerRegistryEvent extends BlazeRegistryEvent<Layer> {
        public LayerRegistryEvent() {
            super(BlazeMapAPI.LAYERS);
        }
    }

    public static class MapTypeRegistryEvent extends BlazeRegistryEvent<MapType> {
        public MapTypeRegistryEvent() {
            super(BlazeMapAPI.MAPTYPES);
        }
    }
}
