package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.PresetDeletePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class PresetListWidget extends ObjectSelectionList<PresetListWidget.Entry> {
    private final PresetManagerScreen screen;

    public PresetListWidget(PresetManagerScreen screen, Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.screen = screen;
        refreshEntries();
    }

    public void refreshEntries() {
        this.clearEntries();
        for (var preset : PresetManager.getInstance().getPresets()) {
            this.addEntry(new Entry(preset.name()));
        }
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final String presetName;

        public Entry(String presetName) {
            this.presetName = presetName;
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            Minecraft.getInstance().font.draw(poseStack, presetName, left + 5, top + 5, 0xFFFFFF);
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

        public void addButtons(int x, int y) {
            screen.addRenderableWidget(Button.builder(Component.literal("Edit"), button -> {
                screen.minecraft.setScreen(new PresetEditorScreen(false));
            }).pos(x, y).size(60, 20).build());

            screen.addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
                NetworkHandler.sendToServer(new PresetDeletePacket(presetName));
                refreshEntries();
            }).pos(x + 65, y).size(60, 20).build());
        }
    }
}