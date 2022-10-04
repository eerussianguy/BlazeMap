package com.eerussianguy.blazemap.mixin;

import java.util.BitSet;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;

import com.eerussianguy.blazemap.engine.server.BlazeMapServerEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {
    @Shadow private boolean hasChangedSections;
    @Shadow @Final private BitSet skyChangedLightSectionFilter;
    @Shadow @Final private BitSet blockChangedLightSectionFilter;

    /*@Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(ChunkPos pos, int p_142987_, LevelHeightAccessor levelha, LevelLightEngine p_142989_, ChunkHolder.LevelChangeListener p_142990_, ChunkHolder.PlayerProvider p_142991_, CallbackInfo ci) {
        if(levelha instanceof Level level) {
            BlazeMapServerEngine.onChunkChanged(level.dimension(), pos);
        }
    }*/

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void broadcastChanges(LevelChunk chunk, CallbackInfo ci) {
        if(this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            BlazeMapServerEngine.onChunkChanged(chunk.getLevel().dimension(), chunk.getPos());
        }
    }
}
