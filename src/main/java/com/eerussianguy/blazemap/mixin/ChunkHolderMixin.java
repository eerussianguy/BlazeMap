package com.eerussianguy.blazemap.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;

import com.eerussianguy.blazemap.engine.server.BlazeMapServerEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {

    @Inject(method = "broadcastChanges", at = @At("RETURN"))
    private void broadcastChanges(LevelChunk chunk, CallbackInfo ci) {
        BlazeMapServerEngine.onChunkChanged(chunk.getLevel().dimension(), chunk.getPos());
    }
}
