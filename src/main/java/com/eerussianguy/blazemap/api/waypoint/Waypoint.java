package com.eerussianguy.blazemap.api.waypoint;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.BlazeMap;
import com.eerussianguy.blazemap.Helpers;

public class Waypoint {
    public static Waypoint deserialize(CompoundTag tag) {
        final String name = tag.getString("name");
        final GlobalPos pos = GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get("pos")).getOrThrow(false, BlazeMap.LOGGER::error);
        final int color = tag.getInt("color");
        return new Waypoint(name, pos, color);
    }

    private String name;
    private GlobalPos pos;
    private int color;

    public Waypoint(String name, GlobalPos pos, Random random) {
        this(name, pos, Helpers.randomBrightColor(random));
    }

    public Waypoint(String name, GlobalPos pos, int color) {
        this.name = name;
        this.pos = pos;
        this.color = color;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow(false, BlazeMap.LOGGER::error);
        return tag;
    }

    public String getName() {
        return name;
    }

    public ResourceKey<Level> getDimension() {
        return pos.dimension();
    }

    public BlockPos getPos() {
        return pos.pos();
    }

    public int getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPos(BlockPos pos) {
        this.pos = GlobalPos.of(this.pos.dimension(), pos);
    }

    public void setColor(int color) {
        this.color = color;
    }
}
