package xyz.mrmelon54.EnhancedSearchability.mixin.stats.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mrmelon54.EnhancedSearchability.client.EnhancedSearchabilityClient;
import xyz.mrmelon54.EnhancedSearchability.duck.ItemStatsEntryDuckProvider;
import xyz.mrmelon54.EnhancedSearchability.duck.ListWidgetDuckProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Mixin(StatsScreen.ItemStatsListWidget.class)
public abstract class MixinItemStatsListWidget extends EntryListWidget<StatsScreen.ItemStatsListWidget.Entry> implements ListWidgetDuckProvider {
    private final boolean enabled = EnhancedSearchabilityClient.getInstance().enableStatsSearchBar();

    private final List<StatsScreen.ItemStatsListWidget.Entry> storeOriginal = new ArrayList<>();
    private Supplier<String> searchTextStore = () -> "";

    public MixinItemStatsListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectedInit(StatsScreen statsScreen, MinecraftClient client, CallbackInfo ci) {
        for (int i = 0; i < this.getEntryCount(); i++) storeOriginal.add(this.getEntry(i));
    }

    private void customAddEntriesToUI() {
        String s = searchTextStore.get().toLowerCase(Locale.ROOT);
        boolean isEmpty = s.equals("");

        this.clearEntries();
        storeOriginal.forEach(entry -> {
            if (entry instanceof ItemStatsEntryDuckProvider duck) {
                List<String> a = new ArrayList<>();
                duck.getItem().getName().asOrderedText().accept((index, style, codePoint) -> {
                    a.add(Character.toString(codePoint));
                    return true;
                });
                String join = String.join("", a);
                if (isEmpty || join.toLowerCase(Locale.ROOT).contains(s))
                    this.addEntry(entry);
            }
        });
    }

    @Override
    public Text getHeaderText() {
        return Text.empty();
    }

    @Override
    public void filter(Supplier<String> searchTextSupplier) {
        if (enabled) {
            searchTextStore = searchTextSupplier;
            customAddEntriesToUI();
        }
    }

    @Override
    public List<PackListWidget.ResourcePackEntry> getSyncStoreRP() {
        return null;
    }

    @Override
    public void hideHeaderAndShift() {
        this.top += 15;
    }

    @Override
    public double getScrollAmount() {
        double v = super.getScrollAmount();
        int m = getMaxScroll();
        return v > m ? m : v;
    }
}
