package net.onpointcoding.searchableresourcepacks.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.onpointcoding.searchableresourcepacks.duck.PackListWidgetDuckProvider;
import net.onpointcoding.searchableresourcepacks.utils.ClearableTextFieldDual;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(PackScreen.class)
public abstract class MixinPackScreen extends Screen {
    @Shadow
    private PackListWidget availablePackList;
    @Shadow
    private PackListWidget selectedPackList;

    @Shadow
    private ButtonWidget doneButton;
    @Shadow
    @Final
    private ResourcePackOrganizer organizer;

    @Shadow
    public abstract void render(MatrixStack matrices, int mouseX, int mouseY, float delta);

    @Unique
    private TextFieldWidget availablePackSearchBox;
    @Unique
    private TextFieldWidget selectedPackSearchBox;
    private ButtonWidget availablePackClearButton;
    private ButtonWidget selectedPackClearButton;

    protected MixinPackScreen(Text text) {
        super(text);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/pack/PackScreen;refresh()V", shift = At.Shift.BEFORE))
    private void injected_init(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClearableTextFieldDual a = addSearchBox(mc.textRenderer, this.availablePackList, this.availablePackSearchBox);
        this.availablePackSearchBox = a.getTextFieldWidget();
        this.availablePackClearButton = a.getClearButton();
        ClearableTextFieldDual b = addSearchBox(mc.textRenderer, this.selectedPackList, this.selectedPackSearchBox);
        this.selectedPackSearchBox = b.getTextFieldWidget();
        this.selectedPackClearButton = b.getClearButton();
    }

    ClearableTextFieldDual addSearchBox(TextRenderer textRenderer, PackListWidget packListWidget, TextFieldWidget textFieldWidget) {
        textFieldWidget = new TextFieldWidget(textRenderer, packListWidget.getRowLeft() - 1, 47, packListWidget.getRowWidth() - 22, 18, textFieldWidget, new TranslatableText("searchableresourcepacks.searchbox"));
        textFieldWidget.setChangedListener((search) -> {
            if (packListWidget instanceof PackListWidgetDuckProvider duckProvider)
                duckProvider.filter(() -> search);
        });
        this.addSelectableChild(textFieldWidget);
        TextFieldWidget finalTextFieldWidget = textFieldWidget;
        ButtonWidget clearButton = this.addDrawableChild(new ButtonWidget(packListWidget.getRowLeft() + packListWidget.getRowWidth() - 22, 46, 20, 20, new TranslatableText("searchableresourcepacks.clearbutton"), buttonWidget -> finalTextFieldWidget.setText("")));
        return new ClearableTextFieldDual(textFieldWidget, clearButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void injected_render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.availablePackSearchBox.render(matrices, mouseX, mouseY, delta);
        this.selectedPackSearchBox.render(matrices, mouseX, mouseY, delta);
    }

    @Inject(method = "updatePackLists", at = @At("HEAD"), cancellable = true)
    private void injected_updatePackLists(CallbackInfo ci) {
        if (this.client != null) {
            if (this.availablePackList instanceof PackListWidgetDuckProvider duckProvider && this.availablePackSearchBox != null) {
                customUpdatePackList(this.client, this.availablePackList, duckProvider, this.organizer.getDisabledPacks());
                duckProvider.filter(() -> this.availablePackSearchBox.getText());
            }
            if (this.selectedPackList instanceof PackListWidgetDuckProvider duckProvider && this.selectedPackSearchBox != null) {
                customUpdatePackList(this.client, this.selectedPackList, duckProvider, this.organizer.getEnabledPacks());
                duckProvider.filter(() -> this.selectedPackSearchBox.getText());
                this.doneButton.active = !duckProvider.getSyncStore().isEmpty();
            }
            ci.cancel();
        }
    }

    private void customUpdatePackList(MinecraftClient mc, PackListWidget widget, PackListWidgetDuckProvider duck, Stream<ResourcePackOrganizer.Pack> packs) {
        duck.getSyncStore().clear();
        packs.forEach((pack) -> {
            PackListWidget.ResourcePackEntry resourcePackEntry = new PackListWidget.ResourcePackEntry(mc, widget, this, pack);
            duck.getSyncStore().add(resourcePackEntry);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.availablePackSearchBox != null && this.availablePackSearchBox.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.availablePackSearchBox);
            return true;
        }
        if (this.selectedPackSearchBox != null && this.selectedPackSearchBox.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.selectedPackSearchBox);
            return true;
        }
        if (this.availablePackClearButton != null && this.availablePackClearButton.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.availablePackClearButton);
            return true;
        }
        if (this.selectedPackClearButton != null && this.selectedPackClearButton.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.selectedPackClearButton);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
