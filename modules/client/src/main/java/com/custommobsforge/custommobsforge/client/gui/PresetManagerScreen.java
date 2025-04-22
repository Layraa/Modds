package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.network.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PresetManagerScreen extends Screen {
    private PresetListWidget presetList;
    private String selectedPreset;
    private List<String> models = new ArrayList<>();
    private List<String> textures = new ArrayList<>();
    private List<String> animations = new ArrayList<>();

    public PresetManagerScreen() {
        super(Component.literal("Preset Manager"));
    }

    @Override
    protected void init() {
        this.presetList = new PresetListWidget(this, this.minecraft, this.width / 2, this.height, 50, this.height - 50, 30);
        this.addWidget(this.presetList);

        int buttonY = this.height - 40;
        this.addRenderableWidget(Button.builder(Component.literal("Create New"), button -> this.minecraft.setScreen(new PresetEditorScreen(true)))
                .pos(10, buttonY).size(100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Spawn Mob"), button -> {
            if (selectedPreset != null) {
                NetworkHandler.sendToServer(new SpawnMobPacket(selectedPreset));
                this.minecraft.setScreen(null);
            }
        }).pos(this.width - 110, buttonY).size(100, 20).build());

        NetworkHandler.sendToServer(new RequestPresetsPacket());
        NetworkHandler.sendToServer(new ResourceListRequestPacket("model"));
        NetworkHandler.sendToServer(new ResourceListRequestPacket("texture"));
        NetworkHandler.sendToServer(new ResourceListRequestPacket("animation"));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.presetList.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if (this.presetList.getSelected() != null) {
            int buttonX = this.width / 2 + 10;
            int buttonY = this.presetList.getTop() + (this.presetList.getBottom() - this.presetList.getTop() - 20) / 2;
            this.presetList.getSelected().addButtons(buttonX, buttonY);
        }
    }

    public void setSelectedPreset(String presetName) {
        this.selectedPreset = presetName;
        this.presetList.getChildren().forEach(entry -> {
            if (entry instanceof PresetListWidget.Entry presetEntry && presetEntry.presetName.equals(presetName)) {
                this.presetList.setSelected(presetEntry);
            }
        });
    }

    public void handleResourceList(String type, List<String> resources) {
        switch (type) {
            case "model" -> models = resources;
            case "texture" -> textures = resources;
            case "animation" -> animations = resources;
        }
    }

    public void handleResourceValidation(boolean valid, boolean createMode, String name, float health, double speed, String model, String texture, String animation) {
        if (valid) {
            if (createMode) {
                NetworkHandler.sendToServer(new PresetCreatePacket(name, health, speed, model, texture, animation));
            } else {
                NetworkHandler.sendToServer(new PresetEditPacket(name, health, speed, model, texture, animation));
            }
            this.presetList.refreshEntries();
        } else {
            this.minecraft.setScreen(new ErrorScreen(Component.literal("Error"), Component.literal("Invalid resources. Please check the model, texture, and animation files.")));
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}