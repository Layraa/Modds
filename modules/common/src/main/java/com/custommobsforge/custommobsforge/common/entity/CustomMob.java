package com.custommobsforge.custommobsforge.common.entity;

import com.custommobsforge.custommobsforge.common.CustomMobsForge;
import com.custommobsforge.custommobsforge.common.preset.Preset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class CustomMob extends PathfinderMob implements GeoAnimatable {
    public static final EntityDataAccessor<String> PRESET_NAME_ACCESSOR = SynchedEntityData.defineId(CustomMob.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Preset preset;
    private float scale;
    private String behaviorLowerCase;

    public CustomMob(EntityType<? extends PathfinderMob> type, Level level, Preset preset) {
        super(type, level);
        this.preset = preset;
        this.scale = preset != null ? preset.getSize() : 1.0F;
        if (preset != null) {
            this.entityData.set(PRESET_NAME_ACCESSOR, preset.getName());
            this.behaviorLowerCase = preset.getBehavior().toLowerCase();
        }
        CustomMobsForge.LOGGER.debug("CustomMob created with preset: {}", preset != null ? preset.getName() : "null");
    }

    public CustomMob(EntityType<? extends PathfinderMob> type, Level level) {
        this(type, level, null);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PRESET_NAME_ACCESSOR, "");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (preset != null) {
            tag.putString("PresetName", preset.getName());
        }
        tag.putFloat("Scale", scale);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        String presetName = tag.getString("PresetName");
        this.entityData.set(PRESET_NAME_ACCESSOR, presetName);
        this.scale = tag.getFloat("Scale");
    }

    public void setCustomPreset(Preset preset) {
        this.preset = preset;
        if (preset != null) {
            this.entityData.set(PRESET_NAME_ACCESSOR, preset.getName());
            this.scale = preset.getSize();
            this.behaviorLowerCase = preset.getBehavior().toLowerCase();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(preset.getHp());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(preset.getSpeed());
            this.setHealth(this.getMaxHealth());
            this.refreshDimensions();
        } else {
            this.entityData.set(PRESET_NAME_ACCESSOR, "");
            this.behaviorLowerCase = null;
        }
        CustomMobsForge.LOGGER.debug("Set preset for CustomMob: {}", preset != null ? preset.getName() : "null");
    }

    public Preset getPreset() {
        return preset;
    }

    public String getPresetName() {
        return this.entityData.get(PRESET_NAME_ACCESSOR);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (preset == null) {
            this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
            CustomMobsForge.LOGGER.debug("Preset is null, using default neutral behavior");
        } else {
            switch (behaviorLowerCase) {
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
        }
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (preset != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(preset.getHp());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(preset.getSpeed());
            this.setHealth(this.getMaxHealth());
            CustomMobsForge.LOGGER.debug("Set attributes from preset: HP={}, Speed={}", preset.getHp(), preset.getSpeed());
        }
        this.refreshDimensions();
        return result;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            try {
                if (preset != null && behaviorLowerCase.equals("aggressive") && this.getTarget() != null) {
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

    public void setScale(float scale) {
        this.scale = scale;
        this.refreshDimensions();
        CustomMobsForge.LOGGER.debug("Set scale for CustomMob: {}", scale);
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
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        CustomMobsForge.LOGGER.debug("CustomMob added to world");
    }
}