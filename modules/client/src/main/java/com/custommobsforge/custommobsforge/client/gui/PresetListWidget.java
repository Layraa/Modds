package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.Preset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class PresetListWidget extends ObjectSelectionList<PresetListWidget.Entry> {
    private final PresetManagerScreen screen;

    public PresetListWidget(PresetManagerScreen screen, Minecraft minecraft, int width, int height, int top, int bottom) {
        super(minecraft, width, height, top, bottom, 30);
        this.screen = screen;
        refreshEntries();
    }

    public void refreshEntries() {
        this.clearEntries();
        for (Preset preset : screen.getPresets()) {
            this.addEntry(new Entry(preset.getName()));
        }
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final String presetName;

        public Entry(String presetName) {
            this.presetName = presetName;
        }

        public String getPresetName() {
            return presetName;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            guiGraphics.drawString(Minecraft.getInstance().font, presetName, left + 5, top + 5, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                screen.setSelectedPreset(presetName);
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(presetName);
        }
    }
}