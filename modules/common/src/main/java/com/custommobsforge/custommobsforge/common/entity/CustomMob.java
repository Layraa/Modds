package com.custommobsforge.custommobsforge.common.entity;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
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
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CustomMob extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private String modelName;
    private String textureName;
    private String animationName;
    private float sizeWidth = 0.6F;
    private float sizeHeight = 1.95F;

    public CustomMob(EntityType<? extends PathfinderMob> entityType, Level level) {
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

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName != null ? modelName : "test_model";
    }

    public void setTextureName(String textureName) {
        this.textureName = textureName;
    }

    public String getTextureName() {
        return textureName != null ? textureName : "test_texture";
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public String getAnimationName() {
        return animationName != null ? animationName : "test_animation";
    }

    public void setHealthValue(float health) {
        if (this.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
            this.setHealth(health);
        }
    }

    public void setSpeedValue(double speed) {
        if (this.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        }
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
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationPredicate));
    }

    private <E extends GeoEntity> AnimationState<E> animationPredicate(AnimationState<E> state) {
        state.getController().setAnimation(RawAnimation.begin().thenPlay("animation." + getAnimationName() + ".walk"));
        return state;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}