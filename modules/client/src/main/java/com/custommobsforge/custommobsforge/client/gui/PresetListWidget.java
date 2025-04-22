package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PresetListWidget extends ObjectSelectionList<PresetListWidget.Entry> {
    private final PresetManagerScreen parent;

    public PresetListWidget(PresetManagerScreen parent, net.minecraft.client.Minecraft minecraft, int width, int height, int top, int bottom) {
        super(minecraft, width, height, top, bottom, 20);
        this.parent = parent;
        this.refreshEntries();
    }

    public void refreshEntries() {
        this.clearEntries();
        for (Preset preset : parent.getPresets()) {
            this.addEntry(new Entry(preset));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final Preset preset;

        public Entry(Preset preset) {
            this.preset = preset;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            guiGraphics.drawString(minecraft.font, preset.name(), left + 5, top + 5, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                PresetListWidget.this.setSelected(this);
                parent.setSelectedPreset(preset.name());
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(preset.name());
        }

        public String getPresetName() {
            return preset.name();
        }
    }
}