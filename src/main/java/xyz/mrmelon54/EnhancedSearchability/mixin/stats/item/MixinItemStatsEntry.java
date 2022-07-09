package xyz.mrmelon54.EnhancedSearchability.mixin.stats.item;

import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.mrmelon54.EnhancedSearchability.duck.ItemStatsEntryDuckProvider;

@Mixin(StatsScreen.ItemStatsListWidget.Entry.class)
public class MixinItemStatsEntry implements ItemStatsEntryDuckProvider {
    @Shadow
    @Final
    private Item item;

    public Item getItem() {
        return item;
    }
}
