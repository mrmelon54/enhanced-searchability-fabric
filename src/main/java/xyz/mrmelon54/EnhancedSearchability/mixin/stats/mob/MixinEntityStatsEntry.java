package xyz.mrmelon54.EnhancedSearchability.mixin.stats.mob;

import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.mrmelon54.EnhancedSearchability.duck.EntityStatsEntryDuckProvider;

@Mixin(StatsScreen.EntityStatsListWidget.Entry.class)
public class MixinEntityStatsEntry implements EntityStatsEntryDuckProvider {
    @Shadow
    @Final
    private Text entityTypeName;

    public Text getEntityName() {
        return entityTypeName;
    }
}
