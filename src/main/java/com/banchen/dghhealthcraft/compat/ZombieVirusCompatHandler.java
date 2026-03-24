package com.banchen.dghhealthcraft.compat;

import com.banchen.dghhealthcraft.Config;
import com.banchen.dghhealthcraft.DGH_HealthcraftMod;
import com.lastimp.dgh.common.capability.HealthCapability;
import com.lastimp.dgh.common.capability.bodyPart.ConditionAccessor;
import com.lastimp.dgh.common.capability.bodyPart.base.AbstractBody;
import com.lastimp.dgh.common.capability.bodyPart.base.BodyCondition;
import com.lastimp.dgh.common.capability.bodyPart.bodies.Blood;
import com.lastimp.dgh.common.enums.BodyComponents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

@Mod.EventBusSubscriber(modid = DGH_HealthcraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZombieVirusCompatHandler {
    private static final Random RANDOM = new Random();

    // 定义三个不同等级的尸毒感染
    // 初期 - 黄色
    public static final ResourceLocation ZOMBIE_VIRUS_EARLY = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "zombie_virus_early"),
            name -> createEarlyVirusCondition(name));

    // 中期 - 橙色
    public static final ResourceLocation ZOMBIE_VIRUS_MID = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "zombie_virus_mid"),
            name -> createMidVirusCondition(name));

    // 晚期 - 红色
    public static final ResourceLocation ZOMBIE_VIRUS_LATE = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "zombie_virus_late"),
            name -> createLateVirusCondition(name));

    // 尸变状态（最终阶段）
    public static final ResourceLocation ZOMBIFICATION = ConditionAccessor.addCondition(
            ResourceLocation.fromNamespaceAndPath(DGH_HealthcraftMod.MODID, "zombification"),
            name -> createZombificationCondition(name));

    // 兼容旧代码的引用
    public static ResourceLocation ZOMBIE_VIRUS = ZOMBIE_VIRUS_EARLY;

    /**
     * 创建初期尸毒条件（黄色）
     */
    private static BodyCondition createEarlyVirusCondition(ResourceLocation name) {
        return createZombieVirusCondition(name,
                0xFFFFFF55, // 黄色
                true, true, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建中期尸毒条件（橙色）
     */
    private static BodyCondition createMidVirusCondition(ResourceLocation name) {
        return createZombieVirusCondition(name,
                0xFFFFA500, // 橙色
                true, true, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建晚期尸毒条件（红色）
     */
    private static BodyCondition createLateVirusCondition(ResourceLocation name) {
        return createZombieVirusCondition(name,
                0xFFFF0000, // 红色
                true, true, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 创建尸变条件
     */
    private static BodyCondition createZombificationCondition(ResourceLocation name) {
        return createZombieVirusCondition(name,
                0xFF8B4513, // 棕色（腐烂色）
                true, true, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * 通用的创建尸毒条件方法
     */
    private static BodyCondition createZombieVirusCondition(ResourceLocation name,
            int color,
            boolean isInjury,
            boolean isPain,
            float healingSpeed,
            float healingTS,
            float minValue,
            float maxValue,
            float defaultValue) {
        try {
            Constructor<BodyCondition> ctor = BodyCondition.class.getDeclaredConstructor(ResourceLocation.class);
            ctor.setAccessible(true);
            BodyCondition cond = ctor.newInstance(name);

            setField(cond, "defaultValue", defaultValue);
            setField(cond, "minValue", minValue);
            setField(cond, "maxValue", maxValue);
            setField(cond, "healingSpeed", healingSpeed);
            setField(cond, "healingTS", healingTS);
            setField(cond, "isInjury", isInjury);
            setField(cond, "isPain", isPain);
            setField(cond, "isComfort", false);
            setField(cond, "isResist", false);
            setField(cond, "color", color);

            if (isInjury) {
                ConditionAccessor.injuryConditions.add(name);
            }
            if (isPain) {
                ConditionAccessor.painConditions.add(name);
            }
            ConditionAccessor.eyeVisible.add(name);

            return cond;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = BodyCondition.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * 检查实体是否是可以传播尸毒的亡灵生物
     */
    private static boolean isValidZombieEntity(LivingEntity entity) {
        if (entity == null)
            return false;

        ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (registryName == null)
            return false;

        String entityId = registryName.toString();

        if (Config.CORPSE_POISON_CUSTOM_ENTITIES != null &&
                Config.CORPSE_POISON_CUSTOM_ENTITIES.contains(entityId)) {
            return true;
        }

        return entity instanceof Zombie ||
                entity instanceof ZombieVillager ||
                entity instanceof Husk ||
                entity instanceof Drowned ||
                entity instanceof ZombifiedPiglin ||
                entity instanceof Skeleton ||
                entity instanceof Stray ||
                entity instanceof WitherSkeleton;
    }

    public static final String BLOCKER_TICKS_KEY = "dghhealthcraft:blocker_ticks";

    public static boolean isBlockerActive(Player player) {
        return player != null && player.getPersistentData().getInt(BLOCKER_TICKS_KEY) > 0;
    }

    public static void activateBlocker(Player player, int ticks) {
        if (player == null)
            return;
        player.getPersistentData().putInt(BLOCKER_TICKS_KEY, Math.max(0, ticks));
    }

    private static void tickBlocker(Player player) {
        if (player == null)
            return;
        int ticks = player.getPersistentData().getInt(BLOCKER_TICKS_KEY);
        if (ticks <= 0)
            return;

        ticks = Math.max(0, ticks - 1);
        player.getPersistentData().putInt(BLOCKER_TICKS_KEY, ticks);

        if (ticks == 0) {
        }
    }

    /**
     * 玩家受到伤害时触发尸毒感染
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (player.level().isClientSide())
            return;

        LivingEntity attacker = null;
        if (event.getSource().getEntity() instanceof LivingEntity) {
            attacker = (LivingEntity) event.getSource().getEntity();
        }

        if (isBlockerActive(player)) {
            return;
        }

        if (attacker != null && isValidZombieEntity(attacker)) {
            float zombificationValue = getZombificationValue(player);
            if (zombificationValue > 0.1f)
                return;

            float chance = Config.CORPSE_POISON_UNDEAD_ATTACK_CHANCE;
            if (RANDOM.nextFloat() < chance) {
                float infectionAmount = 0.03f + RANDOM.nextFloat() * 0.05f;
                applyInfection(player, ZOMBIE_VIRUS_EARLY, infectionAmount);
            }
        }
    }

    /**
     * 玩家 tick 处理
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        Player player = event.player;
        if (player.level().isClientSide())
            return;

        tickBlocker(player);

        float virusValue = getInfectionValue(player);
        float zombificationValue = getZombificationValue(player);

        if (zombificationValue > 0) {
            handleZombification(player, zombificationValue);
            return;
        }

        if (virusValue > 0) {
            // 获取当前感染等级
            ResourceLocation currentCondition = getCurrentCondition(player);

            // 计算恶化速度
            float progression = calculateProgression(virusValue, player);

            // 应用进展
            applyInfection(player, currentCondition, progression);

            // 检查等级转换
            checkAndUpdateInfectionLevel(player, virusValue + progression);

            // 重新获取感染值
            float newInfectionValue = getInfectionValue(player);

            // 施加症状
            applyVirusSymptoms(player, newInfectionValue);

            // 检查是否达到尸变阈值
            if (newInfectionValue >= 1.0f) {
                triggerZombification(player);
            }
        }
    }

    /**
     * 计算尸毒恶化速度
     */
    private static float calculateProgression(float currentValue, Player player) {
        if (currentValue >= 1.0f)
            return 0.0f;

        int transformationDays = Math.max(1, Config.CORPSE_TRANSFORMATION_DAYS);
        float dailyProgress = 1.0f / transformationDays;
        float tickBaseProgress = dailyProgress / 24000f;

        // 初期到中期阈值 0.4，中期到晚期阈值 0.7
        if (currentValue < 0.4f) {
            // 初期到中期：需要配置的天数
            int days = Config.UPPER_RESPIRATORY_INFECTION_MILD_TO_MODERATE_DAYS;
            tickBaseProgress = 0.4f / (days * 24000f);
        } else if (currentValue < 0.7f) {
            // 中期到晚期
            int days = Config.UPPER_RESPIRATORY_INFECTION_MODERATE_TO_SEVERE_DAYS;
            tickBaseProgress = 0.3f / (days * 24000f);
        }

        // 睡眠时恶化更快
        if (player.isSleeping()) {
            tickBaseProgress *= 1.5f;
        }

        // 低免疫力加速恶化
        if (hasCompromisedImmunity(player)) {
            tickBaseProgress *= Config.LOW_PROTEIN_DISEASE_BOOST;
        }

        // 艾滋病加速恶化
        if (HIVCompatHandler.isHIVActive(player)) {
            tickBaseProgress *= Config.AIDS_DISEASE_BOOST;
        }

        return tickBaseProgress;
    }

    /**
     * 检查并更新感染等级
     */
    private static void checkAndUpdateInfectionLevel(Player player, float infectionValue) {
        ResourceLocation targetCondition;
        float targetValue = infectionValue;

        if (infectionValue < 0.4f) {
            targetCondition = ZOMBIE_VIRUS_EARLY;
        } else if (infectionValue < 0.7f) {
            targetCondition = ZOMBIE_VIRUS_MID;
        } else {
            targetCondition = ZOMBIE_VIRUS_LATE;
            targetValue = Math.min(infectionValue, 1.0f);
        }

        ResourceLocation currentCondition = getCurrentCondition(player);

        if (currentCondition == null) {
            applyInfection(player, targetCondition, targetValue);
            return;
        }

        if (!currentCondition.equals(targetCondition)) {
            float currentValue = getInfectionValue(player);
            clearAllInfections(player);
            applyInfection(player, targetCondition, currentValue);
        } else {
            float currentValue = getInfectionValue(player);
            if (currentValue > 1.0f) {
                float excess = currentValue - 1.0f;
                applyInfection(player, targetCondition, -excess);
            }
        }
    }

    /**
     * 获取当前活动的感染条件
     */
    private static ResourceLocation getCurrentCondition(Player player) {
        if (!HealthCapability.has(player))
            return null;

        return HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                if (body.getBodyConditions().contains(ZOMBIE_VIRUS_EARLY))
                    return ZOMBIE_VIRUS_EARLY;
                if (body.getBodyConditions().contains(ZOMBIE_VIRUS_MID))
                    return ZOMBIE_VIRUS_MID;
                if (body.getBodyConditions().contains(ZOMBIE_VIRUS_LATE))
                    return ZOMBIE_VIRUS_LATE;
            }
            return null;
        }, null);
    }

    /**
     * 清除所有等级的感染
     */
    private static void clearAllInfections(Player player) {
        if (!HealthCapability.has(player))
            return;
        clearInfection(player, ZOMBIE_VIRUS_EARLY);
        clearInfection(player, ZOMBIE_VIRUS_MID);
        clearInfection(player, ZOMBIE_VIRUS_LATE);
    }

    /**
     * 清除特定感染
     */
    private static void clearInfection(Player player, ResourceLocation condition) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null && body.getBodyConditions().contains(condition)) {
                    float currentValue = body.getConditionValue(condition);
                    body.injury(condition, -currentValue);
                }
            }
            return null;
        }, null);
    }

    /**
     * 应用感染（治疗使用负值）
     */
    private static void applyInfection(Player player, ResourceLocation condition, float amount) {
        if (!HealthCapability.has(player))
            return;

        if (amount > 0) {
            ResourceLocation current = getCurrentCondition(player);
            if (current != null && !current.equals(condition)) {
                clearAllInfections(player);
            }
        }

        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null || !body.getBodyConditions().contains(condition))
                    continue;
                body.injury(condition, amount);
            }
            return null;
        }, null);
    }

    /**
     * 获取感染值
     */
    public static float getInfectionValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            float maxValue = 0f;
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                if (body.getBodyConditions().contains(ZOMBIE_VIRUS_EARLY)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(ZOMBIE_VIRUS_EARLY));
                }
                if (body.getBodyConditions().contains(ZOMBIE_VIRUS_MID)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(ZOMBIE_VIRUS_MID));
                }
                if (body.getBodyConditions().contains(ZOMBIE_VIRUS_LATE)) {
                    maxValue = Math.max(maxValue, body.getConditionValue(ZOMBIE_VIRUS_LATE));
                }
            }
            return maxValue;
        }, 0f);
    }

    /**
     * 获取尸变值
     */
    private static float getZombificationValue(Player player) {
        if (!HealthCapability.has(player))
            return 0f;
        return HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null && body.getBodyConditions().contains(ZOMBIFICATION)) {
                    return body.getConditionValue(ZOMBIFICATION);
                }
            }
            return 0f;
        }, 0f);
    }

    /**
     * 处理尸变状态
     */
    private static void handleZombification(Player player, float zombificationValue) {
        if (player.tickCount % 24000 == 0) {
            float deterioration = Config.CORPSE_TRANSFORMATION_DAILY_DETERIORATION;
            deteriorateAllBodyParts(player, zombificationValue * deterioration);
        }

        applyZombificationEffects(player, zombificationValue);

        if (Config.DEATH_ON_ALL_PARTS_LOST && checkAllPartsFailed(player)) {
            player.kill();
        }
    }

    /**
     * 所有部位每天恶化
     */
    private static void deteriorateAllBodyParts(Player player, float deteriorationAmount) {
        if (deteriorationAmount <= 0f || !HealthCapability.has(player))
            return;

        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body == null)
                    continue;

                for (ResourceLocation condition : body.getBodyConditions()) {
                    if (condition.equals(ZOMBIFICATION) ||
                            condition.equals(ZOMBIE_VIRUS_EARLY) ||
                            condition.equals(ZOMBIE_VIRUS_MID) ||
                            condition.equals(ZOMBIE_VIRUS_LATE)) {
                        continue;
                    }

                    BodyCondition cond = ConditionAccessor.get(condition);
                    if (cond == null)
                        continue;
                    if (!cond.isInjury() && !cond.isPain())
                        continue;

                    float currentValue = body.getConditionValue(condition);
                    if (currentValue >= cond.maxValue() - 1e-6f)
                        continue;

                    float applyAmount = Math.min(deteriorationAmount, cond.maxValue() - currentValue);
                    if (applyAmount <= 0f)
                        continue;

                    body.injury(condition, applyAmount);
                }
            }
            return null;
        }, null);
    }

    /**
     * 应用尸变效果
     */
    private static void applyZombificationEffects(Player player, float zombificationValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;
        int weaknessLevel = (int) (zombificationValue * 3);
        int fatigueLevel = (int) (zombificationValue * 2);
        int slownessLevel = (int) (zombificationValue * 2);

        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration,
                Math.min(weaknessLevel, 4)));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration,
                Math.min(fatigueLevel, 3)));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration,
                Math.min(slownessLevel, 3)));

        if (zombificationValue > 0.5f) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
        }

        if (zombificationValue > 0.8f) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration / 2, 0));
        }

        if (player.tickCount % 100 == 0) {
            player.level().getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(3))
                    .forEach(entity -> {
                        if (entity != player && !(entity instanceof Player)) {
                            entity.hurt(player.damageSources().mobAttack(player), 2.0f);
                        }
                    });
        }
    }

    /**
     * 检查是否所有部位都失去功能
     */
    private static boolean checkAllPartsFailed(Player player) {
        if (!HealthCapability.has(player))
            return true;

        return HealthCapability.getAndApply(player, health -> {
            int failedParts = 0;
            int totalParts = 0;

            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null) {
                    totalParts++;
                    boolean isFailed = false;
                    for (ResourceLocation condition : body.getBodyConditions()) {
                        float value = body.getConditionValue(condition);
                        if (value > 0.8f) {
                            isFailed = true;
                            break;
                        }
                    }
                    if (isFailed)
                        failedParts++;
                }
            }

            return totalParts > 0 && failedParts >= totalParts;
        }, true);
    }

    /**
     * 根据感染程度施加症状
     */
    private static void applyVirusSymptoms(Player player, float virusValue) {
        int duration = Config.PTSD_EFFECT_DURATION_TICKS;

        if (virusValue > 0.2f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
        }

        if (virusValue > 0.4f) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 0));
        }

        if (virusValue > 0.6f) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
        }

        if (virusValue > 0.85f) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }
    }

    /**
     * 触发尸变
     */
    private static void triggerZombification(Player player) {
        float currentZombification = getZombificationValue(player);
        if (currentZombification > 0)
            return;

        // 应用尸变
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null && body.getBodyConditions().contains(ZOMBIFICATION)) {
                    body.injury(ZOMBIFICATION, 0.1f);
                    break;
                }
            }
            return null;
        }, null);

        // 清除尸毒
        clearAllInfections(player);

        player.playSound(net.minecraft.sounds.SoundEvents.ZOMBIE_AMBIENT, 1.0f, 1.0f);
    }

    /**
     * 检查免疫力是否低下
     */
    private static boolean hasCompromisedImmunity(Player player) {
        if (!HealthCapability.has(player))
            return false;
        return HealthCapability.getAndApply(player, health -> {
            AbstractBody blood = health.getComponent(BodyComponents.BLOOD);
            if (blood == null || !blood.getBodyConditions().contains(BodyCondition.IMMUNITY))
                return false;
            return blood.getConditionValue(BodyCondition.IMMUNITY) < 0.3f;
        }, false);
    }

    /**
     * 缓解尸毒（可由靶向剂调用）
     */
    public static void alleviateZombieVirus(Player player) {
        float currentValue = getInfectionValue(player);
        if (currentValue <= 0)
            return;

        float reduction = Config.TARGETED_AGENT_DETERIORATION_REDUCTION;
        ResourceLocation currentCondition = getCurrentCondition(player);
        if (currentCondition != null) {
            applyInfection(player, currentCondition, -currentValue * reduction);
        }
    }

    /**
     * 缓解尸变（使用靶向剂）
     */
    public static void alleviateZombification(Player player) {
        if (!HealthCapability.has(player))
            return;
        HealthCapability.getAndApply(player, health -> {
            for (BodyComponents component : BodyComponents.values()) {
                AbstractBody body = health.getComponent(component);
                if (body != null && body.getBodyConditions().contains(ZOMBIFICATION)) {
                    float currentValue = body.getConditionValue(ZOMBIFICATION);
                    if (currentValue > 0) {
                        float reduction = Config.TARGETED_AGENT_DETERIORATION_REDUCTION;
                        body.injury(ZOMBIFICATION, -currentValue * reduction);
                    }
                    break;
                }
            }
            return null;
        }, null);
    }

    /**
     * 检查是否感染尸毒
     */
    public static boolean isZombieVirusActive(Player player) {
        return getInfectionValue(player) > 0.01f;
    }

    /**
     * 检查是否尸变
     */
    public static boolean isZombified(Player player) {
        return getZombificationValue(player) > 0.01f;
    }

    /**
     * 获取感染阶段
     * 0: 未感染, 1: 初期, 2: 中期, 3: 晚期, 4: 尸变
     */
    public static int getInfectionStage(Player player) {
        if (isZombified(player))
            return 4;
        float value = getInfectionValue(player);
        if (value <= 0.001f)
            return 0;
        if (value < 0.4f)
            return 1;
        if (value < 0.7f)
            return 2;
        return 3;
    }

    /**
     * 注册
     */
    public static void register() {
        if (registered)
            return;
        registered = true;

        ConditionAccessor.get(ZOMBIE_VIRUS_EARLY);
        ConditionAccessor.get(ZOMBIE_VIRUS_MID);
        ConditionAccessor.get(ZOMBIE_VIRUS_LATE);
        ConditionAccessor.get(ZOMBIFICATION);

        Blood.addCondition(List.of(ZOMBIE_VIRUS_EARLY, ZOMBIE_VIRUS_MID, ZOMBIE_VIRUS_LATE, ZOMBIFICATION));
        ConditionAccessor.bloodConditions.add(ZOMBIE_VIRUS_EARLY);
        ConditionAccessor.bloodConditions.add(ZOMBIE_VIRUS_MID);
        ConditionAccessor.bloodConditions.add(ZOMBIE_VIRUS_LATE);
        ConditionAccessor.bloodConditions.add(ZOMBIFICATION);

        ConditionAccessor.injuryConditions.add(ZOMBIE_VIRUS_EARLY);
        ConditionAccessor.injuryConditions.add(ZOMBIE_VIRUS_MID);
        ConditionAccessor.injuryConditions.add(ZOMBIE_VIRUS_LATE);
        ConditionAccessor.injuryConditions.add(ZOMBIFICATION);

        ConditionAccessor.painConditions.add(ZOMBIE_VIRUS_EARLY);
        ConditionAccessor.painConditions.add(ZOMBIE_VIRUS_MID);
        ConditionAccessor.painConditions.add(ZOMBIE_VIRUS_LATE);
        ConditionAccessor.painConditions.add(ZOMBIFICATION);

        ConditionAccessor.eyeVisible.add(ZOMBIE_VIRUS_EARLY);
        ConditionAccessor.eyeVisible.add(ZOMBIE_VIRUS_MID);
        ConditionAccessor.eyeVisible.add(ZOMBIE_VIRUS_LATE);
        ConditionAccessor.eyeVisible.add(ZOMBIFICATION);

        refreshBloodConditionsCache();
    }

    private static void refreshBloodConditionsCache() {
        try {
            var field = Blood.class.getDeclaredField("BLOOD_CONDITIONS");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static int getBlockerRemainingSeconds(Player player) {
        if (player == null)
            return 0;
        return player.getPersistentData().getInt(BLOCKER_TICKS_KEY) / 20;
    }

    public static int getBlockerRemainingTicks(Player player) {
        if (player == null)
            return 0;
        return player.getPersistentData().getInt(BLOCKER_TICKS_KEY);
    }

    private static boolean registered = false;
}