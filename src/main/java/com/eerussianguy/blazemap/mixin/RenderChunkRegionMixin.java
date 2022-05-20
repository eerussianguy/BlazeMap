package com.eerussianguy.blazemap.mixin;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import com.eerussianguy.blazemap.engine.BlazeMapEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderChunkRegion.class)
public class RenderChunkRegionMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(Level level, int centerX, int centerZ, RenderChunk[][] renderChunks, CallbackInfo ci) {
        for(RenderChunk[] rcs : renderChunks) {
            for(RenderChunk rc : rcs) {
                ChunkPos pos = rc.wrapped.getPos();
                BlazeMapEngine.onChunkChanged(pos);
            }
        }
    }
}
