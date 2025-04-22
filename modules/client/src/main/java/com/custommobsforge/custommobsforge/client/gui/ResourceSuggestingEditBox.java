package com.custommobsforge.custommobsforge.client.gui;

import com.custommobsforge.custommobsforge.common.network.NetworkHandler;
import com.custommobsforge.custommobsforge.common.network.ResourceListRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ResourceSuggestingEditBox extends EditBox {
    private List<String> suggestions = new ArrayList<>();
    private int selectedSuggestion = -1;
    private final String resourceType; // "model", "texture", "animation"
    private final Consumer<List<String>> suggestionUpdater;

    public ResourceSuggestingEditBox(Font font, int x, int y, int width, int height, Component message, String resourceType, Consumer<List<String>> suggestionUpdater) {
        super(font, x, y, width, height, message);
        this.resourceType = resourceType;
        this.suggestionUpdater = suggestionUpdater;
        requestSuggestions();
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
        this.selectedSuggestion = -1;
        suggestionUpdater.accept(suggestions);
    }

    private void requestSuggestions() {
        NetworkHandler.sendToServer(new ResourceListRequestPacket(resourceType));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258) { // Tab
            if (!suggestions.isEmpty()) {
                selectedSuggestion = (selectedSuggestion + 1) % suggestions.size();
                setValue(suggestions.get(selectedSuggestion));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (!suggestions.isEmpty() && isFocused()) {
            int y = getY() + getHeight() + 2;
            for (int i = 0; i < Math.min(suggestions.size(), 5); i++) {
                String suggestion = suggestions.get(i);
                int color = i == selectedSuggestion ? 0xFFFF00 : 0xFFFFFF;
                guiGraphics.fill(getX(), y, getX() + getWidth(), y + 10, 0x80000000);
                guiGraphics.drawString(Minecraft.getInstance().font, suggestion, getX() + 2, y + 1, color);
                y += 10;
            }
        }
    }
}