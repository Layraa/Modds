package com.custommobsforge.custommobsforge.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CustomButton extends Button {
    private static final ResourceLocation BUTTON_NORMAL = ResourceLocation.fromNamespaceAndPath("custommobsforge", "textures/gui/button_normal.png");
    private static final ResourceLocation BUTTON_PRESSED = ResourceLocation.fromNamespaceAndPath("custommobsforge", "textures/gui/button_pressed.png");

    public CustomButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceLocation texture = this.isHoveredOrFocused() || this.isPressed() ? BUTTON_PRESSED : BUTTON_NORMAL;
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
    }

    private boolean isPressed() {
        return this.active && this.isHoveredOrFocused();
    }
}