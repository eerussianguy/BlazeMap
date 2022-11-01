package com.eerussianguy.blazemap.feature.waypoints;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.event.ServerJoinedEvent;
import com.eerussianguy.blazemap.api.event.WaypointEvent;
import com.eerussianguy.blazemap.api.markers.IMarkerStorage;
import com.eerussianguy.blazemap.api.markers.Waypoint;
import com.eerussianguy.blazemap.api.util.IOSupplier;
import com.eerussianguy.blazemap.api.util.MinecraftStreams;

public class WaypointStore implements IMarkerStorage<Waypoint> {
    private final Map<ResourceLocation, Waypoint> store = new HashMap<>();
    private final Collection<Waypoint> view = Collections.unmodifiableCollection(store.values());

    public static void onServerJoined(ServerJoinedEvent event) {
        event.setWaypointStorageFactory(WaypointStore::new);
    }

    @Override
    public Collection<Waypoint> getAll() {
        return view;
    }

    @Override
    public void add(Waypoint waypoint) {
        if(store.containsKey(waypoint.getID()))
            throw new IllegalStateException("The waypoint is already registered!");
        store.put(waypoint.getID(), waypoint);
        MinecraftForge.EVENT_BUS.post(new WaypointEvent.Created(waypoint));
        save();
    }

    @Override
    public void remove(ResourceLocation id) {
        if(store.containsKey(id)) {
            Waypoint waypoint = store.remove(id);
            MinecraftForge.EVENT_BUS.post(new WaypointEvent.Removed(waypoint));
            save();
        }
    }

    @Override
    public boolean has(ResourceLocation id) {
        return store.containsKey(id);
    }


    // =================================================================================================================
    private final IOSupplier<MinecraftStreams.Output> outputSupplier;
    private final IOSupplier<MinecraftStreams.Input> inputSupplier;
    private final Supplier<Boolean> exists;

    public WaypointStore(IOSupplier<MinecraftStreams.Input> inputSupplier, IOSupplier<MinecraftStreams.Output> outputSupplier, Supplier<Boolean> exists) {
        this.outputSupplier = outputSupplier;
        this.inputSupplier = inputSupplier;
        this.exists = exists;
        load();
    }

    public void save() {
        if(outputSupplier == null) {
            BlazeMap.LOGGER.warn("Waypoint store storage supplier is null, ignoring save request");
            return;
        }

        if(store.size() > 0) {
            try(MinecraftStreams.Output output = outputSupplier.get()) {
                output.writeInt(store.size());
                for(Waypoint waypoint : store.values()) {
                    output.writeResourceLocation(waypoint.getID());
                    output.writeDimensionKey(waypoint.getDimension());
                    output.writeBlockPos(waypoint.getPosition());
                    output.writeUTF(waypoint.getName());
                    output.writeResourceLocation(waypoint.getIcon());
                    output.writeInt(waypoint.getColor());
                    output.writeFloat(waypoint.getRotation());
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load() {
        if(!exists.get()) return;
        try(MinecraftStreams.Input input = inputSupplier.get()) {
            int count = input.readInt();
            for(int i = 0; i < count; i++) {
                Waypoint waypoint = new Waypoint(
                    input.readResourceLocation(),
                    input.readDimensionKey(),
                    input.readBlockPos(),
                    input.readUTF(),
                    input.readResourceLocation(),
                    input.readInt(),
                    input.readFloat()
                );
                store.put(waypoint.getID(), waypoint);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
