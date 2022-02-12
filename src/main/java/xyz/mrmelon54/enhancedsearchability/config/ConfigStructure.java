package xyz.mrmelon54.enhancedsearchability.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings("unused")
@Config(name = "enhancedsearchability")
@Config.Gui.Background("minecraft:textures/block/oak_planks.png")
public class ConfigStructure implements ConfigData {
    public boolean resourcePacksEnabled = true;
    public boolean serversEnabled = true;
}
