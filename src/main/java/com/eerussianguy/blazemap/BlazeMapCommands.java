package com.eerussianguy.blazemap;


import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.server.command.EnumArgument;

import com.eerussianguy.blazemap.api.BlazeMapAPI;
import com.eerussianguy.blazemap.api.BlazeRegistry;
import com.eerussianguy.blazemap.api.mapping.Layer;
import com.eerussianguy.blazemap.api.mapping.MapType;
import com.eerussianguy.blazemap.feature.Overlays;
import com.eerussianguy.blazemap.feature.maps.MinimapRenderer;
import com.eerussianguy.blazemap.feature.maps.MinimapSize;
import com.eerussianguy.blazemap.feature.maps.MinimapZoom;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class BlazeMapCommands {
    private static final EnumArgument<MinimapSize> MINIMAP_SIZE = EnumArgument.enumArgument(MinimapSize.class);
    private static final EnumArgument<MinimapZoom> MINIMAP_ZOOM = EnumArgument.enumArgument(MinimapZoom.class);

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("blazemap")
            .then(createDebug())
            .then(createMinimap());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createDebug() {
        return Commands.literal("debug")
            .then(Commands.literal("on").executes($ -> {
                BlazeMapConfig.CLIENT.enableDebug.set(true);
                Overlays.reload();
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("off").executes($ -> {
                BlazeMapConfig.CLIENT.enableDebug.set(false);
                Overlays.reload();
                return Command.SINGLE_SUCCESS;
            }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createMinimap() {
        return Commands.literal("minimap")
            .then(minimapLayer())
            .then(minimapSize())
            .then(minimapType())
            .then(minimapZoom());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> minimapLayer() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("layer");

        for(final BlazeRegistry.Key<Layer> key : BlazeMapAPI.LAYERS.keys()) {
            builder.then(Commands.literal(key.toString())
                .then(Commands.literal("on").executes($ -> {
                    MinimapRenderer.enableLayer(key);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("off").executes($ -> {
                    MinimapRenderer.disableLayer(key);
                    return Command.SINGLE_SUCCESS;
                }))
            );
        }

        return builder;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> minimapSize() {
        return Commands.literal("size")
            .then(Commands.argument("value", MINIMAP_SIZE)
                .executes(cmd -> {
                    MinimapSize size = cmd.getArgument("value", MinimapSize.class);
                    BlazeMapConfig.CLIENT.minimapSize.set(size);
                    return Command.SINGLE_SUCCESS;
                })
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> minimapType() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("type");

        for(BlazeRegistry.Key<MapType> key : BlazeMapAPI.MAPTYPES.keys()) {
            final MapType type = BlazeMapAPI.MAPTYPES.get(key);
            builder.then(Commands.literal(key.toString()).executes($ -> {
                MinimapRenderer.INSTANCE.setMapType(type);
                return Command.SINGLE_SUCCESS;
            }));
        }

        return builder;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> minimapZoom() {
        return Commands.literal("zoom")
            .then(Commands.argument("value", MINIMAP_ZOOM)
                .executes(cmd -> {
                    MinimapZoom zoom = cmd.getArgument("value", MinimapZoom.class);
                    BlazeMapConfig.CLIENT.minimapZoom.set(zoom);
                    return Command.SINGLE_SUCCESS;
                })
            );
    }
}
