package net.onpointcoding.enhancedsearchability.utils;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class ClearableTextFieldDual {
    private final TextFieldWidget textFieldWidget;
    private final ButtonWidget clearButton;

    public ClearableTextFieldDual(TextFieldWidget textFieldWidget, ButtonWidget clearButton) {
        this.textFieldWidget = textFieldWidget;
        this.clearButton = clearButton;
    }

    public TextFieldWidget getTextFieldWidget() {
        return textFieldWidget;
    }

    public ButtonWidget getClearButton() {
        return clearButton;
    }
}
