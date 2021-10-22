package net.onpointcoding.enhancedsearchability.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.onpointcoding.enhancedsearchability.duck.ListWidgetDuckProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    private TextFieldWidget availablePackSearchBox;
    private TextFieldWidget selectedPackSearchBox;

    @Shadow
    public abstract void render(MatrixStack matrices, int mouseX, int mouseY, float delta);

    protected MixinPackScreen(Text text) {
        super(text);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/pack/PackScreen;refresh()V", shift = At.Shift.BEFORE))
    private void injected_init(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        this.availablePackSearchBox = addSearchBox(mc, this.availablePackList, this.availablePackSearchBox);
        this.selectedPackSearchBox = addSearchBox(mc, this.selectedPackList, this.selectedPackSearchBox);

        setupOriginalPackListOffset(this.availablePackList);
        setupOriginalPackListOffset(this.selectedPackList);
    }

    void setupOriginalPackListOffset(PackListWidget packListWidget) {
        if (packListWidget instanceof ListWidgetDuckProvider duck)
            duck.hideHeaderAndShift();
    }

    TextFieldWidget addSearchBox(MinecraftClient mc, PackListWidget packListWidget, TextFieldWidget textFieldWidget) {
        textFieldWidget = new TextFieldWidget(mc.textRenderer, packListWidget.getRowLeft() - 1, 47, packListWidget.getRowWidth() - 2, 20, textFieldWidget, new TranslatableText("enhancedsearchability.searchbox"));
        textFieldWidget.setChangedListener((search) -> {
            if (packListWidget instanceof ListWidgetDuckProvider duckProvider)
                duckProvider.filter(() -> search);
        });
        this.addSelectableChild(textFieldWidget);
        return textFieldWidget;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void injected_render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.availablePackSearchBox.render(matrices, mouseX, mouseY, delta);
        this.selectedPackSearchBox.render(matrices, mouseX, mouseY, delta);

        MinecraftClient mc = MinecraftClient.getInstance();
        renderOverlayHeader(matrices, mc, this.availablePackList);
        renderOverlayHeader(matrices, mc, this.selectedPackList);
    }

    void renderOverlayHeader(MatrixStack matrices, MinecraftClient mc, PackListWidget packListWidget) {
        int top = 32;
        int bottom = (int) (top + 9 * 1.5f) + 1;
        int left = packListWidget.getRowLeft() - 2;
        int right = packListWidget.getRowRight() - 2;
        int w = packListWidget.getRowWidth();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(left, bottom, 0).texture(left / 32f, bottom / 32f).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(right, bottom, 0).texture(right / 32f, bottom / 32f).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(right, top, 0).texture(right / 32f, top / 32f).color(32, 32, 32, 255).next();
        bufferBuilder.vertex(left, top, 0).texture(left / 32f, top / 32f).color(32, 32, 32, 255).next();
        tessellator.draw();

        Text text1 = packListWidget instanceof ListWidgetDuckProvider duck ? duck.getHeaderText() : new LiteralText("");
        Text text = (new LiteralText("")).append(text1).formatted(Formatting.UNDERLINE, Formatting.BOLD);
        mc.textRenderer.draw(matrices, text, (float) (left + w / 2 - mc.textRenderer.getWidth(text) / 2), 35, 0xffffff);
    }

    @Inject(method = "updatePackLists", at = @At("HEAD"), cancellable = true)
    private void injected_updatePackLists(CallbackInfo ci) {
        if (this.client != null) {
            if (this.availablePackList instanceof ListWidgetDuckProvider duckProvider && this.availablePackSearchBox != null) {
                customUpdatePackList(this.client, this.availablePackList, duckProvider, this.organizer.getDisabledPacks());
                duckProvider.filter(() -> this.availablePackSearchBox.getText());
            }
            if (this.selectedPackList instanceof ListWidgetDuckProvider duckProvider && this.selectedPackSearchBox != null) {
                customUpdatePackList(this.client, this.selectedPackList, duckProvider, this.organizer.getEnabledPacks());
                duckProvider.filter(() -> this.selectedPackSearchBox.getText());
                this.doneButton.active = !duckProvider.getSyncStoreRP().isEmpty();
            }
            ci.cancel();
        }
    }

    private void customUpdatePackList(MinecraftClient mc, PackListWidget widget, ListWidgetDuckProvider duck, Stream<ResourcePackOrganizer.Pack> packs) {
        duck.getSyncStoreRP().clear();
        packs.forEach((pack) -> {
            PackListWidget.ResourcePackEntry resourcePackEntry = new PackListWidget.ResourcePackEntry(mc, widget, this, pack);
            duck.getSyncStoreRP().add(resourcePackEntry);
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
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
