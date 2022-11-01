package com.eerussianguy.blazemap.api.event;

import java.util.Objects;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

import com.eerussianguy.blazemap.api.pipeline.MasterDataDispatcher;
import com.eerussianguy.blazemap.api.util.IStorageAccess;

/**
 * Fired when the Blaze Map server engine initializes a pipeline to serve a new dimension.
 */
public class ServerPipelineInitEvent extends Event {

    /**
     * The dimension this ServerPipeline operates in.
     */
    public final ResourceKey<Level> dimension;

    /**
     * Server file storage where all map data for this dimension is stored
     */
    public final IStorageAccess dimensionStorage;

    /**
     * The dispatcher that forwards MasterData updates to the clients
     */
    private MasterDataDispatcher dispatcher;

    public ServerPipelineInitEvent(ResourceKey<Level> dimension, IStorageAccess dimensionStorage, MasterDataDispatcher dispatcher) {
        this.dimension = dimension;
        this.dimensionStorage = dimensionStorage;
        this.dispatcher = dispatcher;
    }

    public MasterDataDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(MasterDataDispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }
}
