package net.onpointcoding.enhancedsearchability.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.onpointcoding.enhancedsearchability.duck.ListWidgetDuckProvider;
import net.onpointcoding.enhancedsearchability.utils.ClearableTextFieldDual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen {
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;
    @Shadow
    private boolean initialized;
    private TextFieldWidget serverSearchBox;
    private ButtonWidget serverClearButton;

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void injected_init(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClearableTextFieldDual a = addSearchBox(mc, this.serverListWidget, this.serverSearchBox);
        this.serverSearchBox = a.getTextFieldWidget();
        this.serverClearButton = a.getClearButton();

        if (this.initialized)
            setupOriginalServerListOffset(this.serverListWidget);
    }

    void setupOriginalServerListOffset(MultiplayerServerListWidget multiplayerServerListWidget) {
        if (multiplayerServerListWidget instanceof ListWidgetDuckProvider duck)
            duck.hideHeaderAndShift();
    }

    ClearableTextFieldDual addSearchBox(MinecraftClient mc, MultiplayerServerListWidget serverListWidget, TextFieldWidget textFieldWidget) {
        textFieldWidget = new TextFieldWidget(mc.textRenderer, serverListWidget.getRowLeft() - 1, 34, serverListWidget.getRowWidth() - 22, 18, textFieldWidget, new TranslatableText("enhancedsearchability.searchbox"));
        textFieldWidget.setChangedListener((search) -> {
            if (serverListWidget instanceof ListWidgetDuckProvider duckProvider)
                duckProvider.filter(() -> search);
        });
        this.addSelectableChild(textFieldWidget);
        TextFieldWidget finalTextFieldWidget = textFieldWidget;
        ButtonWidget clearButton = this.addDrawableChild(new ButtonWidget(serverListWidget.getRowLeft() + serverListWidget.getRowWidth() - 22, 33, 20, 20, new TranslatableText("enhancedsearchability.clearbutton"), buttonWidget -> finalTextFieldWidget.setText("")));
        return new ClearableTextFieldDual(textFieldWidget, clearButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void injected_render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.serverSearchBox.render(matrices, mouseX, mouseY, delta);
    }
}
