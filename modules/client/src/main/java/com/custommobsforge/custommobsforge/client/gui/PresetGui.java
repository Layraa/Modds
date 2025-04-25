package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.client.ClientCustomMobsForge;
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
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SMALL_BUTTON_WIDTH = 60;
    private static final int BUTTON_SPACING = 5;
    private static final int LIST_START_Y = 80;
    private static final int TAB_BUTTON_Y = 30;
    private static final int FIELD_X_OFFSET = -100;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int SELECT_BUTTON_OFFSET = 120;
    private static final int DELETE_BUTTON_OFFSET = 190;
    private static final int SPAWN_BUTTON_OFFSET = 260;
    private static final long ACTION_COOLDOWN = 500;

    private Tab currentTab = Tab.CREATE;
    private EditBox nameField, modelField, animationField, textureField, behaviorField, hpField, speedField, sizeField;
    private Preset selectedPreset = null;
    private int scrollOffset = 0;
    private int visibleCount = 0;
    private final List<PresetButtonSet> visibleButtonSets = new ArrayList<>();
    private CustomButton createButton, editButton, manageButton;
    private CustomButton scrollUpButton, scrollDownButton;
    private CustomButton saveButton;
    private int saveButtonY = 0;
    private long lastSaveTime = 0;
    private long lastSpawnTime = 0;

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
        if (Minecraft.getInstance().getConnection() != null) {
            CustomMobsForge.CHANNEL.sendToServer(new RequestPresetsPacket());
        }

        int centerX = this.width / 2;
        int startY = TAB_BUTTON_Y;

        createButton = new CustomButton(centerX - 310, startY, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Create"), button -> {
            currentTab = Tab.CREATE;
            updateFieldVisibility();
        }, Tab.CREATE, this);
        editButton = new CustomButton(centerX - 100, startY, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Edit"), button -> {
            currentTab = Tab.EDIT;
            updateFieldVisibility();
        }, Tab.EDIT, this);
        manageButton = new CustomButton(centerX + 110, startY, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Manage"), button -> {
            currentTab = Tab.MANAGE;
            updateFieldVisibility();
        }, Tab.MANAGE, this);

        this.addRenderableWidget(createButton);
        this.addRenderableWidget(editButton);
        this.addRenderableWidget(manageButton);

        saveButtonY = startY + 280;
        saveButton = new CustomButton(centerX - 100, saveButtonY, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Save"), button -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSaveTime >= ACTION_COOLDOWN) {
                savePreset();
                lastSaveTime = currentTime;
            }
        }, null, this);
        this.addRenderableWidget(saveButton);

        int totalWidth = SMALL_BUTTON_WIDTH + 10 + SMALL_BUTTON_WIDTH;
        int startX = (this.width - totalWidth) / 2;
        scrollUpButton = new CustomButton(startX, saveButtonY, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Up"), button -> {
            if (scrollOffset > 0) {
                scrollOffset--;
                updatePresetButtons();
            }
        }, null, this);
        scrollDownButton = new CustomButton(startX + 70, saveButtonY, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Down"), button -> {
            List<Preset> presets = ClientPresetHandler.getPresets();
            if (scrollOffset < presets.size() - visibleCount) {
                scrollOffset++;
                updatePresetButtons();
            }
        }, null, this);
        this.addRenderableWidget(scrollUpButton);
        this.addRenderableWidget(scrollDownButton);

        updateFieldVisibility();
        updateVisibleCount();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        updateVisibleCount();
        updateFieldVisibility();
    }

    private void updateVisibleCount() {
        int availableHeight = saveButtonY - LIST_START_Y - 10;
        visibleCount = availableHeight / (BUTTON_HEIGHT + BUTTON_SPACING);
        initializeVisibleButtons();
        updatePresetButtons();
    }

    private void initFields(int centerX, int startY) {
        clearFields();

        nameField = createEditBox(centerX + FIELD_X_OFFSET, startY + 40, Component.literal("Name"), "Enter mob name");
        modelField = createEditBox(centerX + FIELD_X_OFFSET, startY + 70, Component.literal("Model"), "Model name");
        animationField = createEditBox(centerX + FIELD_X_OFFSET, startY + 100, Component.literal("Animation"), "Animation name");
        textureField = createEditBox(centerX + FIELD_X_OFFSET, startY + 130, Component.literal("Texture"), "Texture name");
        behaviorField = createEditBox(centerX + FIELD_X_OFFSET, startY + 160, Component.literal("Behavior"), "Behavior (hostile/passive/neutral)");
        hpField = createEditBox(centerX + FIELD_X_OFFSET, startY + 190, Component.literal("HP"), "Health points");
        speedField = createEditBox(centerX + FIELD_X_OFFSET, startY + 220, Component.literal("Speed"), "Movement speed");
        sizeField = createEditBox(centerX + FIELD_X_OFFSET, startY + 250, Component.literal("Size"), "Mob size");

        addFields();
    }

    private EditBox createEditBox(int x, int y, Component title, String hint) {
        EditBox editBox = new EditBox(this.font, x, y, FIELD_WIDTH, FIELD_HEIGHT, title);
        editBox.setHint(Component.literal(hint));
        editBox.setEditable(true);
        editBox.setCanLoseFocus(true);
        return editBox;
    }

    private void addFields() {
        this.addRenderableWidget(nameField);
        this.addRenderableWidget(modelField);
        this.addRenderableWidget(animationField);
        this.addRenderableWidget(textureField);
        this.addRenderableWidget(behaviorField);
        this.addRenderableWidget(hpField);
        this.addRenderableWidget(speedField);
        this.addRenderableWidget(sizeField);
    }

    private void clearFields() {
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
            modelField = null;
            animationField = null;
            textureField = null;
            behaviorField = null;
            hpField = null;
            speedField = null;
            sizeField = null;
        }
    }

    private void updateFieldVisibility() {
        int centerX = this.width / 2;
        int startY = TAB_BUTTON_Y;
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
                nameField.setEditable(false);
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
            clearFields();
        }

        saveButton.visible = showFields;
        scrollUpButton.visible = currentTab == Tab.MANAGE;
        scrollDownButton.visible = currentTab == Tab.MANAGE;

        if (currentTab != Tab.MANAGE) {
            visibleButtonSets.forEach(set -> {
                set.selectButton.visible = false;
                set.deleteButton.visible = false;
                set.spawnButton.visible = false;
            });
        } else {
            updatePresetButtons();
            List<Preset> presets = ClientPresetHandler.getPresets();
            scrollUpButton.active = scrollOffset > 0;
            scrollDownButton.active = scrollOffset < presets.size() - visibleCount;
        }
    }

    private void initializeVisibleButtons() {
        visibleButtonSets.clear();
        for (int i = 0; i < visibleCount; i++) {
            CustomButton selectButton = new CustomButton(0, 0, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Select"), null, null, this);
            CustomButton deleteButton = new CustomButton(0, 0, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Delete"), null, null, this);
            CustomButton spawnButton = new CustomButton(0, 0, SMALL_BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Spawn"), null, null, this);
            PresetButtonSet set = new PresetButtonSet(selectButton, deleteButton, spawnButton);
            visibleButtonSets.add(set);
            this.addRenderableWidget(selectButton);
            this.addRenderableWidget(deleteButton);
            this.addRenderableWidget(spawnButton);
        }
    }

    private ScrollRange calculateScrollRange(List<Preset> presets) {
        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + visibleCount, presets.size());
        return new ScrollRange(startIndex, endIndex);
    }

    public void updatePresetButtons() {
        if (currentTab != Tab.MANAGE) {
            return;
        }

        List<Preset> presets = ClientPresetHandler.getPresets();
        ScrollRange range = calculateScrollRange(presets);
        int startIndex = range.startIndex;
        int endIndex = range.endIndex;

        for (int i = 0; i < visibleButtonSets.size(); i++) {
            PresetButtonSet set = visibleButtonSets.get(i);
            int presetIndex = startIndex + i;
            if (presetIndex < endIndex) {
                Preset preset = presets.get(presetIndex);
                int y = LIST_START_Y + i * (BUTTON_HEIGHT + BUTTON_SPACING);
                int buttonX = this.width / 2 - 150;

                set.selectButton.setX(buttonX + SELECT_BUTTON_OFFSET);
                set.selectButton.setY(y);
                set.deleteButton.setX(buttonX + DELETE_BUTTON_OFFSET);
                set.deleteButton.setY(y);
                set.spawnButton.setX(buttonX + SPAWN_BUTTON_OFFSET);
                set.spawnButton.setY(y);

                set.selectButton.setAction(button -> selectPreset(preset));
                set.deleteButton.setAction(button -> deletePreset(preset));
                set.spawnButton.setAction(button -> {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastSpawnTime >= ACTION_COOLDOWN) {
                        spawnPreset(preset);
                        lastSpawnTime = currentTime;
                    }
                });

                set.selectButton.visible = true;
                set.deleteButton.visible = true;
                set.spawnButton.visible = true;

                set.displayText = preset.getName() + " (by " + getCreatorName(preset) + ")";
            } else {
                set.selectButton.visible = false;
                set.deleteButton.visible = false;
                set.spawnButton.visible = false;
            }
        }

        scrollUpButton.active = scrollOffset > 0;
        scrollDownButton.active = scrollOffset < presets.size() - visibleCount;
    }

    private String getCreatorName(Preset preset) {
        String creatorName = "Unknown";
        if (preset.getCreator() != null && this.minecraft != null && this.minecraft.getConnection() != null) {
            try {
                net.minecraft.client.multiplayer.PlayerInfo playerInfo = this.minecraft.getConnection().getPlayerInfo(UUID.fromString(preset.getCreator()));
                if (playerInfo != null) {
                    creatorName = playerInfo.getProfile().getName();
                }
            } catch (IllegalArgumentException e) {
                CustomMobsForge.LOGGER.warn("Invalid UUID for creator: " + preset.getCreator());
            }
        }
        return creatorName;
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
                break;
            case MANAGE:
                guiGraphics.drawCenteredString(this.font, "Manage Presets", centerX, startY, 0xFFFFFF);
                renderPresetList(guiGraphics, startY + 20);
                break;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderPresetList(GuiGraphics guiGraphics, int startY) {
        if (currentTab != Tab.MANAGE) {
            return;
        }

        List<Preset> presets = ClientPresetHandler.getPresets();
        ScrollRange range = calculateScrollRange(presets);
        int startIndex = range.startIndex;
        int endIndex = range.endIndex;

        if (presets.isEmpty()) {
            guiGraphics.drawString(this.font, "No presets available", this.width / 2 - 150, startY + 5, 0xAAAAAA);
            return;
        }

        for (int i = 0; i < visibleButtonSets.size(); i++) {
            PresetButtonSet set = visibleButtonSets.get(i);
            if (set.displayText != null && startIndex + i < endIndex) {
                int y = startY + i * (BUTTON_HEIGHT + BUTTON_SPACING);
                guiGraphics.drawString(this.font, set.displayText, this.width / 2 - 150, y + 5, 0xFFFFFF);
            }
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
        CustomMobsForge.LOGGER.info("deletePreset called for preset: {}", preset.getName());
        CustomMobsForge.LOGGER.info("deletePreset call stack:", new Throwable());
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(Component.literal("Deleting preset: " + preset.getName()));
            ClientPresetHandler.removePreset(preset.getName());
            CustomMobsForge.CHANNEL.sendToServer(new PresetDeletePacket(preset.getName()));
            if (currentTab == Tab.MANAGE) {
                updatePresetButtons();
            }
        } else {
            CustomMobsForge.LOGGER.warn("Cannot delete preset: Minecraft or player is null");
        }
    }

    private void spawnPreset(Preset preset) {
        if (this.minecraft != null && this.minecraft.player != null) {
            Vec3 position = this.minecraft.player.position().add(0, 1, 0);
            this.minecraft.player.sendSystemMessage(Component.literal("Requesting to spawn preset: " + preset.getName()));
            CustomMobsForge.LOGGER.debug("Client sending SpawnMobPacket for preset: " + preset.getName() + " at position: (" + position.x + ", " + position.y + ", " + position.z + ")");
            CustomMobsForge.CHANNEL.sendToServer(new SpawnMobPacket(preset.getName(), position));
        } else {
            CustomMobsForge.LOGGER.error("Failed to send SpawnMobPacket: Minecraft or player is null");
        }
    }

    private void savePreset() {
        CustomMobsForge.LOGGER.info("savePreset called for tab: {}", currentTab);
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.getConnection() == null) {
            CustomMobsForge.LOGGER.warn("Cannot save preset: Not connected to server");
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("You must be connected to a server to save presets."));
            }
            return;
        }

        String name = nameField.getValue();
        String model = modelField.getValue();
        String animation = animationField.getValue();
        String texture = textureField.getValue();
        String behavior = behaviorField.getValue();
        String hp = hpField.getValue();
        String speed = speedField.getValue();
        String size = sizeField.getValue();

        CustomMobsForge.LOGGER.info("Preset data - Name: {}, Model: {}, Animation: {}, Texture: {}, Behavior: {}, HP: {}, Speed: {}, Size: {}",
                name, model, animation, texture, behavior, hp, speed, size);

        if (!ResourceValidator.validateResources(model, animation, texture)) {
            CustomMobsForge.LOGGER.warn("Resource validation failed for model: {}, animation: {}, texture: {}", model, animation, texture);
            this.minecraft.player.sendSystemMessage(Component.literal("Invalid resources: model, animation, or texture not found."));
            return;
        }

        boolean isEdit = (currentTab == Tab.EDIT && selectedPreset != null);
        if (!isEdit && ClientPresetHandler.getPresetsMap().containsKey(name)) {
            this.minecraft.player.sendSystemMessage(Component.literal("A preset with the name '" + name + "' already exists."));
            CustomMobsForge.LOGGER.info("Client blocked preset creation: preset {} already exists", name);
            return;
        }

        try {
            int hpValue = Integer.parseInt(hp);
            float speedValue = Float.parseFloat(speed);
            float sizeValue = Float.parseFloat(size);

            String creator = isEdit ? selectedPreset.getCreator() : this.minecraft.player.getUUID().toString();

            Preset preset = new Preset(
                    name,
                    model,
                    animation,
                    texture,
                    behavior,
                    hpValue,
                    speedValue,
                    sizeValue,
                    creator,
                    ClientCustomMobsForge.MOD_ID
            );

            CustomMobsForge.CHANNEL.sendToServer(new PresetSavePacket(preset, isEdit));
            this.minecraft.player.sendSystemMessage(Component.literal("Request sent to server to " + (isEdit ? "update" : "create") + " preset: " + preset.getName()));
            CustomMobsForge.LOGGER.info("Preset " + (isEdit ? "updated" : "created") + " by client: " + preset.getName());
        } catch (NumberFormatException e) {
            CustomMobsForge.LOGGER.warn("Invalid number format - HP: {}, Speed: {}, Size: {}", hp, speed, size);
            this.minecraft.player.sendSystemMessage(Component.literal("Invalid number format for HP, Speed, or Size."));
        } catch (Exception e) {
            CustomMobsForge.LOGGER.error("Unexpected error while saving preset: {}", e.getMessage(), e);
            this.minecraft.player.sendSystemMessage(Component.literal("An unexpected error occurred while saving the preset."));
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

        if (!clickedOnField && !super.mouseClicked(mouseX, mouseY, button)) {
            setFocused(null);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (currentTab == Tab.MANAGE) {
            List<Preset> presets = ClientPresetHandler.getPresets();
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
    public void onClose() {
        visibleButtonSets.clear();
        clearFields();
        super.onClose();
    }

    public Tab getCurrentTab() {
        return currentTab;
    }
}

class OnPressWrapper implements Button.OnPress {
    private Button.OnPress action;

    public OnPressWrapper(Button.OnPress initialAction) {
        this.action = initialAction;
    }

    public void setAction(Button.OnPress newAction) {
        this.action = newAction;
    }

    @Override
    public void onPress(Button button) {
        if (action != null) {
            action.onPress(button);
        }
    }
}

class CustomButton extends Button {
    private static final ResourceLocation BUTTON_NORMAL = new ResourceLocation(ClientCustomMobsForge.MOD_ID, "textures/gui/button_normal.png");
    private static final ResourceLocation BUTTON_PRESSED = new ResourceLocation(ClientCustomMobsForge.MOD_ID, "textures/gui/button_pressed.png");

    private final PresetGui.Tab associatedTab;
    private final PresetGui gui;

    public CustomButton(int x, int y, int width, int height, Component message, Button.OnPress onPress, PresetGui.Tab associatedTab, PresetGui gui) {
        super(x, y, width, height, message, new OnPressWrapper(onPress), DEFAULT_NARRATION);
        this.associatedTab = associatedTab;
        this.gui = gui;
        CustomMobsForge.LOGGER.debug("Creating CustomButton with normal texture: " + BUTTON_NORMAL + ", pressed texture: " + BUTTON_PRESSED);
    }

    public void setAction(Button.OnPress newAction) {
        ((OnPressWrapper) this.onPress).setAction(newAction);
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

class PresetButtonSet {
    CustomButton selectButton;
    CustomButton deleteButton;
    CustomButton spawnButton;
    String displayText;

    PresetButtonSet(CustomButton selectButton, CustomButton deleteButton, CustomButton spawnButton) {
        this.selectButton = selectButton;
        this.deleteButton = deleteButton;
        this.spawnButton = spawnButton;
    }
}

class ScrollRange {
    final int startIndex;
    final int endIndex;

    ScrollRange(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
}