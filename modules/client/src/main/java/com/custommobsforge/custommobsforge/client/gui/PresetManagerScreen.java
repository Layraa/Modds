package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.network.*;
import net.minecraft.client.gui.GuiGraphics;
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
    private Button editButton;
    private Button deleteButton;

    public PresetManagerScreen() {
        super(Component.literal("Preset Manager"));
    }

    @Override
    protected void init() {
        this.presetList = new PresetListWidget(this, this.minecraft, this.width / 2, this.height, 50, this.height - 50);
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        this.presetList.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (this.presetList.getSelected() != null) {
            int buttonX = this.width / 2 + 10;
            int buttonY = this.presetList.getTop() + (this.presetList.getBottom() - this.presetList.getTop() - 20) / 2;

            if (editButton == null) {
                editButton = Button.builder(Component.literal("Edit"), button -> this.minecraft.setScreen(new PresetEditorScreen(false)))
                        .pos(buttonX, buttonY).size(60, 20).build();
                this.addRenderableWidget(editButton);
            }
            if (deleteButton == null) {
                deleteButton = Button.builder(Component.literal("Delete"), button -> {
                    NetworkHandler.sendToServer(new PresetDeletePacket(selectedPreset));
                    this.presetList.refreshEntries();
                }).pos(buttonX + 65, buttonY).size(60, 20).build();
                this.addRenderableWidget(deleteButton);
            }
        } else {
            if (editButton != null) {
                this.removeWidget(editButton);
                editButton = null;
            }
            if (deleteButton != null) {
                this.removeWidget(deleteButton);
                deleteButton = null;
            }
        }
    }

    public void setSelectedPreset(String presetName) {
        this.selectedPreset = presetName;
        for (PresetListWidget.Entry entry : this.presetList.children()) {
            if (entry.getPresetName().equals(presetName)) {
                this.presetList.setSelected(entry);
                break;
            }
        }
    }

    public List<Preset> getPresets() {
        return new ArrayList<>(PresetManager.getInstance().getPresets());
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
            this.minecraft.setScreen(new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.literal("Invalid resources. Please check the model, texture, and animation files.")));
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}