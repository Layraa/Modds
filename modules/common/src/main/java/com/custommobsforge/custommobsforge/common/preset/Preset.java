package com.custommobsforge.custommobsforge.common.preset;

public class Preset {
    private String name;
    private String model;
    private String animation;
    private String texture;
    private String behavior;
    private int hp;
    private float speed;
    private float size;

    public Preset(String name, String model, String animation, String texture, String behavior, int hp, float speed, float size) {
        this.name = name;
        this.model = model;
        this.animation = animation;
        this.texture = texture;
        this.behavior = behavior;
        this.hp = hp;
        this.speed = speed;
        this.size = size;
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

    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setAnimation(String animation) { this.animation = animation; }
    public void setTexture(String texture) { this.texture = texture; }
    public void setBehavior(String behavior) { this.behavior = behavior; }
    public void setHp(int hp) { this.hp = hp; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setSize(float size) { this.size = size; }
}