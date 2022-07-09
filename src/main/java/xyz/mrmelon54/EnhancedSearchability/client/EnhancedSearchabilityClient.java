package xyz.mrmelon54.EnhancedSearchability.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import xyz.mrmelon54.EnhancedSearchability.config.ConfigStructure;

@Environment(EnvType.CLIENT)
public class EnhancedSearchabilityClient implements ClientModInitializer {
    private static EnhancedSearchabilityClient instance;
    private ConfigStructure config;

    @Override
    public void onInitializeClient() {
        instance = this;

        AutoConfig.register(ConfigStructure.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ConfigStructure.class).getConfig();
    }

    public static EnhancedSearchabilityClient getInstance() {
        return instance;
    }

    public ConfigStructure getConfig() {
        return config;
    }

    public boolean enableServerSearchBar() {
        return getConfig().serversEnabled;
    }

    public boolean enableResourcePackSearchBar() {
        return getConfig().resourcePacksEnabled;
    }

    public boolean enableStatsSearchBar() {
        return getConfig().statsEnabled;
    }
}
