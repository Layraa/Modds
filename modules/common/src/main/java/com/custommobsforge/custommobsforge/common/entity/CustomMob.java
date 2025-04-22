package com.custommobsforge.custommobsforge.common.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CustomMob extends Mob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private String presetName;
    private String modelName;
    private String textureName;
    private String animationName;
    private float sizeWidth = 0.6F; // Значения по умолчанию
    private float sizeHeight = 1.95F;

    public CustomMob(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.refreshDimensions();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setTextureName(String textureName) {
        this.textureName = textureName;
    }

    public String getTextureName() {
        return textureName;
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public String getAnimationName() {
        return animationName;
    }

    public void setHealthValue(float health) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        this.setHealth(health);
    }

    public void setSpeedValue(double speed) {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    public void setSize(float width, float height) {
        this.sizeWidth = width;
        this.sizeHeight = height;
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(sizeWidth, sizeHeight);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenPlay("animation." + (animationName != null ? animationName : "test_model") + ".walk"));
            return state.setAndContinue(state.getController().getAnimationState());
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}