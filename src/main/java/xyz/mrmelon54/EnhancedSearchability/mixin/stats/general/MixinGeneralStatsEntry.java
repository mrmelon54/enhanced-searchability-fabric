package xyz.mrmelon54.EnhancedSearchability.mixin.stats.general;

import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.mrmelon54.EnhancedSearchability.duck.GeneralStatsEntryDuckProvider;

@Mixin(StatsScreen.GeneralStatsListWidget.Entry.class)
public class MixinGeneralStatsEntry implements GeneralStatsEntryDuckProvider {
    @Shadow
    @Final
    private Text displayName;

    public Text getDisplayName() {
        return displayName;
    }
}
