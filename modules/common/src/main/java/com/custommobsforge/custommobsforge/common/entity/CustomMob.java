package com.custommobsforge.custommobsforge.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CustomMob extends Mob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private String modelName;
    private String textureName;
    private String animationName;
    private float health;
    private double speed;

    protected CustomMob(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5);
    }

    // Методы для получения имени модели, текстуры и анимации
    public String getModelName() {
        return modelName != null ? modelName : "zombie"; // Значение по умолчанию, если не задано
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getTextureName() {
        return textureName != null ? textureName : "zombie"; // Значение по умолчанию
    }

    public void setTextureName(String textureName) {
        this.textureName = textureName;
    }

    public String getAnimationName() {
        return animationName != null ? animationName : "zombie"; // Значение по умолчанию
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public float getHealthValue() {
        return health;
    }

    public void setHealthValue(float health) {
        this.health = health;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        this.setHealth(health);
    }

    public double getSpeedValue() {
        return speed;
    }

    public void setSpeedValue(double speed) {
        this.speed = speed;
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    // Реализация GeoEntity
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Здесь можно добавить контроллеры анимаций, если нужно
        // Например:
        // controllers.add(new AnimationController<>(this, "controllerName", 0, this::predicate));
    }

    @Override
    public double getTick(Object o) {
        return getAge();
    }
}