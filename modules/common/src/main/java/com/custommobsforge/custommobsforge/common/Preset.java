package com.custommobsforge.custommobsforge.common;

public class Preset {
    private final String name;
    private final float health;
    private final double speed;
    private final String modelName;
    private final String textureName;
    private final String animationName;

    public Preset(String name, float health, double speed, String modelName, String textureName, String animationName) {
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.modelName = modelName;
        this.textureName = textureName;
        this.animationName = animationName;
    }

    public String getName() {
        return name;
    }

    public float health() {
        return health;
    }

    public double speed() {
        return speed;
    }

    public String modelName() {
        return modelName;
    }

    public String textureName() {
        return textureName;
    }

    public String animationName() {
        return animationName;
    }
}