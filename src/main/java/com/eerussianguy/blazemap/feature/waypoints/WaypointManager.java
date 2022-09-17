package com.eerussianguy.blazemap.feature.waypoints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.api.event.WaypointEvent;
import com.eerussianguy.blazemap.api.waypoint.IWaypointStore;
import com.eerussianguy.blazemap.api.waypoint.Waypoint;
import com.eerussianguy.blazemap.util.MinecraftStreams;

public class WaypointManager implements IWaypointStore {
    private final Map<ResourceKey<Level>, Map<ResourceLocation, Waypoint>> store = new HashMap<>();
    private final Map<ResourceKey<Level>, Collection<Waypoint>> views = new HashMap<>();
    private final Set<ResourceLocation> ids = new HashSet<>();

    @Override
    public Collection<Waypoint> getWaypoints(ResourceKey<Level> dimension) {
        return views.computeIfAbsent(dimension, v -> Collections.unmodifiableCollection(store.computeIfAbsent(dimension, s -> new HashMap<>()).values()));
    }

    @Override
    public void addWaypoint(Waypoint waypoint) {
        if(ids.contains(waypoint.getID()))
            throw new IllegalStateException("The waypoint is already registered!");
        store
            .computeIfAbsent(waypoint.getDimension(), $ -> new HashMap<>())
            .put(waypoint.getID(), waypoint);
        ids.add(waypoint.getID());
        MinecraftForge.EVENT_BUS.post(new WaypointEvent.Created(waypoint));
        save(waypoint.getDimension());
    }

    @Override
    public void removeWaypoint(Waypoint waypoint) {
        ResourceLocation id = waypoint.getID();
        if(ids.contains(id)) {
            store.get(waypoint.getDimension()).remove(id);
            ids.remove(id);
            MinecraftForge.EVENT_BUS.post(new WaypointEvent.Created(waypoint));
            save(waypoint.getDimension());
        }
    }

    @Override
    public boolean hasWaypoint(Waypoint waypoint) {
        return ids.contains(waypoint.getID());
    }

    @Override
    public boolean hasWaypoint(ResourceLocation id) {
        return ids.contains(id);
    }


    // =================================================================================================================
    private Function<ResourceKey<Level>, OutputStream> supplier;

    public void save(ResourceKey<Level> dimension) {
        if(supplier == null) {
            BlazeMap.LOGGER.warn("Waypoint store storage supplier is null, ignoring save request");
            return;
        }
        Map<ResourceLocation, Waypoint> waypoints = store.computeIfAbsent(dimension, $ -> new HashMap<>());
        if(waypoints.size() > 0) {
            try(MinecraftStreams.Output output = new MinecraftStreams.Output(supplier.apply(dimension))) {
                output.writeInt(waypoints.size());
                for(Waypoint waypoint : waypoints.values()) {
                    output.writeResourceLocation(waypoint.getID());
                    output.writeDimensionKey(dimension);
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

    public void load(ResourceKey<Level> dimension, InputStream stream) {
        Map<ResourceLocation, Waypoint> waypoints = store.computeIfAbsent(dimension, $ -> new HashMap<>());
        try(MinecraftStreams.Input input = new MinecraftStreams.Input(stream)) {
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
                ids.add(waypoint.getID());
                waypoints.put(waypoint.getID(), waypoint);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void setSupplier(Function<ResourceKey<Level>, OutputStream> supplier) {
        this.supplier = supplier;
    }

    public void clear() {
        supplier = null;
        store.clear();
        ids.clear();
    }
}
