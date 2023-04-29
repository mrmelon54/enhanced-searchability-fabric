package xyz.mrmelon54.EnhancedSearchability.mixin.server;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
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

import java.util.List;

@Mixin(value = MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen {
    private final boolean enabled = EnhancedSearchabilityClient.getInstance().enableServerSearchBar();

    @Shadow
    protected MultiplayerServerListWidget serverListWidget;
    @Shadow
    private boolean initialized;
    @Shadow
    @Nullable
    private List<Text> multiplayerScreenTooltip;
    private TextFieldWidget serverSearchBox;

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injected_init(CallbackInfo ci) {
        if (enabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            this.serverSearchBox = addSearchBox(mc, this.serverListWidget, this.serverSearchBox);
            this.setInitialFocus(this.serverSearchBox);

            if (this.initialized)
                setupOriginalServerListOffset(this.serverListWidget);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;drawCenteredTextWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V", shift = At.Shift.BEFORE), cancellable = true)
    private void injected_render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (enabled) {
            drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
            super.render(matrices, mouseX, mouseY, delta);
            if (this.multiplayerScreenTooltip != null) {
                this.renderTooltip(matrices, this.multiplayerScreenTooltip, mouseX, mouseY);
            }
            this.serverSearchBox.render(matrices, mouseX, mouseY, delta);
            ci.cancel();
        }
    }

    void setupOriginalServerListOffset(MultiplayerServerListWidget multiplayerServerListWidget) {
        if (multiplayerServerListWidget instanceof ListWidgetDuckProvider duck)
            duck.hideHeaderAndShift();
    }

    TextFieldWidget addSearchBox(MinecraftClient mc, MultiplayerServerListWidget serverListWidget, TextFieldWidget textFieldWidget) {
        textFieldWidget = new TextFieldWidget(mc.textRenderer, this.width / 2 - 100, 22, 200, 20, textFieldWidget, Text.translatable("enhanced-searchability.searchBox"));
        textFieldWidget.setChangedListener((search) -> {
            if (serverListWidget instanceof ListWidgetDuckProvider duckProvider)
                duckProvider.filter(() -> search);
        });
        this.addSelectableChild(textFieldWidget);
        return textFieldWidget;
    }
}
