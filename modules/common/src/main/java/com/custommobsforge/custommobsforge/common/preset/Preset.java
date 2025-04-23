package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.resources.ResourceLocation;

public class Preset {
    private String name;
    private String model;
    private String animation;
    private String texture;
    private String behavior;
    private int hp;
    private float speed;
    private float size;
    private String creator;

    public Preset(String name, String model, String animation, String texture, String behavior, int hp, float speed, float size, String creator) {
        this.name = name;
        this.model = model;
        this.animation = animation;
        this.texture = texture;
        this.behavior = behavior;
        this.hp = hp;
        this.speed = speed;
        this.size = size;
        this.creator = creator;
    }

    public Preset(String name, String model, String animation, String texture, String behavior, int hp, float speed, float size) {
        this(name, model, animation, texture, behavior, hp, speed, size, null);
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getAnimation() { return animation; }
    public String getTexture() { return texture; }
    public String getBehavior() { return behavior; }
    public int getHp() { return hp; }
    public float getSpeed() { return speed; }
    public float getSize() { return size; }
    public String getCreator() { return creator; }

    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setAnimation(String animation) { this.animation = animation; }
    public void setTexture(String texture) { this.texture = texture; }
    public void setBehavior(String behavior) { this.behavior = behavior; }
    public void setHp(int hp) { this.hp = hp; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setSize(float size) { this.size = size; }
    public void setCreator(String creator) { this.creator = creator; }

    // Методы для получения путей ресурсов
    public ResourceLocation getModelLocation() {
        return new ResourceLocation("custommobsforge", "geo/" + model + ".geo.json");
    }

    public ResourceLocation getAnimationLocation() {
        return new ResourceLocation("custommobsforge", "animations/" + animation + ".animation.json");
    }

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation("custommobsforge", "textures/entity/" + texture + ".png");
    }
}