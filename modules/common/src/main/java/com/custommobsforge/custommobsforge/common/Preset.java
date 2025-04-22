package com.custommobsforge.custommobsforge.common;

public record Preset(String name, float health, double speed, float sizeWidth, float sizeHeight, String modelName, String textureName, String animationName) {
    public Preset {
        if (health <= 0) throw new IllegalArgumentException("Health must be positive");
        if (speed < 0) throw new IllegalArgumentException("Speed must be non-negative");
        if (sizeWidth <= 0) throw new IllegalArgumentException("Size width must be positive");
        if (sizeHeight <= 0) throw new IllegalArgumentException("Size height must be positive");
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name must not be empty");
        if (modelName == null) throw new IllegalArgumentException("Model name must not be null");
        if (textureName == null) throw new IllegalArgumentException("Texture name must not be null");
        if (animationName == null) throw new IllegalArgumentException("Animation name must not be null");
    }
}