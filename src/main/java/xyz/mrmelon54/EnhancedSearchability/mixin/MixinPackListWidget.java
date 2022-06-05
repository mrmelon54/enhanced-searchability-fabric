package xyz.mrmelon54.EnhancedSearchability.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.mrmelon54.EnhancedSearchability.client.EnhancedSearchabilityClient;
import xyz.mrmelon54.EnhancedSearchability.duck.ListWidgetDuckProvider;
import xyz.mrmelon54.EnhancedSearchability.duck.ResourcePackEntryDuckProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Mixin(PackListWidget.class)
public abstract class MixinPackListWidget extends EntryListWidget<PackListWidget.ResourcePackEntry> implements ListWidgetDuckProvider {
    private final boolean enabled = EnhancedSearchabilityClient.getInstance().enableResourcePackSearchBar();

    @Shadow
    @Final
    private Text title;
    private final List<PackListWidget.ResourcePackEntry> storeChildren = new ArrayList<>();

    public MixinPackListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/pack/PackListWidget;setRenderHeader(ZI)V"))
    private void redirected_setRenderHeader(PackListWidget instance, boolean b, int i) {
        if (enabled)
            this.setRenderHeader(b, i + 22);
    }

    @Override
    public Text getHeaderText() {
        return this.title;
    }

    @Override
    public void hideHeaderAndShift() {
        this.top += this.headerHeight + 1;
        this.setRenderHeader(false, 0);
    }

    @Override
    public double getScrollAmount() {
        double v = super.getScrollAmount();
        int m = super.getMaxScroll();
        return v > m ? m : v;
    }

    @Override
    public void filter(Supplier<String> searchTextSupplier) {
        String a = searchTextSupplier.get().toLowerCase(Locale.ROOT);

        if (a.trim().equals("")) {
            this.children().clear();
            this.storeChildren.forEach(item -> this.children().add(item));
            return;
        }

        this.children().clear();
        this.storeChildren.forEach(child -> {
            if (this.hasMatchingName(child, a))
                this.children().add(child);
        });
    }

    boolean hasMatchingName(PackListWidget.ResourcePackEntry child, String a) {
        return child instanceof ResourcePackEntryDuckProvider duckProvider &&
                (duckProvider.getPack().getDisplayName().asString().toLowerCase(Locale.ROOT).contains(a) ||
                        duckProvider.getPack().getDescription().asString().toLowerCase(Locale.ROOT).contains(a));
    }

    @Override
    public List<PackListWidget.ResourcePackEntry> getSyncStoreRP() {
        return storeChildren;
    }

}
