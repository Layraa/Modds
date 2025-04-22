package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.client.ClientNetworkHandler; // Новый импорт
import com.custommobsforge.custommobsforge.common.PresetManager;
import com.custommobsforge.custommobsforge.common.network.ResourceListRequestPacket;
import com.custommobsforge.custommobsforge.common.network.ValidateResourcesPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class PresetEditorScreen extends Screen {
    private final boolean createMode;
    private String presetName;
    private EditBox nameField;
    private EditBox healthField;
    private EditBox speedField;
    private EditBox modelField;
    private EditBox textureField;
    private EditBox animationField;

    public PresetEditorScreen(boolean createMode) {
        super(Component.literal(createMode ? "Create Preset" : "Edit Preset"));
        this.createMode = createMode;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;

        this.nameField = new EditBox(this.font, centerX - 100, startY, 200, 20, Component.literal("Preset Name"));
        this.nameField.setValue(createMode ? "" : PresetManager.getInstance().getPreset(presetName).getName());
        this.addRenderableWidget(this.nameField);

        this.healthField = new EditBox(this.font, centerX - 100, startY + 30, 200, 20, Component.literal("Health"));
        this.healthField.setValue(createMode ? "20.0" : String.valueOf(PresetManager.getInstance().getPreset(presetName).health()));
        this.addRenderableWidget(this.healthField);

        this.speedField = new EditBox(this.font, centerX - 100, startY + 60, 200, 20, Component.literal("Speed"));
        this.speedField.setValue(createMode ? "0.2" : String.valueOf(PresetManager.getInstance().getPreset(presetName).speed()));
        this.addRenderableWidget(this.speedField);

        this.modelField = new EditBox(this.font, centerX - 100, startY + 90, 200, 20, Component.literal("Model"));
        this.modelField.setValue(createMode ? "" : PresetManager.getInstance().getPreset(presetName).modelName());
        this.addRenderableWidget(this.modelField);

        this.textureField = new EditBox(this.font, centerX - 100, startY + 120, 200, 20, Component.literal("Texture"));
        this.textureField.setValue(createMode ? "" : PresetManager.getInstance().getPreset(presetName).textureName());
        this.addRenderableWidget(this.textureField);

        this.animationField = new EditBox(this.font, centerX - 100, startY + 150, 200, 20, Component.literal("Animation"));
        this.animationField.setValue(createMode ? "" : PresetManager.getInstance().getPreset(presetName).animationName());
        this.addRenderableWidget(this.animationField);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
            try {
                String name = this.nameField.getValue();
                float health = Float.parseFloat(this.healthField.getValue());
                double speed = Double.parseDouble(this.speedField.getValue());
                String model = this.modelField.getValue();
                String texture = this.textureField.getValue();
                String animation = this.animationField.getValue();

                ClientNetworkHandler.sendToServer(new ValidateResourcesPacket(createMode, name, health, speed, model, texture, animation)); // Обновлено
            } catch (NumberFormatException e) {
                this.minecraft.setScreen(new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.literal("Invalid health or speed value")));
            }
        }).pos(centerX - 50, startY + 180).size(100, 20).build());

        ClientNetworkHandler.sendToServer(new ResourceListRequestPacket("model")); // Обновлено
        ClientNetworkHandler.sendToServer(new ResourceListRequestPacket("texture")); // Обновлено
        ClientNetworkHandler.sendToServer(new ResourceListRequestPacket("animation")); // Обновлено
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Name:", this.width / 2 - 110, this.height / 4, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Health:", this.width / 2 - 110, this.height / 4 + 30, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Speed:", this.width / 2 - 110, this.height / 4 + 60, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Model:", this.width / 2 - 110, this.height / 4 + 90, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Texture:", this.width / 2 - 110, this.height / 4 + 120, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Animation:", this.width / 2 - 110, this.height / 4 + 150, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
        if (this.nameField != null) {
            this.nameField.setValue(presetName);
        }
    }

    public void handleResourceList(String type, List<String> resources) {
        // Здесь можно реализовать автодополнение для полей ввода
    }

    public void handleResourceValidation(boolean valid, boolean createMode, String name, float health, double speed, String model, String texture, String animation) {
        if (valid) {
            this.minecraft.setScreen(new PresetManagerScreen());
        } else {
            this.minecraft.setScreen(new net.minecraft.client.gui.screens.GenericDirtMessageScreen(Component.literal("Invalid resources. Please check the model, texture, and animation files.")));
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new PresetManagerScreen());
    }
}