package com.custommobsforge.custommobsforge.common.preset;

import net.minecraft.network.FriendlyByteBuf;
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
    private transient ResourceLocation modelLocation;
    private transient ResourceLocation animationLocation;
    private transient ResourceLocation textureLocation;
    private transient String resourceNamespace; // Для определения пространства имён ресурсов

    public Preset(String name, String model, String animation, String texture, String behavior, int hp, float speed, float size, String creator, String resourceNamespace) {
        this.name = name;
        this.model = model;
        this.animation = animation;
        this.texture = texture;
        this.behavior = behavior;
        this.hp = hp;
        this.speed = speed;
        this.size = size;
        this.creator = creator;
        this.resourceNamespace = resourceNamespace != null ? resourceNamespace : "custommobsforge_client"; // По умолчанию клиентский namespace
    }

    public Preset(String name, String model, String animation, String texture, String behavior, int hp, float speed, float size, String resourceNamespace) {
        this(name, model, animation, texture, behavior, hp, speed, size, null, resourceNamespace);
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
    public void setModel(String model) {
        this.model = model;
        this.modelLocation = null;
    }
    public void setAnimation(String animation) {
        this.animation = animation;
        this.animationLocation = null;
    }
    public void setTexture(String texture) {
        this.texture = texture;
        this.textureLocation = null;
    }
    public void setBehavior(String behavior) { this.behavior = behavior; }
    public void setHp(int hp) { this.hp = hp; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setSize(float size) { this.size = size; }
    public void setCreator(String creator) { this.creator = creator; }
    public void setResourceNamespace(String resourceNamespace) {
        this.resourceNamespace = resourceNamespace;
        this.modelLocation = null;
        this.animationLocation = null;
        this.textureLocation = null;
    }

    // Методы для получения путей ресурсов с кэшированием
    public ResourceLocation getModelLocation() {
        if (modelLocation == null) {
            modelLocation = new ResourceLocation(resourceNamespace, "geo/" + model + ".geo.json");
        }
        return modelLocation;
    }

    public ResourceLocation getAnimationLocation() {
        if (animationLocation == null) {
            animationLocation = new ResourceLocation(resourceNamespace, "animations/" + animation + ".animation.json");
        }
        return animationLocation;
    }

    public ResourceLocation getTextureLocation() {
        if (textureLocation == null) {
            textureLocation = new ResourceLocation(resourceNamespace, "textures/entity/" + texture + ".png");
        }
        return textureLocation;
    }

    // Методы для сериализации/десериализации
    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeUtf(model);
        buf.writeUtf(animation);
        buf.writeUtf(texture);
        buf.writeUtf(behavior);
        buf.writeInt(hp);
        buf.writeFloat(speed);
        buf.writeFloat(size);
        buf.writeUtf(creator != null ? creator : "");
        buf.writeUtf(resourceNamespace != null ? resourceNamespace : "custommobsforge_client");
    }

    public static Preset readFromBuf(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        String model = buf.readUtf();
        String animation = buf.readUtf();
        String texture = buf.readUtf();
        String behavior = buf.readUtf();
        int hp = buf.readInt();
        float speed = buf.readFloat();
        float size = buf.readFloat();
        String creator = buf.readUtf();
        String resourceNamespace = buf.readUtf();
        return new Preset(name, model, animation, texture, behavior, hp, speed, size, creator.isEmpty() ? null : creator, resourceNamespace);
    }
}