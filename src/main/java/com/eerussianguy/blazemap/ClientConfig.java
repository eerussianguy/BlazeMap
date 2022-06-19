package com.eerussianguy.blazemap;

import java.util.List;
import java.util.function.Function;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.eerussianguy.blazemap.feature.maps.MinimapSize;
import com.eerussianguy.blazemap.feature.maps.MinimapZoom;

import static com.eerussianguy.blazemap.BlazeMap.MOD_ID;

/**
 * Forge configs happen to be a very simple way to serialize things across saves and hold data within a particular instance
 * It is not necessarily expected that the player will be editing the config
 * We are free to use key binds to allow what is essentially config editing on the fly
 */
public class ClientConfig {
    public final BooleanValue enableMinimap;
    public final BooleanValue enableDebug;
    public final ConfigValue<List<? extends String>> disabledLayers;
    public final EnumValue<MinimapSize> minimapSize;
    public final EnumValue<MinimapZoom> minimapZoom;

    ClientConfig(ForgeConfigSpec.Builder innerBuilder) {
        Function<String, ForgeConfigSpec.Builder> builder = name -> innerBuilder.translation(MOD_ID + ".config.server." + name);

        innerBuilder.push("general");

        enableMinimap = builder.apply("enableMinimap").comment("Enable the minimap?").define("enableMinimap", true);
        enableDebug = builder.apply("enableDebug").comment("Enable debug mode?").define("enableDebug", !FMLEnvironment.production);
        disabledLayers = builder.apply("disabledLayers").comment("List of disabled Layers, comma separated").defineList("disabledLayers", List::of, o -> o instanceof String);
        minimapSize = builder.apply("minimapSize").comment("Minimap size").defineEnum("minimapSize", MinimapSize.LARGE);
        minimapZoom = builder.apply("minimapZoom").comment("Minimap zoom").defineEnum("minimapZoom", MinimapZoom.MEDIUM);

        innerBuilder.pop();
    }
}
