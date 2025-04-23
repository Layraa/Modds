package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.client.ClientPresetHandler;
import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.client.render.ResourceValidator;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import com.custommobsforge.custommobsforge.common.preset.PresetDeletePacket;
import com.custommobsforge.custommobsforge.common.preset.PresetSavePacket;
import com.custommobsforge.custommobsforge.common.preset.RequestPresetsPacket;
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
import java.util.UUID;

public class PresetGui extends Screen {
    public enum Tab { CREATE, EDIT, MANAGE }
    private Tab currentTab = Tab.CREATE;
    private EditBox nameField, modelField, animationField, textureField, behaviorField, hpField, speedField, sizeField;
    private Preset selectedPreset = null;
    private int scrollOffset = 0;
    private final List<Button> presetButtons = new ArrayList<>();
    private CustomButton createButton, editButton, manageButton;
    private CustomButton scrollUpButton, scrollDownButton;
    private CustomButton saveButton;
    private int saveButtonY = 0;

    public PresetGui() {
        super(Component.literal("Custom Mobs Preset Manager"));
        if (Minecraft.getInstance().getConnection() == null) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("You must be connected to a server to use this GUI"));
            }
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    protected void init() {
        // Запрашиваем пресеты у сервера при открытии GUI
        if (Minecraft.getInstance().getConnection() != null) {
            CustomMobsForge.CHANNEL.sendToServer(new RequestPresetsPacket());
        }

        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int startY = 30;

        createButton = new CustomButton(centerX - 310, startY, buttonWidth, buttonHeight, Component.literal("Create"), button -> {
            currentTab = Tab.CREATE;
            updateFieldVisibility();
        }, Tab.CREATE, this);
        editButton = new CustomButton(centerX - 100, startY, buttonWidth, buttonHeight, Component.literal("Edit"), button -> {
            currentTab = Tab.EDIT;
            updateFieldVisibility();
        }, Tab.EDIT, this);
        manageButton = new CustomButton(centerX + 110, startY, buttonWidth, buttonHeight, Component.literal("Manage"), button -> {
            currentTab = Tab.MANAGE;
            updateFieldVisibility();
        }, Tab.MANAGE, this);

        this.addRenderableWidget(createButton);
        this.addRenderableWidget(editButton);
        this.addRenderableWidget(manageButton);

        saveButtonY = startY + 280;
        saveButton = new CustomButton(centerX - 100, saveButtonY, buttonWidth, buttonHeight, Component.literal("Save"), button -> savePreset(), null, this);
        this.addRenderableWidget(saveButton);

        updateFieldVisibility();
    }

    private void initFields(int centerX, int startY) {
        if (nameField != null) {
            this.removeWidget(nameField);
            this.removeWidget(modelField);
            this.removeWidget(animationField);
            this.removeWidget(textureField);
            this.removeWidget(behaviorField);
            this.removeWidget(hpField);
            this.removeWidget(speedField);
            this.removeWidget(sizeField);
        }

        nameField = new EditBox(this.font, centerX - 100, startY + 40, 200, 20, Component.literal("Name"));
        modelField = new EditBox(this.font, centerX - 100, startY + 70, 200, 20, Component.literal("Model"));
        animationField = new EditBox(this.font, centerX - 100, startY + 100, 200, 20, Component.literal("Animation"));
        textureField = new EditBox(this.font, centerX - 100, startY + 130, 200, 20, Component.literal("Texture"));
        behaviorField = new EditBox(this.font, centerX - 100, startY + 160, 200, 20, Component.literal("Behavior"));
        hpField = new EditBox(this.font, centerX - 100, startY + 190, 200, 20, Component.literal("HP"));
        speedField = new EditBox(this.font, centerX - 100, startY + 220, 200, 20, Component.literal("Speed"));
        sizeField = new EditBox(this.font, centerX - 100, startY + 250, 200, 20, Component.literal("Size"));

        nameField.setHint(Component.literal("Enter mob name"));
        modelField.setHint(Component.literal("Model name (e.g., custom_mob)"));
        animationField.setHint(Component.literal("Animation name (e.g., custom_mob)"));
        textureField.setHint(Component.literal("Texture name (e.g., custom_mob)"));
        behaviorField.setHint(Component.literal("Behavior (hostile/passive/neutral)"));
        hpField.setHint(Component.literal("Health points"));
        speedField.setHint(Component.literal("Movement speed"));
        sizeField.setHint(Component.literal("Mob size"));

        nameField.setEditable(true);
        modelField.setEditable(true);
        animationField.setEditable(true);
        textureField.setEditable(true);
        behaviorField.setEditable(true);
        hpField.setEditable(true);
        speedField.setEditable(true);
        sizeField.setEditable(true);

        nameField.setCanLoseFocus(true);
        modelField.setCanLoseFocus(true);
        animationField.setCanLoseFocus(true);
        textureField.setCanLoseFocus(true);
        behaviorField.setCanLoseFocus(true);
        hpField.setCanLoseFocus(true);
        speedField.setCanLoseFocus(true);
        sizeField.setCanLoseFocus(true);

        nameField.setResponder(text -> CustomMobsForge.LOGGER.info("Name field updated: " + text));
        modelField.setResponder(text -> CustomMobsForge.LOGGER.info("Model field updated: " + text));
        animationField.setResponder(text -> CustomMobsForge.LOGGER.info("Animation field updated: " + text));
        textureField.setResponder(text -> CustomMobsForge.LOGGER.info("Texture field updated: " + text));
        behaviorField.setResponder(text -> CustomMobsForge.LOGGER.info("Behavior field updated: " + text));
        hpField.setResponder(text -> CustomMobsForge.LOGGER.info("HP field updated: " + text));
        speedField.setResponder(text -> CustomMobsForge.LOGGER.info("Speed field updated: " + text));
        sizeField.setResponder(text -> CustomMobsForge.LOGGER.info("Size field updated: " + text));

        this.addRenderableWidget(nameField);
        this.addRenderableWidget(modelField);
        this.addRenderableWidget(animationField);
        this.addRenderableWidget(textureField);
        this.addRenderableWidget(behaviorField);
        this.addRenderableWidget(hpField);
        this.addRenderableWidget(speedField);
        this.addRenderableWidget(sizeField);
    }

    private void updateFieldVisibility() {
        int centerX = this.width / 2;
        int startY = 30;
        boolean showFields = currentTab == Tab.CREATE || currentTab == Tab.EDIT;

        if (showFields) {
            initFields(centerX, startY);
            if (currentTab == Tab.CREATE) {
                nameField.setValue("");
                modelField.setValue("");
                animationField.setValue("");
                textureField.setValue("");
                behaviorField.setValue("");
                hpField.setValue("");
                speedField.setValue("");
                sizeField.setValue("");
            } else if (currentTab == Tab.EDIT && selectedPreset != null) {
                nameField.setValue(selectedPreset.getName());
                nameField.setEditable(false); // Запрещаем редактировать имя в режиме "Edit"
                modelField.setValue(selectedPreset.getModel());
                animationField.setValue(selectedPreset.getAnimation());
                textureField.setValue(selectedPreset.getTexture());
                behaviorField.setValue(selectedPreset.getBehavior());
                hpField.setValue(String.valueOf(selectedPreset.getHp()));
                speedField.setValue(String.valueOf(selectedPreset.getSpeed()));
                sizeField.setValue(String.valueOf(selectedPreset.getSize()));
                Minecraft.getInstance().execute(() -> {
                    setFocused(nameField);
                    nameField.setFocused(true);
                });
            }
        } else {
            if (nameField != null) {
                this.removeWidget(nameField);
                this.removeWidget(modelField);
                this.removeWidget(animationField);
                this.removeWidget(textureField);
                this.removeWidget(behaviorField);
                this.removeWidget(hpField);
                this.removeWidget(speedField);
                this.removeWidget(sizeField);
                nameField = null;
            }
        }

        saveButton.visible = showFields;

        presetButtons.forEach(this::removeWidget);
        presetButtons.clear();

        if (scrollUpButton != null) {
            this.removeWidget(scrollUpButton);
            scrollUpButton = null;
        }
        if (scrollDownButton != null) {
            this.removeWidget(scrollDownButton);
            scrollDownButton = null;
        }

        if (currentTab == Tab.MANAGE) {
            updatePresetButtons();

            int buttonHeight = 20;
            int totalWidth = 60 + 10 + 60;
            int startX = (this.width - totalWidth) / 2;
            scrollUpButton = new CustomButton(startX, saveButtonY, 60, buttonHeight, Component.literal("Up"), button -> {
                if (scrollOffset > 0) {
                    scrollOffset--;
                    updatePresetButtons();
                }
            }, null, this);
            scrollDownButton = new CustomButton(startX + 70, saveButtonY, 60, buttonHeight, Component.literal("Down"), button -> {
                List<Preset> presets = ClientPresetHandler.getPresets();
                int visibleCount = calculateVisibleCount();
                if (scrollOffset < presets.size() - visibleCount) {
                    scrollOffset++;
                    updatePresetButtons();
                }
            }, null, this);

            List<Preset> presets = ClientPresetHandler.getPresets();
            int visibleCount = calculateVisibleCount();
            scrollUpButton.active = scrollOffset > 0;
            scrollDownButton.active = scrollOffset < presets.size() - visibleCount;

            this.addRenderableWidget(scrollUpButton);
            this.addRenderableWidget(scrollDownButton);
        }
    }

    private int calculateVisibleCount() {
        int buttonHeight = 20;
        int buttonSpacing = 5;
        int startY = 80;
        int availableHeight = saveButtonY - startY - 10;
        return availableHeight / (buttonHeight + buttonSpacing);
    }

    public void updatePresetButtons() {
        if (currentTab != Tab.MANAGE) {
            return;
        }

        List<Preset> presets = ClientPresetHandler.getPresets();
        int buttonHeight = 20;
        int buttonSpacing = 5;
        int startY = 80;

        int visibleCount = calculateVisibleCount();

        presetButtons.forEach(this::removeWidget);
        presetButtons.clear();

        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + visibleCount, presets.size());

        for (int i = startIndex; i < endIndex; i++) {
            Preset preset = presets.get(i);
            int y = startY + (i - startIndex) * (buttonHeight + buttonSpacing);
            int buttonX = this.width / 2 - 150;
            Button selectButton = new CustomButton(buttonX + 120, y, 60, buttonHeight, Component.literal("Select"), button -> selectPreset(preset), null, this);
            Button deleteButton = new CustomButton(buttonX + 190, y, 60, buttonHeight, Component.literal("Delete"), button -> deletePreset(preset), null, this);
            Button spawnButton = new CustomButton(buttonX + 260, y, 60, buttonHeight, Component.literal("Spawn"), button -> spawnPreset(preset), null, this);
            presetButtons.add(selectButton);
            presetButtons.add(deleteButton);
            presetButtons.add(spawnButton);
            this.addRenderableWidget(selectButton);
            this.addRenderableWidget(deleteButton);
            this.addRenderableWidget(spawnButton);
        }

        if (scrollUpButton != null) {
            scrollUpButton.active = scrollOffset > 0;
        }
        if (scrollDownButton != null) {
            scrollDownButton.active = scrollOffset < presets.size() - visibleCount;
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        if (currentTab != Tab.MANAGE && !presetButtons.isEmpty()) {
            presetButtons.forEach(this::removeWidget);
            presetButtons.clear();
        }

        int centerX = this.width / 2;
        int startY = 60;

        switch (currentTab) {
            case CREATE:
                guiGraphics.drawCenteredString(this.font, "Create New Preset", centerX, startY, 0xFFFFFF);
                break;
            case EDIT:
                guiGraphics.drawCenteredString(this.font, "Edit Preset", centerX, startY, 0xFFFFFF);
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
        int buttonSpacing = 5;
        int visibleCount = calculateVisibleCount();
        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + visibleCount, presets.size());

        if (presets.isEmpty()) {
            guiGraphics.drawString(this.font, "No presets available", this.width / 2 - 150, startY + 5, 0xAAAAAA);
            return;
        }

        for (int i = startIndex; i < endIndex; i++) {
            Preset preset = presets.get(i);
            int y = startY + (i - startIndex) * (buttonHeight + buttonSpacing);
            int buttonX = this.width / 2 - 150;
            String creatorName = "Unknown";
            if (preset.getCreator() != null && this.minecraft != null && this.minecraft.getConnection() != null) {
                net.minecraft.client.multiplayer.PlayerInfo playerInfo = this.minecraft.getConnection().getPlayerInfo(UUID.fromString(preset.getCreator()));
                if (playerInfo != null) {
                    creatorName = playerInfo.getProfile().getName();
                }
            }
            guiGraphics.drawString(this.font, preset.getName() + " (by " + creatorName + ")", buttonX, y + 5, 0xFFFFFF);
        }

        int totalPresets = presets.size();
        String scrollText = "Showing " + (startIndex + 1) + "-" + Math.min(endIndex, totalPresets) + " of " + totalPresets;
        guiGraphics.drawCenteredString(this.font, scrollText, this.width / 2, saveButtonY - 15, 0x55FF55);
    }

    private void selectPreset(Preset preset) {
        this.selectedPreset = preset;
        this.currentTab = Tab.EDIT;
        updateFieldVisibility();
    }

    private void deletePreset(Preset preset) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(Component.literal("Deleting preset: " + preset.getName()));
            ClientPresetHandler.removePreset(preset.getName());
            CustomMobsForge.CHANNEL.sendToServer(new PresetDeletePacket(preset.getName()));
            if (currentTab == Tab.MANAGE) {
                updatePresetButtons();
            }
        }
    }

    private void spawnPreset(Preset preset) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(Component.literal("Requesting to spawn preset: " + preset.getName()));
            Vec3 position = this.minecraft.player.position().add(0, 1, 0);
            CustomMobsForge.CHANNEL.sendToServer(new SpawnMobPacket(preset.getName(), position));
        }
    }

    private void savePreset() {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.getConnection() == null) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("You must be connected to a server to save presets."));
            }
            return;
        }

        String name = nameField.getValue();
        String model = modelField.getValue();
        String animation = animationField.getValue();
        String texture = textureField.getValue();

        if (!ResourceValidator.validateResources(model, animation, texture)) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("Invalid resources: model, animation, or texture not found."));
            }
            return;
        }

        try {
            int hp = Integer.parseInt(hpField.getValue());
            float speed = Float.parseFloat(speedField.getValue());
            float size = Float.parseFloat(sizeField.getValue());

            boolean isEdit = (currentTab == Tab.EDIT && selectedPreset != null);
            String creator = isEdit ? selectedPreset.getCreator() : this.minecraft.player.getUUID().toString();

            Preset preset = new Preset(
                    name,
                    model,
                    animation,
                    texture,
                    behaviorField.getValue(),
                    hp,
                    speed,
                    size,
                    creator
            );

            CustomMobsForge.CHANNEL.sendToServer(new PresetSavePacket(preset, isEdit));
            if (isEdit) {
                ClientPresetHandler.removePreset(selectedPreset.getName());
                ClientPresetHandler.addPreset(preset);
                selectedPreset = preset;
                this.minecraft.player.sendSystemMessage(Component.literal("Preset updated: " + preset.getName()));
            } else {
                ClientPresetHandler.addPreset(preset);
                this.minecraft.player.sendSystemMessage(Component.literal("Preset created: " + preset.getName()));
            }
        } catch (NumberFormatException e) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("Invalid number format for HP, Speed, or Size."));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clickedOnField = nameField != null && (
                nameField.mouseClicked(mouseX, mouseY, button) ||
                        modelField.mouseClicked(mouseX, mouseY, button) ||
                        animationField.mouseClicked(mouseX, mouseY, button) ||
                        textureField.mouseClicked(mouseX, mouseY, button) ||
                        behaviorField.mouseClicked(mouseX, mouseY, button) ||
                        hpField.mouseClicked(mouseX, mouseY, button) ||
                        speedField.mouseClicked(mouseX, mouseY, button) ||
                        sizeField.mouseClicked(mouseX, mouseY, button)
        );

        if (!clickedOnField) {
            setFocused(null);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (currentTab == Tab.MANAGE) {
            List<Preset> presets = ClientPresetHandler.getPresets();
            int visibleCount = calculateVisibleCount();
            if (delta > 0 && scrollOffset > 0) {
                scrollOffset--;
                updatePresetButtons();
            } else if (delta < 0 && scrollOffset < presets.size() - visibleCount) {
                scrollOffset++;
                updatePresetButtons();
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        CustomMobsForge.LOGGER.info("Key pressed: keyCode=" + keyCode + ", scanCode=" + scanCode + ", modifiers=" + modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        CustomMobsForge.LOGGER.info("Char typed: codePoint=" + codePoint + ", modifiers=" + modifiers);
        return super.charTyped(codePoint, modifiers);
    }

    public Tab getCurrentTab() {
        return currentTab;
    }
}

class CustomButton extends Button {
    private static final ResourceLocation BUTTON_NORMAL = new ResourceLocation("custommobsforge", "textures/gui/button_normal.png");
    private static final ResourceLocation BUTTON_PRESSED = new ResourceLocation("custommobsforge", "textures/gui/button_pressed.png");

    private final PresetGui.Tab associatedTab;
    private final PresetGui gui;

    public CustomButton(int x, int y, int width, int height, Component message, OnPress onPress, PresetGui.Tab associatedTab, PresetGui gui) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.associatedTab = associatedTab;
        this.gui = gui;
        CustomMobsForge.LOGGER.info("Creating CustomButton with normal texture: " + BUTTON_NORMAL + ", pressed texture: " + BUTTON_PRESSED);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ResourceLocation texture = (associatedTab != null && gui.getCurrentTab() == associatedTab) ? BUTTON_PRESSED : BUTTON_NORMAL;
        try {
            guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        } catch (Exception e) {
            CustomMobsForge.LOGGER.error("Failed to render button texture: " + texture, e);
        }
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
    }
}