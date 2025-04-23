package com.custommobsforge.custommobsforge.common.entity;

import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CustomMob extends PathfinderMob implements GeoAnimatable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Preset preset;
    private float scale = 1.0F;

    public CustomMob(EntityType<? extends PathfinderMob> type, Level level, Preset preset) {
        super(type, level);
        this.preset = preset;
        this.scale = preset.getSize();
        // Инициализируем атрибуты в конструкторе
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(preset.getHp());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(preset.getSpeed());
        this.setHealth(this.getMaxHealth());
        this.refreshDimensions();
    }

    public CustomMob(EntityType<? extends PathfinderMob> type, Level level) {
        this(type, level, new Preset("default", "example", "example", "example", "neutral", 20, 0.3F, 1.0F));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        switch (preset.getBehavior().toLowerCase()) {
            case "aggressive":
                this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
                this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
                break;
            case "passive":
                this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
                break;
            case "neutral":
            default:
                this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
                this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
                break;
        }
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            try {
                if (preset.getBehavior().equalsIgnoreCase("aggressive") && this.getTarget() != null) {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("animation.custommob.attack"));
                }
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.custommob.walk"));
            } catch (Exception e) {
                return state.setAndContinue(RawAnimation.begin());
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object animatable) {
        return this.tickCount;
    }

    public Preset getPreset() {
        return preset;
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.refreshDimensions();
    }

    public float getScale() {
        return this.scale;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions baseDimensions = super.getDimensions(pose);
        return baseDimensions.scale(this.scale);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        // Удаляем установку атрибутов здесь, так как она теперь в конструкторе
    }
}