package xyz.mrmelon54.EnhancedSearchability.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mrmelon54.EnhancedSearchability.client.EnhancedSearchabilityClient;
import xyz.mrmelon54.EnhancedSearchability.duck.ListWidgetDuckProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(MultiplayerServerListWidget.class)
public class MixinMultiplayerServerListWidget extends AlwaysSelectedEntryListWidget<MultiplayerServerListWidget.Entry> implements ListWidgetDuckProvider {
    private final boolean enabled = EnhancedSearchabilityClient.getInstance().enableServerSearchBar();

    @Shadow
    @Final
    private MultiplayerServerListWidget.Entry scanningEntry;

    @Shadow
    @Final
    private List<MultiplayerServerListWidget.ServerEntry> servers;
    @Shadow
    @Final
    private List<MultiplayerServerListWidget.LanServerEntry> lanServers;
    private final List<MultiplayerServerListWidget.ServerEntry> serverSyncStore = new ArrayList<>();
    private final List<MultiplayerServerListWidget.LanServerEntry> lanServerSyncStore = new ArrayList<>();
    private Supplier<String> searchTextStore = () -> "";

    public MixinMultiplayerServerListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    public Text getHeaderText() {
        return null;
    }

    @Override
    public void filter(Supplier<String> searchTextSupplier) {
        if (enabled) {
            searchTextStore = searchTextSupplier;
            customAddServerStreamToUI(this.serverSyncStore.stream(), this.lanServerSyncStore.stream(), searchTextStore);
        }
    }

    @Inject(method = "updateEntries", at = @At("TAIL"))
    private void injected_updateEntries(CallbackInfo ci) {
        if (enabled)
            customAddServerStreamToUI(this.serverSyncStore.stream(), this.lanServerSyncStore.stream(), searchTextStore);
    }

    @Redirect(method = "setServers", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;servers:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<MultiplayerServerListWidget.ServerEntry> redirectServersList(MultiplayerServerListWidget instance) {
        return enabled ? serverSyncStore : this.servers;
    }

    @Redirect(method = "setLanServers", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget;lanServers:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<MultiplayerServerListWidget.LanServerEntry> redirectLanServersList(MultiplayerServerListWidget instance) {
        return enabled ? lanServerSyncStore : this.lanServers;
    }

    private void customAddServerStreamToUI(Stream<MultiplayerServerListWidget.ServerEntry> serverStream, Stream<MultiplayerServerListWidget.LanServerEntry> lanServerStream, Supplier<String> searchTextSupplier) {
        String s = searchTextSupplier.get().toLowerCase(Locale.ROOT);
        boolean isEmpty = s.equals("");

        this.children().clear();
        serverStream.forEach(serverEntry -> {
            List<String> a = new ArrayList<>();
            if (serverEntry.getServer().label != null)
                serverEntry.getServer().label.asOrderedText().accept((index, style, codePoint) -> {
                    a.add(Character.toString(codePoint));
                    return true;
                });
            String join = String.join("", a);
            if (isEmpty || serverEntry.getServer().name.toLowerCase(Locale.ROOT).contains(s) || join.toLowerCase(Locale.ROOT).contains(s))
                this.children().add(serverEntry);
        });
        this.children().add(this.scanningEntry);
        lanServerStream.forEach(lanServerEntry -> {
            if (isEmpty || isMatchingLanServer(lanServerEntry, s))
                this.children().add(lanServerEntry);
        });
    }

    private boolean isMatchingLanServer(MultiplayerServerListWidget.LanServerEntry lanServerEntry, String s) {
        return lanServerEntry.getLanServerEntry().getAddressPort().toLowerCase(Locale.ROOT).contains(s)
                || lanServerEntry.getLanServerEntry().getMotd().toLowerCase(Locale.ROOT).contains(s);
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
