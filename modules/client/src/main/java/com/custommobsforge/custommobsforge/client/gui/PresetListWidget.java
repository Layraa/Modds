package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.PresetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PresetListWidget extends AbstractWidget {
    private final List<PresetManager.Preset> presets;
    private final OnPresetSelected onPresetSelected;

    public interface OnPresetSelected {
        void onSelected(PresetManager.Preset preset);
    }

    public PresetListWidget(int x, int y, int width, int height, OnPresetSelected onPresetSelected) {
        super(x, y, width, height, Component.literal("Preset List"));
        this.presets = new ArrayList<>(PresetManager.getInstance().getAllPresets().values());
        this.onPresetSelected = onPresetSelected;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int y = this.getY();
        for (PresetManager.Preset preset : presets) {
            if (y + 20 <= this.getY() + this.height) {
                guiGraphics.fill(this.getX(), y, this.getX() + this.width, y + 20, isMouseOverPreset(mouseX, mouseY, y) ? 0x80FFFFFF : 0x80000000);
                guiGraphics.drawString(Minecraft.getInstance().font, preset.name(), this.getX() + 5, y + 6, 0xFFFFFF);
            }
            y += 20;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int y = this.getY();
            for (PresetManager.Preset preset : presets) {
                if (y + 20 <= this.getY() + this.height && isMouseOverPreset((int) mouseX, (int) mouseY, y)) {
                    onPresetSelected.onSelected(preset);
                    return true;
                }
                y += 20;
            }
        }
        return false;
    }

    private boolean isMouseOverPreset(int mouseX, int mouseY, int y) {
        return mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= y && mouseY < y + 20;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}