package xyz.mrmelon54.enhancedsearchability.duck;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public interface ListWidgetDuckProvider {
    Text getHeaderText();

    void filter(Supplier<String> searchTextSupplier);

    List<PackListWidget.ResourcePackEntry> getSyncStoreRP();

    void hideHeaderAndShift();
}
