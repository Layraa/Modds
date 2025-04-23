package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.client.ClientPresetHandler;
import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.client.render.ResourceValidator;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.SpawnMobPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PresetGui extends Screen {
    private enum Tab { CREATE, EDIT, MANAGE }
    private Tab currentTab = Tab.CREATE;
    private EditBox nameField, modelField, animationField, textureField, behaviorField, hpField, speedField, sizeField;
    private Preset selectedPreset = null;
    private final int scrollOffset = 0;
    private final List<Button> presetButtons = new ArrayList<>();

    public PresetGui() {
        super(Component.literal("Custom Mobs Preset Manager"));
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = 30;

        this.addRenderableWidget(new CustomButton(centerX - 310, startY, buttonWidth, buttonHeight, Component.literal("Create"), button -> {
            currentTab = Tab.CREATE;
            updateFieldVisibility();
        }));
        this.addRenderableWidget(new CustomButton(centerX - 100, startY, buttonWidth, buttonHeight, Component.literal("Edit"), button -> {
            currentTab = Tab.EDIT;
            updateFieldVisibility();
        }));
        this.addRenderableWidget(new CustomButton(centerX + 110, startY, buttonWidth, buttonHeight, Component.literal("Manage"), button -> {
            currentTab = Tab.MANAGE;
            updateFieldVisibility();
            updatePresetButtons();
        }));

        nameField = new EditBox(this.font, centerX - 100, startY + 40, 200, 20, Component.literal("Name"));
        modelField = new EditBox(this.font, centerX - 100, startY + 70, 200, 20, Component.literal("Model"));
        animationField = new EditBox(this.font, centerX - 100, startY + 100, 200, 20, Component.literal("Animation"));
        textureField = new EditBox(this.font, centerX - 100, startY + 130, 200, 20, Component.literal("Texture"));
        behaviorField = new EditBox(this.font, centerX - 100, startY + 160, 200, 20, Component.literal("Behavior"));
        hpField = new EditBox(this.font, centerX - 100, startY + 190, 200, 20, Component.literal("HP"));
        speedField = new EditBox(this.font, centerX - 100, startY + 220, 200, 20, Component.literal("Speed"));
        sizeField = new EditBox(this.font, centerX - 100, startY + 250, 200, 20, Component.literal("Size"));

        // Устанавливаем более понятные placeholder-ы
        nameField.setHint(Component.literal("Enter mob name (e.g., MyMob)"));
        modelField.setHint(Component.literal("Model name (e.g., spider)"));
        animationField.setHint(Component.literal("Animation name (e.g., walk)"));
        textureField.setHint(Component.literal("Texture name (e.g., blue_skin)"));
        behaviorField.setHint(Component.literal("Behavior (e.g., hostile, passive)"));
        hpField.setHint(Component.literal("Health points (e.g., 20)"));
        speedField.setHint(Component.literal("Movement speed (e.g., 0.25)"));
        sizeField.setHint(Component.literal("Mob size (e.g., 1.0)"));

        this.addRenderableWidget(nameField);
        this.addRenderableWidget(modelField);
        this.addRenderableWidget(animationField);
        this.addRenderableWidget(textureField);
        this.addRenderableWidget(behaviorField);
        this.addRenderableWidget(hpField);
        this.addRenderableWidget(speedField);
        this.addRenderableWidget(sizeField);

        this.addRenderableWidget(new CustomButton(centerX - 100, startY + 280, buttonWidth, buttonHeight, Component.literal("Save"), button -> savePreset()));

        updateFieldVisibility();
    }

    private void updateFieldVisibility() {
        boolean showFields = currentTab == Tab.CREATE || currentTab == Tab.EDIT;
        nameField.visible = showFields;
        modelField.visible = showFields;
        animationField.visible = showFields;
        textureField.visible = showFields;
        behaviorField.visible = showFields;
        hpField.visible = showFields;
        speedField.visible = showFields;
        sizeField.visible = showFields;

        presetButtons.forEach(this::removeWidget);
        presetButtons.clear();
        if (currentTab == Tab.MANAGE) {
            updatePresetButtons();
        }
    }

    private void updatePresetButtons() {
        List<Preset> presets = ClientPresetHandler.getPresets();
        int buttonHeight = 20;
        int visibleCount = 5;
        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + visibleCount, presets.size());
        int startY = 80;

        for (int i = startIndex; i < endIndex; i++) {
            Preset preset = presets.get(i);
            int y = startY + (i - startIndex) * (buttonHeight + 5);
            int buttonX = this.width / 2 - 150;
            Button selectButton = new CustomButton(buttonX + 120, y, 60, buttonHeight, Component.literal("Select"), button -> selectPreset(preset));
            Button deleteButton = new CustomButton(buttonX + 190, y, 60, buttonHeight, Component.literal("Delete"), button -> deletePreset(preset));
            Button spawnButton = new CustomButton(buttonX + 260, y, 60, buttonHeight, Component.literal("Spawn"), button -> spawnPreset(preset));
            presetButtons.add(selectButton);
            presetButtons.add(deleteButton);
            presetButtons.add(spawnButton);
            this.addRenderableWidget(selectButton);
            this.addRenderableWidget(deleteButton);
            this.addRenderableWidget(spawnButton);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int centerX = this.width / 2;
        int startY = 60;

        switch (currentTab) {
            case CREATE:
                guiGraphics.drawCenteredString(this.font, "Create New Preset", centerX, startY, 0xFFFFFF);
                break;
            case EDIT:
                guiGraphics.drawCenteredString(this.font, "Edit Preset", centerX, startY, 0xFFFFFF);
                if (selectedPreset != null) {
                    nameField.setValue(selectedPreset.getName());
                    modelField.setValue(selectedPreset.getModel());
                    animationField.setValue(selectedPreset.getAnimation());
                    textureField.setValue(selectedPreset.getTexture());
                    behaviorField.setValue(selectedPreset.getBehavior());
                    hpField.setValue(String.valueOf(selectedPreset.getHp()));
                    speedField.setValue(String.valueOf(selectedPreset.getSpeed()));
                    sizeField.setValue(String.valueOf(selectedPreset.getSize()));
                }
                break;
            case MANAGE:
                guiGraphics.drawCenteredString(this.font, "Manage Presets", centerX, startY, 0xFFFFFF);
                renderPresetList(guiGraphics, startY + 20);
                break;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderPresetList(GuiGraphics guiGraphics, int startY) {
        List<Preset> presets = ClientPresetHandler.getPresets();
        int buttonHeight = 20;
        int visibleCount = 5;
        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + visibleCount, presets.size());

        for (int i = startIndex; i < endIndex; i++) {
            Preset preset = presets.get(i);
            int y = startY + (i - startIndex) * (buttonHeight + 5);
            int buttonX = this.width / 2 - 150;
            guiGraphics.drawString(this.font, preset.getName(), buttonX, y + 5, 0xFFFFFF);
        }
    }

    private void selectPreset(Preset preset) {
        this.selectedPreset = preset;
        this.currentTab = Tab.EDIT;
        updateFieldVisibility();
    }

    private void deletePreset(Preset preset) {
        CustomMobsForge.CHANNEL.sendToServer(new PresetDeletePacket(preset.getName()));
    }

    private void spawnPreset(Preset preset) {
        if (this.minecraft != null && this.minecraft.player != null) {
            Vec3 position = this.minecraft.player.position().add(0, 1, 0);
            CustomMobsForge.CHANNEL.sendToServer(new SpawnMobPacket(preset.getName(), position));
        }
    }

    private void savePreset() {
        String model = addExtension(modelField.getValue(), ".geo.json");
        String animation = addExtension(animationField.getValue(), ".animation.json");
        String texture = addExtension(textureField.getValue(), ".png");

        ResourceLocation modelLocation = new ResourceLocation(CustomMobsForge.MOD_ID, model);
        ResourceLocation animationLocation = new ResourceLocation(CustomMobsForge.MOD_ID, animation);
        ResourceLocation textureLocation = new ResourceLocation(CustomMobsForge.MOD_ID, texture);

        if (!ResourceValidator.validateResources(modelLocation, animationLocation, textureLocation)) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("Invalid resources: model, animation, or texture not found."));
            }
            return;
        }

        try {
            int hp = Integer.parseInt(hpField.getValue());
            float speed = Float.parseFloat(speedField.getValue());
            float size = Float.parseFloat(sizeField.getValue());

            Preset preset = new Preset(
                    nameField.getValue(),
                    model,
                    animation,
                    texture,
                    behaviorField.getValue(),
                    hp,
                    speed,
                    size
            );
            CustomMobsForge.CHANNEL.sendToServer(new PresetSavePacket(preset));
        } catch (NumberFormatException e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("Invalid number format for HP, Speed, or Size."));
            }
        }
    }

    private String addExtension(String fileName, String extension) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }
        String baseName = fileName.endsWith(extension) ? fileName.substring(0, fileName.length() - extension.length()) : fileName;
        return baseName + extension;
    }
}

class CustomButton extends Button {
    private static final ResourceLocation BUTTON_NORMAL = new ResourceLocation("custommobsforge", "textures/gui/button_normal.png");
    private static final ResourceLocation BUTTON_PRESSED = new ResourceLocation("custommobsforge", "textures/gui/button_pressed.png");

    public CustomButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        CustomMobsForge.LOGGER.info("Creating CustomButton with normal texture: " + BUTTON_NORMAL + ", pressed texture: " + BUTTON_PRESSED);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ResourceLocation texture = this.isHoveredOrFocused() ? BUTTON_PRESSED : BUTTON_NORMAL;
        try {
            guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        } catch (Exception e) {
            CustomMobsForge.LOGGER.error("Failed to render button texture: " + texture, e);
        }
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
    }
}