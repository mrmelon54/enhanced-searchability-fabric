package xyz.mrmelon54.EnhancedSearchability.mixin.pack;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.mrmelon54.EnhancedSearchability.duck.ResourcePackEntryDuckProvider;

@Mixin(PackListWidget.ResourcePackEntry.class)
public class MixinResourcePackEntry implements ResourcePackEntryDuckProvider {
    @Shadow
    @Final
    private ResourcePackOrganizer.Pack pack;

    @Override
    public ResourcePackOrganizer.Pack getPack() {
        return this.pack;
    }
}
