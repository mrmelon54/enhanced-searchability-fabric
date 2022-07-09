package xyz.mrmelon54.EnhancedSearchability.mixin.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.mrmelon54.EnhancedSearchability.client.EnhancedSearchabilityClient;
import xyz.mrmelon54.EnhancedSearchability.duck.ListWidgetDuckProvider;

@Mixin(StatsScreen.class)
public abstract class MixinStatScreen extends Screen {
    private final boolean enabled = EnhancedSearchabilityClient.getInstance().enableStatsSearchBar();
    private TextFieldWidget statSearchField;

    @Shadow
    @Nullable
    public abstract AlwaysSelectedEntryListWidget<?> getSelectedStatList();

    @Shadow
    private StatsScreen.GeneralStatsListWidget generalStats;

    @Shadow
    StatsScreen.ItemStatsListWidget itemStats;

    @Shadow
    private StatsScreen.EntityStatsListWidget mobStats;

    protected MixinStatScreen(Text title) {
        super(title);
    }

    @Inject(method = "createLists", at = @At("TAIL"))
    private void injectedCreateLists(CallbackInfo ci) {
        if(enabled) {
            setupOriginalServerListOffset(this.generalStats);
            setupOriginalServerListOffset(this.itemStats);
            setupOriginalServerListOffset(this.mobStats);
        }
    }

    @Inject(method = "createButtons", at = @At("TAIL"))
    private void injectedCreateButtons(CallbackInfo ci) {
        if (enabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            statSearchField = new TextFieldWidget(mc.textRenderer, width / 2 - 100, 22, 200, 20, null, Text.translatable("enhanced-searchability.searchBox"));
            statSearchField.setChangedListener((search) -> {
                if (this.getSelectedStatList() instanceof ListWidgetDuckProvider duckProvider)
                    duckProvider.filter(() -> search);
                else System.out.println("Failed to search selected stat list");
            });
            this.addSelectableChild(statSearchField);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/AlwaysSelectedEntryListWidget;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"), cancellable = true)
    private void injectedRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (enabled) {
            if (this.getSelectedStatList() != null) this.getSelectedStatList().render(matrices, mouseX, mouseY, delta);
            StatsScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
            super.render(matrices, mouseX, mouseY, delta);
            if (statSearchField != null) statSearchField.render(matrices, mouseX, mouseY, delta);
            ci.cancel();
        }
    }

    void setupOriginalServerListOffset(EntryListWidget<?> listWidget) {
        if (listWidget instanceof ListWidgetDuckProvider duck)
            duck.hideHeaderAndShift();
    }
}
