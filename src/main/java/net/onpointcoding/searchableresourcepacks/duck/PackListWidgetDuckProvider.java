package net.onpointcoding.searchableresourcepacks.duck;

import net.minecraft.client.gui.screen.pack.PackListWidget;

import java.util.List;
import java.util.function.Supplier;

public interface PackListWidgetDuckProvider {
    void filter(Supplier<String> searchTextSupplier);

    List<PackListWidget.ResourcePackEntry> getSyncStore();
}
