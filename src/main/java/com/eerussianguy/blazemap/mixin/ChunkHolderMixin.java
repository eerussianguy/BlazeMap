package com.eerussianguy.blazemap.mixin;

import java.util.BitSet;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;

import com.eerussianguy.blazemap.engine.server.BlazeMapServerEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {
    @Shadow private boolean hasChangedSections;
    @Shadow @Final private BitSet skyChangedLightSectionFilter;
    @Shadow @Final private BitSet blockChangedLightSectionFilter;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void broadcastChanges(LevelChunk chunk, CallbackInfo ci) {
        if(this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            BlazeMapServerEngine.onChunkChanged(chunk.getLevel().dimension(), chunk.getPos());
        }
    }
}
