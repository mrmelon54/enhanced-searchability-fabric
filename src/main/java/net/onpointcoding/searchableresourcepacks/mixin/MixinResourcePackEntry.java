package net.onpointcoding.searchableresourcepacks.mixin;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PackListWidget.ResourcePackEntry.class)
public class MixinResourcePackEntry implements net.onpointcoding.searchableresourcepacks.duck.ResourcePackEntryDuckProvider {
    @Shadow
    @Final
    private ResourcePackOrganizer.Pack pack;

    @Override
    public ResourcePackOrganizer.Pack getPack() {
        return this.pack;
    }
}
