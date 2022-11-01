package com.eerussianguy.blazemap.mixin;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;

import com.eerussianguy.blazemap.engine.server.BlazeMapServerEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundChunkPacketMixin {

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At("RETURN"))
    private void constructor(LevelChunk chunk, CallbackInfo ci) {
        BlazeMapServerEngine.onChunkChanged(chunk.getLevel().dimension(), chunk.getPos());
    }
}
